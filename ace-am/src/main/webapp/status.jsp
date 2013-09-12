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
            function toggleVisibility(id,type) {

                var t = document.getElementById(id);
                if (t.style.display == type) {
                    t.style.display = "none";
                } else {
                    t.style.display = type;
                }
            }
            function showGroup(id)
            {
                var divs = document.getElementsByTagName('tr');

                for (i = 0; i < divs.length; i++)
                {
                    if (divs[i].className.indexOf(id) != -1)
                    {
                        divs[i].style.display = "";
                    }
                }
            }
            function hideGroup(id)
            {
                var divs = document.getElementsByTagName('tr');

                for (i = 0; i < divs.length; i++)
                {
                    if (divs[i].className.indexOf(id) != -1)
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
        </style>
    </head>

    <body>
        <jsp:include page="header.jsp" />
        <script type="text/javascript">document.getElementById('status').style.backgroundColor = '#ccccff';</script>
        <c:if test="${workingCollection != null}">
            <div id="details">
                <jsp:include page="statusdetails.jsp"/>
            </div>

        </c:if>

        <table id="statustable">
            <thead>
                <td></td><td width="45%">Collection Name</td>
                <td>Type</td><td>Total Files*</td>
                <td>Last Audit</td>
                <td>Next Audit</td>
            </thead>
            <c:set var="count" value="0" />
            <jsp:useBean id="today" class="java.util.Date"/>
            <c:set var="counttotal" value="0" />
            <c:set var="sizetotal" value="0" />
            <c:forEach var="item" items="${collections}">

                <c:if test="${currgroup != item.collection.group && item.collection.group != null}">
                    <script type="text/javascript">
                        myp=document.getElementById("group${currgroup}");
                        if (myp != null)
                        {
                            myp.innerHTML='${counttotal} / <c:choose><c:when test="${sizetotal > 0}"><d:FileSize value="${sizetotal}" /></c:when><c:otherwise>0 B</c:otherwise></c:choose>';
                        }
                    </script>
                    <tr>
                        <td class="groupheader" colspan="3" onclick="toggleVisibility('spexpand${item.collection.group}','inline'); toggleVisibility('sphide${item.collection.group}','inline');">
                            <span onclick="showGroup('grouptr${item.collection.group}')" id="spexpand${item.collection.group}" style="display:none;float: left;width: 25px;" >[+]</span>
                            <span onclick="hideGroup('grouptr${item.collection.group}')" id="sphide${item.collection.group}" style="display:inline;float: left;width: 25px;" >[-]</span>
                            ${item.collection.group}
                        </td>
                        <td class="groupheader" colspan="3" id="group${item.collection.group}"></td>
                    </tr>
                    <c:set var="counttotal" value="0" />
                    <c:set var="sizetotal" value="0" />
                </c:if>

                <tr class="statusrow grouptr${item.collection.group}" >
                    <td>

                        <c:choose>
                            <c:when test="${item.fileAuditRunning || item.tokenAuditRunning}">
                                <img src="images/running.jpg" title="Audit in progress" alt="running" />
                            </c:when>
                            <c:when test="${item.queued}">
                                <img src="images/queued.jpg" title="Audit is queued" alt="queued" />
                            </c:when>
                            <c:otherwise>
                                <img src="images/stopped.jpg" title="No audit in progress" alt="idle" />
                            </c:otherwise>
                        </c:choose>
                        <c:choose>
                            <c:when test="${'A'.bytes[0] == item.collection.state }">
                                <img src="images/file-ok.jpg" title="Last audit successful" alt="Last audit successful"/>
                            </c:when>
                            <c:when test="${'E'.bytes[0] == item.collection.state }">
                                <img src="images/error.jpg" title="Collection contains errors" alt="Collection contains errors"/>
                            </c:when>
                            <c:otherwise>
                                <img src="images/file-bad.jpg" title="Complete audit has not occured" alt="Complete audit has not occured"/>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        <a href="Status?collectionid=${item.collection.id}">${item.collection.name}</a>
                    </td>
                    <td>${item.collection.storage}</td>
                    <td><h:DefaultValue test="${item.totalFiles > -1}" success="${item.totalFiles}" failure="Unknown" /></td>
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
                </tr>
                <c:set var="count" value="${count + 1}" />
                <c:set var="currgroup" value="${item.collection.group}" />
                <c:set var="counttotal" value="${counttotal + item.totalFiles}" />
                <c:set var="sizetotal" value="${sizetotal + item.totalSize}" />
            </c:forEach>
            <script type="text/javascript">
                myp=document.getElementById("group${currgroup}");
                myp.innerHTML='${counttotal} / <c:choose><c:when test="${sizetotal > 0}"><d:FileSize value="${sizetotal}" /></c:when><c:otherwise>0 B</c:otherwise></c:choose>';
            </script>

            <tr><td colspan="5"><br/><d:Auth role="Collection Modify" showUnauthenticated="true"><a href="ManageCollection">Add Collection</a></d:Auth> &nbsp;&nbsp;&nbsp&nbsp;&nbsp;
                    <d:Auth role="Audit">
                        <c:choose>
                            <c:when test="${pause.paused}"><a href="Pause?pause=0">Enable Automated Auditing</a></c:when>
                            <c:otherwise><a href="Pause?pause=1">Pause Automated Auditing</a></c:otherwise>
                        </c:choose>
                    </d:Auth>
                </td></tr>
            <tr><td colspan="5"><br /><img src="images/running.jpg" alt="running"/> - Audit in progress&nbsp;&nbsp;&nbsp;&nbsp;<img src="images/stopped.jpg" alt="stopped"/> - Audit idle</td></tr>
            <tr><td colspan="5">* - Total files and status not updated until after first sync.</td></tr>
            <tr><td colspan="5"><c:choose>
                        <c:when test="${pause.paused}"><span id="inactiveaudit">Automated auditing is currently paused.</span> </c:when>
                        <c:otherwise>Automated auditing active.</c:otherwise>
                    </c:choose></td></tr>

        </table>

        <jsp:include page="footer.jsp" />
    </body>
</html>
