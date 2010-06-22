<%@taglib prefix="json" uri="http://www.atg.com/taglibs/json" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
<%@page contentType="text/plain" pageEncoding="UTF-8"%>
<json:object>
    <json:property name="collection" value="${collection.collection.id}"/>
    <json:property name="count" value="${count}"/>
    <json:property name="next" value="${next}"/>
    
    <json:array name="entries" items="${items}" var="item">
        <json:object>
            <json:property name="id" value="${item.id}"/>
            <json:property name="state" value="${item.state}"/>
            <json:property name="directory" value="${item.directory}"/>
            <json:property name="path" value="${item.path}"/>
            <json:property name="parentPath" value="${item.parentPath}"/>
            <json:property name="lastSeen" value="${item.lastSeen}"/>
            <json:property name="stateChange" value="${item.stateChange}"/>
            <json:property name="lastVisited" value="${item.lastVisited}"/>
            <json:property name="fileDigest" value="${item.fileDigest}"/>
        </json:object>
    </json:array>
</json:object>