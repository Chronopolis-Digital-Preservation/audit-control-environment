<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib tagdir="/WEB-INF/tags" prefix="h" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="/WEB-INF/tlds/monitor" prefix="d"%>

<!DOCTYPE html
    PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />

        <c:choose>
            <c:when test="${workingCollection != null && (workingCollection.fileAuditRunning || workingCollection.tokenAuditRunning) }">
                <meta http-equiv="refresh" content="10"/>
            </c:when>
            <c:otherwise>
                <meta http-equiv="refresh" content="600"/>
            </c:otherwise>
        </c:choose>


        <title>ACE Audit Manager</title>
        <script type="text/javascript" SRC="srbFunctions.js" ></script>
        <script type="text/javascript">
            var groupheaderrow = 'groupheaderrow';
            var grouptr = 'grouptr';
            var statusrow = 'statusrow';

            function search() {
                document.getElementById('action').value = 'search';

                document.getElementById("searchForm").submit();
            }

            function toggleVisibility(id,type) {
                var t = document.getElementById(id);
                var display = t.style.display;
                t.style.display = display === 'none' ? 'inline' : 'none';
            }
            function showGroup(id)
            {
                var divs = document.getElementsByTagName('tr');

                for (i = 0; i < divs.length; i++)
                {
                    if (divs[i].className.split(' ').includes(id))
                    {
                        divs[i].style.display = "";
                    }
                }
            }
            function browseGroup(group)
            {
                var action = document.getElementById('action').value;
            	if (action == 'search')
            	{
            	    showGroup(grouptr + group)
            	    return;
            	}
                document.getElementById('group-filter').value = group;

                document.getElementById("searchForm").submit();
                
            }
            function hideGroup(id)
            {
                var divs = document.getElementsByTagName('tr');

                for (i = 0; i < divs.length; i++)
                {
                    if (divs[i].className.split(' ').includes(id))
                    {
                        divs[i].style.display = "none";
                    }
                }
            }
            function closeColContainer() {
                document.getElementById('collectionid').value = '-1';
                document.getElementById('col-container').style.display='none';
            }
            function collapseGroups()
            {
                var action = document.getElementById('action').value;
                if (action === 'search')
                    return;
                
                var group = document.getElementById('group-filter').value.trim();
                var divs = document.getElementsByTagName('tr');

                for (i = 0; i < divs.length; i++)
                {
                	var iclass = divs[i].className;

                    if (iclass.indexOf(groupheaderrow) == 0) {
                        var igroup = iclass.substring(iclass.indexOf(groupheaderrow) + groupheaderrow.length);
                        if (group != igroup) {
                            document.getElementById('spexpand' + igroup).style.display = 'inline';
                            document.getElementById('sphide' + igroup).style.display = 'none';
                        } else {
                            document.getElementById('spexpand' + igroup).style.display = 'none';
                            document.getElementById('sphide' + igroup).style.display = 'inline';
                        }
                    }
                    else if (iclass.indexOf(statusrow) == 0 && iclass != statusrow && (group.length == 0 || !iclass.endsWith(grouptr + group)))
                    {
                        divs[i].style.display = "none";
                    }
                }
            }
        </script>
        <link rel="stylesheet" type="text/css" href="style.css" />
        <style type="text/css">
            .groupheader {
                background: #e8e8e8;
                padding-left: 25px
            }
            #inactiveaudit
            {
                color: red;
                font-weight: bold;
            }

            .statusrow:hover {
                background-color: #e8e8e8;
            }

            .statusrow {
                background-color: #FFFFFF;
            }

            .statuserrrow:hover {
                background-color: #e8e8e8;
            }

            .statuserrrow {
                background-color: #FFFFFF;
            }

            #linktable {
                width: 655px;
                margin-left: auto;
                margin-right: auto;
            }

            #threaddetailsTD:hover div {
                display: block;
            }

            #threaddetailsDIV {
                left: 100px;
                position: absolute;
                background-color: #FFFFFF;
                width: 600px;
                z-index: 300;
                display: none;
                font-size: x-small;
                border: solid 1px #000000;

            }
            #threaddetailsDIV table {
                margin: 5px;

                text-align: left;

            }
            #threaddetailsDIV table tr td {
                background-color: #f1f1f1;
                padding-right: 5px;
                text-align: left;

            }

            #details {
                z-index: 100;
                position: relative;
            }
            #detailstbl1 tr td
            {
                vertical-align: top;
                padding-left: 10px;
                margin-top: 10px;
            }
            #detailstbl1 tr
            {
                background-color: #FFFFFF;
            }
            #detailstbl1 tr:hover
            {
                background-color: #e8e8e8;
            }

            .lbl-group
            {
            	color: #444444;
            	width: 100%;
            }
            .lbl-indicator
            {
            	padding-left: 5px;
            	padding-right: 20px;
            	vertical-align: top;
            }
            .title-error
            {
            	text-align: left;
            	font-weight: bold;
            	size: 20px;
            	color: brown;
            	margin: 16px 0px -0px 24px;
            }

            #statustable-errors
            {
            	font-size: 16px;
            	border: 1px solid brown;
            	margin-top: 6px;
            	margin-bottom: 25px;
            	width: 96%;
            }  
            .audit-success
            {
            	font-size: 14px;
            	color: darkgreen;
            }
            .audit-error, .audit-interrupted
            {
            	font-size: 14px;
            	color: red;
            }
            .audit-never
            {
            	font-size: 14px;
            	color: #444;
            }
            .auditing, .queued, .idle
            {
            	font-size: 14px;
            	color: #336699;
            }
        </style>
    </head>

    <body onload="javascript: collapseGroups();">
        <jsp:include page="header.jsp" />
        <script type="text/javascript">
        	if (document.getElementById('status') !== null) {
        		document.getElementById('status').style.backgroundColor = '#ccccff';
    		}
		</script>
        <c:if test="${workingCollection != null}">
            <div id="details">
                <jsp:include page="statusdetails.jsp"/>
            </div>

        </c:if>

        <div id="searchtable" align="center">
            <form method="GET" role="form" id="searchForm" onsubmit="search()">
                <div class="input">
                    <span class="input-group-addon">Group</span>
                    <input type="text" class="form-input" id="group-filter"
                           name="status_group" placeholder="Search Group" value="${status_group}"/>
                </div>
                <div class="input">
                    <span class="input-group-addon">Collection</span>
                    <input type="text" class="form-input" id="coll-filter"
                           name="status_collection" placeholder="Search Collection" value="${status_collection}"/>
                </div>
                <div class="input">
                    <span class="input-group-addon">State</span>
                    <select name="status_state" id="state-filter" class="form-select">
                        <option value="">Select a Collection State</option>
                        <c:forEach var="s" items="${states}">
                            <c:choose>
                                <c:when test="${s.state eq status_state}">
                                    <option value="${s.state}" selected="selected">${s.name()}</option>
                                </c:when>
                                <c:otherwise>
                                    <option value="${s.state}">${s.name()}</option>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>
                    </select>
                </div>

                <div class="input" align="left">
                    <input type="hidden" id="collectionid" name="collectionid" value="${collectionid}"/>
                    <input type="hidden" id="action" name="action" value="${action}"/>
                    <button type="submit" class="btn is-secondary" value="Submit"><span>Submit</span></button>
                </div>
            </form>
        </div>

		<c:if test="${errorCollections.size() > 0}">
	        <div style="width: 100%" align="center">
	          <div class="title-error">Error / Interrupted collections</div>
	          <table id="statustable-errors">
	            <thead>
	                <td colspan="2"><span style="padding-left:20px;">Group Name</span></td>
	                <td>Type</td>
	                <td nowrap>Total Files*</td>
	                <td nowrap>Disk Size</td>
	                <td nowrap>Last Audit</td>
	                <td nowrap>Next Audit</td>
	                <td nowrap>Audit Period</td>
	            </thead>
	            <c:forEach var="item" items="${errorCollections}">
	
	                <c:if test="${currErrGroup != item.collection.group && item.collection.group != null}">
	                	<c:set var="errgroup" value="${item.collection.group}"/>
	                    <tr>
	                        <td class="groupheader" style="padding-left:4px;" colspan="3" onclick="toggleVisibility('errspexpand${errgroup}','inline'); toggleVisibility('errsphide${errgroup}','inline');">
	                        	<div onclick="showGroup('grouptrerr${errgroup}')" id="errspexpand${errgroup}" style="display:none;cursor: pointer;" >
	                        		<span class="lbl-group">[+]</span><span style="margin-left:6px;">${errgroup}</span>
	                        	</div>
	                        	<div onclick="hideGroup('grouptrerr${errgroup}')" id="errsphide${errgroup}" style="display:inline;cursor: pointer;" >
	                        		<span class="lbl-group">[-]</span><span style="margin-left:6px;">${errgroup}</span>
	                        	</div>
	                        </td>
	                        <td class="groupheader" colspan="5" ></td>
	                    </tr>
	                </c:if>
	
	                <tr class="statuserrrow grouptrerr${item.collection.group}" >
	                    <td width="6%" nowrap>
	
	                        <c:choose>
	                            <c:when test="${item.fileAuditRunning || item.tokenAuditRunning}">
	                                <span class="auditing" title="Audit in progress">Auditing</span>
	                            </c:when>
	                            <c:when test="${item.queued}">
	                                <span class="queued" title="Audit is queued">Queued</span>
	                            </c:when>
	                            <c:otherwise>
	                                <span class="idle" title="No audit in progress">Idle</span>
	                            </c:otherwise>
	                        </c:choose>
	                        <c:choose>
	                            <c:when test="${'A'.bytes[0] == item.collection.state }">
	                                <span class="audit-success" title="Last audit successful">Passed</span>
	                            </c:when>
	                            <c:when test="${'E'.bytes[0] == item.collection.state }">
	                                <span class="audit-error" title="Collection contains errors">Error</span>
	                            </c:when>
	                            <c:when test="${'I'.bytes[0] == item.collection.state }">
	                                <span class="audit-interrupted" title="Last audit was interrupted">Interrupted</span>
	                            </c:when>
	                            <c:otherwise>
	                                <span class="audit-never" title="Complete audit has not occurred">N/A</span>
	                            </c:otherwise>
	                        </c:choose>
	                    </td>
		                <td width="36%">
		                	<a href="Status?collectionid=${item.collection.id}">${item.collection.name}</a>
		                </td>
	                    <td>${item.collection.storage}</td>
	                    <td><h:DefaultValue test="${item.totalFiles > -1}" success="${item.totalFiles}" failure="Unknown" /></td>
	                    <td>
	                        <c:choose>
	                            <c:when test="${item.totalSize > 0}"><d:FileSize value="${item.totalSize}" /></c:when>
	                            <c:otherwise>0 B</c:otherwise>
	                        </c:choose>
	                    </td>
	                    <td>
	                        <fmt:formatDate pattern="MMM dd yyyy" value="${item.collection.lastSync}"/>
	                    </td>
	                    <td>
	                        <c:choose>
	                            <c:when test="${item.fileAuditRunning || item.tokenAuditRunning}">
	                                In Progress
	                            </c:when>
	                            <c:when test="${item.queued}">
	                                Queued
	                            </c:when>
	                            <c:when test="${item.collection.state eq 'R'.charAt(0)}">
	                                Excluded
	                            </c:when>
	                            <c:when test="${item.collection.lastSync == null || item.collection.settings['audit.period'] < 1 || pause.paused}">
	                                Unknown
	                            </c:when>
	                            <c:when test="${today.time > (item.collection.lastSync.time + item.collection.settings['audit.period'] * 1000 * 60 * 60 * 24)}">
	                                <span style="color: red; font-weight: bold">
	                                    <d:DateAdd date="${item.collection.lastSync}" format="MMM dd yyyy" period="${item.collection.settings['audit.period']}"/>
	                                </span>
	                            </c:when>
	                            <c:otherwise>
	                                <d:DateAdd date="${item.collection.lastSync}" format="MMM dd yyyy" period="${item.collection.settings['audit.period']}"/>
	                            </c:otherwise>
	                        </c:choose>
	                    </td>
	                    <td nowrap>${item.collection.settings['audit.period']} days</td>
	                </tr>
	                <c:set var="currErrGroup" value="${item.collection.group}" />
	            </c:forEach>
	          </table>
	        </div>
        </c:if>

        <div style="width: 100%" align="center">
          <table id="statustable">
            <thead>
                <td colspan="2"><span style="padding-left:20px;">Group Name</span></td>
                <td>Type</td>
                <td nowrap>Total Files*</td>
                <td nowrap>Disk Size</td>
                <td nowrap>Last Audit</td>
                <td nowrap>Next Audit</td>
                <td nowrap>Audit Period</td>
            </thead>
            <c:set var="count" value="0" />
            <jsp:useBean id="today" class="java.util.Date"/>

            <c:forEach var="item" items="${noGroupCollections}">	
                <tr class="statusrow" >
                    <td width="6%" nowrap>

                        <c:choose>
                            <c:when test="${item.fileAuditRunning || item.tokenAuditRunning}">
                                <span class="auditing" title="Audit in progress">Auditing</span>
                            </c:when>
                            <c:when test="${item.queued}">
                                <span class="queued" title="Audit is queued">Queued</span>
                            </c:when>
                            <c:otherwise>
                                <span class="idle" title="No audit in progress">Idle</span>
                            </c:otherwise>
                        </c:choose>
                        <c:choose>
                            <c:when test="${'A'.bytes[0] == item.collection.state }">
                                <span class="audit-success" title="Last audit successful">Passed</span>
                            </c:when>
                            <c:when test="${'E'.bytes[0] == item.collection.state }">
                                <span class="audit-error" title="Collection contains errors">Error</span>
                            </c:when>
                            <c:when test="${'I'.bytes[0] == item.collection.state }">
                                <span class="audit-interrupted" title="Last audit was interrupted">Interrupted</span>
                            </c:when>
                            <c:otherwise>
                                <span class="audit-never" title="Complete audit has not occurred">N/A</span>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td width="36%">
                        <a href="Status?collectionid=${item.collection.id}&page=${page.page}&count=${page.count}&action=${action}">${item.collection.name}</a>
                    </td>
                    <td>${item.collection.storage}</td>
                    <td><h:DefaultValue test="${item.totalFiles > -1}" success="${item.totalFiles}" failure="Unknown" /></td>
                    <td>
                        <c:choose>
                            <c:when test="${item.totalSize > 0}"><d:FileSize value="${item.totalSize}" /></c:when>
                            <c:otherwise>0 B</c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        <fmt:formatDate pattern="MMM dd yyyy" value="${item.collection.lastSync}"/>
                    </td>
                    <td>
                        <c:choose>
                            <c:when test="${item.fileAuditRunning || item.tokenAuditRunning}">
                                In Progress
                            </c:when>
                            <c:when test="${item.queued}">
                                Queued
                            </c:when>
                            <c:when test="${item.collection.state eq 'R'.charAt(0)}">
                                Excluded
                            </c:when>
                            <c:when test="${item.collection.lastSync == null || item.collection.settings['audit.period'] < 1 || pause.paused}">
                                Unknown
                            </c:when>
                            <c:when test="${today.time > (item.collection.lastSync.time + item.collection.settings['audit.period'] * 1000 * 60 * 60 * 24)}">
                                <span style="color: red; font-weight: bold">
                                    <d:DateAdd date="${item.collection.lastSync}" format="MMM dd yyyy" period="${item.collection.settings['audit.period']}"/>
                                </span>
                            </c:when>
                            <c:otherwise>
                                <d:DateAdd date="${item.collection.lastSync}" format="MMM dd yyyy" period="${item.collection.settings['audit.period']}"/>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td nowrap>${item.collection.settings['audit.period']} days</td>
                </tr>

                <c:set var="count" value="${count + 1}" />
            </c:forEach>

            <c:forEach var="group" items="${colGroups}">
                <c:set var="group_count" value="${groups[group].count}"/>
                <c:set var="size" value="${groups[group].size}"/>
                <c:set var="counttotal" value="0" />
                <c:set var="sizetotal" value="0" />         
                <tr class="groupheaderrow${group}">
                    <td class="groupheader" colspan="3" onclick="toggleVisibility('spexpand${group}','inline'); toggleVisibility('sphide${group}','inline');">
                    	<div onclick="browseGroup('${group}')" id="spexpand${group}" style="display:none;cursor: pointer;" >
                    		<span class="lbl-group">[+]</span><span style="margin-left:6px;">${group}</span>
                    	</div>
                    	<div onclick="hideGroup('grouptr${group}')" id="sphide${group}" style="display:inline;cursor: pointer;" >
                    		<span class="lbl-group">[-]</span><span style="margin-left:6px;">${group}</span>
                    	</div>
                    </td>
                    <td class="groupheader" id="group${group}">
                            ${group_count}
                    </td>
                    <td class="groupheader">
                        <c:choose>
                            <c:when test="${size > 0}"><d:FileSize value="${size}" /></c:when>
                            <c:otherwise>0 B</c:otherwise>
                        </c:choose>
                    </td>
                    <td class="groupheader" colspan="3"></td>
                </tr>

                <c:forEach var="item" items="${collections}">        	
	                <c:if test="${item.collection.group != null && group == item.collection.group}">
	
		                <tr class="statusrow grouptr${item.collection.group}" >
		                    <td width="6%" nowrap>
		
		                        <c:choose>
		                            <c:when test="${item.fileAuditRunning || item.tokenAuditRunning}">
		                                <span class="auditing" title="Audit in progress">Auditing</span>
		                            </c:when>
		                            <c:when test="${item.queued}">
		                                <span class="queued" title="Audit is queued">Queued</span>
		                            </c:when>
		                            <c:otherwise>
		                                <span class="idle" title="No audit in progress">Idle</span>
		                            </c:otherwise>
		                        </c:choose>
		                        <c:choose>
		                            <c:when test="${'A'.bytes[0] == item.collection.state }">
		                                <span class="audit-success" title="Last audit successful">Passed</span>
		                            </c:when>
		                            <c:when test="${'E'.bytes[0] == item.collection.state }">
		                                <span class="audit-error" title="Collection contains errors">Error</span>
		                            </c:when>
		                            <c:when test="${'I'.bytes[0] == item.collection.state }">
		                                <span class="audit-interrupted" title="Last audit was interrupted">Interrupted</span>
		                            </c:when>
		                            <c:otherwise>
		                                <span class="audit-never" title="Complete audit has not occurred">N/A</span>
		                            </c:otherwise>
		                        </c:choose>
		                    </td>
		                    <td width="36%">
		                        <a href="Status?collectionid=${item.collection.id}&status_group=${group}&page=${page.page}&count=${page.count}&action=${action}">${item.collection.name}</a>
		                    </td>
		                    <td>${item.collection.storage}</td>
		                    <td><h:DefaultValue test="${item.totalFiles > -1}" success="${item.totalFiles}" failure="Unknown" /></td>
		                    <td>
		                        <c:choose>
		                            <c:when test="${item.totalSize > 0}"><d:FileSize value="${item.totalSize}" /></c:when>
		                            <c:otherwise>0 B</c:otherwise>
		                        </c:choose>
		                    </td>
		                    <td>
		                        <fmt:formatDate pattern="MMM dd yyyy" value="${item.collection.lastSync}"/>
		                    </td>
		                    <td>
		                        <c:choose>
		                            <c:when test="${item.fileAuditRunning || item.tokenAuditRunning}">
		                                In Progress
		                            </c:when>
		                            <c:when test="${item.queued}">
		                                Queued
		                            </c:when>
		                            <c:when test="${item.collection.state eq 'R'.charAt(0)}">
		                                Excluded
		                            </c:when>
		                            <c:when test="${item.collection.lastSync == null || item.collection.settings['audit.period'] < 1 || pause.paused}">
		                                Unknown
		                            </c:when>
		                            <c:when test="${today.time > (item.collection.lastSync.time + item.collection.settings['audit.period'] * 1000 * 60 * 60 * 24)}">
		                                <span style="color: red; font-weight: bold">
		                                    <d:DateAdd date="${item.collection.lastSync}" format="MMM dd yyyy" period="${item.collection.settings['audit.period']}"/>
		                                </span>
		                            </c:when>
		                            <c:otherwise>
		                                <d:DateAdd date="${item.collection.lastSync}" format="MMM dd yyyy" period="${item.collection.settings['audit.period']}"/>
		                            </c:otherwise>
		                        </c:choose>
		                    </td>
		                    <td nowrap>${item.collection.settings['audit.period']} days</td>
		                </tr>

		                <c:set var="count" value="${count + 1}" />
	                </c:if>
                </c:forEach>
            </c:forEach>

            <tr><td colspan="5"><br/><d:Auth role="Collection Modify" showUnauthenticated="true"><a href="ManageCollection">Add Collection</a></d:Auth> &nbsp;&nbsp;&nbsp&nbsp;&nbsp;
                    <d:Auth role="Audit">
                        <c:choose>
                            <c:when test="${pause.paused}"><a href="Pause?pause=0">Enable Automated Auditing</a></c:when>
                            <c:otherwise><a href="Pause?pause=1">Pause Automated Auditing</a></c:otherwise>
                        </c:choose>
                    </d:Auth>
                </td></tr>
            <tr><td colspan="7">* - Total files and status not updated until after first sync.</td></tr>
            <tr><td colspan="7"><c:choose>
                        <c:when test="${pause.paused}"><span id="inactiveaudit">Automated auditing is currently paused.</span> </c:when>
                        <c:otherwise>Automated auditing active.</c:otherwise>
                    </c:choose></td></tr>

          </table>
        </div>

       <table id="linktable">
            <tr>
                <td align="left">
                    <a href="${page.first}">|&lt;</a>&nbsp;&nbsp;&nbsp;
                    <a href="${page.previous}">&lt;&lt;</a>
                </td>
                <td align="center">
                    Show per page:
                    <a href="${page.getCount(100)}">100</a>
                    <a href="${page.getCount(500)}">500</a>
                    <a href="${page.getCount(1000)}">1000</a>
                </td>
                <td align="right">
                    <a href="${page.next}">&gt;&gt;</a>&nbsp;&nbsp;&nbsp;
                    <a href="${page.end}">&gt;|</a>
                </td>
            </tr>
        </table>

        <jsp:include page="footer.jsp" />
    </body>
</html>
