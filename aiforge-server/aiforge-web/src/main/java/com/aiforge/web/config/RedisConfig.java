package com.aiforge.web.config;

import io.lettuce.core.ReadFrom;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStaticMasterReplicaConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

/**
 * @Description: Redis 配置
 */
@Configuration
public class RedisConfig {

    /**
     * 从节点 地址
     */
    @Value("${spring.data.redis.slave.host}")
    private String slaveHost;

    /**
     * 从节点 端口
     */
    @Value("${spring.data.redis.slave.port:6379}")
    private int slavePort;

    /**
     * Redis连接工厂配置 - 使用Lettuce客户端实现主从读写分离
     * 核心作用：创建支持连接池和主从架构的Redis连接工厂
     * @param properties Spring Boot自动配置的Redis属性
     * @return Lettuce连接工厂实例
     */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory(RedisProperties properties) {
        // 1. 创建连接池配置 - 管理Redis连接的复用
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        if (properties.getLettuce() != null && properties.getLettuce().getPool() != null) {
            // 设置最大连接数 - 控制并发连接上限
            poolConfig.setMaxTotal(properties.getLettuce().getPool().getMaxActive());
            // 设置最大空闲连接 - 避免资源浪费
            poolConfig.setMaxIdle(properties.getLettuce().getPool().getMaxIdle());
            // 设置最小空闲连接 - 保持连接预热
            poolConfig.setMinIdle(properties.getLettuce().getPool().getMinIdle());
        }

        // 2. 创建Lettuce客户端配置 - 设置连接池和读写策略
        LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .poolConfig(poolConfig)  // 应用连接池配置
                .readFrom(ReadFrom.REPLICA_PREFERRED)  // 关键配置：优先从节点读，从节点不可用时降级到主节点读
                .build();

        // 3. 配置静态主从架构 - 指定主节点地址
        RedisStaticMasterReplicaConfiguration config = new RedisStaticMasterReplicaConfiguration(
                properties.getHost(), properties.getPort());  // 主节点来自spring.data.redis.host/port

        // 4. 设置认证信息 - 如果配置了密码
        if (properties.getPassword() != null) {
            config.setPassword(RedisPassword.of(properties.getPassword()));
        }
        // 5. 设置数据库索引 - 默认使用DB0
        config.setDatabase(properties.getDatabase());

        // 6. 添加从节点 - 实现读写分离的关键步骤
        config.addNode(slaveHost, slavePort);

        // 7. 创建并返回连接工厂 - 整合所有配置
        return new LettuceConnectionFactory(config, clientConfig);
    }

    /**
     * Redisson客户端配置 - 提供分布式锁、集合等高级功能
     * 与Lettuce共享相同的主从配置，保持一致性
     * @param properties Spring Boot自动配置的Redis属性
     * @return Redisson客户端实例
     */
    @Bean
    public RedissonClient redissonClient(RedisProperties properties) {
        // 1. 创建Redisson配置对象
        Config config = new Config();
        String prefix = "redis://";  // Redis协议前缀

        // 2. 构建主节点地址 - 格式：redis://host:port
        String masterAddress = prefix + properties.getHost() + ":" + properties.getPort();
        // 3. 构建从节点地址 - 使用配置的从节点信息
        String slaveAddress = prefix + slaveHost + ":" + slavePort;

        // 4. 配置主从服务器模式
        config.useMasterSlaveServers()
                .setMasterAddress(masterAddress)  // 设置主节点
                .addSlaveAddress(slaveAddress)    // 添加从节点
                .setPassword(properties.getPassword())  // 设置认证密码
                .setDatabase(properties.getDatabase());  // 设置数据库索引

        // 5. 创建并返回Redisson客户端
        return Redisson.create(config);
    }
}
