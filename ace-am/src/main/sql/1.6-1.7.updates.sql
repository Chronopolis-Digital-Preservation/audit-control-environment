CREATE TABLE `settings` (
  `ID` bigint(20) NOT NULL auto_increment,
  `ATTR` varchar(255) default NULL,
  `VALUE` varchar(255) default NULL,
  `COLLECTION_ID` bigint(20) NOT NULL,
  PRIMARY KEY  (`ID`),
  UNIQUE idx_coll_setting (`COLLECTION_ID`,`ATTR`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

CREATE TABLE `system_settings` (
  `ID` bigint(20) NOT NULL auto_increment,
  `ATTR` varchar(255) default NULL,
  `VALUE` varchar(255) default NULL,
  `CUSTOM` BOOLEAN default FALSE,
  PRIMARY KEY  (`ID`),
  UNIQUE (`ATTR`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

ALTER TABLE monitored_item CHARACTER SET utf8;
ALTER TABLE monitored_item MODIFY `PARENTPATH` varchar(512) CHARACTER SET utf8;
ALTER TABLE monitored_item MODIFY `PATH` varchar(512) CHARACTER SET utf8;
ALTER TABLE monitored_item MODIFY `FILEDIGEST` varchar(255) CHARACTER SET utf8;
ALTER TABLE monitored_item MODIFY `STATE` char(1) CHARACTER SET utf8;
ALTER TABLE logevent CHARACTER SET utf8;
ALTER TABLE logevent MODIFY `DESCRIPTION` text CHARACTER SET utf8;
ALTER TABLE logevent MODIFY `PATH` text CHARACTER SET utf8;
ALTER TABLE collection CHARACTER SET utf8;
ALTER TABLE collection MODIFY `DIRECTORY` varchar(255) CHARACTER SET utf8;
ALTER TABLE collection MODIFY `NAME` varchar(255) CHARACTER SET utf8;
ALTER TABLE collection MODIFY `COLGROUP` varchar(255) CHARACTER SET utf8;
