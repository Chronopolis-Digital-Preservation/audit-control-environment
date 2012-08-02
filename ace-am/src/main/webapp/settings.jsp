<%-- 
    Document   : settings
    Created on : Jul 30, 2012, 4:07:16 PM
    Author     : shake
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
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
            <div id="settingstable">
                <table border="0">
                    <tr>
                        <td>Mail Server:</td>
                        <td><input type=text name="mail.server" value="${currSettings['mail.server']}"></td>
                        <td><img src="images/help.png" title="Mail server to use when mailing reports. You will need to set this if you want reports to be mailed properly."></td>
                    </tr>
                    <tr>
                        <td>Mail From:</td>
                        <td><input type=text name="mail.from" value="${currSettings['mail.from']}"></td>
                        <td><img src="images/help.png" title="Set this e-mail address to an address e-mail should originate from"></td>
                    </tr>
                    <tr>
                        <td>Max Audit:</td>
                        <td><input type=text name="throttle.maxaudit" value="${currSettings['throttle.maxaudit']}"></td>
                        <td><img src="images/help.png" title="Max number of running audits"></td>
                    </tr>
                    <tr>
                        <td>Audit Wait Time:</td>
                        <td><input type=text name="throttle.wait" value="${currSettings['throttle.wait']}"></td>
                        <td><img src="images/help.png" title="Minimum time between srb file reads in milliseconds"></td>
                    </tr>
                    <tr>
                        <td>Audit BPS:</td>
                        <td><input type=text name="throttle.bps" value="${currSettings['throttle.bps']}"></td>
                        <td><img src="images/help.png" title="Maximum bytes per second per running audit, default = 0 = unlimited"></td>
                    </tr>
                    <tr>
                        <td>Auto Audit:</td>
                        <td><input type=text name="auto.audit.disable" value="${currSettings['auto.audit.disable']}"></td>
                        <td><img src="images/help.png" title="Start automated auditing"></td>
                    </tr>
                    <tr>
                        <td>IMS Host:</td>
                        <td><input type=text name="ims" value="${currSettings['ims']}"></td>
                        <td><img src="images/help.png" title="IMS hostname to use. Unless you deployed your own IMS, this should probably not be changed"></td>
                    </tr>
                    <tr>
                        <td>User Management:</td>
                        <td><input type=text name="auth.management" value="${currSettings['auth.management']}"></td>
                        <td><img src="images/help.png" title="Set this to true to disable internal user management. This should only be used in conjunction with changes to the Authentication realm listed above."></td>
                    </tr>
                    <tr>
                        <td>Log Location:</td>
                        <td><input type=text name="log4j.appender.A1.File" value="${currSettings['log4j.appender.A1.File']}"></td>
                        <td><img src="images/help.png" title="The location of your logfile"></td>
                    </tr>
                    <c:choose>
                        <c:when test="${fileAppender}">
                            <tr>
                                <td>Log Type:</td>
                                <td><input type=text name="log4j.appender.A1" value="${currSettings['log4j.appender.A1']}"></td>
                                <td><img src="images/help.png"></td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td>Log Type:</td>
                                <td><input type=text name="log4j.appender.A1" value="${currSettings['log4j.appender.A1']}"></td>
                                <td><img src="images/help.png"></td>
                            </tr>
                            <tr>
                                <td>Log File Size:</td>
                                <td><input type=text name="log4j.appender.A1.maxFileSize" value="${currSettings['log4j.appender.A1.maxFileSize']}"></td>
                                <td><img src="images/help.png" title="File size of your log"></td>
                            </tr>
                            <tr>
                                <td>Log Backup Index:</td>
                                <td><input type=text name="log4j.appender.A1.maxBackupIndex" value="${currSettings['log4j.appender.A1.maxBackupIndex']}"></td>
                                <td><img src="images/help.png"></td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                    <tr></tr>
                    <tr>
                        <td>Generic logging properties </td>
                    </tr>
                    <tr>
                        <td> change only if you know what you are doing</td>
                    </tr>

                    <tr>
                        <td>Root Logger:</td>
                        <td><input type=text name="log4j.rootLogger" value="${currSettings['log4j.rootLogger']}"></td>
                        <td><img src="images/help.png"></td>
                    </tr>
                    <tr>
                        <td>Pattern Layout:</td>
                        <td><input type=text name="log4j.appender.A1.layout" value="${currSettings['log4j.appender.A1.layout']}"></td>
                        <td><img src="images/help.png"></td>
                    </tr>
                    <tr>
                        <td>Conversion Pattern:</td>
                        <td><input type=text name="log4j.appender.A1.layout.ConversionPattern" value="${currSettings['log4j.appender.A1.layout.ConversionPattern']}"></td>
                        <td><img src="images/help.png"></td>
                    </tr>
                    <tr>
                        <td>IRods:</td>
                        <td><input type=text name="log4j.logger.edu.umiacs.irods" value="${currSettings['log4j.logger.edu.umiacs.irods']}"></td>
                        <td><img src="images/help.png"></td>
                    </tr>
                    <tr>
                        <td>umiacs:</td>
                        <td><input type=text name="log4j.logger.edu.umiacs" value="${currSettings['log4j.logger.edu.umiacs']}"></td>
                        <td><img src="images/help.png"></td>
                    </tr>
                    <tr>
                        <td>IRods Connection:</td>
                        <td><input type=text name="irods.connection" value="${currSettings['irods.connection']}"></td>
                        <td><img src="images/help.png"></td>
                    </tr>
                </table>
            </div>

            <input type=submit value="Submit" name="update" class="submitLink" style="margin-left: 50px;"> <input type=submit value="Default" name="default" class="submitLink"> <a href="Status">Cancel</a>
        </FORM>

        <jsp:include page="footer.jsp"/>
    </body>
</html>
