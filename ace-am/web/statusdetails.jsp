<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
<%@taglib tagdir="/WEB-INF/tags" prefix="h" %>
<%@taglib uri="/WEB-INF/tlds/monitor" prefix="um"%> 
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<c:if test="${workingCollection != null}">
    <fieldset>
        <legend>${workingCollection.collection.name} <a href="Status?collectionid=-1">x</a></legend>
        <table id="detailstbl1">
            <tr>
                <td>Audit Status: </td>
                <td id="threaddetailsTD">
                    <c:choose>
                        <c:when test="${workingCollection.fileAuditRunning}">File Audit in progress
                            <div id="threaddetailsDIV">
                                <table>
                                    <tr><td>Reading thread</td><td>State</td><td>Bytes</td><td>Idle</td><td>File</td></tr>
                                    <c:forEach var="thread" items="${workingCollection.fileAuditThread.driverStatus}">
                                        <tr>
                                            <td>${thread.runningThread.name}</td>
                                            <td>${thread.state}</td>
                                            <td>${thread.read}<c:if test="${thread.totalSize > 0}">/${thread.totalSize}</c:if></td>
                                            <td>${thread.idle/1000}s</td>
                                            <td>${thread.file}</td>
                                        </tr>
                                    </c:forEach>
                                </table>
                            </div></c:when>
                        <c:when test="${workingCollection.tokenAuditRunning}">Token Audit in progress</c:when>
                        <c:otherwise>Idle</c:otherwise>
                    </c:choose>
                </td>
            </tr>

            <tr>
                <td>Last&nbsp;Complete&nbsp;Update</td>
                <td>${workingCollection.collection.lastSync}</td>
            </tr>
            <tr >
                <td>Allow Outside Data Access</td>
                <td>${workingCollection.collection.proxyData}</td>
            </tr>
            <tr>
                <td>Directory</td>
                <td>${workingCollection.collection.directory}</td>
            </tr>
            <tr>
                <td>Collection Type</td>
                <td>${workingCollection.collection.storage}</td>
            </tr>
            <tr>
                <td>Digest Type</td>
                <td>${workingCollection.collection.digestAlgorithm}</td>
            </tr>
            <tr>
                <td>Collection Size</td>
                <td><c:choose>
                        <c:when test="${workingCollection.totalSize > 0}"><um:FileSize value="${workingCollection.totalSize}" /></c:when>
                        <c:otherwise>0 B</c:otherwise>
                    </c:choose>
                </td>
            </tr>
            <tr>
                <td>Audit Period</td>
                <td>${workingCollection.collection.checkPeriod} <c:if test="${pause.paused && workingCollection.collection.checkPeriod > 0}">Automated auditing is paused</c:if></td>
            </tr>
            <tr>
                <td>Total Monitored Files</td>
                <td><h:DefaultValue test="${workingCollection.totalFiles > -1}" success="${workingCollection.totalFiles}" failure="Unknown" /></td>
            </tr>
            <c:if test="${!workingCollection.fileAuditRunning}">
                <tr>
                    <td>Total Errors:</td>
                    <td><h:DefaultValue test="${workingCollection.totalErrors > -1}" success="${workingCollection.totalErrors}" failure="Unknown"/></td>
                </tr>
            </c:if>
            <c:if test="${workingCollection.fileAuditRunning}">
                <tr>
                    <td>Total Files Scanned</td>
                    <td>${workingCollection.fileAuditThread.filesSeen}</td>
                </tr>

                <tr onmouseout='javascript:this.style.background="#ffffff"; return false;' onmouseover='javascript:this.style.background="#e8e8e8"; return false;'>
                    <td>New Files Found: </td>
                    <td>${workingCollection.fileAuditThread.newFilesFound}</td>
                </tr>

                <tr>
                    <td>Tokens Added</td>
                    <td>${workingCollection.fileAuditThread.tokensAdded}</td>
                </tr>

                <tr>
                    <td>Errors</td>
                    <td>${workingCollection.fileAuditThread.totalErrors}</td>
                </tr>
                <tr>
                    <td>Last File Processed</td>
                    <td>${fn:replace(workingCollection.fileAuditThread.lastFileSeen, "/", "&#x200B;/")}</td>
                </tr>

            </c:if>

            <c:if test="${workingCollection.tokenAuditRunning}">
                <tr>
                    <td>Total Tokens Scanned</td>
                    <td>${workingCollection.tokenAuditThread.tokensSeen}</td>
                </tr>

                <tr>
                    <td>Tokens Validated</td>
                    <td>${workingCollection.tokenAuditThread.validTokens}</td>
                </tr>


                <tr>
                    <td>Errors</td>
                    <td>${workingCollection.fileAuditThread.totalErrors}</td>
                </tr>


            </c:if>

        </table>
        <table id="dettable">        
            <tr>
                <c:if test="${!workingCollection.tokenAuditRunning && workingCollection.collection.storage != null}">
                    <c:choose>
                        <c:when test="${workingCollection.fileAuditRunning}">
                            <td><a href="StopSync?type=file&amp;collectionid=${workingCollection.collection.id}" title="Stop File Audit" ><img src="images/stop.jpg" alt="Stop File Audit" ></a></td>
                                </c:when>
                                <c:otherwise>
                            <td><a href="StartSync?type=file&amp;collectionid=${workingCollection.collection.id}" title="Start File Audit"><img src="images/file-audit-start.jpg" alt="Start File Audit" ></a></td>
                                </c:otherwise>
                            </c:choose>
                        </c:if>


                <c:if test="${!workingCollection.fileAuditRunning}">
                    <c:choose>
                        <c:when test="${workingCollection.tokenAuditRunning}">
                            <td><a href="StopSync?type=token&amp;collectionid=${workingCollection.collection.id}" title="Stop Token Audit" ><img src="images/stop.jpg" alt="Stop Token Audit" ></a></td>
                                </c:when>
                                <c:otherwise>
                            <td><a href="StartSync?type=token&amp;collectionid=${workingCollection.collection.id}" title="Start Token Audit"><img src="images/token-audit-start.jpg" alt="Token File Audit" ></a></td>
                                </c:otherwise>
                            </c:choose>
                        </c:if>


