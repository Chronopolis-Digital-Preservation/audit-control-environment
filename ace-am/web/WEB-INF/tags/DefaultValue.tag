
<%@tag description="Tag to evaluate the supplied test and fill in a default if test fails" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute required="true" name="test"%>
<%@attribute required="true" name="success"%>
<%@attribute required="true" name="failure"%>
<c:choose>
    <c:when test="${test}">${success}</c:when>
    <c:otherwise>${failure}</c:otherwise>
</c:choose>