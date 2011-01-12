START TRANSACTION;
CREATE TABLE `report_item` (
  `ID` bigint(20) NOT NULL auto_increment,
  `VALUE` bigint(20) default NULL,
  `LOGTYPE` tinyint(1) default '0',
  `ATTRIBUTE` varchar(255) default NULL,
  `REPORT_ID` bigint(20) default NULL,
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

CREATE TABLE `report_summary` (
  `ID` bigint(20) NOT NULL auto_increment,
  `FIRSTLOGENTRY` bigint(20) default NULL,
  `LASTLOGENTRY` bigint(20) default NULL,
  `GENERATEDDATE` datetime default NULL,
  `REPORTNAME` varchar(255) default NULL,
  `ENDDATE` datetime default NULL,
  `STARTDATE` datetime default NULL,
  `COLLECTION_ID` bigint(20) default NULL,
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

CREATE TABLE `report_policy` (
  `ID` bigint(20) NOT NULL auto_increment,
  `EMAILLIST` text default NULL,
  `CRONSTRING` varchar(255) default NULL,
  `NAME` varchar(255) default NULL,
  `COLLECTION_ID` bigint(20) default NULL,
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

alter table collection add column COLGROUP varchar(255) default NULL;
alter table collection add column EMAILLIST text default NULL;
alter table monitored_item add column FILEDIGEST varchar(255) default NULL;
alter table token add column PARENTCOLLECTION_ID bigint(20) default NULL;
update monitored_item, token set monitored_item.FILEDIGEST = token.FILEDIGEST, token.PARENTCOLLECTION_ID = monitored_item.PARENTCOLLECTION_ID where monitored_item.TOKEN_ID = token.ID;
alter table token drop column FILEDIGEST;
create index idx_monitored_item_digest on monitored_item(filedigest);
create index FK_token_PARENTCOLLECTION_ID on token(PARENTCOLLECTION_ID);
alter table token max_rows = 200000000000;
alter table logevent max_rows = 200000000000;
alter table monitored_item max_rows = 200000000000;
COMMIT;