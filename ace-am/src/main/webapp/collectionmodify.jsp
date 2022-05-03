<%-- 
Document   : modifyresource
Created on : Nov 2, 2007, 10:55:07 AM
Author     : toaster
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>${pageHeader}</title>
    <link rel="stylesheet" type="text/css" href="style.css" />
    <script language="javascript">
        isclicked = false;
         
        function turnon(id) {
            var t = document.getElementById(id);
            t.style.display = "block";
        }
        
        function turnoff(id) {
            if (!isclicked)
            {
                var t = document.getElementById(id);
                t.style.display = "none";
            }
        }
        function updategroup(val) {
            document.getElementById('groupBox').value=val;
            var t = document.getElementById('grouplist');
            t.style.display = "none";
        }
    </script>
    <style type="text/css">
        #error {
            color: #ff0000;
        }
        
        #grouplist {
            display: none; 
            z-index: 2; 
            position: absolute; 
            background-color: #FFFFFF; 
            border: thin solid #000000;
            width: 150px;
        }
    </style>
</head>
<body>
<jsp:include page="header.jsp" />
<h1 class="page_header">${pageHeader}</h1>

<div class="standardBody">
<form action="ManageCollection" method="post">
    <c:choose>
        <c:when test='${collection.id > 0}' >
            <h3>Modify Collection</h3>
            <input type="hidden" name="collectionid" value="${collection.id}" />
            
        </c:when>
        <c:otherwise>
            <h3>Create Collection</h3>
        </c:otherwise>
    </c:choose>
    <c:if test="${error != null}">
        <span id="error">${error}</span>
    </c:if>
    <table border="0">
        
        <tbody></tbody>
        <tr>
            <td>Collection Name</td>
            <td><input type="text" size="30" name="name" value="${collection.name}" /></td>
        </tr>
        <tr>
            <td>Location</td>
            <td><input type="text" size="50" name="directory" value="${collection.directory}"></td>
        </tr>
        
        <tr>
            <td>Audit Collection</td>
            <td><input type="text" size="3" name="checkperiod" value="${collection.settings['audit.period']}"/> days</td>
        </tr>
        <tr>
            <td>Collection Group</td>
            <td><input id="groupBox" onfocus="turnon('grouplist');" on onblur="turnoff('grouplist');" type="text" size="20" name="group" value="${collection.group}"/>
                <br>
                <div id="grouplist" onmouseover="isclicked=true;" onmouseout="isclicked=false;">
                    <c:forEach var="group" items="${grouplist}">
                        <a href="#" target="_parent" onclick="updategroup('${group}');">${group}</a><BR>
                    </c:forEach>
                </div>
            </td>
        </tr>
        <tr>
            <td>Digest Type</td>
            <td>
                <c:choose>
                    <c:when test='${collection.id > 0}' >
                    ${collection.digestAlgorithm}                            
                    </c:when>
                    <c:otherwise>
                        <select name="digest">
                            <option value="MD5">MD5</option>
                            <option selected value="SHA-256">SHA 256</option>
                            <option value="SHA-512">SHA 512</option>
                        </select>
                    </c:otherwise>
                </c:choose>
            </td>
        </tr>
        <tr>
            <td>E-mail audit notifications</td>
            <td><input type="text" size="50" name="emaillist" value="${collection.settings['email.recipients']}" /></td>
        </tr>
        <tr>
            <td>Audit tokens</td>
            <td>
                <c:choose>
                    <c:when test="${!collection.settings['audit.tokens'] && collection.id > 0}">
                        <input type="radio" name="audittokens" value="true" >Check Digests
                        <input type="radio" name="audittokens" value="false" checked>Do Not Check Digests
                    </c:when>
                    <c:otherwise>
                        <input type="radio" name="audittokens" value="true" checked>Check Digests
                        <input type="radio" name="audittokens" value="false">Do Not Check Digests
                    </c:otherwise>
                </c:choose>
            </td>
        </tr>
        
        <tr>
            <td>Allow outside access to data</td>
            <td>
                <c:choose>
                    <c:when test="${collection.settings['proxy.data']}">
                        <input type="radio" name="proxy" value="true" checked>Allow
                        <input type="radio" name="proxy" value="false">Disallow
                    </c:when>
                    <c:otherwise>
                        <input type="radio" name="proxy" value="true" >Allow
                        <input type="radio" name="proxy" value="false" checked>Disallow                    </c:otherwise>
                </c:choose>
            </td>
        </tr>
        
        <c:forEach var="partner" items="${collection.peerCollections}">
            <tr>
                <td>Peer Collection</td>
                <td><c:forEach var="remoteSite" items="${partnerList.sites}">
                        <c:if test="${remoteSite.id == partner.site.id}">
                            <c:choose>
                                <c:when test="${remoteSite.online}">
                                    <c:forEach var="remoteColl" items="${remoteSite.collections}">
                                        <c:if test="${remoteColl.id == partner.peerId}">${remoteColl.name}</c:if>
                                    </c:forEach>
                                    @ ${partner.site.remoteURL} <a href="ManagePeer?collectionid=${collection.id}&amp;remove=${partner.id}">Remove</a>
                                </c:when>
                                <c:otherwise>
                                ${partner.site.remoteURL} is inaccessible. <a href="ManagePeer?collectionid=${collection.id}&amp;remove=${partner.id}">Remove</a> 
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                </c:forEach> </td>
            </tr>
            
        </c:forEach>
        <tr>
            <td><a href="select_peer.jsp">Add peers</a></td>
            <td>Add remote collections to compare during auditing</td>
        </tr>
        <c:choose>
            <c:when test="${driver != null}">
            
            </table>
            <c:if test="${driver.page != null}">
                <fieldset>
                    <legend>${collection.storage} configuration</legend>
                    <c:import url="${driver.page}"></c:import>
                </fieldset>
            </c:if>
            <input type="hidden" name="driver" value="${collection.storage}">
            <input type="submit" name="commit" value="Save" class="submitLink" />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            <a href="clearstorage.jsp">Change Storage</a>
        </c:when>
        <c:otherwise>
            <tr><td>Storage Type</td>
                <td>
                    <select name="driver">
                        <c:forEach var="item" items="${availdrivers}">
                            <option>${item}</option>
                        </c:forEach>
                    </select>
            </td></tr>
            </table>
            <br>
            <input type="submit" name="commit" value="Configure Storage" class="submitLink" />
        </c:otherwise>
    </c:choose>
    
    
</form>

</div>
<div>
</div>
<jsp:include page="footer.jsp" />
</body>
</html>
