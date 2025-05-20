@echo off
chcp 65001 >nul
set JAVA_OPTS=-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8
mvn spring-boot:run 