#!/bin/sh
set -eu

CONNECTOR_JAR="${1:-mysql-connector-j-8.4.0.jar}"

if [ ! -f "$CONNECTOR_JAR" ]; then
  echo "MySQL Connector/J jar not found: $CONNECTOR_JAR"
  echo "Download it from MySQL, place it in this folder, then run:"
  echo "  sh build.sh $CONNECTOR_JAR"
  exit 1
fi

javac -cp ".:$CONNECTOR_JAR" ProjectFrame.java
cat > MANIFEST.MF <<EOF
Main-Class: ProjectFrame
Class-Path: $CONNECTOR_JAR

EOF
jar cfm project.jar MANIFEST.MF *.class

echo "Built project.jar"
