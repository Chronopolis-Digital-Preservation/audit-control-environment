CREATE TABLE `settings` (
  `ID` bigint(20) NOT NULL auto_increment,
  `ATTR` varchar(255) default NULL,
  `VALUE` varchar(255) default NULL,
  `COLLECTION_ID` bigint(20) NOT NULL,
  PRIMARY KEY  (`ID`),
  UNIQUE idx_coll_setting (`COLLECTION_ID`,`ATTR`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;


ALTER TABLE monitored_item CHARACTER SET utf8 COLLATE  utf8_general_ci;
ALTER TABLE monitored_tem MODIFY `PARENTPATH` varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE monitored_tem MODIFY `PATH` varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE monitored_tem MODIFY `FILEDIGEST` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE monitored_tem MODIFY `STATE` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE logevent CHARACTER SET utf8 COLLATE  utf8_general_ci;
ALTER TABLE logevent MODIFY `DESCRIPTION` text CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE logevent MODIFY `PATH` text CHARACTER SET utf8 COLLATE utf8_general_ci;