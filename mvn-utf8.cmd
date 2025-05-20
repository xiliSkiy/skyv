@echo off
rem UTF-8 Encoding Wrapper for Maven
chcp 65001 > nul
set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8
mvn %*
