
<%@page pageEncoding="UTF-8"%>
<%--
The taglib directive below imports the JSTL library. If you uncomment it,
you must also add the JSTL library to the project. The Add Library... action
on Libraries node in Projects view can be used to add the JSTL 1.1 library.
--%>
<%--
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
--%>

<!DOCTYPE html
    PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html>

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <title>Clear Storage Configuration</title>
        <link rel="stylesheet" type="text/css" href="style.css" />
    </head>
    <body>
        <jsp:include page="header.jsp" />
        <div class="standardBody">
            <h3 class="standardHeader" id="compareCollectionHeader">Clear storage configuration for ${workingCollection.collection.name}. This is not reversible.</h3>
            <br/>You will have to reconfigure a storage resource for this collection if you wish
                to continue auditing. No log files or digests will be removed.<br/><br/>
                <a href="ClearResource?collectionid=${workingCollection.collection.id}">Clear</a>
        </div>
        <jsp:include page="footer.jsp" />
    </body>
</html>
