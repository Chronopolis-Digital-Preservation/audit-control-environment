<%@page pageEncoding="UTF-8" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<!DOCTYPE html
PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html>

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <c:if test="${results.running}">
        <meta http-equiv="refresh" content="5;URL=comparison.jsp">
    </c:if>

    <title>Comparison results</title>
    <link rel="stylesheet" type="text/css" href="style.css"/>
</head>
<body>
<jsp:include page="header.jsp"/>
<c:choose>
    <c:when test="${results.running}">
        <h3>Processing collection</h3><BR>
        <pre>${results.message}</pre>
    </c:when>
    <c:otherwise>
        <div class="standardBody">
            <h3>Collection Differences</h3>
            <pre>${results.message}</pre>

            <h4>Files monitored here, but not in supplied/remote
                (${results.unseenTargetFilesSize})</h4>
            <ul>
                <c:forEach items="${results.unseenTargetFiles}" var="item">
                    <li> ${item} </li>
                </c:forEach>
            </ul>

            <h4>Files in supplied/remote, but not monitored here (${results.unseenSuppliedFilesSize})</h4>
            <ul>
                <c:forEach items="${results.unseenSuppliedFiles}" var="item">
                    <li> ${item} </li>
                </c:forEach>
            </ul>

            <h4>Files with different names, but same digests (${results.differingNamesSize})</h4>
            <c:forEach items="${results.differingNames}" var="item">
                <ul>
                    <li>Hash: ${item.digest}
                        <ul>
                            <li>Local File: ${item.sourceName}</li>
                            <li>Compare File: ${item.destinationName}</li>
                        </ul>
                    </li>
                </ul>
            </c:forEach>

            <h4>Files with same names, but different digests (${results.differingDigestsSize})</h4>
            <c:forEach items="${results.differingDigests}" var="item">
                <ul>
                    <li>Name: ${item.name}
                        <ul>
                            <li>Local File: ${item.sourceDigest}</li>
                            <li>Compare File: ${item.targetDigest}</li>
                        </ul>
                    </li>
                </ul>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>

<jsp:include page="footer.jsp"/>
</body>
</html>
