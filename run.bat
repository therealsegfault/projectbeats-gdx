@echo off
cd /d %~dp0
mvn -pl :projectbeats-desktop -DskipTests exec:java
pause