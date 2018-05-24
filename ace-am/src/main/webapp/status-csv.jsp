<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@page contentType="text/csv" pageEncoding="UTF-8"%>id,name,group,directory,last sync,storage,check period,proxy enabled,audit tokens,state,total files,total size,total errors,missing tokens,missing files,active files,corrupt files,invalid digests,remote missing files,remote corrupt files
<c:forEach items="${collections}" var="item">${item.collection.id},${item.collection.name},${item.collection.group},${item.collection.directory},${item.collection.lastSync},${item.collection.storage},${item.collection.settings['audit.period']},${item.collection.settings['proxy.data']},${item.collection.settings['audit.tokens']},${item.collection.state},${item.totalFiles},${item.totalSize},${item.totalErrors < 0 ? 0 : item.totalErrors},${item.missingTokens < 0 ? 0 : item.missingTokens},${item.missingFiles < 0 ? 0 : item.missingFiles},${item.activeFiles < 0 ? 0 : item.activeFiles},${item.corruptFiles < 0 ? 0 : item.corruptFiles},${item.invalidDigests < 0 ? 0 : item.invalidDigests},${item.remoteMissing < 0 ? 0 : item.remoteMissing},${item.remoteCorrupt < 0 ? 0 : item.remoteCorrupt}
</c:forEach>