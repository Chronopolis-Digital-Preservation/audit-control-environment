alter table collection add column `DIGESTALGORITHM` varchar(20) default 'SHA-256';
alter table collection modify COLGROUP varchar(255) default null;
alter table monitored_item add column `SIZE` bigint(20) default 0;

CREATE TABLE `peer_collection` (
  `ID` bigint(20) NOT NULL auto_increment,
  `PARENT_ID` bigint(20) NOT NULL,
  `SITE_ID` bigint(20) NOT NULL,
  `PEERID` bigint(20) NOT NULL,
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;