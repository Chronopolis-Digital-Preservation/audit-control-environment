
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
        <script type="text/javascript">
            var next = ${next};
            function addNewRow(tableRef){
                var myTable = document.getElementById(tableRef);
                var tBody = myTable.getElementsByTagName('tbody')[0];
                var newTR = document.createElement('tr');
                newTR.id = 'tr'+next;
                
                var td1 = document.createElement('td');
                td1.innerHTML = '<input type="text" name="regex' + next +'>';
                
                var td2 = document.createElement('td');
                td2.innerHTML = '<input type=radio name="type'+next+'" value="1" checked> Files<br> <input type=radio name="type'+next+'" value="2"> Directories<br><input type=radio name="type'+next+'" value="3"> Both';
                
                var td3 = document.createElement('td');
                td3.innerHTML = '<a href="#" onclick="removeElement(\'tr' + next + '\')">Remove</a>';
                
                newTR.appendChild (td1);
                newTR.appendChild (td2);
                newTR.appendChild (td3);
                tBody.appendChild(newTR);
                next++;
            } 
            function removeElement(trNum) {
                var child = document.getElementById(trNum);
                child.parentNode.removeChild(child);
                
            }
        </script>
        <style type="text/css">
            
            #exp {
                width: 200px;
            }
            #apply {
                width: 100px;
            }
            #removecol {
                width: 75px;
            }
            .regex {
                font-size: large
            }
        </style>
    </head>
    <body>
        <jsp:include page="header.jsp" />
        <div class="standardBody">
            
            <h3>Filters for files in '${collection.name}'</h3>
            <br>
            <c:if test="${error}">
                There are errors in the listed filters therefore changes were not saved.<br>
            </c:if>
            <form action="ManageFilters" method="post">
                
                Sample file to ignore: <input type="text" name="teststring" value="${teststring}">
                <br><br>
                <input type=hidden name="collectionid" value="${collection.id}">
                <table id="regexTable">
                    <thead>
                        <tr>
                            <td id="exp">File pattern</td>
                            <td id="apply">Apply to</td>
                            <td id="removecol"></td>
                            <c:if test="${teststring != null}">
                                <td id="match">Will file be ignored?</td>
                            </c:if>
                        </tr>
                    </thead>
                    <tbody >
                        <c:set var="count" value="0"/>
                        <c:forEach var="item" items="${regexlist}">
                            <c:set var="filecheck" value=""/>
                            <c:set var="dircheck" value=""/>
                            <c:set var="bothcheck" value=""/>
                            
                            <c:choose>
                                <c:when test="${item.affectedItem == 1}">
                                    <c:set var="filecheck" value="checked"/>
                                </c:when>
                                <c:when test="${item.affectedItem == 2}">
                                    <c:set var="dircheck" value="checked"/>
                                </c:when>
                                <c:when test="${item.affectedItem == 3}">
                                    <c:set var="bothcheck" value="checked"/>
                                </c:when>
                            </c:choose>
                            <tr id="tr${count}">
                                <td><input type="text" name="regex${count}" value="${item.regex}"><br>${item.errorMessage}</td>
                                <td>
                                
                                <input type=radio name="type${count}" value="1" ${filecheck}>Files<br>
                                <input type=radio name="type${count}" value="2" ${dircheck}>Directories<br>
                                <input type=radio name="type${count}" value="3" ${bothcheck}>Both
                                       </td>
                                <td><a href="#" onclick="removeElement('tr${count}')">Remove</a></td>
                                <c:if test="${teststring != null}"><td>${item.matchesTest}</td></c:if>
                            </tr>
                            <c:set var="count" value="${count + 1}"/>
                        </c:forEach>
                    </tbody>
                    <tfoot><tr><td><a href="#" onclick="addNewRow('regexTable')">Add New Filter</a>&nbsp;&nbsp;<input type="submit" name="modify" value="Save"></td></tr></tfoot>
                </table>
            </form>
            <br>File patterns follow the java regular expression syntax described on <a href="http://java.sun.com/docs/books/tutorial/essential/regex/">Sun's website</a>
            <br>
                Some basic file patterns
                <dl>
                    <dt>Ignore all files containing 'test'
                    <dd><span class="regex">.*test.*</span><br>Will match ddtest, test, testdd, and ddtestdd
                    <dt>Ignore files named '.DS_Store' (useful on a OS-X)
                    <dd><span class="regex">\.DS_Store</span> <br> will match .DS_Store, but not DS_Store or .ds_store
                    <dt>Only include files that contain .jpg or .JPG
                    <dd>
                    </dl>
        </div>
        
        <jsp:include page="footer.jsp" />
    </body>
</html>
