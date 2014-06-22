#!/bin/bash

cd "$(dirname "$0")"

(cd android-smart-image-view; ant jar)
(cd ../libs;
wget https://github.com/loopj/android-async-http/raw/master/releases/android-async-http-1.4.4.jar)
(cd commons-io; ant jar)
(cd commons-lang; ant jar)
(cd scribe-java; mvn source:jar)
#(cd jackson-core; mvn source:jar)
#(cd jackson-databind; mvn source:jar)
(cd ../libs
 wget http://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.4.0/jackson-databind-2.4.0.jar
 wget http://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.4.0/jackson-core-2.4.0.jar
 wget http://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.4.0/jackson-annotations-2.4.0.jar
 )
(cd ext/JSONassert; mvn package)
#(cd ext/org.json-java; mvn package)
(cd ../libs
wget -O org.json-20131017.jar http://search.maven.org/remotecontent?filepath=org/codeartisans/org.json/20131017/org.json-20131017.jar )
