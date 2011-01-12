CREATE TABLE `filter_entry` (
  `ID` bigint(20) NOT NULL auto_increment,
  `REGEX` varchar(255) default NULL,
  `AFFECTEDITEM` int(11) default NULL,
  `COLLECTION_ID` bigint(20) NOT NULL,
  PRIMARY KEY  (`ID`),
  KEY `FK_filter_entry_COLLECTION_ID` (`COLLECTION_ID`)
) ENGINE=MyISAM AUTO_INCREMENT=15 DEFAULT CHARSET=latin1;
CREATE TABLE `partner_site` (
  `ID` bigint(20) NOT NULL auto_increment,
  `REMOTEURL` varchar(255) default NULL,
  `USER` varchar(255) default NULL,
  `PASS` varchar(255) default NULL,
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM AUTO_INCREMENT=15 DEFAULT CHARSET=latin1;
ALTER TABLE monitored_item ADD COLUMN `LASTVISITED` datetime default NULL;