package edu.umiacs.ace.monitor.support;

import edu.umiacs.ace.monitor.access.CollectionCountContext;
import org.apache.log4j.Logger;

/**
 *
 * Created by shake on 1/28/16.
 */
public class PageBean {

    private static Logger LOG = Logger.getLogger(PageBean.class);

    // current page
    int page;

    // page number
    int count;

    // item offset (for db queries)
    int offset;

    int previous;
    int next;
    int end;

    StringBuilder url;
    private boolean firstParam;

    public PageBean(int page, int count, String root) {
        this.firstParam = true;
        this.page = page;
        this.count = count;
        this.offset = page*count;
        if (page == 0) {
            this.previous = 0;
        } else {
            this.previous = page - 1;
        }

        setPages(CollectionCountContext.getTotalCollections());

        this.url = new StringBuilder(root);
        LOG.info("Next page is " + next);
    }

    private void setPages(int totalCollections) {
        double endPrecise = totalCollections / (double) count;
        double endRounded = Math.floor(endPrecise);

        // If divided evenly, the last page will have nothing on it
        // Else keep it to show the remainder
        if (endRounded >= endPrecise) {
            this.end =(int) endRounded - 1;
        } else {
            this.end = (int) endRounded;
        }

        if (page == end) {
            this.next = end;
        } else {
            this.next = page + 1;
        }
    }

    public void setUrl(StringBuilder url) {
        this.url = url;
    }

    public void addParam(String param, String value) {
        if (firstParam) {
            url.append("?");
            firstParam = false;
        } else {
            url.append("&");
        }

        url.append(param).append("=").append(value);
    }

    public int getPage() {
        return page;
    }

    public int getCount() {
        return count;
    }

    public int getOffset() {
        return offset;
    }

    public String getFirst() {
        return getUrl(0, count);
    }

    public String getPrevious() {
        return getUrl(previous, count);
    }

    public String getNext() {
        return getUrl(next, count);
    }

    public String getEnd() {
        return getUrl(end, count);
    }

    public String getCount(int count) {
        return getUrl(0, count);
    }

    private String getUrl(int page, int count) {
        StringBuilder copy = new StringBuilder(url);
        if (firstParam) {
            copy.append("?");
        } else {
            copy.append("&");
        }
        copy.append("page=").append(page);
        copy.append("&count=").append(count);
        return copy.toString();
    }



    public void update(long totalResults) {
        setPages((int) totalResults);
    }
}
