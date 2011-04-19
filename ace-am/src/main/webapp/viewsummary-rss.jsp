<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@page contentType="text/xml" pageEncoding="UTF-8"%>
<feed xmlns="http://www.w3.org/2005/Atom">
    <c:choose><c:when test="${coll != null}">
            <title>Report Feed for ${coll.name}</title>
            <link href="ViewSummary?collectionid=${coll.id}"/>
        </c:when><c:otherwise>
            <title>ACE Activity Report Feed</title>
            <link href="${request.requestURL}"/>
        </c:otherwise></c:choose>
        <!--<updated>2003-12-13T18:30:02Z</updated>-->
        <author>
            <name>ACE Audit Manager</name>
        </author>
    <c:forEach var="summary" items="${summaries}">
        <entry>
            <title>${summary.reportName}</title>
            <link href="ViewSummary?summaryid=${summary.id}"/>
            <id>${summary.id}</id>
            <updated><fmt:formatDate pattern="yyyy-MM-dd'T'h:m:ssZ" value="${summary.generatedDate}"/></updated>
            <summary>Report Time Span: ${summary.startDate} - ${summary.endDate},<c:forEach items="${summary.summaryItems}" var="item"><c:if test="${!item.logType}">${item.attribute}:  ${item.value}, </c:if></c:forEach></summary>
        </entry>
    </c:forEach>
</feed>