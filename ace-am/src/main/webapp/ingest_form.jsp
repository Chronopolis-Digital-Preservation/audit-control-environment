<%-- 
    Document   : ingest
    Created on : Jun 4, 2012, 3:29:33 PM
    Author     : shake
--%>
<%--
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<%--
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Ingest Token</title>
    </head>
    <body>
        <jsp:include page="header.jsp" />
        <div class="standardBody">
        <form method="POST">
            <input type="text" name="teft"/>
            <input type="submit" value="click"/>
            <c:if test="${pageContext.request.method=='POST'}">
                <c:if test="${param.teft=='test'}">Bingo</c:if>
            </c:if>
        </form>
        </div>
        <jsp:include page="footer.jsp" />
    </body>
</html>
--%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Ingest Tokens</title>
        <link rel="stylesheet" type="text/css" href="style.css" />
    </head>
    <body>
        <jsp:include page="header.jsp" />
        <div class="standardBody">
        <h3>Ingest Tokens to collection ${workingCollection.collection.name}</h3>
        
         <FORM name="ingestform" METHOD="POST" ENCTYPE="multipart/form-data" ACTION="Ingest">
             <input  type="hidden" name="collectionid" value="${workingCollection.collection.id}">

             Select token store to ingest: <input type="file" name="ingested"/><br><br>
            <input type="submit" value="Submit"/>
        </FORM>
        </div>
        <jsp:include page="footer.jsp" />

    </body>
</html>
