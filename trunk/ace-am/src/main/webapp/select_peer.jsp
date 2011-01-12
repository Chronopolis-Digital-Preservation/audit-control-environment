
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
        <title>Assign Peer Collection</title>
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
                /*display:none; */
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
                            document.peerform.remotecollectionid.length=0;
                            if (site == 0 )
                            {
                                document.peerform.remotecollectionid.disabled = true;
                                
                                elem.style.display = "none";
                            }
                            else if (site == (document.peerform.partnerid.length -1))
                                {
                                    window.location="PartnerSite";
                                }
                            else
                            {
                                document.peerform.remotecollectionid.disabled = false;
                            
                                for (i = 0; i < collectionArray[site].length ; i++)
                                {
                                    document.peerform.remotecollectionid[i] = new Option(collectionArray[site][i].name, collectionArray[site][i].id,false,false);
                                }
                                elem.style.display = "block";
                            }
                        }
                        
                        /*function toggleVisible()
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
                            
                        }*/
        </script>
    </head>
    <body>
        <jsp:include page="header.jsp" />
        <div class="standardBody">
            <h3 class="standardHeader" id="compareCollectionHeader">Add audit peer for ${workingCollection.collection.name}</h3>
            
            <FORM name="peerform" METHOD=GET ENCTYPE="multipart/form-data" ACTION="ManagePeer">
                <input  type="hidden" name="collectionid" value="${workingCollection.collection.id}">
                
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
        </div>
        
        <jsp:include page="footer.jsp" />
    </body>
</html>
