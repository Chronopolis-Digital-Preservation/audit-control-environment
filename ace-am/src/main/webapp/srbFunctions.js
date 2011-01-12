

function popupBoxLinkClicked(inputBox,popupBox,value) {
    document.getElementById(inputBox).value=value;
    Hide(popupBox);
}

function Hide(target) {
    document.getElementById(target).style.display="none";
}

function loadUrlFrame(url, target) {
    
    var doc = document.getElementById(target).contentDocument;
    if (doc == undefined || doc == null)
        doc = document.getElementById(target).contentWindow.document;
    doc.open();
    doc.write( 'Fetching data...' );
    doc.close();
    
    
    //document.getElementById(target).innerHTML = ' Fetching data...';
    if (window.XMLHttpRequest) {
        req = new XMLHttpRequest();
    } 
    else if (window.ActiveXObject) {
        req = new ActiveXObject("Microsoft.XMLHTTP");
    }
    if (req != undefined) {
        req.onreadystatechange = function() {
            updateFrame(url, target);
        };
        req.open("GET", url, true);
        req.send("");
    }
}  

function loadUrlDiv(url, target) {
    
    document.getElementById(target).innerHTML = ' Fetching data...';
    if (window.XMLHttpRequest) {
        req = new XMLHttpRequest();
    } 
    else if (window.ActiveXObject) {
        req = new ActiveXObject("Microsoft.XMLHTTP");
    }
    if (req != undefined) {
        req.onreadystatechange = function() {
            updateDiv(url, target);
        };
        req.open("GET", url, true);
        req.send("");
    }
}  

function updateFrame(url, target) {
    if (req.readyState == 4) {
        
        var doc = document.getElementById(target).contentDocument;
        if (doc == undefined || doc == null)
            doc = document.getElementById(target).contentWindow.document;
        doc.open();
        
        if (req.status == 200) {
            //document.getElementById(target).innerHTML = req.responseText;
            doc.write(req.responseText);
        } 
        else {
            //document.getElementById(target).innerHTML=" Could not load SRB information:\n"+ req.status + "\n" +req.statusText;
            doc.write( "Could not load information:\n"+ req.status + "\n" +req.statusText);
        }
        doc.close();
    }
}

function updateDiv(url, target) {
    if (req.readyState == 4) {
        
        if (req.status == 200) {
            document.getElementById(target).innerHTML = req.responseText;
        } 
        else {
            document.getElementById(target).innerHTML=" Could not load information:\n"+ req.status + "\n" +req.statusText;
        }
        
    }
}

// Correctly handle PNG transparency in Win IE 5.5 or higher.
// http://homepage.ntlworld.com/bobosola. Updated 02-March-2004

window.onload = function() 
   {
   for(var i=0; i<document.images.length; i++)
      {
          var img = document.images[i]
          var imgName = img.src.toUpperCase()
          if (imgName.substring(imgName.length-3, imgName.length) == "PNG")
             {
                 var imgID = (img.id) ? "id='" + img.id + "' " : ""
                 var imgClass = (img.className) ? "class='" + img.className + "' " : ""
                 var imgTitle = (img.title) ? "title='" + img.title + "' " : "title='" + img.alt + "' "
                 var imgStyle = "display:inline-block;" + img.style.cssText 
                 if (img.align == "left") imgStyle = "float:left;" + imgStyle
                 if (img.align == "right") imgStyle = "float:right;" + imgStyle
                 if (img.parentElement.href) imgStyle = "cursor:hand;" + imgStyle
                 var strNewHTML = "<span " + imgID + imgClass + imgTitle
                 + " style=\"" + "width:" + img.width + "px; height:" + img.height + "px;" + imgStyle + ";"
             + "filter:progid:DXImageTransform.Microsoft.AlphaImageLoader"
                 + "(src=\'" + img.src + "\', sizingMethod='scale');\"></span>" 
                 img.outerHTML = strNewHTML
                 i = i-1
             }
      }
   }



function rollover(id) 
    {
        row = document.getElementById(id);
        row.className='rollover';
    }

function rollout(id) 
    {
        row = document.getElementById(id);
        row.className=' ';
    }
function display(id)
    {
        row = document.getElementById(id);
        row.style.display='block';
    }