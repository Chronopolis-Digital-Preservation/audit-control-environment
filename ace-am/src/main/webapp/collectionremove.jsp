<%-- 
Document   : resourcefinish
Created on : Nov 2, 2007, 2:00:03 PM
Author     : toaster
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>${pageHeader}</title>
        <link rel="stylesheet" type="text/css" href="style.css" />
    </head>
    <body>
        <jsp:include page="header.jsp" />
        <h1 class="page_header">${pageHeader}</h1>

        <div class="standardBody">
        <h2 style="color: #333;">Are you sure you want to Remove this collection? </h2>
            <p>
                This action will permanently change the state of collection <span class="highlight-text">${workingCollection.collection.name}</span> to <span class="highlight-text">REMOVED</span>. ACE will not continue tracking or auditing the collection. This action <span class="highlight-text">CANNOT</span> be undone. This will not remove any files from storage.
            </p>

            <p>Click <span class="highlight-text">Remove</span> to proceed.</p>

        <a href="ManageCollection?remove=yes&collectionid=${workingCollection.collection.id}">
            Remove</a>&nbsp;&nbsp;&nbsp;
        <a href="Status">Back</a>
    </div>
        <jsp:include page="footer.jsp" />

    </body>
</html>
