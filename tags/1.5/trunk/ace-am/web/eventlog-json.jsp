<%@taglib prefix="json" uri="http://www.atg.com/taglibs/json" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
<%@page contentType="text/plain" pageEncoding="UTF-8"%>
<json:object>
    <json:property name="start" value="${start}"/>
    <json:property name="count" value="${count}"/>
    <c:if test="${sessionId > 0}">
        <json:property name="session" value="${sessionId}"/>
    </c:if>
    <c:if test="${collection > 0}">
        <json:property name="collection" value="${collection}"/>
    </c:if>
    <c:if test="${logpath != null && logpath ne ''}">
        <json:property name="path" value="${logpath}"/>
    </c:if> 
    <json:array name="filter">
        <c:forEach items="${selects}" var="item">
            <json:property value="${item.key}"/>
        </c:forEach>
    </json:array>
    
    <json:array name="events" items="${loglist}" var="item">
        <json:object>
            <json:property name="id" value="${item.id}"/>
            <json:property name="date" value="${item.date}"/>
            <json:property name="session" value="${item.session}"/>
            <json:property name="type" value="${item.logType}"/>
            <json:property name="description" value="${item.description}"/>
            <c:if test="${item.collection != null}">
                <json:property value="${item.collection.id}" name="collection"/>
            </c:if>
            <c:if test="${item.path != null}">
                <json:property value="${item.path}" name="path"/>
            </c:if>
        </json:object>
    </json:array>
</json:object>