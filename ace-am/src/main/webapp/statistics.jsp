<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <jsp:include page="imports.jsp"/>
    <title>Statistics</title>

    <style type="text/css">
        /**
         * some compatibility issues arise between the older css and bootstrap
         * a few fixes to make everything in line with the rest of ACE
         */
        body {
            width: 752px !important;
            margin-top: 8px !important;
        }

        /**
         * alignment for our button
         */
        .btn {
            margin-left: 0;
        }

        .form-group-sm {
            width: 150px;
        }

        .results {
            padding: 1rem;
            margin: 1rem -15px;
            background-color: #f7f7f9;
            -ms-overflow-style: -ms-autohiding-scrollbar;
        }
    </style>
</head>
<body>
<jsp:include page="header.jsp"/>
<h3 style="text-align: center;">Ingestion Statistics</h3>
<div class="container">
    <div id="searchtable">
        <form method="POST" role="form">
            <div class="form-inline">
                <div class="form-group form-group-sm">
                    <input type="text" class="form-input" id="before-filter" name="before" placeholder="Before"/>
                </div>
                <div class="form-group form-group-sm">
                    <input type="text" class="form-input" id="after-filter" name="after" placeholder="After"/>
                </div>
                <div class="form-group form-group-sm">
                    <input type="text" class="form-input" id="group-filter" name="group" placeholder="Group"/>
                </div>
                <div class="form-group form-group-sm">
                    <input type="text" class="form-input" id="coll-filter" name="collection" placeholder="Collection"/>
                </div>
            </div>

            <div class="form-inline">
                <div class="form-check">
                    <label class="form-check-label">
                        <input class="form-check-input" name="csv" type="checkbox"> Export to CSV
                    </label>
                </div>

            </div>

            <button type="submit" class="btn btn-primary"
                    value="Submit"><span>Submit</span></button>
        </form>
    </div>

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
                    <td>${item.date}</td>
                    <td>${item.collection}</td>
                    <td>${item.group}</td>
                    <td>${item.count}</td>
                    <td>${item.size}</td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</div>
<jsp:include page="footer.jsp"/>
</body>
</html>
