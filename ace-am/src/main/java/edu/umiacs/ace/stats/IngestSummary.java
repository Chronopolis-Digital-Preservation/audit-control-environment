package edu.umiacs.ace.stats;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * A summary of a collection
 *
 * Created by shake on 8/29/16.
 */
public final class IngestSummary {
    public final Timestamp date;
    public final String collection;
    public final String group;
    public final Long count;
    public final BigDecimal size;

    public IngestSummary(Timestamp date, String collection, String group, Long count, BigDecimal size) {
        this.date = date;
        this.collection = collection;
        this.group = group;
        this.count = count;
        this.size = size;
    }

    public Timestamp getDate() {
        return date;
    }

    public String getCollection() {
        return collection;
    }

    public String getGroup() {
        return group;
    }

    public Long getCount() {
        return count;
    }

    public BigDecimal getSize() {
        return size;
    }
}
