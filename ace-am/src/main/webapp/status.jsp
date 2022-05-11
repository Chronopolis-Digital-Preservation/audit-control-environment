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


        <title>${pageHeader}</title>
        <script type="text/javascript" SRC="srbFunctions.js" ></script>
        <script type="text/javascript">
            var groupheaderrow = 'groupheaderrow';
            var grouptr = 'grouptr';
            var statusrow = 'statusrow';
            var nogroup = 'nogroup';

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
                document.getElementById("searchForm").submit();
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

                const urlParams = new URLSearchParams(window.location.search);
                const collectionId = urlParams.get('collectionid')
                if (collectionId == null || collectionId.length == 0 || Number(collectionId) < 0) {
                    var groupToggle = document.getElementById(grouptr + group);
                    if (groupToggle != null) {
                        groupToggle.scrollIntoView();
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
                background-color: #F2FBFF;
            }

            .groupexpanded:hover {
                background-color: #FFFFFF !important;
            } 

            .statusrow {
                background-color: #FFFFFF;
            }

            .statuserrrow:hover {
                background-color: #F2FBFF;
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
            .auditing, .queued, .idle
            {
            	font-size: 14px;
            	color: #336699;
            }
            #statustable thead td, #statustable-errors thead td
            {
            	padding-top: 6px !important;
            	padding-bottom: 6px !important;
            }
			#statustable td, #statustable-errors td
            {
            	padding-top: 4px !important;
            	padding-bottom: 4px !important;
            }
            tr.statusrow td, tr.statuserrrow td
            {
                border-bottom: 1px solid #dee2e6;
            }
            #statustable thead, #statustable-errors thead
            {
            	background-color: #e0e0e0;
            	color: #444;
            	font-weight: bold;
            	font-size: 15px;
            }
            #statustable-group thead, #statustable-errors-group thead
            {
            	font-size: 16px;
            	margin-top: 6px;
            	margin-bottom: 25px;
            	width: 100%;
            	background-color: #F2FBFF;
            	font-weight: normal;
            }
            .row-strip-highlighted
            {
                background-color: #F5f5f5;
            }
            .row-strip-normal
            {
                background-color: #FFF;
            }
        </style>
    </head>

    <body onload="javascript: collapseGroups();">
        <jsp:include page="header.jsp" />
        <h1 class="page_header">${pageHeader}</h1>
 
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

        <!-- Start error collections -->
		<c:if test="${errorCollections.size() > 0}">
	        <div style="width: 100%" align="center">
	          <div class="title-error">Error / Interrupted collections</div>
	          <table id="statustable-errors">
	            <thead>
	                <td nowrap><span style="padding-left:2px;">Group Name</span></td>
	                <td nowrap>Total Files*</td>
	                <td nowrap>Total Size</td>
	            </thead>

	            <c:forEach var="item" items="${errorCollections}" varStatus="loop">
	                <c:if test="${currErrGroup != item.collection.group || loop.index == 0}">
	                	<c:set var="errgroup" value="${item.collection.group}"/>
	                	<c:set var="stripclasserrors">
	                	    <c:choose>
	                	        <c:when test="${stripclasserrors.index % 2 == 0}">row-strip-highlighted</c:when>
	                	        <c:otherwise>row-strip-highnormal</c:otherwise>
	                	    </c:choose>
	                	</c:set>
	                	
	                	<!-- close the collection group table -->
	                	<c:if test="${loop.index > 0}">
	                        </table>
	                        </td>
	                        </tr>
	                	</c:if>

	                    <tr>
	                        <td class="groupheader" style="padding-left:2px;" onclick="toggleVisibility('errspexpand${errgroup}','inline'); toggleVisibility('errsphide${errgroup}','inline');">
	                        	<div onclick="showGroup('grouptrerr${errgroup}')" id="errspexpand${errgroup}" style="display:none;cursor: pointer;" >
	                        		<span class="lbl-group">[+]</span><span style="margin-left:6px;">${errgroup}</span>
	                        	</div>
	                        	<div onclick="hideGroup('grouptrerr${errgroup}')" id="errsphide${errgroup}" style="display:inline;cursor: pointer;" >
	                        		<span class="lbl-group">[-]</span><span style="margin-left:6px;">${errgroup}</span>
	                        	</div>
	                        </td>
	                        <td class="groupheader"></td>
	                        <td class="groupheader"></td>
	                    </tr>

	                    <tr class="grouptrerr${errgroup}">
	                        <td colspan="3" style="border-bottom: none;">
	                          <div  style="margin: 8px 4px 8px 16px;">
	                            <table id="statustable-errors-group" border="0" cellpadding="0" cellspacing="0">
	                              <thead>
	                                <td width="4%" nowrap>Audit</td>
	                                <td width="6%" nowrap>Status</td>
	                                <td width="38%" nowrap>Collection Name</td>
	                                <td width="6%">Type</td>
	                                <td width="6%" nowrap>Files*</td>
	                                <td width="8%" nowrap>Size</td>
	                                <td width="12%" nowrap>Last Audit</td>
	                                <td width="12%" nowrap>Next Audit</td>
	                                <td width="8%" nowrap>Audit Period</td>
	                              </thead>
	                        </td>
	                    </tr>
	                </c:if>

	                <tr class="statuserrrow ${stripclasserrors} grouptrerr${item.collection.group}">
	                    <td nowrap>
	
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
	                    </td>
	                    <td>
	                        <c:choose>
	                            <c:when test="${'A'.bytes[0] == item.collection.state }">
	                                <img src="images/file-ok.jpg" title="Last audit successful" alt="Last audit successful"/>
	                            </c:when>
	                            <c:when test="${'E'.bytes[0] == item.collection.state }">
	                                <img src="images/error.jpg" title="Collection contains errors" alt="Collection contains errors"/>
	                            </c:when>
	                            <c:when test="${'I'.bytes[0] == item.collection.state }">
	                                <img src="images/warning.jpg" title="Last audit was interrupted" alt="Last audit was interrupted"/>
	                            </c:when>
	                            <c:otherwise>
	                                <img src="images/file-bad.jpg" title="Complete audit has not occurred" alt="Complete audit has not occurred"/>
	                            </c:otherwise>
	                        </c:choose>
	                    </td>
		                <td>
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
	                <c:set var="currErrGroup" value="${item.collection.group}" />
	            </c:forEach>
	            </table>
	            </div>
	            </td>
	            </tr>
	          </table>
	        </div>
        </c:if>
        <!-- End error collections -->

        <div style="width: 100%" align="center">
          <table id="statustable">
            <thead>
                <td nowrap><span style="padding-left:2px;">Group Name</span></td>
                <td nowrap>Total Files*</td>
                <td nowrap>Total Size</td>
            </thead>
            <c:set var="count" value="0" />
            <jsp:useBean id="today" class="java.util.Date"/>

            <c:forEach var="group" items="${colGroups}">
                <c:set var="group_count" value="${groups[group].count}"/>
                <c:set var="size" value="${groups[group].size}"/>
                <c:set var="counttotal" value="0" />
                <c:set var="sizetotal" value="0" />         
                <tr class="groupheaderrow${group}">
                    <td class="groupheader" onclick="toggleVisibility('spexpand${group}','inline'); toggleVisibility('sphide${group}','inline');">
                      <div id="grouptr${group}">
                    	<div onclick="browseGroup('${group}')" id="spexpand${group}" style="display:none;cursor: pointer;" >
                    		<span class="lbl-group">[+]</span><span style="margin-left:6px;">${group}</span>
                    	</div>
                    	<div onclick="hideGroup('grouptr${group}')" id="sphide${group}" style="display:inline;cursor: pointer;" >
                    		<span class="lbl-group">[-]</span><span style="margin-left:6px;">${group}</span>
                    	</div>
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
                </tr>

                <tr class="statusrow groupexpanded grouptr${group}">
                    <td colspan="3" style="border-bottom: none;">
                      <div  style="margin: 8px 4px 8px 16px;">
                        <table id="statustable-group" border="0" cellpadding="0" cellspacing="0">
                          <thead>
                            <td width="4%" nowrap>Audit</td>
                            <td width="6%" nowrap>Status</td>
                            <td width="38%" nowrap>Collection Name</td>
                            <td width="6%">Type</td>
                            <td width="6%" nowrap>Files*</td>
                            <td width="8%" nowrap>Size</td>
                            <td width="12%" nowrap>Last Audit</td>
                            <td width="12%" nowrap>Next Audit</td>
                            <td width="8%" nowrap>Audit Period</td>
                          </thead>
                    </td>
                </tr>
	                
                <c:forEach var="item" items="${collections}" varStatus="loopcollections">
        	        <c:set var="stripclasscollections">
            	        <c:choose>
            	            <c:when test="${loopcollections.index % 2 == 0}">row-strip-highlighted</c:when>
            	            <c:otherwise>row-strip-highnormal</c:otherwise>
            	        </c:choose>
            	    </c:set>       	

	                <c:if test="${item.collection.group != null && group == item.collection.group}">
	
		                <tr class="statusrow ${stripclasscollections} grouptr${item.collection.group}" >
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
		                    </td>
		                    <td>
		                        <c:choose>
		                            <c:when test="${'A'.bytes[0] == item.collection.state }">
		                                <img src="images/file-ok.jpg" title="Last audit successful" alt="Last audit successful"/>
		                            </c:when>
		                            <c:when test="${'E'.bytes[0] == item.collection.state }">
		                                <img src="images/error.jpg" title="Collection contains errors" alt="Collection contains errors"/>
		                            </c:when>
		                            <c:when test="${'I'.bytes[0] == item.collection.state }">
		                                <img src="images/warning.jpg" title="Last audit was interrupted" alt="Last audit was interrupted"/>
		                            </c:when>
		                            <c:otherwise>
		                                <img src="images/file-bad.jpg" title="Complete audit has not occurred" alt="Complete audit has not occurred"/>
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
                </table>
                </div>
                </td>
                </tr>
            </c:forEach>

            <!-- Start no group collections -->
            <c:if test="${noGroupCollections.size() > 0}">
              <tr class="groupheaderrownogroup">
                <td class="groupheader" onclick="toggleVisibility('spexpandnogroup','inline'); toggleVisibility('sphidenogroup','inline');">
                	<div onclick="showGroup('grouptrnogroup')" id="spexpandnogroup" style="display:none;cursor: pointer;" >
                		<span class="lbl-group">[+]</span><span style="margin-left:6px;"></span>
                	</div>
                	<div onclick="hideGroup('grouptrnogroup')" id="sphidenogroup" style="display:inline;cursor: pointer;" >
                		<span class="lbl-group">[-]</span><span style="margin-left:6px;"></span>
                	</div>
                </td>
                <td class="groupheader" id="groupnogroup">
                        ${noGroupCollections.size()}
                </td>
                <td class="groupheader"></td>
              </tr>
              <tr class="statusrow grouptrnogroup" style="border-bottom: none;">
                <td colspan="3">
                  <div  style="margin: 8px 4px 8px 16px;">
                    <table id="statustable-group" border="0" cellpadding="0" cellspacing="0">
                      <thead>
                        <td width="4%" nowrap>Audit</td>
                        <td width="6%" nowrap>Status</td>
                        <td width="38%" nowrap>Collection Name</td>
                        <td width="6%">Type</td>
                        <td width="6%" nowrap>Files*</td>
                        <td width="8%" nowrap>Size</td>
                        <td width="12%" nowrap>Last Audit</td>
                        <td width="12%" nowrap>Next Audit</td>
                        <td width="8%" nowrap>Audit Period</td>
                      </thead>
                </td>
              </tr>

              <c:forEach var="item" items="${noGroupCollections}" varStatus="loopnogroups">
        	    <c:set var="stripclassnogroups">
            	    <c:choose>
            	        <c:when test="${loopnogroups.index % 2 == 0}">row-strip-highlighted</c:when>
            	        <c:otherwise>row-strip-highnormal</c:otherwise>
            	    </c:choose>
            	</c:set>	

                <tr class="statusrow stripclassnogroups grouptrnogroup">
                    <td>

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
                    </td>
                    <td>
                        <c:choose>
                            <c:when test="${'A'.bytes[0] == item.collection.state }">
                                <img src="images/file-ok.jpg" title="Last audit successful" alt="Last audit successful"/>
                            </c:when>
                            <c:when test="${'E'.bytes[0] == item.collection.state }">
                                <img src="images/error.jpg" title="Collection contains errors" alt="Collection contains errors"/>
                            </c:when>
                            <c:when test="${'I'.bytes[0] == item.collection.state }">
                                <img src="images/warning.jpg" title="Last audit was interrupted" alt="Last audit was interrupted"/>
                            </c:when>
                            <c:otherwise>
                                <img src="images/file-bad.jpg" title="Complete audit has not occurred" alt="Complete audit has not occurred"/>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td>
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
                </table>
                </td>
              </tr>
            </c:if>
            <!-- End no group collections -->

            <tr><td colspan="5"><br/><d:Auth role="Collection Modify" showUnauthenticated="true"><a href="ManageCollection">Add Collection</a></d:Auth> &nbsp;&nbsp;&nbsp&nbsp;&nbsp;
                    <d:Auth role="Audit">
                        <c:choose>
                            <c:when test="${pause.paused}"><a href="Pause?pause=0">Enable Automated Auditing</a></c:when>
                            <c:otherwise><a href="Pause?pause=1">Pause Automated Auditing</a></c:otherwise>
                        </c:choose>
                    </d:Auth>
                </td></tr>
            <tr>
            	<td colspan="7">
            		<br />
            		<img src="images/file-ok.jpg" alt="Last audit successful"/><span class="lbl-indicator">- OK</span>
            		<img src="images/error.jpg" alt="Error"/><span class="lbl-indicator">- Error</span>
            		<img src="images/warning.jpg" alt="Interrupted"/><span class="lbl-indicator">- Interrupted</span>
            		<img src="images/file-bad.jpg" alt="Complete audit has not occurred"/><span class="lbl-indicator">- No audit</span>
        		</td>
    		</tr>
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
