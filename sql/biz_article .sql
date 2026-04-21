-- MySQL dump 10.13  Distrib 8.0.36, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: aiforge
-- ------------------------------------------------------
-- Server version	8.0.36

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `biz_article`
--

DROP TABLE IF EXISTS `biz_article`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `biz_article` (
  `article_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '文章ID',
  `article_title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文章标题',
  `article_tags` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文章标签',
  `content` longtext COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文章内容',
  `author_id` bigint NOT NULL COMMENT '文章作者',
  `publish_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `article_status` tinyint NOT NULL DEFAULT '3' COMMENT '文章状态：1-已发布，2-已下架，3-草稿(默认值)',
  PRIMARY KEY (`article_id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `biz_article`
--

LOCK TABLES `biz_article` WRITE;
/*!40000 ALTER TABLE `biz_article` DISABLE KEYS */;
INSERT INTO `biz_article` VALUES (1,'2024年后端开发趋势：Rust 会取代 Go 吗？','后端,编程,技术趋势','近年来，Rust 在系统编程领域的呼声越来越高。本文将深入对比 Go 和 Rust 在并发模型、内存管理以及生态成熟度上的差异，探讨在未来的云原生架构中，哪种语言更具优势...',1,'2024-01-15 09:30:00',1),(2,'极简主义生活指南：如何断舍离你的数字生活','生活,极简主义,效率','不仅仅是整理房间，数字空间的清理同样重要。从清理手机相册到整理电脑桌面，再到取消关注那些消耗你精力的社交媒体账号，让我们一起通过断舍离找回专注力...',1,'2024-01-18 14:20:00',1),(3,'深入理解 MySQL 索引失效的常见场景','数据库,MySQL,优化','明明加了索引为什么查询还是很慢？本文总结了 B+ 树索引失效的几种典型情况，包括最左前缀原则被破坏、隐式类型转换以及在索引列上进行计算等问题...',1,'2024-01-22 10:15:00',1),(4,'周末探店：藏在巷子里的日式手作咖啡馆','探店,咖啡,周末','在这个快节奏的城市里，找到一家安静的角落并不容易。这家名为“慢时光”的小店，没有嘈杂的音乐，只有手冲咖啡的香气和老板精心挑选的黑胶唱片...',1,'2024-02-01 16:45:00',1),(5,'Docker 容器化部署实战：从入门到精通','运维,Docker,DevOps','“一次构建，到处运行”是开发者的梦想。本文将手把手教你如何编写 Dockerfile，优化镜像分层，以及使用 docker-compose 编排复杂的多容器应用...',1,'2024-02-05 11:00:00',1),(6,'读书笔记：《置身事内》看懂中国经济逻辑','读书,经济,书评','兰小欢教授的这本书深入浅出地讲解了地方政府在经济发展中的作用。通过这本书，我们能更好地理解土地财政、地方债以及招商引资背后的深层逻辑...',1,'2024-02-10 20:00:00',1),(7,'Redis 分布式锁的正确实现姿势','Redis,分布式,并发','在分布式系统中，如何保证共享资源的互斥访问是一个经典问题。本文将分析基于 setnx 的简单实现存在的隐患，并介绍 Redisson 框架提供的看门狗机制...',1,'2024-02-14 09:10:00',1),(8,'新手必看：家庭咖啡烘焙入门指南','咖啡,烘焙,教程','想要喝到最新鲜的咖啡？不如尝试自己烘焙。从生豆的选择，到烘焙度的判断，再到养豆期的注意事项，这篇指南将带你开启咖啡烘焙之旅...',1,'2024-02-20 13:30:00',1),(9,'Spring Boot 3.0 新特性详解与升级避坑','Java,Spring Boot,后端','Spring Boot 3.0 正式拥抱 Jakarta EE 9+，这意味着所有的包名都发生了变化。本文整理了升级过程中常见的报错及解决方案，并介绍了原生镜像支持等新特性...',1,'2024-02-25 15:20:00',1),(10,'春季护肤攻略：敏感肌如何度过换季期','护肤,健康,春季','春季花粉和温差变化容易导致皮肤屏障受损。建议精简护肤步骤，选择含有神经酰胺成分的修护产品，并严格做好物理防晒...',1,'2024-03-01 08:50:00',1),(11,'Python 自动化办公：批量处理 Excel 报表','Python,自动化,办公技巧','还在加班手动复制粘贴 Excel 吗？利用 Python 的 openpyxl 和 pandas 库，我们可以轻松实现数据的自动汇总、格式调整和报表生成，效率提升 10 倍...',1,'2024-03-05 17:00:00',1),(12,'关于远程办公的冷思考：效率与孤独的博弈','职场,远程办公,思考','远程办公虽然节省了通勤时间，但也模糊了工作与生活的边界。如何保持团队沟通的透明度，以及如何克服独自工作的孤独感，是远程工作者必须面对的课题...',1,'2024-03-10 19:30:00',1),(13,'Vue 3 组合式 API 最佳实践分享','前端,Vue,JavaScript','相比选项式 API，组合式 API 提供了更好的逻辑复用能力。本文将通过几个实际的业务组件案例，展示如何利用 useHooks 模式来组织代码，让组件逻辑更加清晰...',1,'2024-03-15 10:40:00',1),(14,'露营装备清单：第一次露营需要准备什么？','户外,露营,攻略','不需要一开始就购买昂贵的装备。帐篷、防潮垫、睡袋是基础三件套，再配合卡式炉和营地灯，就能享受一个舒适的户外夜晚。这里有一份详细的清单...',1,'2024-03-20 12:15:00',1),(15,'Kubernetes 核心概念解析：Pod 与 Service','云原生,K8s,架构','K8s 的复杂度往往让人望而却步。本文试图用通俗易懂的语言解释 Pod 作为最小调度单元的意义，以及 Service 如何作为稳定的网络入口暴露应用...',1,'2024-03-25 14:00:00',1),(16,'胶片摄影的魅力：为什么我还在用单反相机','摄影,胶片,爱好','在数码照片泛滥的今天，胶片的颗粒感和不可预测性显得尤为珍贵。每一次快门的按下都需要深思熟虑，这种仪式感是数码相机无法替代的...',1,'2024-03-28 16:20:00',1),(17,'JWT 身份认证原理及安全性分析','安全,后端,认证','JSON Web Token 是无状态认证的常用方案。本文将剖析 JWT 的三段式结构，讨论 HS256 与 RS256 算法的区别，以及如何防范 Token 被劫持的风险...',1,'2024-04-01 09:00:00',1),(18,'城市夜跑路线图：发现不一样的夜景','运动,跑步,城市','沿着滨江大道奔跑，看着两岸的灯火辉煌，是一天中最解压的时刻。推荐几条适合夜跑的路线，路面平整且路灯明亮，安全又惬意...',1,'2024-04-05 21:00:00',1),(19,'微服务架构下的分布式事务解决方案','架构,微服务,事务','在微服务拆分后，本地事务失效了。本文将对比 TCC、Saga 模式以及基于消息队列的最终一致性方案，帮助你在业务一致性和系统可用性之间找到平衡点...',1,'2024-04-10 11:30:00',1),(20,'AI 绘画工具 Midjourney 提示词技巧大全','AI,设计,工具','想要生成高质量的 AI 画作，提示词是关键。本文整理了常用的风格修饰词、光影参数以及构图指令，助你轻松驾驭 AI 创作...',1,'2024-04-15 15:45:00',1);
/*!40000 ALTER TABLE `biz_article` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-20 22:32:37
