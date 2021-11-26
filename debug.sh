#!/usr/bin/env bash


#编译运行
./gradlew installDebug
if [ $? -eq 0 ]; then
  adb shell am start -n com.heye.justifytextview/com.heye.justifytextview.MainActivity
else
  echo "启动失败"
fi