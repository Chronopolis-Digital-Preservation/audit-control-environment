CREATE TABLE `settings` (
  `ID` bigint(20) NOT NULL auto_increment,
  `ATTR` varchar(255) default NULL,
  `VALUE` varchar(255) default NULL,
  `COLLECTION_ID` bigint(20) NOT NULL,
  PRIMARY KEY  (`ID`),
  UNIQUE idx_coll_setting (`COLLECTION_ID`,`ATTR`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;