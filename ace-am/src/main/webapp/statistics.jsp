<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <jsp:include page="imports.jsp"/>
    <title>Stats for Nerds</title>

    <style type="text/css">
        .form-group-sm {
            width: 150px;
        }

        .box {
            margin-left: auto;
            margin-right: auto;
            margin-top: 12px;
            margin-bottom: 10px;
            width: 720px;
            border-top: 1px solid #000000;
            border-left: 1px solid #000000;
            border-right: 1px solid #000000;
            border-bottom: 1px solid #000000;
        }
    </style>
</head>
<body>
<jsp:include page="header.jsp"/>
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

            <div class="form-group row">
                <div class="col-sm-10">
                    <div class="form-check">
                        <label class="form-check-label">
                            <input class="form-check-input" name="csv" type="checkbox"> Export to CSV
                        </label>
                    </div>
                </div>
            </div>

            <div class="form-group">
                <button type="submit" class="btn btn-sm"
                        value="Submit"><span>Submit</span></button>
            </div>
        </form>
    </div>

    <div class="box">
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