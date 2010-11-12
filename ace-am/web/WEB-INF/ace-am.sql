--
-- schema for ACE 1.4
--
DROP TABLE IF EXISTS `collection`;
CREATE TABLE `collection` (
  `ID` bigint(20) NOT NULL auto_increment,
  `STORAGE` varchar(255) default NULL,
  `CHECKPERIOD` int(11) default NULL,
  `DIRECTORY` varchar(255) NOT NULL,
  `PROXYDATA` tinyint(1) default '0',
  `NAME` varchar(255) NOT NULL,
  `COLGROUP` varchar(255) default NULL,
  `AUDITTOKENS` tinyint(1) default '0',
  `LASTSYNC` datetime default NULL,
  `STATE` char(1) default NULL,
  `EMAILLIST` text default NULL,
  `DIGESTALGORITHM` varchar(20) default 'SHA-256',
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `monitored_item`;
CREATE TABLE `monitored_item` (
  `ID` bigint(20) NOT NULL auto_increment,
  `LASTSEEN` datetime default NULL,
  `STATECHANGE` datetime default NULL,
  `LASTVISITED` datetime default NULL,
  `PARENTPATH` varchar(512) character set latin1 collate latin1_bin default NULL,
  `STATE` char(1) NOT NULL,
  `PATH` varchar(512) character set latin1 collate latin1_bin default NULL,
  `DIRECTORY` tinyint(1) NOT NULL default '0',
  `TOKEN_ID` bigint(20) default NULL,
  `PARENTCOLLECTION_ID` bigint(20) NOT NULL,
  `FILEDIGEST` varchar(255) default NULL,
  `SIZE` bigint(20) default 0,
  PRIMARY KEY  (`ID`),
  KEY `idx_monitored_item_path` (`PATH`(512)),
  KEY `logevent_parentpath_idx` (`PARENTPATH`(512)),
  KEY `idx_monitored_item_state` (`STATE`),
  KEY `FK_monitored_item_PARENTCOLLECTION_ID` (`PARENTCOLLECTION_ID`),
  KEY `FK_monitored_item_TOKEN_ID` (`TOKEN_ID`),
  KEY `idx_monitored_item_digest` (`FILEDIGEST`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 MAX_ROWS=2431504384;

DROP TABLE IF EXISTS `token`;
CREATE TABLE `token` (
  `ID` bigint(20) NOT NULL auto_increment,
  `CREATEDATE` datetime default NULL,
  `VALID` tinyint(1) default '0',
  `LASTVALIDATED` datetime default NULL,
  `java_token_response` blob,
  `PARENTCOLLECTION_ID` bigint(20) NOT NULL,
  PRIMARY KEY  (`ID`),
  KEY `FK_token_PARENTCOLLECTION_ID` (`PARENTCOLLECTION_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 MAX_ROWS=2431504384;

DROP TABLE IF EXISTS `logevent`;
CREATE TABLE `logevent` (
  `ID` bigint(20) NOT NULL auto_increment,
  `SESSION` bigint(20) default NULL,
  `PATH` text character set latin1 collate latin1_bin,
  `DESCRIPTION` text,
  `DATE` datetime default NULL,
  `LOGTYPE` int(11) default NULL,
  `COLLECTION_ID` bigint(20) default NULL,
  PRIMARY KEY  (`ID`),
  KEY `logevent_path_idx` (`PATH`(512)),
  KEY `logevent_session_idx` (`SESSION`),
  KEY `logevent_type_idx` (`LOGTYPE`),
  KEY `FK_logevent_COLLECTION_ID` (`COLLECTION_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 MAX_ROWS=2431504384;

DROP TABLE IF EXISTS `benchmarksettings`;
CREATE TABLE `benchmarksettings` (
  `ID` bigint(20) NOT NULL auto_increment,
  `DIRS` int(11) NOT NULL,
  `READFILES` tinyint(1) default '0',
  `FILES` int(11) NOT NULL,
  `FILELENGTH` bigint(20) default NULL,
  `BLOCKSIZE` int(11) default NULL,
  `DEPTH` int(11) NOT NULL,
  `COLLECTION_ID` bigint(20) NOT NULL,
  PRIMARY KEY  (`ID`),
  KEY `FK_benchmarksettings_COLLECTION_ID` (`COLLECTION_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `srbsettings`;
CREATE TABLE `srbsettings` (
  `ID` bigint(20) NOT NULL auto_increment,
  `ZONE` varchar(255) NOT NULL,
  `SERVER` varchar(255) NOT NULL,
  `USERNAME` varchar(255) NOT NULL,
  `DOMAIN` varchar(255) NOT NULL,
  `PORT` int(11) NOT NULL,
  `PASSWORD` varchar(255) NOT NULL,
  `COLLECTION_ID` bigint(20) NOT NULL,
  PRIMARY KEY  (`ID`),
  KEY `FK_srbsettings_COLLECTION_ID` (`COLLECTION_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `swapsettings`;
CREATE TABLE `swapsettings` (
  `ID` bigint(20) NOT NULL auto_increment,
  `USERNAME` varchar(255) NOT NULL,
  `SERVERS` varchar(255) NOT NULL,
  `PREFIX` varchar(255) NOT NULL,
  `PORT` int(11) NOT NULL,
  `PASSWORD` varchar(255) NOT NULL,
  `COLLECTION_ID` bigint(20) NOT NULL,
  PRIMARY KEY  (`ID`),
  KEY `FK_srbsettings_COLLECTION_ID` (`COLLECTION_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `irodssettings`;
CREATE TABLE `irodssettings` (
  `ID` bigint(20) NOT NULL auto_increment,
  `ZONE` varchar(255) NOT NULL,
  `SERVER` varchar(255) NOT NULL,
  `PASSWORD` varchar(255) NOT NULL,
  `PORT` int(11) NOT NULL,
  `USERNAME` varchar(255) NOT NULL,
  `COLLECTION_ID` bigint(20) NOT NULL,
  PRIMARY KEY  (`ID`),
  KEY `FK_irodssettings_COLLECTION_ID` (`COLLECTION_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `userroles`;
CREATE TABLE `userroles` (
  `ID` bigint(20) NOT NULL auto_increment,
  `USERNAME` varchar(255) default NULL,
  `ROLENAME` varchar(255) default NULL,
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `ID` bigint(20) NOT NULL auto_increment,
  `USERNAME` varchar(255) default NULL,
  `PASSWORD` varchar(255) default NULL,
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `filter_entry`;
CREATE TABLE `filter_entry` (
  `ID` bigint(20) NOT NULL auto_increment,
  `REGEX` varchar(255) default NULL,
  `AFFECTEDITEM` int(11) default NULL,
  `COLLECTION_ID` bigint(20) NOT NULL,
  PRIMARY KEY  (`ID`),
  KEY `FK_filter_entry_COLLECTION_ID` (`COLLECTION_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `partner_site`;
CREATE TABLE `partner_site` (
  `ID` bigint(20) NOT NULL auto_increment,
  `REMOTEURL` varchar(255) default NULL,
  `USER` varchar(255) default NULL,
  `PASS` varchar(255) default NULL,
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `report_item`;
CREATE TABLE `report_item` (
  `ID` bigint(20) NOT NULL auto_increment,
  `VALUE` bigint(20) default NULL,
  `LOGTYPE` tinyint(1) default '0',
  `ATTRIBUTE` varchar(255) default NULL,
  `REPORT_ID` bigint(20) default NULL,
  PRIMARY KEY  (`ID`),
  KEY `idx_report_id` (`REPORT_ID`)
) ENGINE=MyISAM AUTO_INCREMENT=21 DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `report_summary`;
CREATE TABLE `report_summary` (
  `ID` bigint(20) NOT NULL auto_increment,
  `FIRSTLOGENTRY` bigint(20) default NULL,
  `LASTLOGENTRY` bigint(20) default NULL,
  `GENERATEDDATE` datetime default NULL,
  `REPORTNAME` varchar(255) default NULL,
  `ENDDATE` datetime default NULL,
  `STARTDATE` datetime default NULL,
  `COLLECTION_ID` bigint(20) default NULL,
  PRIMARY KEY  (`ID`),
  KEY `idx_summary_parent` (`COLLECTION_ID`)
) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;

CREATE TABLE `report_policy` (
  `ID` bigint(20) NOT NULL auto_increment,
  `EMAILLIST` text default NULL,
  `CRONSTRING` varchar(255) default NULL,
  `NAME` varchar(255) default NULL,
  `COLLECTION_ID` bigint(20) default NULL,
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `peer_collection`;
CREATE TABLE `peer_collection` (
  `ID` bigint(20) NOT NULL auto_increment,
  `PARENT_ID` bigint(20) NOT NULL,
  `SITE_ID` bigint(20) NOT NULL,
  `PEERID` bigint(20) NOT NULL,
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;