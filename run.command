#!/bin/zsh
cd "$(dirname "$0")"

mvn -pl desktop -am -DskipTests compile exec:java \
  -Dexec.mainClass=io.github.therealsegfault.projectbeatsgdx.desktop.DesktopLauncher \
  -Dexec.jvmArgs="-XstartOnFirstThread"