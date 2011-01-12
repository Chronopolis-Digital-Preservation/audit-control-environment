
<%@page pageEncoding="UTF-8"%>
<%--
The taglib directive below imports the JSTL library. If you uncomment it,
you must also add the JSTL library to the project. The Add Library... action
on Libraries node in Projects view can be used to add the JSTL 1.1 library.
--%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:if test="${workingCollection == null}">
    <c:redirect url="Status"/>
</c:if>

<!DOCTYPE html
PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html>
    
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <title>Compare Collection</title>
        <link rel="stylesheet" type="text/css" href="style.css" />
        <style>
            #compareOr {
                text-align: center;
                margin-top: 15px;
                margin-bottom: 15px;
            }
            
            #compareInputFile {
                margin-top: 15px;
            }
            
            #compareSubmitButton {
                margin-top: 15px;
            }
            
            #compareToSite {
                margin-top: 15px;
                display:none; 
            }
            
            #collectionSelection {
                display: none;
            }
        </style>
        <script type="text/javascript">
            var collectionArray = new Array();
            
            <c:forEach var="partner" items="${partnerList.sites}" varStatus="pstatus">
                var partnerArray = [];
                collectionArray[${pstatus.index + 1}] = partnerArray;
                <c:forEach var="collection" items="${partner.collections}" varStatus="cstatus">
                        partnerArray[${cstatus.index}] = {'id':${collection.id}, 'name': '${collection.group} ${collection.name}'};
                </c:forEach>
            </c:forEach>
                
                        function setOptions(site)
                        {
                            var elem = document.getElementById('collectionSelection');
                            document.compareform.remotecollectionid.length=0;
                            if (site == 0 )
                            {
                                document.compareform.remotecollectionid.disabled = true;
                                
                                elem.style.display = "none";
                            }
                            else if (site == (document.compareform.partnerid.length -1))
                                {
                                    window.location="PartnerSite";
                                }
                            else
                            {
                                document.compareform.remotecollectionid.disabled = false;
                            
                                for (i = 0; i < collectionArray[site].length ; i++)
                                {
                                    document.compareform.remotecollectionid[i] = new Option(collectionArray[site][i].name, collectionArray[site][i].id,false,false);
                                }
                                elem.style.display = "block";
                            }
                        }
                        
                        function toggleVisible()
                        {
                            
                               
                            var elem;
                            if ( document.compareform.source[0].checked)
                            {
                                elem = document.getElementById('compareInputFile');
                                elem.style.display = "block";
                                elem = document.getElementById('compareToSite');
                                elem.style.display = "none";
                            }
                            else
                            {
                                elem = document.getElementById('compareInputFile');
                                elem.style.display = "none";
                                elem = document.getElementById('compareToSite');
                                elem.style.display = "block";     
                            }
                            
                        }
        </script>
    </head>
    <body>
        <jsp:include page="header.jsp" />
        <div class="standardBody">
            <h3 class="standardHeader" id="compareCollectionHeader">Report Collection Differences for ${workingCollection.collection.name}</h3>
            
            <FORM name="compareform" METHOD=POST ENCTYPE="multipart/form-data" ACTION="Compare">
                <input  type="hidden" name="collectionid" value="${workingCollection.collection.id}">
                Compare collection to: <input checked onchange="toggleVisible()" type="radio" name="source" value="upload">Uploaded File
                <input onchange="toggleVisible()" type="radio" name="source" value="partner">Partner
                
                <div id="compareInputFile">
                    Select file to compare collection against: <INPUT TYPE=FILE NAME="upfile">
                </div>
                
                <div id="compareToSite">1. Select Partner Site: 
                    <select name="partnerid"  onchange="setOptions(this.selectedIndex)">
                        <option selected>Select Site</option>
                        <c:forEach var="item" items="${partnerList.sites}">
                            <option value="${item.id}">${item.remoteURL}</option>
                        </c:forEach>
                        <option>Add New</option>
                    </select>
                    <div id="collectionSelection">
                    2. Select Collection: 
                    <select name="remotecollectionid" disabled>
                        
                    </select>
                </div>
                </div>
                <div id="compareSubmitButton"><INPUT TYPE=SUBMIT VALUE="Submit"></div>
            </FORM>
            <BR><BR>
            <fieldset>
                <legend>Format of file</legend>
                Comparison file must contain sha-256 digest followed by a tab(not spaces) 
                and the full file path. The file generated from the 'Download Digests' function
                is in the correct format. The following is a sample of what the file should look like.
                <pre>
ff8f6fef0caafaef518cd1dca791ad4b0488a92e8ece3aa23a2bc1a74b50268a  /pawn-0.6/README.txt
9dcc24918ce1317e18a6da3d0fe6ce4d0c18ec0b3992f58fca1f27fe488ae5ad  /pawn-0.6/dir-wcprops
7de1555df0c2700329e815b93b32c571c3ea54dc967b89e81ab73b9972b72d1d  /pawn-0.6/format
6c5951f80bd989768fd144ed01969647fbc804f66dbad284a4a220ebbbff9045  /pawn-0.6/entries
e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855  /pawn-0.6/empty-file
1f6ffb5171ecc69ae415ed3ae2dec75aef58e563deb3633027e97d40015d444e  /lib/xsdlib.jar
                </pre>
            </fieldset>
        </div>
        
        <jsp:include page="footer.jsp" />
    </body>
</html>
