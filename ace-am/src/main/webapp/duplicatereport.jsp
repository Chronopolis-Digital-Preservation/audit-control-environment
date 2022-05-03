
<%@page pageEncoding="UTF-8"%>
<%--
The taglib directive below imports the JSTL library. If you uncomment it,
you must also add the JSTL library to the project. The Add Library... action
on Libraries node in Projects view can be used to add the JSTL 1.1 library.
--%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<!DOCTYPE html
PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html>
    
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <title>${pageHeader}</title>
        <link rel="stylesheet" type="text/css" href="style.css" />
        <style type="text/css">
            .digest {
                font-family: monospace;
                padding-right: 15px;
            }
        </style>
    </head>
    <body>
        <jsp:include page="header.jsp" />
        <h1 class="page_header">${pageHeader}</h1>

        <div class="standardBody">
            Total Files: ${totalfiles} Duplicate Files: ${totaldups} <fmt:formatNumber maxFractionDigits="2" type="number" value="${(totaldups / totalfiles) * 100}" />% Generation time: ${time}ms
            <table>
                
                <thead><tr><td>Number of Duplicates</td><td>Occurances</td></tr></thead>
                <c:forEach var="item" items="${histogram}">
                    <tr><td>${item.digestCount}</td><td>${item.instances}</td></tr>
                </c:forEach>
            </table>
            
            <table>
                <thead><tr><td>Digest</td><td>Number of occurances</td></tr></thead>
                <c:forEach var="item" items="${duplicates}">
                    <tr><td class="digest"><a href="ShowDuplicates?collectionid=${collection}&amp;digest=${item.digest}">${item.digest}</a></td><td>${item.count}</td></tr>
                </c:forEach>
            </table>
           
        </div>
        
        <jsp:include page="footer.jsp" />
    </body>
</html>
