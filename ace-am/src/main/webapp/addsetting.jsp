<%-- 
    Document   : addsetting
    Created on : Aug 6, 2012, 2:41:40 PM
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
        <script type="text/javascript">
            var counter = 0;

            function addNewSetting() {
                counter++;
                var newFields = document.getElementById('customSetting').cloneNode(true);
                newFields.id = '';
                newFields.style.display = 'block';
                var newField = newFields.childNodes;
                for (var i=0;i<newField.length;i++) {
                    var theName = newField[i].name
                    if (theName)
                        newField[i].name = theName + counter;
                }

                var insertHere = document.getElementById('writeSetting');
                    insertHere.parentNode.insertBefore(newFields,insertHere);
            }

            window.onload = addNewSetting;
        </script>

    </head>
    <body>
        <jsp:include page="header.jsp"/>
        <fieldset id="customSetting" style="display: none">
            <div class="settingsRow">
                <div class="settingsName">Setting Name:</div>
                <div class="settingsVal"><input type=text name="custom-name"></div>
            </div>
            <div class="settingsRow">
                <div class="settingsName">Setting Val:</div>
                <div class="settingsVal"><input type=text name="custom-val"></div>
            </div>
        </fieldset>
        <FORM name="settingsform" METHOD="POST" ENCTYPE="multipart/form-data" ACTION="AddSettings">
            <fieldset id="settingsTable">
                <legend>
                    <h2>Add Settings</h2>
                </legend>
                <span id="writeSetting"></span>
                <input type="button" value="+" onClick="addNewSetting()"/>
            </fieldset>
            <br>
            <input type=submit value="Submit" name="addSettings" class="submitLink" style="margin-left: 5%"/>
            <a href="UpdateSettings" style="font-size: medium; text-decoration: underline;">Cancel</a>
        </FORM>

        <jsp:include page="footer.jsp"/>
    </body>
</html>

