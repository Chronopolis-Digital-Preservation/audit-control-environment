<%-- 
    Document   : managefilters2
    Created on : Jan 29, 2013, 3:13:13 PM
    Author     : shake
--%>

<%@page pageEncoding="UTF-8"%>
<%--
The taglib directive below imports the JSTL library. If you uncomment it,
you must also add the JSTL library to the project. The Add Library... action
on Libraries node in Projects view can be used to add the JSTL 1.1 library.
--%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<!DOCTYPE html
PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html>

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <title>Modify Collection Filters</title>
        <link rel="stylesheet" type="text/css" href="style.css" />
        <script type="text/javascript" src="jquery-1.7.1.min.js"></script>
        <script type="text/javascript">
            var counter = 0;
            var $regval = $(
                '<div name="item-0">' +
                '<input class="regexVal" name="regex" value=""/>' +
                '<input type="radio" name="affected" value="1" /> Files' +
                '<input type="radio" name="affected" value="2" /> Directories' +
                '<input type="radio" name="affected" value="3" /> Both' +
                '<img src="images/file-bad.png" style="margin-left: 1%; cursor: hand; cursor: pointer;"/>' +
                '<br/>' +
                '</div>'
            );

            function addRegexItem(re, affected) {
                var re = re || '';
                var affected = affected || 1;
                var $item = $regval.clone();
                $item.attr('class', 'item-'+ ++counter);
                $item.children("input[name=regex]").attr({'name': 'regex-'+counter, 'value':re});
                $item.children("input[name=affected]").attr('name', 'affected-'+counter);
                $item.children("input[value="+affected+"]").attr('checked', 'checked');
                $("div.list").append($item)
            }

            $(document).ready(function() {
                $("input[name=add]").click(function() {
                    addRegexItem();
                });

                $("img").live('click' ,function () {
                    $(this).parent().remove();
                });
            });

        </script>
    </head>
    <body>
        <jsp:include page="header.jsp" />
        <div class="standardBody">

            <h3>Filters for files in '${collection.name}'</h3>
            <h4> 
                <span style="width: 25%">Regular Expression</span>
                <span style="margin-left: 11%">Affected Types </span>
            </h4>
            <form method=post action="ManageFilters">
                <input type=hidden name="collectionid" value="${collection.id}"/>
                <div class="list">
                </div>

                <script type="text/javascript">
                <c:forEach var="re" items="${regexlist}" >
                        addRegexItem('${re.regex}', '${re.affectedItem}');
                </c:forEach>
                </script>
                <input type="button" name="add" value="Add New Filter" class="submitLink" style="font-size: small; padding: 5px; margin-left: -1%; text-decoration: none"/>
                <br/><br/>
                <input type="submit" name="modify" value="Save" class="submitLink" style="margin-left: -1%"/>
            </form>
            <br/>File patterns follow the java regular expression syntax described on <a href="http://java.sun.com/docs/books/tutorial/essential/regex/">Sun's website</a>
            <br/>
                Some basic file patterns
                <dl>
                    <dt/>Ignore all files containing 'test'
                    <dd/><span class="regex">.*test.*</span><br/>Will match ddtest, test, testdd, and ddtestdd
                    <dt/>Ignore files named '.DS_Store' (useful on a OS-X)
                    <dd/><span class="regex">\.DS_Store</span> <br/> will match .DS_Store, but not DS_Store or .ds_store
                    <dt/>Only include files that contain .jpg or .JPG
                    <dd/>
                    </dl>
        </div>

        </div>

        <jsp:include page="footer.jsp" />
    </body>
</html>
