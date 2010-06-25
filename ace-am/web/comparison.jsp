
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
        <c:if test="${results.running}">
            <meta http-equiv="refresh" content="5;URL=comparison.jsp"  >
        </c:if>
        
        <title>Comparison results</title>
        <link rel="stylesheet" type="text/css" href="style.css" />
        <style type="text/css">
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
            
        </style>
    </head>
    <body>
        <jsp:include page="header.jsp" />
        <c:choose>
            <c:when test="${results.running}">
                <h3>Processing collection</h3><BR>
                <pre>${results.message}</pre>
            </c:when>
            <c:otherwise>
                <div class="standardBody">
                    <h3>Collection Differences</h3>
                <pre>${results.message}</pre>
                    
                    <h4>Files in original collection, but not in supplied</h4>
                    <ul>
                        <c:forEach items="${results.unseenTargetFiles}" var="item" >
                            <li> ${item} </li>
                        </c:forEach>
                    </ul>
                    
                    <h4>Files in supplied file, but not original collection</h4>
                    <ul>
                        <c:forEach items="${results.unseenSuppliedFiles}" var="item" >
                            <li> ${item} </li>
                        </c:forEach>
                    </ul>
                    
                    <h4>Files with different names, but same digests</h4>
                    <c:forEach items="${results.differingNames}" var="item" >
                        <ul>
                            <li>Hash: ${item.digest}</li>
                            <ul>
                                <li>Supplied:  ${item.destinationName}</li>
                                <li>Collection: ${item.sourceName}</li>
                            </ul>
                        </ul>
                    </c:forEach>
                    
                    <h4>Files with same names, but different digests</h4>
                    <c:forEach items="${results.differingDigests}" var="item" >
                        <ul>
                            <li>Name: ${item.name}</li>
                            <ul>
                                <li>Collection: ${item.targetDigest}</li>
                                <li>Supplied: ${item.sourceDigest}</li>
                            </ul>
                            
                        </ul>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
        
        <jsp:include page="footer.jsp" />
    </body>
</html>