<!--<td><a href="#" onclick="javascript:showBrowse(${workingCollection.collection.id}); return false;">Browse</a>-->
                <td><a href="Browse?collection=${workingCollection.collection.id}" title="Browse"><img src="images/browse.jpg" alt="Browse"></a>
                <td><a href="EventLog?collection=${workingCollection.collection.id}&amp;clear=1&amp;toggletype=sync"><img title="Event Log" src="images/log.jpg" alt="View Log"></a></td>
                <td><a href="Report?collectionid=${workingCollection.collection.id}" title="Report"><img src="images/report.jpg" alt="Report"></a></td>
                <td>
                    <fieldset id="dropmenu2" style="display: none; z-index: 2; position: absolute; background-color: #FFFFFF; width: 150px;">
                        <legend><span onclick="toggleVisibility('dropmenu1'); toggleVisibility('dropmenu2');">close</span></legend>
                        <c:if test="${!(workingCollection.fileAuditRunning || workingCollection.tokenAuditRunning)}">
                            <a href="ManageCollection?collectionid=${workingCollection.collection.id}" title="Configure connection settings for this collection" >Collection Settings</a><br>
                            <a href="collectionremove.jsp" title="Delete Collection">Remove Collection</a><br>              
                            <a href="ManageFilters?collectionid=${workingCollection.collection.id}">Modify Filters</a><BR>
                            <a href="ReportConfiguration?collectionid=${workingCollection.collection.id}">Modify Reporting</a><br>
                        </c:if>
                        <a href="Summary?collectionid=${workingCollection.collection.id}" title="Download a list of all digests in this collection">Download Digests</a><br>
                        <c:if test="${workingCollection.collection.proxyData}">
                            <a href="Summary?collectionid=${workingCollection.collection.id}&amp;wget=1" title="Down wget-compatible list of files in this collection">Download wget file list</a><br>
                        </c:if>
                        <a href="compare_form.jsp">Compare Collection</a><br>
                        <a href="ReportDuplicates?collectionid=${workingCollection.collection.id}">Show Duplicate Files</a>
                        <a href="ViewSummary?collectionid=${workingCollection.collection.id}&amp;limit=50" title="View all activity reports">Activity Reports</a>
                    </fieldset>

                    <div id="dropmenu1" style="display: block;" onclick="toggleVisibility('dropmenu1'); toggleVisibility('dropmenu2'); ">more...</div>

                </td>
            </tr>
        </table>
    </fieldset>
</c:if>


