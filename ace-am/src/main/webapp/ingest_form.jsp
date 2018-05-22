<%--
    Document   : ingest
    Created on : Jun 4, 2012, 3:29:33 PM
    Author     : shake
--%>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Ingest Tokens</title>
    <jsp:include page="imports.jsp"/>
    <style type="text/css">
        button.btn {
            height: 100%
        }
    </style>
</head>
<body>
<jsp:include page="header.jsp"/>
<div class="standardBody">
    <h4>Import Tokens</h4>
    <h6>
        Working Collection is ${workingCollection.collection.group} -
        ${workingCollection.collection.name}
    </h6>
    <%-- Might be fun to do peer import some day --%>
    <FORM name="ingestform" METHOD="POST" ENCTYPE="multipart/form-data" ACTION="Ingest">
        <input type="hidden" name="collectionid" value="${workingCollection.collection.id}">

        <div class="form-group">
            <label for="fileInput">Select token store to import</label>
            <input type="file" class="form-control-file" id="fileInput" name="ingested"/>
        </div>
        <button type="submit" class="btn btn-primary">Submit</button>

    </FORM>
</div>
<jsp:include page="footer.jsp"/>
</body>
</html>
