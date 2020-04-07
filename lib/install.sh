#!/bin/sh
while read -r fileData; do
	# Split the input line
	fileName=$(echo "$fileData" | cut -f1 -d,)
	groupId=$(echo "$fileData" | cut -f2 -d,)
	artifactId=$(echo "$fileData" | cut -f3 -d,)
	version=$(echo "$fileData" | cut -f4 -d,)

	printf '> Installing %s...' "$fileName"
	mvn install:install-file -Dfile="$fileName" -DgroupId="$groupId" \
						-DartifactId="$artifactId" -Dversion="$version" \
						-Dpackaging=jar >/dev/null 2>&1 && \
	printf ' ✔\n' || \
	printf ' ✗ | Status code: %d\n' $?
done \
<<'FILE_LIST'
jsonld-java-0.13.0.jar,com.github.jsonld-java,jsonld-java,0.13.0
FILE_LIST
