<%@taglib prefix="json" uri="http://www.atg.com/taglibs/json" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
<%@page contentType="text/plain" pageEncoding="UTF-8"%>
<json:object>
    <json:property name="collection" value="${coll.id}"/>
    <json:array name="summaries" items="${summaries}" var="summary">
        <json:object>
            <json:property name="reportName" value="${summary.reportName}"/>
            <json:property name="id" value="${summary.id}"/>
            <json:property name="collection" value="${summary.collection.id}"/>
            <json:property name="collectionName" value="${summary.collection.name}"/>
            <json:property name="start" value="${summary.startDate}"/>
            <json:property name="end" value="${summary.endDate}"/>
            <json:object name="collectionState">
                <c:forEach items="${summary.summaryItems}" var="item">
                    <c:if test="${!item.logType}">
                        <json:property name="${item.attribute}" value="${item.value}"/>
                    </c:if>
                </c:forEach> 
            </json:object>
            <json:object name="logSummary">
                <c:forEach items="${summary.summaryItems}" var="item">
                    <c:if test="${item.logType}">
                        <json:property name="${item.attribute}" value="${item.value}"/>
                    </c:if>
                </c:forEach> 
            </json:object>
        </json:object>
    </json:array>
</json:object>
