package edu.umiacs.ace.monitor.access;

import java.math.BigDecimal;

/**
 *
 * Created by shake on 4/4/17.
 */
public final class GroupSummary {

    private final String group;
    private final BigDecimal size;
    private final BigDecimal count;

    public GroupSummary(String group, BigDecimal size, BigDecimal count) {
        this.group = group;
        this.size = size;
        this.count = count;
    }

    public String getGroup() {
        return group;
    }

    public BigDecimal getSize() {
        return size;
    }

    public BigDecimal getCount() {
        return count;
    }
}
