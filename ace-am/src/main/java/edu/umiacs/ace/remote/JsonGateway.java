/*
 * Copyright (c) 2007-2010, University of Maryland
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * Neither the name of the University of Maryland nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ACE Components were written in the ADAPT Project at the University of
 * Maryland Institute for Advanced Computer Study.
 */
// $Id$

package edu.umiacs.ace.remote;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.monitor.peers.PartnerSite;
import edu.umiacs.util.Strings;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to cache various ACE json elements according to some arbitrary policy.
 * 
 * @author toaster
 */
public class JsonGateway {

    private static JsonGateway gateway = null;
    private static final Logger LOG = Logger.getLogger(JsonGateway.class);
    private static long MAX_STATUS = 10 * 1000; // 10 seconds expiration for status
    private static long MAX_SUMMARY = 600 * 1000; // 5m expiration for summary
    private static long MAX_REPORT = 600 * 1000; // 5m expiration for reports
    private static String STATUS_SUFFIX = "/Status?json=1";
    private static String SUMMARY_SUFFIX = "/ViewSummary?json=1";
    private static String REPORT_SUFFIX = "/Report?json=1&count=100&collectionid=";
    private static String ITEM_SUFFIX = "/ListItem?json=1&collectionid=";
    private static String ITEMROOT_SUFFIX = "/ListItem?json=1&collectionid=";
    private static String DIGEST_SUFFIX = "/Summary?collectionid=";
    private static String GROUP_LIST_SUFFIX = "/rest/groups";
    private static String COLL_LIST_SUFFIX = "/rest/collections";
    private final ObjectMapper mapper;
    private Map<PartnerSite, JsonCache> cache = new HashMap<>();

    private JsonGateway() {
        mapper = new ObjectMapper();

        DeserializationConfig cfg = mapper.getDeserializationConfig();
        cfg.with(new SimpleDateFormat("EEE MMM dd hh:mm:ss zzz yyyy"));
    }

    public static final JsonGateway getGateway() {
        if ( gateway == null ) {
            gateway = new JsonGateway();
        }
        return gateway;
    }

    public InputStream getDigestList( PartnerSite site, long collection ) {
        try {

            URL u = new URL(site.getRemoteURL() + DIGEST_SUFFIX + collection);
            LOG.trace("Attempting to pull: " + u);

            return getURLInputStream(u, site.getCredentials());
        } catch ( IOException ioe ) {
            LOG.error("Error reading site " + site.getRemoteURL(), ioe);
            return null;
        }
    }

    public ParentChildBean getAceItem( PartnerSite site, long collectionid,
            String parent ) {
        synchronized ( mapper ) {
            JsonCache c = getCache(site);

            if ( Strings.isEmpty(parent) ) {
                if ( !c.itemRootMap.containsKey(collectionid) || (long) (System.currentTimeMillis()
                        - c.itemRootMapUpdate.get(collectionid)) > MAX_REPORT ) {
                    c.itemRootMap.remove(collectionid);
                    c.itemRootMapUpdate.remove(collectionid);

                    c.itemRootMap.put(collectionid, updateItemRootBean(site, collectionid));
                    c.itemRootMapUpdate.put(collectionid, System.currentTimeMillis());
                }

                return c.itemRootMap.get(collectionid);
            } else {
                if ( !c.itemMap.containsKey(parent) || (long) (System.currentTimeMillis()
                        - c.itemMapUpdate.get(parent)) > MAX_REPORT ) {
                    c.itemMap.remove(parent);
                    c.itemMapUpdate.remove(parent);

                    c.itemMap.put(parent, updateItemBean(site, collectionid, parent));
                    c.itemMapUpdate.put(parent, System.currentTimeMillis());
                }

                return c.itemMap.get(parent);
            }

        }
    }

    public ReportBean getReportBean( PartnerSite site, long collectionid ) {
        synchronized ( mapper ) {
            JsonCache c = getCache(site);

            if ( !c.reportMap.containsKey(collectionid) || (long) (System.currentTimeMillis()
                    - c.reportMapUpdate.get(collectionid)) > MAX_REPORT ) {
                c.reportMap.remove(collectionid);
                c.reportMapUpdate.remove(collectionid);
                c.reportMap.put(collectionid, updateReportBean(site, collectionid));
                c.reportMapUpdate.put(collectionid, System.currentTimeMillis());
            }

            return c.reportMap.get(collectionid);
        }
    }

    public SummaryBean getSummaryBean( PartnerSite site ) {
        synchronized ( mapper ) {
            JsonCache c = getCache(site);

            if ( c.summary == null || (long) (System.currentTimeMillis() - c.summaryUpdate)
                    > MAX_SUMMARY ) {
                c.summary = updateSummaryBean(site);
                c.summaryUpdate = System.currentTimeMillis();
            }

            return c.summary;
        }
    }

