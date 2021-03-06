<%@page pageEncoding="UTF-8" %>
<%--
The taglib directive below imports the JSTL library. If you uncomment it,
you must also add the JSTL library to the project. The Add Library... action
on Libraries node in Projects view can be used to add the JSTL 1.1 library.
--%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="h" %>
<%@taglib uri="/WEB-INF/tlds/monitor" prefix="um" %>


<!DOCTYPE html
PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html>

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>${pageHeader}</title>
    <jsp:include page="imports.jsp"/>

    <style type="text/css">
        body {
            width: 980px !important;
            margin-top: 8px !important;
            padding-right: 0px !important;
        }

        button.btn {
            height: 100%
        }

        a.badge {
            font-size: 95%;
        }

        #summaryTable {
            margin-right: auto;
        }

        .lblTd {
            min-width: 200px;
            white-space: nowrap;
        }

        .dataTd {
            padding-left: 10px;
            padding-right: 10px;
            min-width: 50px;
        }

        #reportTable {
            width: 100%;
            margin: 20px auto 10px;
            border: 1px solid #000000;
            border-collapse: separate;
        }

        #reportTable thead {
            border-bottom: 1px solid #000000;
            background-color: #e8e8e8;
        }

        #navtable {
            width: 100%;
        }

        .datecol {
            width: 150px;
        }

        .tblLinks {
            margin-right: 24px;
        }

        .container {
            width: 94%;
            margin-bottom: 20px;
        }
    </style>
</head>
<body>
<jsp:include page="header.jsp"/>
<h1 class="page_header">${pageHeader}</h1>

<jsp:useBean id="collection" scope="request"
             type="edu.umiacs.ace.monitor.access.CollectionSummaryBean"/>
<div class="container">
  <table id="summaryTable">
    <tr>
        <td class="lblTd">Active Files</td>
        <td class="dataTd">
            <h:DefaultValue test="${collection.activeFiles > -1}"
                            success="${collection.activeFiles}" failure="0"/></td>
        <td class="lblTd">Corrupt Remote Files</td>
        <td class="dataTd"><h:DefaultValue test="${collection.remoteCorrupt > -1}"
                                           success="${collection.remoteCorrupt}" failure="0"/></td>
        <td class="lblTd">Files without Tokens</td>
        <td class="dataTd"><h:DefaultValue test="${collection.missingTokens > -1}"
                                           success="${collection.missingTokens}" failure="0"/></td>
    </tr>
    <tr>
        <td class="lblTd">Missing Files</td>
        <td class="dataTd"><h:DefaultValue test="${collection.missingFiles > -1}"
                                           success="${collection.missingFiles}" failure="0"/>
            <c:if test="${collection.missingFiles > 0}">
                <a class="badge badge-danger text-white"
                   data-toggle="modal" data-target="#mutableModal"
                   data-href="RemoveItem?redirect=Report&collectionid=${collection.collection.id}&type=missing">
                    Remove All
                </a>
            </c:if>
        </td>
        <td class="lblTd">Missing Remote Files</td>
        <td class="dataTd"><h:DefaultValue test="${collection.remoteMissing > -1}"
                                           success="${collection.remoteMissing}" failure="0"/></td>
        <td class="lblTd">Files with possible corrupt digests</td>
        <td class="dataTd"><h:DefaultValue test="${collection.invalidDigests > -1}"
                                           success="${collection.invalidDigests}" failure="0"/></td>
    </tr>
    <tr>
        <td class="lblTd">Corrupt Files</td>
        <td class="dataTd"><h:DefaultValue test="${collection.corruptFiles > -1}"
                                           success="${collection.corruptFiles}" failure="0"/>
            <um:Auth role="Remove Item">
            <c:if test="${collection.corruptFiles > 0}">
                <a class="badge badge-danger text-white"
                   data-toggle="modal" data-target="#mutableModal"
                   data-href="RemoveItem?redirect=Report&collectionid=${collection.collection.id}&type=corrupt">
                    Remove All
                </a>
            </c:if>
            </um:Auth>
        </td>
    </tr>
    <tr>
      <td colspan="6">
        <span class="tblLinks">
            <a href="Report?collectionid=${collection.collection.id}&amp;text=1&amp;count=-1">
                Download List</a></span>
        <um:Auth role="Audit">
        <span class="tblLinks">
            <a href="StartSync?collectionid=${collection.collection.id}&amp;type=corrupt">
                Audit Corrupt Files
            </a>
        </span>
        </um:Auth>
        <um:Auth role="Log">
        <span class="tblLinks">
            <a href="EventLog?sessionId=${session}">Recent Events</a>
        </span>
        </um:Auth>
      </td>
    </tr>
  </table>
</div>

