
<%@page pageEncoding="UTF-8"%>
<%--
The taglib directive below imports the JSTL library. If you uncomment it,
you must also add the JSTL library to the project. The Add Library... action
on Libraries node in Projects view can be used to add the JSTL 1.1 library.
--%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="h" %>


<!DOCTYPE html
    PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html>

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <title>Collection Errors</title>
        <link rel="stylesheet" type="text/css" href="style.css" />
        <style type="text/css">
            #summaryTable {
                margin-left: 50px;
                margin-right: auto;
            }
            .lblTd {
                padding-left: 50px;
            }
            .dataTd {
                padding-left: 10px;
            }
            #reportTable {
                width: 90%;
                margin-left: auto;
                margin-right: auto;
                margin-top: 20px;
                margin-bottom: 10px;
                border-left: 1px solid #000000;
                border-right: 1px solid #000000;
                border-top: 1px solid #000000;
                border-bottom: 1px solid #000000;

            }
            #reportTable thead {
                border-bottom: 1px solid #000000;
                background-color: #e8e8e8;
            }
            #navtable {
                width: 100%;
            }
            .datecol {
                width: 150px;
            }
            .tblLinks {
                padding-left: 50px;
            }

        </style>
    </head>
    <body>
        <jsp:include page="header.jsp" />

        <table id="summaryTable">
            <tr>
                <td class="lblTd">Active Files</td>
                <td class="dataTd"><h:DefaultValue test="${collection.activeFiles > -1}" success="${collection.activeFiles}" failure="0"/></td>
                <td class="lblTd">Corrupt Remote Files</td>
                <td class="dataTd"><h:DefaultValue test="${collection.remoteCorrupt > -1}" success="${collection.remoteCorrupt}" failure="0"/></td>
                <td class="lblTd">Files without Tokens</td>
                <td class="dataTd"><h:DefaultValue test="${collection.missingTokens} > -1}" success="${collection.missingTokens}" failure="0"/></td>

            </tr>
            <tr>
                <td class="lblTd">Missing Files</td>
                <td class="dataTd"><h:DefaultValue test="${collection.missingFiles > -1}" success="${collection.missingFiles}" failure="0"/></td>
                <td class="lblTd">Missing Remote Files</td>
                <td class="dataTd"><h:DefaultValue test="${collection.remoteMissing > -1}" success="${collection.remoteMissing}" failure="0"/></td>
                <td class="lblTd">Files with possible corrupt digests</td>
                <td class="dataTd"><h:DefaultValue test="${collection.invalidDigests > -1}" success="${collection.invalidDigests}" failure="0"/></td>
            </tr>
            <tr>
                <td class="lblTd">Corrupt Files</td>
                <td class="dataTd"><h:DefaultValue test="${collection.corruptFiles > -1}" success="${collection.corruptFiles}" failure="0"/></td>
            </tr>
            <tr>
                <td class="tblLinks"><a href="Report?collectionid=${collection.collection.id}&amp;text=1&amp;count=-1">Download List</a></td>
                <td class="tblLinks"><a href="StartSync?collectionid=${collection.collection.id}&amp;type=corrupt">Audit Corrupt Files</a></td>
            </tr>
        </table>

        <table id="reportTable">
            <thead>
                <tr>
                    <td>State</td>
                    <td>Path</td>
                    <td>Last Seen</td>
                    <td></td>
                </tr>
            </thead>
            <c:forEach var="item" items="${items}">
                <tr>
                    <td>
                        <c:choose>
                            <c:when test="${item.state eq 'C'}">
                                Corrupt
                            </c:when>
                            <c:when test="${item.state eq 'T'}">
                                Token Missing
                            </c:when>
                            <c:when test="${item.state eq 'M'}">
                                File Missing
                            </c:when>
                            <c:when test="${item.state eq 'I'}">
                                Token Corrupt
                            </c:when>
                            <c:when test="${item.state eq 'P'}">
                                Remote Missing
                            </c:when>
                            <c:when test="${item.state eq 'D'}">
                                Remote Corrupt
                            </c:when>
                            <c:otherwise>
                                ${item.state}
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td>

                        <c:choose>
                            <c:when test="${item.directory}"><span><img src="images/folder.jpg"/>${item.path}</span></c:when>
                            <c:otherwise><span><img src="images/file.jpg" alt=""/>${fn:replace(item.path, "/", "&#x200B;/")}</span></c:otherwise>
                        </c:choose>
                    </td>
                    <td class="datecol"><span>${fn:replace(item.lastSeen, " ", "&nbsp;")}</span></td>
                    <td><a href="EventLog?logpath=${item.path}">log</a> <a href="RemoveItem?itemid=${item.id}&amp;redirect=Report%3Fcollectionid=${collection.collection.id}">remove</a> </td>
                </tr>
            </c:forEach>
            <tr><td colspan="5">
                    <table id="navtable">
                        <tr>
                            <td>
                                <a href="Report?top=${items[0].id}&collectionid=${collection.collection.id}&count=${count}">&lt;&lt;</a></td>
                            <td align="center">Only items with errors are listed</td>
                            <td align="right"><a href="Report?start=${next}&collectionid=${collection.collection.id}&count=${count}">&gt;&gt;</a></td>
                        </tr>
                    </table>
                </td></tr>
        </table>



        <jsp:include page="footer.jsp" />
    </body>
</html>
