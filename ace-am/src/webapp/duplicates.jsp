
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
        <title>Identical Files</title>
        <link rel="stylesheet" type="text/css" href="style.css" />
        
    </head>
    <body>
        <jsp:include page="header.jsp" />
        <div class="standardBody">
            <c:if test="${source != null}">
                <h3>Identical Files for ${source.path}</h3>
            </c:if>
            Digest: ${digest}
            <ul>
                <c:forEach var="item" items="${duplicates}">
                    <c:if test="${item.path != source.path}">
                        <li>
                            ${item.path}
                        </li>
                    </c:if>
                </c:forEach>
            </ul>
        </div>
        
        <jsp:include page="footer.jsp" />
    </body>
</html>
