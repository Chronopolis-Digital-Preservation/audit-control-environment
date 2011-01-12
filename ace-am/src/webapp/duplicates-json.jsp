<%@taglib prefix="json" uri="http://www.atg.com/taglibs/json" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
<%@page contentType="text/plain" pageEncoding="UTF-8"%>
<json:object>
    <json:object name="source">
        <json:property name="id" value="${source.id}"/>
        <json:property name="state" value="${source.state}"/>
        <json:property name="directory" value="${source.directory}"/>
        <json:property name="path" value="${source.path}"/>
        <json:property name="parentPath" value="${source.parentPath}"/>
        <json:property name="lastSeen" value="${source.lastSeen}"/>
        <json:property name="stateChange" value="${source.stateChange}"/>
        <json:property name="lastVisited" value="${parent.lastVisited}"/>
        <json:property name="size" value="${source.size}"/>
    </json:object>
    <json:array name="duplicates" items="${duplicates}" var="item">
        <c:if test="${item.path != source.path}">
            <json:object>
                <json:property name="id" value="${item.id}"/>
                <json:property name="state" value="${item.state}"/>
                <json:property name="directory" value="${item.directory}"/>
                <json:property name="path" value="${item.path}"/>
                <json:property name="parentPath" value="${item.parentPath}"/>
                <json:property name="lastSeen" value="${item.lastSeen}"/>
                <json:property name="stateChange" value="${item.stateChange}"/>
                <json:property name="lastVisited" value="${parent.lastVisited}"/>
                <json:property name="size" value="${source.size}"/>
            </json:object>
        </c:if>
    </json:array>
</json:object>