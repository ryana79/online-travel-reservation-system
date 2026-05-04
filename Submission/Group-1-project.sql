-- MySQL dump 10.13  Distrib 9.6.0, for macos26.3 (arm64)
--
-- Host: localhost    Database: travel_reservation
-- ------------------------------------------------------
-- Server version	9.6.0

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
SET @MYSQLDUMP_TEMP_LOG_BIN = @@SESSION.SQL_LOG_BIN;
SET @@SESSION.SQL_LOG_BIN= 0;

--
-- GTID state at the beginning of the backup 
--

SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ '001e4f76-41e7-11f1-a6cd-4b2e0590c21b:1-55';

--
-- Current Database: `travel_reservation`
--

/*!40000 DROP DATABASE IF EXISTS `travel_reservation`*/;

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `travel_reservation` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `travel_reservation`;

--
-- Table structure for table `aircraft`
--

DROP TABLE IF EXISTS `aircraft`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `aircraft` (
  `aircraft_id` int NOT NULL AUTO_INCREMENT,
  `airline_id` char(2) NOT NULL,
  `model` varchar(50) NOT NULL,
  `num_economy_seats` int NOT NULL,
  `num_business_seats` int NOT NULL,
  `num_first_seats` int NOT NULL,
  PRIMARY KEY (`aircraft_id`),
  KEY `fk_aircraft_airline` (`airline_id`),
  CONSTRAINT `fk_aircraft_airline` FOREIGN KEY (`airline_id`) REFERENCES `airline` (`airline_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `aircraft_chk_1` CHECK ((`num_economy_seats` >= 0)),
  CONSTRAINT `aircraft_chk_2` CHECK ((`num_business_seats` >= 0)),
  CONSTRAINT `aircraft_chk_3` CHECK ((`num_first_seats` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `aircraft`
--

LOCK TABLES `aircraft` WRITE;
/*!40000 ALTER TABLE `aircraft` DISABLE KEYS */;
INSERT INTO `aircraft` VALUES (1,'AA','Boeing 737-800',120,24,8),(2,'DL','Airbus A321',130,20,10),(3,'UA','Boeing 777',220,48,12),(4,'SW','Boeing 737 MAX 8',175,0,0);
/*!40000 ALTER TABLE `aircraft` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `airline`
--

DROP TABLE IF EXISTS `airline`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `airline` (
  `airline_id` char(2) NOT NULL,
  `name` varchar(100) NOT NULL,
  PRIMARY KEY (`airline_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `airline`
--

LOCK TABLES `airline` WRITE;
/*!40000 ALTER TABLE `airline` DISABLE KEYS */;
INSERT INTO `airline` VALUES ('AA','American Airlines'),('DL','Delta Air Lines'),('SW','Southwest Airlines'),('UA','United Airlines');
/*!40000 ALTER TABLE `airline` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `airport`
--

DROP TABLE IF EXISTS `airport`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `airport` (
  `airport_code` char(3) NOT NULL,
  `airport_name` varchar(100) NOT NULL,
  `city` varchar(100) NOT NULL,
  `country` varchar(100) NOT NULL,
  PRIMARY KEY (`airport_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `airport`
--

LOCK TABLES `airport` WRITE;
/*!40000 ALTER TABLE `airport` DISABLE KEYS */;
INSERT INTO `airport` VALUES ('ATL','Hartsfield Jackson Atlanta International Airport','Atlanta','USA'),('EWR','Newark Liberty International Airport','Newark','USA'),('JFK','John F. Kennedy International Airport','New York','USA'),('LAX','Los Angeles International Airport','Los Angeles','USA'),('MIA','Miami International Airport','Miami','USA'),('ORD','O Hare International Airport','Chicago','USA');
/*!40000 ALTER TABLE `airport` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customer`
--

DROP TABLE IF EXISTS `customer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer` (
  `customer_id` int NOT NULL AUTO_INCREMENT,
  `first_name` varchar(50) NOT NULL,
  `last_name` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `street` varchar(100) DEFAULT NULL,
  `city` varchar(50) DEFAULT NULL,
  `state` varchar(50) DEFAULT NULL,
  `zip_code` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`customer_id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customer`
--

LOCK TABLES `customer` WRITE;
/*!40000 ALTER TABLE `customer` DISABLE KEYS */;
INSERT INTO `customer` VALUES (1,'Ryan','Amir','customer@example.com','customer123','732-555-0100','1 College Ave','New Brunswick','NJ','08901'),(2,'Ava','Patel','ava@example.com','ava123','732-555-0101','9 George St','New Brunswick','NJ','08901'),(3,'Noah','Kim','noah@example.com','noah123','973-555-0102','25 Market St','Newark','NJ','07102');
/*!40000 ALTER TABLE `customer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `employee`
--

DROP TABLE IF EXISTS `employee`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `employee` (
  `employee_id` int NOT NULL AUTO_INCREMENT,
  `first_name` varchar(50) NOT NULL,
  `last_name` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `role` enum('admin','customer_rep') NOT NULL,
  PRIMARY KEY (`employee_id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `employee`
--

LOCK TABLES `employee` WRITE;
/*!40000 ALTER TABLE `employee` DISABLE KEYS */;
INSERT INTO `employee` VALUES (1,'Admin','User','admin@example.com','admin123','732-555-1000','admin'),(2,'Casey','Rep','rep@example.com','rep123','732-555-1001','customer_rep');
/*!40000 ALTER TABLE `employee` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flight`
--

DROP TABLE IF EXISTS `flight`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flight` (
  `airline_id` char(2) NOT NULL,
  `flight_number` varchar(10) NOT NULL,
  `aircraft_id` int NOT NULL,
  `departure_airport` char(3) NOT NULL,
  `arrival_airport` char(3) NOT NULL,
  `departure_time` time NOT NULL,
  `arrival_time` time NOT NULL,
  `is_domestic` tinyint(1) NOT NULL,
  `operating_days` varchar(20) NOT NULL,
  `economy_fare` decimal(10,2) NOT NULL,
  `business_fare` decimal(10,2) NOT NULL,
  `first_fare` decimal(10,2) NOT NULL,
  PRIMARY KEY (`airline_id`,`flight_number`),
  KEY `fk_flight_aircraft` (`aircraft_id`),
  KEY `fk_flight_arrival_airport` (`arrival_airport`),
  KEY `idx_flight_route` (`departure_airport`,`arrival_airport`),
  CONSTRAINT `fk_flight_aircraft` FOREIGN KEY (`aircraft_id`) REFERENCES `aircraft` (`aircraft_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_flight_airline` FOREIGN KEY (`airline_id`) REFERENCES `airline` (`airline_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_flight_arrival_airport` FOREIGN KEY (`arrival_airport`) REFERENCES `airport` (`airport_code`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_flight_departure_airport` FOREIGN KEY (`departure_airport`) REFERENCES `airport` (`airport_code`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `flight_chk_1` CHECK ((`economy_fare` >= 0)),
  CONSTRAINT `flight_chk_2` CHECK ((`business_fare` >= 0)),
  CONSTRAINT `flight_chk_3` CHECK ((`first_fare` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flight`
--

LOCK TABLES `flight` WRITE;
/*!40000 ALTER TABLE `flight` DISABLE KEYS */;
INSERT INTO `flight` VALUES ('AA','101',1,'EWR','LAX','08:00:00','11:15:00',1,'Mon,Tue,Wed,Thu,Fri',250.00,520.00,850.00),('AA','202',1,'LAX','EWR','14:00:00','22:10:00',1,'Mon,Tue,Wed,Thu,Fri',255.00,525.00,860.00),('DL','303',2,'JFK','ATL','09:30:00','12:00:00',1,'Daily',135.00,310.00,490.00),('DL','404',2,'ATL','MIA','13:15:00','15:05:00',1,'Daily',120.00,275.00,430.00),('SW','707',4,'EWR','MIA','16:20:00','19:25:00',1,'Tue,Thu,Sat,Sun',160.00,160.00,160.00),('UA','505',3,'EWR','ORD','07:45:00','09:10:00',1,'Daily',110.00,250.00,390.00),('UA','606',3,'ORD','LAX','10:30:00','12:50:00',1,'Mon,Wed,Fri,Sun',205.00,480.00,760.00);
/*!40000 ALTER TABLE `flight` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `operates_from`
--

DROP TABLE IF EXISTS `operates_from`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `operates_from` (
  `airline_id` char(2) NOT NULL,
  `airport_code` char(3) NOT NULL,
  PRIMARY KEY (`airline_id`,`airport_code`),
  KEY `fk_operates_airport` (`airport_code`),
  CONSTRAINT `fk_operates_airline` FOREIGN KEY (`airline_id`) REFERENCES `airline` (`airline_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_operates_airport` FOREIGN KEY (`airport_code`) REFERENCES `airport` (`airport_code`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `operates_from`
--

LOCK TABLES `operates_from` WRITE;
/*!40000 ALTER TABLE `operates_from` DISABLE KEYS */;
INSERT INTO `operates_from` VALUES ('DL','ATL'),('SW','ATL'),('AA','EWR'),('SW','EWR'),('UA','EWR'),('AA','JFK'),('DL','JFK'),('AA','LAX'),('DL','LAX'),('UA','LAX'),('AA','MIA'),('SW','MIA'),('UA','ORD');
/*!40000 ALTER TABLE `operates_from` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `question`
--

DROP TABLE IF EXISTS `question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `question` (
  `question_id` int NOT NULL AUTO_INCREMENT,
  `customer_id` int NOT NULL,
  `employee_id` int DEFAULT NULL,
  `question_text` varchar(1000) NOT NULL,
  `answer_text` varchar(1000) DEFAULT NULL,
  `asked_datetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `answered_datetime` datetime DEFAULT NULL,
  PRIMARY KEY (`question_id`),
  KEY `fk_question_customer` (`customer_id`),
  KEY `fk_question_employee` (`employee_id`),
  CONSTRAINT `fk_question_customer` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_question_employee` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`employee_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `question`
--

LOCK TABLES `question` WRITE;
/*!40000 ALTER TABLE `question` DISABLE KEYS */;
INSERT INTO `question` VALUES (1,1,2,'Can I request a vegetarian meal?','Yes. Enter the meal request when booking your reservation.','2026-04-02 14:00:00','2026-04-02 15:30:00'),(2,2,NULL,'How do I join a waiting list?',NULL,'2026-04-05 11:15:00',NULL);
/*!40000 ALTER TABLE `question` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ticket`
--

DROP TABLE IF EXISTS `ticket`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ticket` (
  `ticket_number` int NOT NULL AUTO_INCREMENT,
  `customer_id` int NOT NULL,
  `ticket_type` enum('one-way','round-trip') NOT NULL,
  `total_fare` decimal(10,2) NOT NULL,
  `booking_fee` decimal(10,2) NOT NULL,
  `purchase_datetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ticket_number`),
  KEY `idx_ticket_customer` (`customer_id`),
  CONSTRAINT `fk_ticket_customer` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `ticket_chk_1` CHECK ((`total_fare` >= 0)),
  CONSTRAINT `ticket_chk_2` CHECK ((`booking_fee` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ticket`
--

LOCK TABLES `ticket` WRITE;
/*!40000 ALTER TABLE `ticket` DISABLE KEYS */;
INSERT INTO `ticket` VALUES (1,1,'one-way',275.00,25.00,'2026-04-01 10:30:00'),(2,2,'round-trip',535.00,25.00,'2026-04-03 12:00:00'),(3,3,'one-way',135.00,15.00,'2026-04-04 09:45:00');
/*!40000 ALTER TABLE `ticket` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ticket_flight`
--

DROP TABLE IF EXISTS `ticket_flight`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ticket_flight` (
  `ticket_number` int NOT NULL,
  `leg_number` int NOT NULL,
  `airline_id` char(2) NOT NULL,
  `flight_number` varchar(10) NOT NULL,
  `departure_date` date NOT NULL,
  `seat_number` varchar(10) DEFAULT NULL,
  `class` enum('economy','business','first') NOT NULL,
  `special_meal` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`ticket_number`,`leg_number`),
  UNIQUE KEY `uq_flight_seat` (`airline_id`,`flight_number`,`departure_date`,`seat_number`),
  KEY `idx_ticket_flight_date` (`airline_id`,`flight_number`,`departure_date`),
  CONSTRAINT `fk_ticket_flight_flight` FOREIGN KEY (`airline_id`, `flight_number`) REFERENCES `flight` (`airline_id`, `flight_number`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_ticket_flight_ticket` FOREIGN KEY (`ticket_number`) REFERENCES `ticket` (`ticket_number`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ticket_flight`
--

LOCK TABLES `ticket_flight` WRITE;
/*!40000 ALTER TABLE `ticket_flight` DISABLE KEYS */;
INSERT INTO `ticket_flight` VALUES (1,1,'AA','101','2026-05-15','12A','economy','Vegetarian'),(2,1,'UA','505','2026-05-20','3B','business',NULL),(2,2,'UA','606','2026-05-20','4B','business',NULL),(3,1,'DL','303','2026-05-18','18C','economy',NULL);
/*!40000 ALTER TABLE `ticket_flight` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `waiting_list`
--

DROP TABLE IF EXISTS `waiting_list`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `waiting_list` (
  `customer_id` int NOT NULL,
  `airline_id` char(2) NOT NULL,
  `flight_number` varchar(10) NOT NULL,
  `flight_date` date NOT NULL,
  `request_datetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `alert_sent` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`customer_id`,`airline_id`,`flight_number`,`flight_date`),
  KEY `fk_wait_flight` (`airline_id`,`flight_number`),
  CONSTRAINT `fk_wait_customer` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_wait_flight` FOREIGN KEY (`airline_id`, `flight_number`) REFERENCES `flight` (`airline_id`, `flight_number`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `waiting_list`
--

LOCK TABLES `waiting_list` WRITE;
/*!40000 ALTER TABLE `waiting_list` DISABLE KEYS */;
INSERT INTO `waiting_list` VALUES (1,'SW','707','2026-05-19','2026-04-26 23:17:30',0),(2,'AA','101','2026-05-15','2026-04-26 23:17:30',0);
/*!40000 ALTER TABLE `waiting_list` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'travel_reservation'
--

--
-- Dumping routines for database 'travel_reservation'
--
SET @@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-26 23:17:31
