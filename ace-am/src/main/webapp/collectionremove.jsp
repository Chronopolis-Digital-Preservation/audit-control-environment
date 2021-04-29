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
        <title>Collection Remove</title>
        <link rel="stylesheet" type="text/css" href="style.css" />
    </head>
    <body>
        <jsp:include page="header.jsp" />
        <div class="standardBody">
        <h3>Confirm Remove</h3>
            <p>
                This will flag the state of collection ${workingCollection.collection.name} to be REMOVED.
                Note that this won't remove any files from the node and you may need to delete files manually.
            </p>

            <p>Click Remove to proceed.</p>

        <a href="ManageCollection?remove=yes&collectionid=${workingCollection.collection.id}">
            Remove</a>&nbsp;&nbsp;&nbsp;
        <a href="Status">Back</a>
    </div>
        <jsp:include page="footer.jsp" />

    </body>
</html>
