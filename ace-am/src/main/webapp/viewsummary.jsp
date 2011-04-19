
<%@page pageEncoding="UTF-8"%>
<%--
The taglib directive below imports the JSTL library. If you uncomment it,
you must also add the JSTL library to the project. The Add Library... action
on Libraries node in Projects view can be used to add the JSTL 1.1 library.
--%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib uri="/WEB-INF/tlds/monitor" prefix="m"%> 

<!DOCTYPE html
    PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html>

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <title>Summary Reports</title>
        <link rel="stylesheet" type="text/css" href="style.css" />
        <c:choose>
            <c:when test="${coll != null}">
                <link rel="alternate" type="application/atom+xml"
                      href="ViewSummary?rss=1&amp;collectionid=${coll.id}" title="RSS feed for  ${coll.name}"/>
            </c:when>
            <c:otherwise>
                <link rel="alternate" type="application/atom+xml"
                      href="ViewSummary?rss=1" title="RSS feed all collection"/>
            </c:otherwise>
        </c:choose>


        <script type="text/javascript">
            function toggleVisibility(id) {
                var t = document.getElementById(id);
                if (t.style.display == "block") {
                    t.style.display = "none";
                } else {
                    t.style.display = "block";
                } 
            }
            
            function showAll()
            {
                var divs = document.getElementsByTagName('div');
                
                for (i = 0; i < divs.length; i++)
                {
                    if (divs[i].className == "reportdiv")
                    {
                        divs[i].style.display = "block";
                    }
                }
            }
            function showNone()
            {
                var divs = document.getElementsByTagName('div');
                
                for (i = 0; i < divs.length; i++)
                {
                    if (divs[i].className == "reportdiv")
                    {
                        divs[i].style.display = "none";
                    }
                }
            }
        </script>
        <style type="text/css"> 
            #summaryheader {
                padding: 10px;
            }
            .reportdiv {
                border: solid 1px;
                padding: 10px;
                display: none;
            }
            .expandspan {
                padding-left: 10px;
                padding-top: 10px;
                font-size: xx-small;
                color: #003388;
            }
            .expandspan a {

                font-size: xx-small;
                color: #003388;
            }
        </style>
    </head>
    <body>
        <jsp:include page="header.jsp" />
        <div class="standardBody">
            <c:choose>
                <c:when test="${coll != null}">
                    <h3>Summary Reports for ${coll.name}</h3>
                </c:when>
                <c:otherwise>
                    <h3>All Summary Reports</h3>
                </c:otherwise>
            </c:choose>
            <span style="font-size: xx-small;">Click on report name to view details</span><br/>
            <ul>
                <c:forEach var="summary" items="${summaries}">
                    <li>
                        <span onclick="toggleVisibility('s${summary.id}');">${summary.reportName}</span>
                        <div id="s${summary.id}" name="reportdiv1" class="reportdiv">
                            <table>
                                <tr><td>Collection </td><td>${summary.collection.name}</td></tr>
                                <tr><td>Report Start</td><td>${summary.startDate}</td></tr>
                                <tr><td>Report End</td><td>${summary.endDate}</td></tr>
                                <c:forEach items="${summary.summaryItems}" var="item">
                                    <c:if test="${!item.logType}">
                                        <tr><td>${item.attribute}</td><td>${item.value}</td></tr>
                                    </c:if>
                                </c:forEach> 
                                <tr><td id="summaryheader" colspan="2"><fieldset>
                                            <legend>Log Events</legend>
                                            <table>
                                                <c:forEach items="${summary.summaryItems}" var="item">
                                                    <c:if test="${item.logType}">
                                                        <tr><td>${item.attribute}</td><td>${item.value}</td></tr>
                                                    </c:if>
                                                </c:forEach> 
                                            </table></fieldset>
                                    </td></tr>
                            </table>
                        </div>
                    </li>
                </c:forEach>
            </ul>
            <span class="expandspan" onclick="showAll();">Expand all</span> <span class="expandspan" onclick="showNone();">Collapse all</span> <span class="expandspan">
                <c:choose>
                    <c:when test="${coll != null}">
                        <a href="ViewSummary?collectionid=${coll.id}">Load All</a>
                    </c:when>
                    <c:otherwise>
                        <a href="ViewSummary">Load All</a>
                    </c:otherwise>
                </c:choose></span>

        </div>

        <jsp:include page="footer.jsp" />
    </body>
</html>
