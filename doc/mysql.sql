-- MySQL dump 10.13  Distrib 5.1.49, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: jtreqs
-- ------------------------------------------------------
-- Server version	5.1.49-1ubuntu8.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `heart_beat`
--

DROP TABLE IF EXISTS `heart_beat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `heart_beat` (
  `pid` int(11) NOT NULL,
  `start_time` datetime NOT NULL,
  `last_time` datetime DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `information`
--

DROP TABLE IF EXISTS `information`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `information` (
  `name` varchar(128) NOT NULL,
  `value` varchar(256) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `jallocations`
--

DROP TABLE IF EXISTS `jallocations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `jallocations` (
  `media_id` tinyint(4) NOT NULL,
  `user` varchar(32) NOT NULL,
  `share` decimal(5,2) NOT NULL,
  PRIMARY KEY (`media_id`,`user`),
  CONSTRAINT `jallocations_ibfk_1` FOREIGN KEY (`media_id`) REFERENCES `jmediatypes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jallocations`
--

LOCK TABLES `jallocations` WRITE;
/*!40000 ALTER TABLE `jallocations` DISABLE KEYS */;
INSERT INTO `jallocations` VALUES (1,'user1','0.30'),(1,'user2','0.15'),(2,'user1','0.30'),(2,'user2','0.10'),(3,'user2','0.50');
/*!40000 ALTER TABLE `jallocations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jmediatypes`
--

DROP TABLE IF EXISTS `jmediatypes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `jmediatypes` (
  `id` tinyint(4) NOT NULL,
  `name` varchar(16) NOT NULL,
  `drives` smallint(6) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jmediatypes`
--

LOCK TABLES `jmediatypes` WRITE;
/*!40000 ALTER TABLE `jmediatypes` DISABLE KEYS */;
INSERT INTO `jmediatypes` VALUES (1,'T10K-A',10),(2,'T10K-B',5),(3,'T10K-C',30);
/*!40000 ALTER TABLE `jmediatypes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jqueues`
--

DROP TABLE IF EXISTS `jqueues`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `jqueues` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` char(12) NOT NULL,
  `creation_time` datetime NOT NULL,
  `mediatype_id` tinyint(4) NOT NULL,
  `suspension_time` datetime DEFAULT NULL,
  `nb_reqs_failed` int(11) NOT NULL DEFAULT '0',
  `activation_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `status` smallint(6) NOT NULL DEFAULT '200',
  `nb_reqs` int(11) NOT NULL DEFAULT '0',
  `owner` varchar(32) DEFAULT NULL,
  `byte_size` bigint(20) NOT NULL DEFAULT '0',
  `nb_reqs_done` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `mediatype_id` (`mediatype_id`),
  CONSTRAINT `jqueues_ibfk_1` FOREIGN KEY (`mediatype_id`) REFERENCES `jmediatypes` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `jrequests`
--

DROP TABLE IF EXISTS `jrequests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `jrequests` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `file` varchar(1280) NOT NULL,
  `creation_time` datetime NOT NULL,
  `user` varchar(32) NOT NULL,
  `client` varchar(32) NOT NULL,
  `version` varchar(16) NOT NULL,
  `email` varchar(64) DEFAULT NULL,
  `queue_id` int(11) DEFAULT NULL,
  `tape` char(8) DEFAULT NULL,
  `position` int(11) DEFAULT NULL,
  `level` tinyint(4) DEFAULT NULL,
  `size` bigint(20) DEFAULT NULL,
  `tries` tinyint(4) DEFAULT '0',
  `errorcode` int(11) DEFAULT NULL,
  `submission_time` datetime DEFAULT NULL,
  `queued_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `status` smallint(6) DEFAULT '100',
  `message` varchar(1024) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `queue_id` (`queue_id`),
  CONSTRAINT `jrequests_ibfk_1` FOREIGN KEY (`queue_id`) REFERENCES `jqueues` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `queue_status`
--

DROP TABLE IF EXISTS `queue_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `queue_status` (
  `id` int(11) DEFAULT NULL,
  `name` char(21) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `queue_status`
--

LOCK TABLES `queue_status` WRITE;
/*!40000 ALTER TABLE `queue_status` DISABLE KEYS */;
INSERT INTO `queue_status` VALUES (240,'Aborted'),(210,'Activated'),(200,'Created'),(230,'Ended'),(220,'Temporarily Suspended');
/*!40000 ALTER TABLE `queue_status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `request_status`
--

DROP TABLE IF EXISTS `request_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `request_status` (
  `id` int(11) DEFAULT NULL,
  `name` char(10) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `request_status`
--

LOCK TABLES `request_status` WRITE;
/*!40000 ALTER TABLE `request_status` DISABLE KEYS */;
INSERT INTO `request_status` VALUES (100,'Created'),(160,'Failed'),(150,'On disk'),(120,'Queued'),(140,'Staged'),(110,'Submitted');
/*!40000 ALTER TABLE `request_status` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2011-04-17 15:35:54
