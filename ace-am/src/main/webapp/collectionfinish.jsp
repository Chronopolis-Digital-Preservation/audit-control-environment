<%-- 
Document   : resourcefinish
Created on : Nov 2, 2007, 2:00:03 PM
Author     : toaster
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Collection Updated</title>
        <link rel="stylesheet" type="text/css" href="style.css" />
    </head>
    <body>
        <jsp:include page="header.jsp"/>
        <div class="standardBody">
            <h3>Collection Modified/Saved</h3>
            <table >
                <thead>
                    <tr>
                        <th></th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>Name</td>
                        <td>${collection.name}</td>
                    </tr>
                    <tr>
                        <td>Source Directory</td>
                        <td>${collection.directory}</td>
                    </tr>
                    <tr>
                        <td>State</td>
                        <td>${collection.state}</td>
                    </tr>
                    <tr>
                        <td><a href="Status">Back</a></td>
                    </tr>
                </tbody>
            </table>
            
        </div>
        <jsp:include page="footer.jsp"/>
    </body>
</html>
