CREATE TABLE `acetoken` (
  `ID` bigint(20) NOT NULL auto_increment,
  `CREATEDATE` datetime default NULL,
  `VALID` tinyint(1) default '0',
  `LASTVALIDATED` datetime default NULL,
  `PROOFTEXT` text default NULL,
  `IMSSERVICE` varchar(64) default NULL,
  `PROOFALGORITHM` varchar(32) default NULL,
  `ROUND` bigint(20) default NULL,
  `PARENTCOLLECTION_ID` bigint(20) NOT NULL,
  PRIMARY KEY  (`ID`),
  KEY `FK_acetoken_PARENTCOLLECTION_ID` (`PARENTCOLLECTION_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 MAX_ROWS=2431504384;
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

