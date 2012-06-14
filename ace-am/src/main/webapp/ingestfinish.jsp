<%-- 
    Document   : ingestfinish
    Created on : Jun 11, 2012, 4:20:58 PM
    Author     : shake
--%>

<%@page pageEncoding="UTF-8"%>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html
    PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <title>Ingest Store</title>
        <link rel="stylesheet" type="text/css" href="style.css" />
        <c:if test="${results.running}">
            <meta http-equiv="refresh" content="5;URL=ingestfinish.jsp">
        </c:if>
    </head>
    <body>
        <jsp:include page="header.jsp"/>
        <div class="standardBody">
            <c:choose>
                <c:when test="${results.running}">
                    <h2>Processing Tokens</h2>
                    <pre>${results.status}</pre>
                </c:when>
                <c:otherwise>
                    <h2>Finished Processing Tokens</h2>
                    <pre>${results.status}</pre>
                    
                    <h4>Updated Tokens: ${results.updatedTokensSize}</h4>
                    <ul>
                        <c:forEach items="${results.updatedTokens}" var="item">
                            <li> ${item} </li>
                        </c:forEach>
                    </ul>

                    <h4>New Tokens: ${results.newTokensSize}</h4>
                    <ul>
                        <c:forEach items="${results.newTokens}" var="item">
                            <li> ${item} </li>
                        </c:forEach>
                    </ul>
                </c:otherwise>
            </c:choose>
        </div>
        <jsp:include page="footer.jsp"/>
    </body>
</html>
