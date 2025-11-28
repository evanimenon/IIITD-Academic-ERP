-- MySQL dump 10.13  Distrib 8.0.44, for macos15 (arm64)
--
-- Host: localhost    Database: erp_db
-- ------------------------------------------------------
-- Server version	8.0.44

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
-- Table structure for table `courses`
--

DROP TABLE IF EXISTS `courses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `courses` (
  `course_id` varchar(64) NOT NULL,
  `code` text,
  `title` text,
  `credits` int DEFAULT NULL,
  PRIMARY KEY (`course_id`),
  CONSTRAINT `chk_credits_nonneg` CHECK ((`credits` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `courses`
--

LOCK TABLES `courses` WRITE;
/*!40000 ALTER TABLE `courses` DISABLE KEYS */;
INSERT INTO `courses` VALUES ('ABC101','ABC','Course 1',4),('ABC102','XYZ','Course 2',4),('BIO101','FOB-I','Foundations of Biology-I',4),('BIO111','ABC','Biochemistry',4),('BIO201','FOB-II','Foundations of Biology II',4),('BIO213','IQB','Introduction to Quantitative Biology',4),('BIO221','PB','Practical Bioinformatics',4),('BIO321','ABIN','Algorithms in Bioinformatics',4),('BIO361','BioP','Biophysics',4),('BIO506','SSSBB','Stochastic Simulations in Systems Biology and Biophysics',4),('BIO522','ACB','Algorithms in Computational Biology',4),('BIO523','CHI','Chemoinformatics',4),('BIO524','BIP','Biomedical Image processing',4),('BIO531','IMB','Introduction to Mathematical Biology',4),('BIO532','NB','Network Biology',4),('BIO533','S&SB','Systems and Synthetic Biology',4),('BIO534','ICN','Introduction to Computational Neuroscience',4),('BIO541','DSG','Data Sciences for Genomics',4),('BIO542','MLBA','Machine Learning for Biomedical Applications',4),('BIO543','BDMH','Big Data Mining in Healthcare',4),('BIO544','CGAS','Computational Gastronomy',4),('BIO545','BioStats','Biostatistics',4),('BIO546','CM','Computing for Medicine',4),('BIO548','HMDS','Human Microbiome Data Science',4),('BIO551A','PCB','Programming for Computational Biology',4),('BIO561','CADD','Computer Aided Drug Design',4),('COM101','COM','Communication Skills',4),('COM301A','TCOM','Technical Communication',4),('CSE101','IP','Introduction to Programming',4),('CSE102','DSA','Data Structures & Algorithms',4),('CSE112','CO','Computer Organization',4),('CSE121','DM','Discrete Mathematics',4),('CSE140','IIS','Introduction to Intelligent Systems',4),('CSE201','AP','Advanced Programming',4),('CSE202','DBMS','Fundamentals of Database Management System',4),('CSE222','ADA','Algorithm Design and Analysis',4),('CSE223','ALD','Algorithm Design and Analysis',4),('CSE231','OS','Operating Systems',4),('CSE232','CN','Computer Networks',4),('CSE233','NAD','Network Administration',4),('CSE319','MAD','Modern Algorithm Design',4),('CSE320','AA','Advanced Algorithms',4),('CSE322','ToC','Theory of Computation',4),('CSE333','CG','Computer Graphics',4),('CSE340','DIP','Digital Image Processing',4),('CSE342','SML','Statistical Machine Learning',4),('CSE344','CV','Computer Vision',4),('CSE345','FCS','Foundations of Computer Security',4),('CSE347','UsS','Usable Security and Privacy',4),('CSE350','NSC','Network Security',4),('CSE354','NSS-II','Networks and System Security II',4),('CSE421','CMPT','Complexity Theory',4),('CSE441','BIOM','Advanced Biometrics',4),('DES101','DDV','Design Drawing & Visualization',4),('DES102','IHCI','Introduction to HCI',4),('DES130','PIS','Prototyping Interactive Systems',4),('DES201','DPP','Design Processes and Perspectives',4),('DES202','VDC','Visual Design & Communication',4),('DES204','HCI','Human Computer Interaction',4),('DES205','DIS','Design of Interactive Systems',4),('DES206','PIS','Prototyping Interactive Systems',4),('DES302','IAG','Introduction to Animation and Graphics',4),('DES303','FMRP','Film Making and Radio Podcasting',4),('DES305','3DAF','3D Animation filmmaking',4),('ECE111','DC','Digital Circuits',4),('ECE113','BE','Basic Electronics',4),('ECE210','PSD','Physics of Semiconductor Devices',4),('ECE211','ESD','Electronic System Design',4),('ECE214','IE','Integrated Electronics',4),('ECE215','CTD','Circuit Theory and Devices',4),('ECE230','F&W','Fields and Waves',4),('ECE240','PCS','Principles of Communication Systems',4),('ECE250','S&S','Signals and Systems',4),('ECE270','ELD','Embedded Logic Design',4),('ECE314','DVD','Digital VLSI Design',4),('ECE315','CMOS','Analog CMOS Circuit Design',4),('ECE318','SSD','Solid State Devices',4),('ECE321','RFCD','RF Circuit Design',4),('ECE331','AFW','Applied Fields and Waves',4),('ECE340','DCS','Digital Communication Systems',4),('ECE343','MCOM','Mobile Communications',4),('ECE351','DSP','Digital Signal Processing',4),('ECE363','ML','Machine Learning',4),('ECE366','NEID','Neural Engineering and Implantable Devices',4),('ECO221','ECO','Econometrics I',4),('ECO223','MB','Money and Banking',4),('ECO301','IEA','Introduction to Economic Analysis',4),('ECO311','GMT','Game Theory',4),('ECO312','IO','Industrial Organization',4),('ECO313','MD','Market Design',4),('ECO314','BEco','Behavioural Economics',4),('ECO331','FF','Foundations of Finance',4),('ECO332','VPM','Valuation and Portfolio Management',4),('ECO333','MA','Macroeconomics',4),('ENT302','DTI','Design Thinking and Innovation',4),('ENT303A','EL','Experiential Learning',4),('ENT304','DTSE','Digital Technologies and Social Entrepreneurship',4),('ENT305','SICSRSD','Social Innovation, Corporate Social Responsibility and Sustainable Development',4),('ENT411','EComm','Entrepreneurial Communication',4),('ENT412','EK','Entrepreneurial Khichadi',4),('ENT413','EF','Entrepreneurial Finance',4),('ENT414','RIPS','Relevance of Intellectual Property for Startups',4),('ENT415','NVP','New Venture Planning',4),('ESC205','EVS','Environmental Sciences',4),('ESC207A','EEE','Ecology, Evolution, and Environment',4),('MTH100','M-I','Linear Algebra',4),('MTH201','P&S','Probability and Statistics',4),('MTH203','M-III','Multivariate Calculus',4),('MTH204','M-IV','Mathematics IV',4),('MTH210','DS','Discrete Structures',4),('MTH211','NT','Number Theory',4),('MTH212','AA-I','Abstract Algebra I',4),('MTH240','RA-I','Real Analysis I',4),('MTH270','NM','Numerical Methods',4),('MTH300','IML','Introduction to Mathematical Logic',4),('MTH302','ALG','Algebra',4),('MTH310','GT','Graph Theory',4),('MTH311','CIA','Combinatorics and its Applications',4),('MTH340','RA-II','Real Analysis-II',4),('MTH371','SPA','Stochastic Processes and Applications',4),('MTH372','SI','Statistical Inference',4),('MTH373','SC','Scientific Computing',4),('MTH376','MB','Mechanics of Bodies',4);
/*!40000 ALTER TABLE `courses` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `enrollments`
--

DROP TABLE IF EXISTS `enrollments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `enrollments` (
  `enrollment_id` int NOT NULL AUTO_INCREMENT,
  `student_id` varchar(64) NOT NULL,
  `section_id` int NOT NULL,
  `status` enum('REGISTERED','DROPPED') NOT NULL DEFAULT 'REGISTERED',
  `final_grade` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`enrollment_id`),
  UNIQUE KEY `uq_student_section` (`student_id`,`section_id`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `enrollments`
--

LOCK TABLES `enrollments` WRITE;
/*!40000 ALTER TABLE `enrollments` DISABLE KEYS */;
INSERT INTO `enrollments` VALUES (6,'evani24210',17,'REGISTERED',NULL),(7,'evani24210',16,'REGISTERED',NULL),(8,'evani24210',18,'REGISTERED',NULL),(9,'evani24210',19,'REGISTERED',NULL),(10,'evani24210',27,'REGISTERED',NULL),(17,'stu1',3,'REGISTERED','B'),(18,'stu1',4,'REGISTERED',NULL),(19,'stu2',3,'REGISTERED',NULL),(20,'stu2',4,'REGISTERED',NULL),(26,'stu1',18,'REGISTERED',NULL);
/*!40000 ALTER TABLE `enrollments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `grades`
--

DROP TABLE IF EXISTS `grades`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `grades` (
  `grade_id` int NOT NULL AUTO_INCREMENT,
  `enrollment_id` int DEFAULT NULL,
  `component_id` int DEFAULT NULL,
  `score` double DEFAULT NULL,
  PRIMARY KEY (`grade_id`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `grades`
--

LOCK TABLES `grades` WRITE;
/*!40000 ALTER TABLE `grades` DISABLE KEYS */;
INSERT INTO `grades` VALUES (1,17,40,26),(2,17,41,16),(3,17,42,25),(4,19,40,20),(5,19,41,15),(6,19,42,22),(7,18,44,26),(8,18,45,17),(9,18,46,27),(10,20,44,21),(11,20,45,15),(12,20,46,24),(13,17,82,24),(14,17,83,16),(15,17,84,25),(16,17,85,15),(29,18,68,27),(30,18,69,18),(31,19,83,15),(32,19,82,20),(33,19,84,22);
/*!40000 ALTER TABLE `grades` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `instructors`
--

DROP TABLE IF EXISTS `instructors`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `instructors` (
  `instructor_id` int NOT NULL,
  `department` text,
  `instructor_name` text,
  PRIMARY KEY (`instructor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `instructors`
--

LOCK TABLES `instructors` WRITE;
/*!40000 ALTER TABLE `instructors` DISABLE KEYS */;
INSERT INTO `instructors` VALUES (1000001,'CSD','Instructor 1'),(1000002,'BIO','Instructor 2'),(1000003,'CSE','Sambuddho C.'),(1000004,'SSH','Payel C.'),(1000005,'CSE','Md. Shad A.'),(1000006,'CSE','Ravi A.'),(1000007,'DES','Sonal K.'),(1000008,'DES','Pragma K.'),(1000009,'ECE','Pravesh B.'),(1000010,'ECE','Manuj M.'),(1000011,'MTH','Subhajit G.'),(1000012,'MTH','Prahlhad D.'),(1000013,'BIO','Jaspreet K.'),(1000014,'BIO','Gaurav A.'),(1000016,'CSE','Vivek K.'),(1000017,'CSE','Raghava M.'),(1000018,'DES','Anmol S.'),(1000019,'ECE','Pragya K.'),(1000020,'ECE','Anubha G.'),(1000021,'ECE','Sumit D.'),(1000022,'MTH','Sarthok S.'),(1000023,'MTH','Diptapriyo M.'),(1000024,'MTH','Debika B.'),(1000025,'MTH','Nabanita R.'),(1000026,'CSE','Nikhil G.'),(1000027,'SSH','Soibam H.'),(1000028,'SSH','Sonia B.'),(1000029,'ENT','Anupam S.'),(1000030,'SSH','Neera C.'),(1000031,'ENT','Aheli C.'),(1000032,'BIO','Debarka S.'),(1000033,'BIO','Arjun R.'),(1000034,'BIO','Sriram K.'),(1000035,'BIO','Vibhor K.'),(1000036,'BIO','Arul M.'),(1000038,'BIO','Ganesh B.'),(1000039,'BIO','Tavpritesh S.'),(1000040,'BIO','Tarini G.'),(1000041,'CSE','Pushpendra S.'),(1000042,'CSE','Piyus K.'),(1000043,'CSE','Jainendra S.'),(1000044,'CSE','Syamantak D.'),(1000045,'CSE','Bapi C.'),(1000046,'CSE','Ojaswa S.'),(1000047,'CSE','Debajyoti B.'),(1000048,'ECE','Vinayak A.'),(1000049,'CSE','Vikram G.'),(1000050,'CSE','Supratim S.'),(1000051,'CSE','Arani B.'),(1000052,'CSE','Rinku S.'),(1000053,'CSE','Rajiv R.'),(1000054,'CSE','Gautam S.'),(1000055,'CSE','Tanmoy K.'),(1000056,'CSE','Mukesh M.'),(1000057,'CSE','Ranjitha P.'),(1000058,'DES','Anoop R.'),(1000059,'DES','Angshu D.'),(1000060,'DES','Aman S.'),(1000061,'DES','Richa G.'),(1000062,'DES','Vinish K.'),(1000063,'DES','Kalpana S.'),(1000064,'ECE','Sujay D.'),(1000065,'ECE','Sayan R.'),(1000066,'ECE','Anuj G.'),(1000067,'ECE','Sayak B.'),(1000068,'ECE','SS J.'),(1000069,'ECE','Shobha R.'),(1000070,'DES','Arun B.'),(1000071,'ECE','Chanekar V.'),(1000072,'ECE','Sneh S.'),(1000073,'ECE','Ram G.'),(1000074,'ECE','Vivek B.'),(1000075,'ECE','Shamik S.'),(1000076,'ECE','Abhijit M.'),(1000077,'ECE','Sanjit K.'),(1000078,'ECE','Sanat B.'),(1000079,'ECE','Subramanyam A.'),(1000082,'ENT','Somrajan P.'),(1000083,'Maths','Kaushik K.'),(1000084,'Maths','Satish P.'),(1000085,'Maths','Rahul R.'),(1000086,'Maths','Subhashree M.'),(1000087,'Maths','Sankha B.'),(1000088,'Maths','Anuradha S.'),(1000089,'Maths','Monika A.'),(1000090,'Maths','Sachchidanand P.'),(1000091,'Maths','Sneha C.'),(1000092,'SSH','Souvik D.'),(1000093,'SSH','Kiriti K.'),(1000094,'SSH','Gaurav A.'),(1000095,'SSH','Ruhi S.'),(1000096,'SSH','Meenakshi J.'),(1000097,'SSH','Pankaj V.'),(1000098,'SSH','D. K. S.'),(1000099,'SSH','Mrinmoy C.'),(1000101,'SSH','Smriti S.'),(1000102,'SSH','Gayatri N.'),(1000103,'SSH','Nishad P.'),(1000104,'SSH','Manohar K.'),(1000105,'SSH','Deepak P.'),(1000107,'ENT','Alok S.'),(1000108,'ENT','Vinay S.'),(1000109,'SSH','Paro M.'),(1000110,'SSH','Praveen P.');
/*!40000 ALTER TABLE `instructors` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `section_components`
--

DROP TABLE IF EXISTS `section_components`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `section_components` (
  `id` int NOT NULL AUTO_INCREMENT,
  `section_id` int DEFAULT NULL,
  `component_name` text,
  `weight` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=86 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `section_components`
--

LOCK TABLES `section_components` WRITE;
/*!40000 ALTER TABLE `section_components` DISABLE KEYS */;
INSERT INTO `section_components` VALUES (43,2,'Assignments',20),(60,16,'Midsem',30),(61,16,'Endsem',30),(62,16,'Assigments',20),(63,16,'Quizzes',20),(68,4,'Quizzes',30),(69,4,'Midsem',20),(70,4,'Endsem',30),(71,4,'Labs',20),(72,18,'Midsem',25),(73,18,'Endsem',25),(74,18,'Assignment 1',10),(75,18,'Assignment 2',10),(76,18,'Quizzes',10),(77,18,'Project',20),(82,3,'Quizzes',25),(83,3,'Midsem',30),(84,3,'Endsem',30),(85,3,'Assignments',15);
/*!40000 ALTER TABLE `section_components` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sections`
--

DROP TABLE IF EXISTS `sections`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sections` (
  `section_id` int NOT NULL AUTO_INCREMENT,
  `course_id` text,
  `instructor_id` int DEFAULT NULL,
  `day_time` text,
  `room` text,
  `capacity` int DEFAULT NULL,
  `semester` text,
  `year` int DEFAULT NULL,
  PRIMARY KEY (`section_id`),
  CONSTRAINT `chk_capacity_nonneg` CHECK ((`capacity` >= 0)),
  CONSTRAINT `chk_year_nonneg` CHECK ((`year` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sections`
--

LOCK TABLES `sections` WRITE;
/*!40000 ALTER TABLE `sections` DISABLE KEYS */;
INSERT INTO `sections` VALUES (3,'ABC101',1000001,'Wed 08:00-09:00','B213',2,'Monsoon',2025),(4,'ABC102',1000002,'Thu 11:00-12:30','A204',2,'Monsoon',2025),(5,'COM101',1000004,'Mon 09:00-10:30','C101',80,'Monsoon',2025),(7,'COM101',1000004,'Wed 09:00-10:30','C102',80,'Monsoon',2025),(8,'CSE101',1000005,'Tue 10:45-12:15','Lab-201',90,'Monsoon',2025),(9,'CSE101',1000006,'Thu 10:45-12:15','Lab-202',90,'Monsoon',2025),(10,'DES102',1000007,'Mon 14:00-15:30','D101',60,'Monsoon',2025),(11,'DES102',1000008,'Wed 14:00-15:30','D102',60,'Monsoon',2025),(12,'ECE111',1000009,'Tue 09:00-10:30','E201',70,'Monsoon',2025),(13,'ECE111',1000010,'Thu 09:00-10:30','E202',70,'Monsoon',2025),(14,'MTH100',1000011,'Mon 16:00-17:30','M101',120,'Monsoon',2025),(15,'MTH100',1000012,'Wed 16:00-17:30','M102',120,'Monsoon',2025),(16,'BIO213',1000013,'Tue 14:00-15:30','B201',60,'Monsoon',2025),(17,'BIO201',1000014,'Thu 14:00-15:30','B203',60,'Monsoon',2025),(18,'CSE201',1000003,'Mon 11:00-12:30','Lab-203',120,'Monsoon',2025),(19,'CSE231',1000016,'Tue 16:00-17:30','C201',100,'Monsoon',2025),(20,'CSE231',1000017,'Thu 16:00-17:30','C202',100,'Monsoon',2025),(21,'DES201',1000018,'Fri 09:00-10:30','D201',60,'Monsoon',2025),(22,'ECE215',1000019,'Fri 10:45-12:15','E203',70,'Monsoon',2025),(23,'ECE250',1000020,'Fri 14:00-15:30','E204',70,'Monsoon',2025),(24,'ECE270',1000021,'Fri 16:00-17:30','E205',70,'Monsoon',2025),(25,'MTH203',1000011,'Tue 11:00-12:30','M201',80,'Monsoon',2025),(26,'MTH203',1000022,'Thu 11:00-12:30','M202',80,'Monsoon',2025),(27,'MTH210',1000023,'Mon 08:00-09:00','M203',80,'Monsoon',2025),(28,'MTH211',1000024,'Wed 08:00-09:00','M204',80,'Monsoon',2025),(29,'MTH240',1000025,'Fri 08:00-09:00','M205',80,'Monsoon',2025),(30,'CSE121',1000026,'Wed 11:00-12:30','C203',100,'Monsoon',2025),(31,'SSH201',1000027,'Tue 13:00-14:00','S101',60,'Monsoon',2025),(32,'ENT201',1000029,'Thu 13:00-14:00','E101',60,'Monsoon',2025),(33,'SSH221',1000030,'Mon 13:00-14:00','S102',60,'Monsoon',2025),(34,'ENT202',1000031,'Wed 13:00-14:00','E102',60,'Monsoon',2025),(37,'BIO111',1000003,'Mon 09:00-10:30','TBA',60,'Monsoon',2025);
/*!40000 ALTER TABLE `sections` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `settings`
--

DROP TABLE IF EXISTS `settings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `settings` (
  `setting_key` varchar(64) NOT NULL,
  `setting_value` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`setting_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `settings`
--

LOCK TABLES `settings` WRITE;
/*!40000 ALTER TABLE `settings` DISABLE KEYS */;
INSERT INTO `settings` VALUES ('COURSE_DROP_DEADLINE','2025-12-30'),('maintenance_mode','OFF');
/*!40000 ALTER TABLE `settings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `students`
--

DROP TABLE IF EXISTS `students`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `students` (
  `student_id` varchar(64) NOT NULL,
  `roll_no` int DEFAULT NULL,
  `full_name` text,
  `program` text,
  `year` int DEFAULT NULL,
  PRIMARY KEY (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `students`
--

LOCK TABLES `students` WRITE;
/*!40000 ALTER TABLE `students` DISABLE KEYS */;
INSERT INTO `students` VALUES ('AB24500',2024500,'A B','CSE',1),('evani24210',2024210,'EVANI MENON','CSB',2024),('faizan24211',2024211,'FAIZAN C.','CSSS',2024),('fardeen24212',2024212,'FARDEEN M.','CSB',2024),('gagan24213',2024213,'GAGAN K.','ECE',2024),('gagandeep24214',2024214,'GAGANDEEP S.','CSAM',2024),('garv24215',2024215,'GARV a.','ECE',2024),('garv24216',2024216,'GARV J.','CSE',2024),('garvit24217',2024217,'GARVIT r.','CSD',2024),('gauranshi24218',2024218,'GAURANSHI G.','CSSS',2024),('gaurav24220',2024220,'GAURAV B.','CSE',2024),('gaurav24221',2024221,'GAURAV J.','CSE',2024),('gaurav24222',2024222,'GAURAV Y.','CSAM',2024),('gerick24223',2024223,'GERICK R.','CSAI',2024),('goyam24224',2024224,'GOYAM J.','CSE',2024),('hansika24225',2024225,'HANSIKA S.','CSE',2024),('hanzala24226',2024226,'HANZALA A.','CSB',2024),('hardik24227',2024227,'HARDIK','CSAM',2024),('hardik24228',2024228,'HARDIK G.','CSE',2024),('hariom24229',2024229,'HARIOM K.','CSE',2024),('harsh24232',2024232,'HARSH','CSE',2024),('harsh24233',2024233,'HARSH','CSAM',2024),('harsh24234',2024234,'HARSH','CSAM',2024),('harsh24235',2024235,'HARSH','CSB',2024),('harsh24236',2024236,'HARSH B.','CSAI',2024),('harsh24237',2024237,'HARSH K.','CSE',2024),('harsh24238',2024238,'HARSH K.','ECE',2024),('harsh24239',2024239,'HARSH K.','CSAM',2024),('harsh24240',2024240,'HARSH N.','CSE',2024),('harsh24241',2024241,'HARSH P.','CSAI',2024),('harsh24242',2024242,'Harsh S.','EVE',2024),('harshit24243',2024243,'HARSHIT B.','CSD',2024),('harshit24244',2024244,'HARSHIT D.','CSD',2024),('harshit24246',2024246,'HARSHIT K.','CSB',2024),('harshit24247',2024247,'HARSHIT K.','EVE',2024),('harshit24248',2024248,'HARSHIT K.','CSB',2024),('harshit24250',2024250,'HARSHIT S.','CSB',2024),('harshita24251',2024251,'HARSHITA','CSE',2024),('harshmeet24252',2024252,'HARSHMEET S.','ECE',2024),('harshul24253',2024253,'HARSHUL K.','CSD',2024),('harshul24254',2024254,'HARSHUL T.','EVE',2024),('harshvardhan24255',2024255,'HARSHVARDHAN M.','EVE',2024),('hemanshu24256',2024256,'HEMANSHU Y.','EVE',2024),('hemanth24257',2024257,'HEMANTH S.','CSE',2024),('hemendra24258',2024258,'HEMENDRA  P.','CSSS',2024),('herumb24259',2024259,'HERUMB S.','CSAM',2024),('himanshu24261',2024261,'HIMANSHU','CSE',2024),('hiten24263',2024263,'HITEN B.','CSE',2024),('hritang24264',2024264,'HRITANG  S.','CSD',2024),('iaikansh24265',2024265,'IAIKANSH B.','CSSS',2024),('ishaan24266',2024266,'ISHAAN A.','CSE',2024),('ishaan24267',2024267,'ISHAAN D.','CSAI',2024),('ishank24268',2024268,'ISHANK C.','CSAM',2024),('ishant24269',2024269,'ISHANT G.','CSAI',2024),('ishika24270',2024270,'ISHIKA D.','CSE',2024),('isobel24271',2024271,'ISOBEL K.','CSE',2024),('jagjot24272',2024272,'JAGJOT  S.','CSSS',2024),('jagrit24273',2024273,'JAGRIT K.','CSSS',2024),('jahanvi24274',2024274,'JAHANVI K.','ECE',2024),('jai24275',2024275,'JAI A.','CSAM',2024),('jai24276',2024276,'JAI B.','EVE',2024),('janisha24277',2024277,'JANISHA M.','CSAM',2024),('jas24278',2024278,'JAS K.','CSD',2024),('jatin24279',2024279,'JATIN P.','CSAM',2024),('jayant24280',2024280,'JAYANT','CSSS',2024),('jayant24281',2024281,'JAYANT G.','CSSS',2024),('jayant24282',2024282,'JAYANT R.','CSAM',2024),('jazl24283',2024283,'JAZL A.','CSE',2024),('jitender24284',2024284,'JITENDER B.','EVE',2024),('jitesh24285',2024285,'JITESH','EVE',2024),('jivansh24286',2024286,'Jivansh K.','CSD',2024),('joban24287',2024287,'JOBAN S.','CSSS',2024),('jyotiraditya24288',2024288,'JYOTIRADITYA','CSD',2024),('jyotirmaya24289',2024289,'JYOTIRMAYA S.','CSE',2024),('kabeer24290',2024290,'KABEER','ECE',2024),('kanak24291',2024291,'KANAK  G.','CSSS',2024),('kanishk24292',2024292,'KANISHK G.','CSAI',2024),('karan24293',2024293,'KARAN S.','CSD',2024),('karanveer24294',2024294,'KARANVEER S.','CSE',2024),('kartik24295',2024295,'KARTIK K.','EVE',2024),('kartik24296',2024296,'KARTIK T.','CSSS',2024),('kartik24297',2024297,'KARTIK Y.','CSD',2024),('kartikeya24298',2024298,'Kartikeya S.','CSB',2024),('keshav24299',2024299,'KESHAV K.','CSB',2024),('keshav24300',2024300,'KESHAV S.','CSAI',2024),('khushal24301',2024301,'KHUSHAL Y.','CSAM',2024),('kirat24303',2024303,'KIRAT G.','CSAM',2024),('krish24304',2024304,'KRISH','CSSS',2024),('krish24305',2024305,'KRISH B.','CSD',2024),('krish24306',2024306,'KRISH P.','ECE',2024),('krishang24307',2024307,'KRISHANG S.','EVE',2024),('krishiv24308',2024308,'KRISHIV V.','CSE',2024),('krishna24309',2024309,'KRISHNA G.','CSAM',2024),('krishna24310',2024310,'KRISHNA K.','EVE',2024),('krishna24311',2024311,'KRISHNA S.','CSE',2024),('krishna24312',2024312,'KRISHNA S.','CSE',2024),('kunal24313',2024313,'KUNAL B.','CSE',2024),('kushagra24314',2024314,'KUSHAGRA A.','CSE',2024),('kushav24316',2024316,'KUSHAV N.','CSAM',2024),('lakshay24317',2024317,'LAKSHAY','ECE',2024),('lakshay24318',2024318,'LAKSHAY G.','CSD',2024),('leelansh24319',2024319,'LEELANSH K.','ECE',2024),('lucky24320',2024320,'LUCKY S.','ECE',2024),('luvya24321',2024321,'LUVYA N.','CSD',2024),('madhav24322',2024322,'MADHAV G.','CSB',2024),('madhav24323',2024323,'MADHAV R.','CSE',2024),('madhu24324',2024324,'MADHU B.','CSD',2024),('madhvesh24325',2024325,'MADHVESH K.','CSSS',2024),('mahi24326',2024326,'MAHI Y.','ECE',2024),('maloth24327',2024327,'MALOTH A.','ECE',2024),('manan24328',2024328,'MANAN K.','CSE',2024),('manan24329',2024329,'MANAN S.','ECE',2024),('manas24330',2024330,'MANAS S.','CSAI',2024),('manish24331',2024331,'MANISH A.','ECE',2024),('mankena24332',2024332,'MANKENA S.','ECE',2024),('mannat24333',2024333,'MANNAT R.','CSSS',2024),('manojna24334',2024334,'MANOJNA R.','CSE',2024),('mantavya24335',2024335,'MANTAVYA A.','CSSS',2024),('manthan24336',2024336,'MANTHAN K.','CSE',2024),('manyata24337',2024337,'MANYATA M.','CSD',2024),('mayank24338',2024338,'MAYANK A.','CSB',2024),('mayank24339',2024339,'MAYANK P.','CSAI',2024),('mayank24340',2024340,'MAYANK R.','CSE',2024),('mayank24341',2024341,'MAYANK R.','CSAM',2024),('mayank24342',2024342,'MAYANK S.','CSD',2024),('mayank24343',2024343,'MAYANK Y.','CSE',2024),('md24344',2024344,'MD A.','CSD',2024),('meera24345',2024345,'Meera I.','CSD',2024),('mehardeep24346',2024346,'MEHARDEEP S.','CSE',2024),('mihir24347',2024347,'MIHIR J.','CSE',2024),('mithil24348',2024348,'MITHIL K.','CSAM',2024),('mohak24349',2024349,'MOHAK G.','ECE',2024),('mohammdd24350',2024350,'ZAID A.','EVE',2024),('mohd24351',2024351,'MOHD A.','ECE',2024),('mohd24352',2024352,'MOHD F.','CSD',2024),('mohd24353',2024353,'MOHD Z.','CSAM',2024),('mohit24354',2024354,'MOHIT G.','ECE',2024),('mohit24355',2024355,'Mohit P.','CSE',2024),('mridul24356',2024356,'MRIDUL V.','CSSS',2024),('mritunjay24357',2024357,'MRITUNJAY K.','CSB',2024),('mrityunjai24358',2024358,'MRITYUNJAI P.','CSSS',2024),('mukul24359',2024359,'MUKUL','CSE',2024),('mukul24360',2024360,'MUKUL','CSAI',2024),('nakul24361',2024361,'NAKUL Z.','CSAM',2024),('nalin24362',2024362,'NALIN G.','CSAM',2024),('naman24363',2024363,'NAMAN  C.','CSSS',2024),('naman24364',2024364,'NAMAN B.','CSE',2024),('naman24365',2024365,'NAMAN D.','EVE',2024),('naman24366',2024366,'NAMAN G.','CSAI',2024),('naman24367',2024367,'NAMAN G.','EVE',2024),('naman24368',2024368,'NAMAN G.','CSE',2024),('namish24369',2024369,'NAMISH B.','CSAI',2024),('nandika24371',2024371,'NANDIKA  ROUTRAY','CSD',2024),('stu1',1,'Student 1','CSB',2024),('stu2',2,'Student 2','CSD',2024);
/*!40000 ALTER TABLE `students` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-28 23:20:55
