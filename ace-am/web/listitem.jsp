
<%@page pageEncoding="UTF-8"%>
<%--
The taglib directive below imports the JSTL library. If you uncomment it,
you must also add the JSTL library to the project. The Add Library... action
on Libraries node in Projects view can be used to add the JSTL 1.1 library.
--%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<!DOCTYPE html
PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html>
    
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <title>List Item</title>
        <link rel="stylesheet" type="text/css" href="style.css" />
        
    </head>
    <body>
        <jsp:include page="header.jsp" />
        <div class="standardBody">
            <h3>Child Files for ${parent.path}</h3>
            <ul>
                <c:forEach var="item" items="${children}">
                    <li>
                        <c:choose>
                            <c:when test="${item.directory}">
                                <a href="ListItem?collectionid=${item.parentCollection.id}&itempath=${item.path}">${item.path}</a>
                            </c:when>
                            <c:otherwise>
                            ${item.path}
                            </c:otherwise>
                        </c:choose>
                    </li>
                </c:forEach>
            </ul>
        </div>
        
        <jsp:include page="footer.jsp" />
    </body>
</html>
