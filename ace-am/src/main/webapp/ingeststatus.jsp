<%-- 
    Document   : ingestfinish
    Created on : Jun 11, 2012, 4:20:58 PM
    Author     : shake
--%>

<%@page pageEncoding="UTF-8"%>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html
    PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <title>Ingest Store</title>
        <link rel="stylesheet" type="text/css" href="style.css" />
        <script type="text/javascript" src="jquery-1.7.1.min.js">
        </script>
        <script type="text/javascript">
            jQuery(document).ready(function() {
                jQuery(".content").hide();
                //toggle the componenet with class msg_body
                jQuery(".heading").click(function()
                {
                    jQuery(this).next(".content").slideToggle(100);
                });
            });
        </script>
    </head>

    <body>
        <jsp:include page="header.jsp"/>
        <div class="standardBody">
            <h2>Ingestion Status</h2>
            <h3>${results.status}</h3>


            <div class="headingContainer">
            <c:forEach items="${results.ingestedItems}" var="entry">
                <p class="heading">Collection: ${entry.key.name}</p>
                <div class="content">
                    <c:forEach items="${entry.value}" var="what">
                        <li>${what}</li>
                    </c:forEach>
                </div>
            </c:forEach>
            </div>
            <!--
            <div class="headingContainer">
                <p class="heading">Header-1 </p>
                <div class="content">Lorem ipsum dolor sit amet, consectetuer adipiscing elit orem ipsum dolor sit amet, consectetuer adipiscing elit</div>
                <p class="heading">Header-2</p>
                <div class="content">Lorem ipsum dolor sit amet, consectetuer adipiscing elit orem ipsum dolor sit amet, consectetuer adipiscing elitdddd
            </div>
            -->
        <jsp:include page="footer.jsp"/>
    </body>
</html>
