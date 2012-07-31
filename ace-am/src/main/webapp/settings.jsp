<%-- 
    Document   : settings
    Created on : Jul 30, 2012, 4:07:16 PM
    Author     : shake
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>System Settings</title>
        <link rel="stylesheet" type="text/css" href="style.css" />
    </head>
    <body>
        <jsp:include page="header.jsp"/>
        <h1>System Settings</h1>
        <FORM name="settingsform" METHOD="POST" ENCTYPE="multipart/form-data" ACTION="UpdateSettings">
            <h3>Mail Server: <input type=text name="mail.server"> </h3>
            <h3>Mail From: <input type=text name="mail.from"> </h3>
            <h3>Max Audit: <input type=text name="throttle.maxaudit"></h3>
            <h3>Audit Wait Time: <input type=text name="throttle.wait"></h3>
            <h3>Audit BPS: <input type=text name="throttle.bps"></h3>
            <h3>Auto Audit: <input type=text name="auto.audit.disable"></h3>
            <h3>IMS Host: <input type=text name="ims"></h3>
            <h3>User Management: <input type=text name="auth.management"></h3>
            <h3>Log Location: <input type=text name="log4j.appender.A1.file"></h3>
            <h3>Log Type: <input type=text name="log4j.appender.A1"></h3>
            <h3>Log File Size: <input type=text name="log4j.appender.A1.maxFileSize"></h3>
            <h3>Log Backup Index: <input type=text name="log4j.appender.A1.maxBackupIndex"></h3>
            <h3>Root Logger: <input type=text name="log4j.rootLogger"></h3>
            <h3>Pattern Layout: <input type=text name="log4j.appender.A1.layout"></h3>
            <h3>Conversion Pattern: <input type=text name="log4j.appender.A1.layout.ConversionPattern"></h3>
            <h3>IRods: <input type=text name="log4j.logger.edu.umiacs.irods"></h3>
            <h3>umiacs: <input type=text name="log4j.logger.edu.umiacs"></h3>
            <h3>IRods Connection: <input type=text name="irods.connection"></h3>

            <h3><input type=submit value="Submit" class="submitLink"> <a href="Status">Cancel</a></h3>
        </FORM>

        <jsp:include page="footer.jsp"/>
    </body>
</html>