<form method="GET" action="RemoveItem">
    <div class="container">
        <um:Auth role="Remove Item">
        <span>Select All:&nbsp;</span> <input type="checkbox" id="selectall"/>
        </um:Auth>
        <input type="hidden" name="redirect" value="Report"/>
        <input type="hidden" name="collectionid" value="${collection.collection.id}"/>
        <table id="reportTable">
            <thead>
            <tr>
                <um:Auth role="Remove Item"><td>Remove</td></um:Auth>
                <td>State</td>
                <td>Path</td>
                <td>Last Seen</td>
                <td></td>
            </tr>
            </thead>
            <c:forEach var="item" items="${items}">
                <tr>
                    <um:Auth role="Remove Item">
                    <td>
                        <input type="checkbox" name="removal" id="removal" value="${item.id}"/>
                    </td>
                    </um:Auth>
                    <td>
                        <c:choose>
                            <c:when test="${item.stateAsString eq 'C'}">
                                Corrupt
                            </c:when>
                            <c:when test="${item.stateAsString eq 'T'}">
                                Token Missing
                            </c:when>
                            <c:when test="${item.stateAsString eq 'M'}">
                                File Missing
                            </c:when>
                            <c:when test="${item.stateAsString eq 'I'}">
                                Token Corrupt
                            </c:when>
                            <c:when test="${item.stateAsString eq 'P'}">
                                Remote Missing
                            </c:when>
                            <c:when test="${item.stateAsString eq 'D'}">
                                Remote Corrupt
                            </c:when>
                            <c:when test="${item.stateAsString eq 'R'}">
                                File Registered
                            </c:when>
                            <c:otherwise>
                                ${item.state}
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        <c:choose>
                            <c:when test="${item.directory}"><span><img
                                    src="images/folder.jpg"/>${item.path}</span></c:when>
                            <c:otherwise><span><img src="images/file.jpg"
                                                    alt=""/>${fn:replace(item.path, "/", "&#x200B;/")}</span></c:otherwise>
                        </c:choose>
                    </td>
                    <td class="datecol"><span>${fn:replace(item.lastSeen, " ", "&nbsp;")}</span>
                    </td>
                    <td>
                        <um:Auth role="Log">
                        <a class="badge badge-primary text-white"
                           href="EventLog?logpath=${item.path}">log
                        </a>
                        </um:Auth>

                        <!-- TODO: Permissions -->
                        <um:Auth role="Remove Item">
                        <a class="badge badge-danger text-white" data-toggle="modal" data-target="#mutableModal"
                           data-href="RemoveItem?itemid=${item.id}&amp;redirect=Report%3Fcollectionid=${collection.collection.id}">
                            remove
                        </a>
                        </um:Auth>
                    </td>
                </tr>
            </c:forEach>
            <tr>
                <td colspan="5">
                    <table id="navtable">
                        <tr>
                            <td>
                                <a href="Report?top=${items[0].id}&collectionid=${collection.collection.id}&count=${count}">&lt;&lt;</a>
                            </td>
                            <td align="center">Only items with errors are listed</td>
                            <td align="right"><a
                                    href="Report?start=${next}&collectionid=${collection.collection.id}&count=${count}">&gt;&gt;</a>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>

        <!-- Button trigger -->
        <um:Auth role="Remove Item">
        <button type="button" class="btn btn-danger" data-toggle="modal"
                data-target="#formModal" style="width: 25%">
            Remove Selected
        </button>
        </um:Auth>

        <!-- Modal -->
        <um:Auth role="Remove Item">
        <div class="modal fade" id="formModal" tabindex="-1" role="dialog"
             aria-labelledby="formModalLabel" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="formModalLabel">Confirmation</h5>
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                            <span aria-hidden="true">&times;</span>
                        </button>
                    </div>
                    <h6 class="modal-body">
                        Submitting will remove all selected items and their tokens from tracking in
                        ACE.
                        <br><br>
                        Continue?
                    </h6>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-dismiss="modal">
                            Close
                        </button>
                        <button type="submit" class="btn btn-danger">Submit</button>
                    </div>
                </div>
            </div>
        </div>
        </um:Auth>
    </div>
</form>

<!-- A separate modal outside the form to handle the hrefs -->
<um:Auth role="Remove Item">
<div class="modal fade" id="mutableModal" tabindex="-1" role="dialog"
     aria-labelledby="mutableModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="mutableModalLabel">Confirmation</h5>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <h6 class="modal-body">
                Submitting will remove all selected items and their tokens from tracking in ACE.
                <br><br>
                Continue?
            </h6>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">
                    Close
                </button>
                <button type="submit" class="btn btn-danger">Submit</button>
            </div>
        </div>
    </div>
</div>
</um:Auth>

<jsp:include page="footer.jsp"/>
<script type="text/javascript">
    $("#selectall").click(function () {
        $("input[name=removal]").prop('checked', $(this).prop("checked"));
    });

    $('#mutableModal').on('show.bs.modal', function (event) {
        var whatever = $(event.relatedTarget);
        var href = whatever.data('href');

        console.log(whatever);
        console.log(href);

        var modal = $(this);
        modal.find('.btn-danger').attr('onClick', 'location.href="' + href + '"');
    });
</script>
</body>
</html>
