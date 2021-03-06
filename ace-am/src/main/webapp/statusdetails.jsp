<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
<%@taglib tagdir="/WEB-INF/tags" prefix="h" %>
<%@taglib uri="/WEB-INF/tlds/monitor" prefix="um"%> 
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<c:if test="${workingCollection != null}">
    <fieldset id="col-container">
        <legend>${workingCollection.collection.name}</legend>
        <a class="btn-close" onclick="javascript: closeColContainer();">Close</a>
        <table id="detailstbl1">
            <tr>
                <td>Collection State</td>
                <td>${workingCollection.collection.getStateEnum()}</td>
            </tr>
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
                                            <td><um:FileSize value="${thread.read}"/><c:if test="${thread.totalSize > 0}">/<um:FileSize value="${thread.totalSize}"/></c:if></td>
                                            <td>${thread.idle/1000}s</td>
                                            <td>${thread.file}</td>
                                        </tr>
                                    </c:forEach>
                                </table>
                            </div></c:when>
                        <c:when test="${workingCollection.tokenAuditRunning}">Token Audit in progress</c:when>
                        <c:when test="${workingCollection.collection.state eq 'R'.charAt(0)}">N/A</c:when>
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
                <td>${workingCollection.collection.settings['proxy.data']}</td>
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
                <td>${workingCollection.collection.settings['audit.period']} <c:if test="${pause.paused && workingCollection.collection.settings['audit.period'] > 0}">Automated auditing is paused</c:if></td>
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
                <um:Auth role="Audit">
                    <c:if test="${workingCollection.collection.state ne 'R'.charAt(0) && (!workingCollection.tokenAuditRunning && workingCollection.collection.storage != null)}">
                        <c:choose>
                            <c:when test="${workingCollection.fileAuditRunning || workingCollection.queued}">
                                <td>
                                    <a href="StopSync?type=file&amp;collectionid=${workingCollection.collection.id}" title="Stop File Audit">
                                        Stop File Audit
                                    </a>
                                </td>
                            </c:when>
                            <c:otherwise>
                                <td>
                                    <a href="StartSync?type=file&amp;collectionid=${workingCollection.collection.id}" title="Start File Audit">
                                        Start File Audit
                                    </a>
                                </td>
                            </c:otherwise>
                        </c:choose>
                    </c:if>

                    <c:if test="${workingCollection.collection.state ne 'R'.charAt(0) && (!workingCollection.fileAuditRunning || workingCollection.queued)}">
                        <c:choose>
                            <c:when test="${workingCollection.tokenAuditRunning}">
                                <td><a href="StopSync?type=token&amp;collectionid=${workingCollection.collection.id}" title="Stop Token Audit">Stop Token Audit</a></td>
                                    </c:when>
                                    <c:otherwise>
                                <td><a href="StartSync?type=token&amp;collectionid=${workingCollection.collection.id}" title="Start Token Audit">Start Token Audit</a></td>
                                    </c:otherwise>
                                </c:choose>
                            </c:if>
                        </um:Auth>
                
                <!--<td><a href="#" onclick="javascript:showBrowse(${workingCollection.collection.id}); return false;">Browse</a>-->
                <um:Auth role="Browse"><td><a href="Browse?collection=${workingCollection.collection.id}" title="Browse">Browse</a></td></um:Auth>
                <um:Auth role="Log"><td><a href="EventLog?collection=${workingCollection.collection.id}&amp;clear=1&amp;toggletype=sync">View Log</a></td></um:Auth>
                <um:Auth role="Report"><td><a href="Report?collectionid=${workingCollection.collection.id}" title="Report">Report</a></td></um:Auth>
                    <td>
                        <fieldset id="dropmenu2" style="display: none; z-index: 2; position: absolute; background-color: #FFFFFF; width: 150px;">
                            <legend><span onclick="toggleVisibility('dropmenu1','block'); toggleVisibility('dropmenu2','block');">close</span></legend>
                        <c:if test="${workingCollection.collection.state ne 'R'.charAt(0) && (!(workingCollection.fileAuditRunning || workingCollection.tokenAuditRunning || workingCollection.queued))}">
                            <um:Auth role="Collection Modify">
                                <a href="ManageCollection?collectionid=${workingCollection.collection.id}" title="Configure connection settings for this collection" >Collection Settings</a><br>
                                <a href="collectionremove.jsp?collectionid=${workingCollection.collection.id}" title="Delete Collection">Remove Collection</a><br>
                                <a href="ingest_form.jsp?collectionid=${workingCollection.collection.id}" title="Import Tokens">Import Tokens</a><br>
                                <a href="ManageFilters?collectionid=${workingCollection.collection.id}">Modify Filters</a><BR>                                
                            </um:Auth>
                            <um:Auth role="Modify Activity Reporting">
                                <a href="ReportConfiguration?collectionid=${workingCollection.collection.id}">Modify Reporting</a><br>
                            </um:Auth>
                        </c:if>
                        <c:if test="${workingCollection.collection.state ne 'R'.charAt(0)}">
	                        <um:Auth role="Summary">
	                            <a href="Summary?collectionid=${workingCollection.collection.id}&amp;output=store" title="Download tokenstore for this collection">Download TokenStore</a><br>
	                            <a href="Summary?collectionid=${workingCollection.collection.id}&amp;output=digest" title="Download a list of all digests in this collection">Download Digests</a><br>
	                            <a href="Summary?collectionid=${workingCollection.collection.id}&amp;output=checkm" title="Download a checkm manifest of all items in this collection">Download checkm list</a><br>
	                            <c:if test="${workingCollection.collection.settings['proxy.data']}">
	                                <a href="Summary?collectionid=${workingCollection.collection.id}&amp;output=wget" title="Down wget-compatible list of files in this collection">Download wget file list</a><br>
	                            </c:if>
	                        </um:Auth>
                        </c:if>
                        <um:Auth role="Compare">
                            <a href="compare_form.jsp?collectionid=${workingCollection.collection.id}">Compare Collection</a><br>
                        </um:Auth>
                        <um:Auth role="Show Duplicates">
                            <a href="ReportDuplicates?collectionid=${workingCollection.collection.id}">Show Duplicate Files</a>
                        </um:Auth>
                        <um:Auth role="View Audit Summaries">
                            <a href="ViewSummary?collectionid=${workingCollection.collection.id}&amp;limit=50" title="View all activity reports">Activity Reports</a>
                        </um:Auth>
                    </fieldset>
                    
                    <div id="dropmenu1" style="display: block;" onclick="toggleVisibility('dropmenu1','block'); toggleVisibility('dropmenu2','block'); ">more...</div>
                    
                </td>
            </tr>
        </table>
    </fieldset>
</c:if>


