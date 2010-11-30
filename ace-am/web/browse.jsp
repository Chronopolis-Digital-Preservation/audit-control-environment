
<%@page pageEncoding="UTF-8"%>
<%--
The taglib directive below imports the JSTL library. If you uncomment it,
you must also add the JSTL library to the project. The Add Library... action
on Libraries node in Projects view can be used to add the JSTL 1.1 library.
--%>
<%--
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
--%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
<%@taglib uri="/WEB-INF/tlds/monitor" prefix="m"%> 
<!DOCTYPE html
PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html>
    
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <title>Browse Collection</title>
        <link rel="stylesheet" type="text/css" href="style.css" />
        <script type="text/javascript" SRC="srbFunctions.js" ></script>
        <style type="text/css">
            .directory {
                font-size: 12px;
                margin-left: 10px;
                white-space: nowrap;
            }
            
            
            #scrollDirectory {
                height: 400px;
                padding-left: 10px;
                padding-top: 10px;
                padding-bottom: 10px;
                width:250px;
                overflow: scroll;
                margin: 0 0 0 auto; 
            }
            #scrollContainer {
                border-right: 1px solid #000000;
                border-top: 1px solid #000000;
                padding-top: 10px;
                padding-bottom: 10px;
                width:265px;
                float: left;
            }
            #description {
                width: 460px;
                float: right;
                font-size: 14px;
                margin-left: 10px;
                margin-top: 10px;
                /**overflow: visible;*/
            }
            #description table {
                font-size: 12px;
            }
            #reptable {
                margin-top: 30px;
                width: 100%;
                
            }
            
            img {
                border: 0 0 0 0;
            }
            
            .statusM {
                color: #ff0000;
            }
            .statusT {
                color: #ff0000;
            }
            .statusC {
                color: #ff0000;
            }
            .badreplica {
            }
            
        </style>
    </head>
    <body>
        
        <jsp:include page="header.jsp" />
        
        <div id="scrollContainer">
            <div id="scrollDirectory">
                <c:forEach var="dir" items="${directoryTree.roots}">
                    
                    <m:tree var="dn" node="${dir}">
                        
                        <m:dirbegin>
                            <c:choose>
                                <c:when test="${dn.directory}">
                                    <a href="Browse?item=${dn.id}"><img src="images/folder.jpg" >&nbsp;<span class="status${dn.status}">${dn.name}</span></a><BR>
                                    <div class="directory">
                                    </c:when>
                                    <c:otherwise>
                                        <a href="Browse?item=${dn.id}"><img src="images/file.jpg">&nbsp;<span class="status${dn.status}">${dn.name}</span></a><br>
                                    </c:otherwise>
                                </c:choose>
                            </m:dirbegin>
                            
                            <m:dirend>
                                <c:if test="${dn.directory}">
                                </div>
                            </c:if>
                            
                        </m:dirend>
                    </m:tree>
                </c:forEach>
            </div>
        </div>
        <div id="description">
            <c:if test="${selectedFile != null}">
                <strong>${selectedFile.name}</strong>
                <table>
                    <tr><td>Last Checked</td><td>${selectedFile.root.lastVisited}</td></tr>
                    <tr><td>Last Seen Intact</td><td>${selectedFile.root.lastSeen}</td></tr>
                    <tr><td>Path</td><td>${selectedFile.root.path}<td></tr>
                    <tr><td>Size</td><td><m:FileSize value="${selectedFile.root.size}" /><td></tr>
                    <tr><td>State</td><td>
                            <c:choose>
                                <c:when test="${selectedFile.root.state eq 'A'}">
                                    <img src="images/file-ok.jpg">
                                </c:when>
                                <c:when test="${selectedFile.root.state eq 'C'}">
                                    Corrupt
                                </c:when>
                                <c:when test="${selectedFile.root.state eq 'T'}">
                                    Token Missing
                                </c:when>
                                <c:when test="${selectedFile.root.state eq 'M'}">
                                    File Missing
                                </c:when>
                                <c:when test="${selectedFile.root.state eq 'P'}">
                                    Remote File Missing
                                </c:when>
                                <c:when test="${selectedFile.root.state eq 'D'}">
                                    Remote File Corrupt
                                </c:when>
                            </c:choose>
                    </td></tr>
                    <c:if test="${!selectedFile.root.directory}">
                        <tr><td>Last Change</td><td>${selectedFile.root.stateChange}</td></tr>
                        <tr><td>${selectedFile.root.parentCollection.digestAlgorithm} Digest</td><td style="font-size: 10px;">${selectedFile.root.fileDigest}</td></tr>
                    </c:if>
                    
                </table>
                <c:if test="${!selectedFile.root.directory}">
                    <a href="EventLog?logpath=${selectedFile.root.path}&amp;clear=1">View Log</a>&nbsp;&nbsp;&nbsp;&nbsp;
                    <c:if test="${selectedFile.root.token.id != null}">
                        <a href="DownloadToken?tokenid=${selectedFile.root.token.id}">View Token</a>&nbsp;&nbsp;&nbsp;&nbsp;
                    </c:if>
                    <c:if test="${selectedFile.root.parentCollection.proxyData}">
                        <a href="Path/${selectedFile.root.parentCollection.name}${selectedFile.root.path}">Download Item</a>&nbsp;&nbsp;&nbsp;&nbsp;
                    </c:if>
                    <a href="ShowDuplicates?itemid=${selectedFile.root.id}">View Duplicates</a>&nbsp;&nbsp;&nbsp;&nbsp;
                </c:if>
                <c:if test="${!auditing}">
                    <a href="RemoveItem?itemid=${selectedFile.root.id}">Remove</a>&nbsp;&nbsp;&nbsp;&nbsp;
                    <c:if test="${selectedFile.root.directory}">
                        <a href="StartSync?type=file&amp;collectionid=${selectedFile.root.parentCollection.id}&amp;itemid=${selectedFile.root.id}">Audit Files</a>&nbsp;&nbsp;&nbsp;&nbsp;
                    </c:if>
                </c:if>
                <c:if test="${selectedFile.root.directory}">
                    <a href="Summary?collectionid=${selectedFile.root.parentCollection.id}&amp;itemid=${selectedFile.root.id}&amp;output=digest">Download Digests</a>&nbsp;&nbsp;&nbsp;&nbsp;
                    <a href="Summary?collectionid=${selectedFile.root.parentCollection.id}&amp;itemid=${selectedFile.root.id}&amp;output=checkm">Checkm Manifest</a>&nbsp;&nbsp;&nbsp;&nbsp;

                    <c:if test="${selectedFile.root.parentCollection.proxyData}">
                        <a href="Summary?collectionid=${selectedFile.root.parentCollection.id}&amp;itemid=${selectedFile.root.id}&amp;output=wget">Web Crawler URL List</a>
                    </c:if>
                    
                    <!-- 
                    <br><br>
                    <FORM METHOD=POST ENCTYPE="multipart/form-data" ACTION="Compare">
                        <input type="hidden" name="collectionid" value="${workingCollection.collection.id}">
                        <input type="hidden" name="itemid" value="${selectedFile.root.id}">
                        Filter supplied file: <input type="text" NAME="filter" size="30" value="${selectedFile.root.path}"><br>
                        Manifest file to compare: <INPUT TYPE=FILE NAME="upfile"><BR><br>
                        <INPUT TYPE=SUBMIT VALUE="Submit">
                    </FORM>
                    -->
                </c:if>
            </c:if>
        </div>
        <jsp:include page="footer.jsp" />
        
    </body>
</html>
