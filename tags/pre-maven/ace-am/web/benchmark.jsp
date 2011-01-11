<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%--
The taglib directive below imports the JSTL library. If you uncomment it,
you must also add the JSTL library to the project. The Add Library... action
on Libraries node in Projects view can be used to add the JSTL 1.1 library.
--%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<div class="benchmark">
<table>
<tr><td>Depth of Directories</td><td><input type=text name="depth" value="${driver.settings.depth}"></td></tr>
<tr><td>Directories per level</td><td><input type=text name="dirs" value="${driver.settings.dirs}"></td></tr>
<tr><td>Files per leaf directory</td><td><input type=text name="files" value="${driver.settings.files}"></td></tr>
<tr><td>Generate Fake Data</td><td><input type=checkbox <c:if test="${driver.settings.readFiles}">checked</c:if> name="readFiles" value="true"></td></tr>
<tr><td>File Size</td><td><input type=text name="length" value="${driver.settings.fileLength}"></td></tr>
<tr><td>Block Size</td><td><input type=text name="block_size" value="${driver.settings.blockSize}"></td></tr>
</table>
        
</div>
