<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <jsp:include page="imports.jsp"/>
    <title>${pageHeader}</title>

    <style type="text/css">
        /**
         * some compatibility issues arise between the older css and bootstrap
         * a few fixes to make everything in line with the rest of ACE
         */
        body {
            width: 980px !important;
            margin-top: 8px !important;
        }

        /**
         * alignment for our button
         */
        .btn {
            margin-left: 0;
        }

        td {
            max-width: 100px;
            overflow: scroll;
            text-overflow: ellipsis;
            white-space: nowrap;
        }

        .form-group-sm {
            width: 150px;
        }

        .results {
            padding: 1rem;
            margin: 1rem -15px;
            background-color: #f7f7f9;
            -ms-overflow-style: -ms-autohiding-scrollbar;
            width: 90%;
            min-height: 120px;
        }
    </style>
</head>
<body>
<jsp:include page="header.jsp"/>
<h1 class="page_header">${pageHeader}</h1>

<div align="center">
    <div id="searchtable">
        <form method="POST" role="form">
            <div class="form-group">
                <select class="custom-select mb-2 mr-sm-2 mb-sm-0" name="truncate" id="truncate">
                    <option selected>Truncate Bytes to...</option>
                    <option value="KiB">KiB</option>
                    <option value="MiB">MiB</option>
                    <option value="GiB">GiB</option>
                    <option value="TiB">TiB</option>
                </select>
            </div>

            <div class="form-inline">
                <div class="form-group form-group-sm">
                    <label for="after-filter">Created After</label>
                    <input type="date" class="form-input" id="after-filter" name="after" placeholder="After"/>
                </div>
                <div class="form-group form-group-sm">
                    <label for="before-filter">Created Before</label>
                    <input type="date" class="form-input" id="before-filter" name="before" placeholder="Before"/>
                </div>
                <div class="form-group form-group-sm">
                    <label for="group-filter">Group Name</label>
                    <input type="text" class="form-input" id="group-filter" name="group" placeholder="Group"/>
                </div>
                <div class="form-group form-group-sm">
                    <label for="coll-filter">Collection Name</label>
                    <input type="text" class="form-input" id="coll-filter" name="collection" placeholder="Collection"/>
                </div>
            </div>

            <div align="left">
                <button type="submit" class="btn btn-primary"
                        value="Submit"><span>Submit</span></button>
                <button type="submit" class="btn btn-primary" name="csv"
                        value="true" style="width: 10rem;"><span>Download As CSV</span></button>
            </div>
        </form>
    </div>

    <div align="center">
      <div class="results">
        <table class="table table-sm">
            <thead>
            <tr>
                <th>Date Ingested</th>
                <th>Collection</th>
                <th>Group</th>
                <th>Total Items</th>
                <th>Size</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="item" items="${summary}">
                <tr>
                    <td title="${item.date}">${item.date}</td>
                    <td title="${item.collection}">${item.collection}</td>
                    <td title="${item.group}">${item.group}</td>
                    <td>${item.count}</td>
                    <td>${item.formattedSize}</td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
      </div>
    </div>
</div>
<jsp:include page="footer.jsp"/>
</body>
</html>
