create index idx_summary_parent on report_summary(collection_id);
create index idx_report_id on report_item(report_id);
delete from report_summary where collection_id not in (select id from collection);
delete from report_item where report_id not in (select id from report_summary);
