<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%--
The taglib directive below imports the JSTL library. If you uncomment it,
you must also add the JSTL library to the project. The Add Library... action
on Libraries node in Projects view can be used to add the JSTL 1.1 library.
--%>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<c:set var="colid">
    <c:choose>
        <c:when test="${param.collectionid != nul && param.collectionid != ''}">${param.collectionid}</c:when>
        <c:when test="${param.collection != nul && param.collection != ''}">${param.collection}</c:when>
        <c:otherwise></c:otherwise>
    </c:choose>
</c:set>
<c:set var="colname">
    <c:choose>
        <c:when test="${workingCollection != null}">${workingCollection.collection.name}</c:when>
        <c:otherwise>${colid}</c:otherwise>
    </c:choose>
</c:set>

<c:set var="pageUri">
    <c:choose>
        <c:when test="${requestScope['javax.servlet.forward.request_uri'] != null}">
            ${requestScope['javax.servlet.forward.request_uri']}
        </c:when>
        <c:otherwise>${pageContext.request.requestURI}</c:otherwise>
    </c:choose>
</c:set>
<c:set var="uriPaths" value="${fn:split(pageUri, '/')}" />
<c:set var="lastPath" value="${uriPaths[fn:length(uriPaths)-1]}" />
<c:set var="pageName">
    <c:choose>
        <c:when test="${fn:indexOf(lastPath, 'Users') >= 0}">User Accounts</c:when>
        <c:when test="${fn:indexOf(lastPath, 'Statistics') >= 0}">Global Ingest Report</c:when>
        <c:when test="${fn:indexOf(lastPath, 'EventLog') >= 0}">Event Log</c:when>
        <c:when test="${fn:indexOf(lastPath, 'UpdateSettings') >= 0}">System Settings</c:when>
        <c:when test="${fn:indexOf(lastPath, 'ManageCollection') >= 0}">Collection Settings</c:when>
        <c:when test="${fn:indexOf(lastPath, 'collectionremove') >= 0}">Remove Collection</c:when>
        <c:when test="${fn:indexOf(lastPath, 'ingest_form') >= 0}">Import Tokens</c:when>
        <c:when test="${fn:indexOf(lastPath, 'ManageFilters') >= 0}">Modify Filters</c:when>
        <c:when test="${fn:indexOf(lastPath, 'ReportConfiguration') >= 0}">Automated Reports</c:when>
        <c:when test="${fn:indexOf(lastPath, 'compare_form') >= 0}">Compare Collection</c:when>
        <c:when test="${fn:indexOf(lastPath, 'ReportDuplicates') >= 0}">Show Duplicate Files</c:when>
        <c:when test="${fn:indexOf(lastPath, 'ViewSummary') >= 0}">Activity Reports</c:when>
        <c:when test="${fn:indexOf(lastPath, '.') > 0}">${fn:substringBefore(lastPath, '.')}</c:when>
        <c:otherwise>${lastPath}</c:otherwise>
    </c:choose>
</c:set>

<ol aria-label="Breadcrumb" class="breadcrumb breadcrumbs-list">
    <li>
        <a class="nav-link" href="${pageContext.servletContext.contextPath}" style="font-size: 15px;">Home</a>
    </li>

    <c:if test="${pageName != 'Status' && colid != '' && collectionUri != null}">
        <li>
            <a class="nav-link" href="${collectionUri}" style="font-size: 15px;">${colname}</a>
        </li>
    </c:if>
    <c:if test="${fn:endsWith(pageContext.servletContext.contextPath, pageName) == false}">
        <li>
            <div class="nav-link" style="color: #666; font-size: 15px;">${pageName}</div>
        </li>
    </c:if>
</ol>
