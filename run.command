#!/bin/zsh
cd "$(dirname "$0")"
mvn -pl :projectbeats-desktop -DskipTests exec:exec \
  -Dexec.executable=java \
  -Dexec.args="-XstartOnFirstThread -cp %classpath io.github.therealsegfault.projectbeatsgdx.desktop.DesktopLauncher"