drop table if exists 'sequence';
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
) ENGINE=MyISAM AUTO_INCREMENT=15 DEFAULT CHARSET=latin1;
create index idx_token_digest on token(filedigest);