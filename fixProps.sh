find ./ -name \*.java -exec svn propset svn:eol-style native {} \;
find ./ -name \*.java -exec svn propset svn:keywords "Id Revision" {} \;