    public StatusBean getStatusBean( PartnerSite site ) {

        synchronized ( mapper ) {
            JsonCache c = getCache(site);

            if ( c.status == null || (long) (System.currentTimeMillis() - c.statusUpdate)
                    > MAX_STATUS ) {
                c.status = updateStatusBean(site);
                c.statusUpdate = System.currentTimeMillis();
            }

            return c.status;
        }

    }

    private JsonCache getCache( PartnerSite site ) {
        JsonCache c = cache.get(site);

        if ( c == null ) {
            c = new JsonCache();
            cache.put(site, c);

        }
        return c;
    }

    private ParentChildBean updateItemRootBean( PartnerSite site, long collectionid ) {
        try {

            URL u = new URL(site.getRemoteURL() + ITEMROOT_SUFFIX + collectionid);
            LOG.trace("Attempting to pull: " + u);

            return readJSONValue(u, site.getCredentials(), ParentChildBean.class);
        } catch ( IOException ioe ) {
            LOG.error("Error reading site " + site.getRemoteURL(), ioe);
            return null;
        }
    }

    private ParentChildBean updateItemBean( PartnerSite site, long collection, String parentpath ) {
        try {

            URL u = new URL(site.getRemoteURL() + ITEM_SUFFIX + collection + "&itempath="
                    + parentpath);
            LOG.trace("Attempting to pull: " + u);

            return readJSONValue(u, site.getCredentials(), ParentChildBean.class);
        } catch ( IOException ioe ) {
            LOG.error("Error reading site " + site.getRemoteURL(), ioe);
            return null;
        }
    }

    private ReportBean updateReportBean( PartnerSite site, long collectionid ) {
        try {

            URL u = new URL(site.getRemoteURL() + REPORT_SUFFIX + collectionid);
            LOG.trace("Attempting to pull: " + u);

            return readJSONValue(u, site.getCredentials(), ReportBean.class);
        } catch ( IOException ioe ) {
            LOG.error("Error reading site " + site.getRemoteURL(), ioe);
            return null;
        }
    }

    private StatusBean updateStatusBean( PartnerSite site ) {
        try {

            URL u = new URL(site.getRemoteURL() + STATUS_SUFFIX);
            LOG.trace("Attempting to pull: " + u);

            return readJSONValue(u, site.getCredentials(), StatusBean.class);
        } catch ( IOException ioe ) {
            LOG.error("Error reading site " + site.getRemoteURL(), ioe);
            return null;
        }
    }

    private SummaryBean updateSummaryBean( PartnerSite site ) {
        try {

            URL u = new URL(site.getRemoteURL() + SUMMARY_SUFFIX);
            LOG.trace("Attempting to pull: " + u);
//mapper.readValue(u, );
            return readJSONValue(u, site.getCredentials(), SummaryBean.class);
        } catch ( IOException ioe ) {
            LOG.error("Error reading site " + site.getRemoteURL(), ioe);
            return null;
        } finally {
            LOG.trace("Finished pulling url");
        }
    }

    public List<Collection> getCollectionListRest(PartnerSite site) {
        try {
            URL u = new URL(site.getRemoteURL() + COLL_LIST_SUFFIX);
            return readJSONValue(u, site.getCredentials(), List.class);
        } catch (IOException e) {
            LOG.error("Error reading site " + site.getRemoteURL(), e);
            return Collections.emptyList();
        }
    }

    private static class JsonCache {

        private StatusBean status = null;
        private long statusUpdate = 0;
        private SummaryBean summary = null;
        private long summaryUpdate = 0;
        private Map<Long, ReportBean> reportMap = new HashMap<>();
        private Map<Long, Long> reportMapUpdate = new HashMap<>();
        private Map<String, ParentChildBean> itemMap = new HashMap<>();
        private Map<String, Long> itemMapUpdate = new HashMap<>();
        private Map<Long, ParentChildBean> itemRootMap = new HashMap<>();
        private Map<Long, Long> itemRootMapUpdate = new HashMap<>();
    }

    private <T> T readJSONValue( URL u, String auth, Class<T> clazz ) throws IOException {
        return mapper.readValue(getURLInputStream(u, auth), clazz);
    }

    /**
     * Return the input stream using the authorization credentials provided
     *
     * Only support basic auth
     *
     * @param u - url to get the input stream of
     * @param auth - auth credentials, ex: user:password
     * @return
     * @throws IOException
     */
    private InputStream getURLInputStream(URL u, String auth) throws IOException {
        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(auth);
        provider.setCredentials(AuthScope.ANY, credentials);

        try {
            HttpClient client = HttpClients.custom().setDefaultCredentialsProvider(provider).build();
            HttpGet get = new HttpGet(u.toURI());
            HttpResponse execute = client.execute(get);
            return execute.getEntity().getContent();
        } catch (URISyntaxException e) {
            // should never happen
            return null;
        }
    }
}
