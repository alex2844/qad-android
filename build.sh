#!/bin/bash
#/*
#=====================================================
# Qad Framework (qad-cli)
#-----------------------------------------------------
# https://pcmasters.ml/
#-----------------------------------------------------
# Copyright (c) 2016 Alex Smith
#=====================================================
#*/
if [ "$1" == "install" ]; then
	echo 'if';
	exit;
fi
if [ "$1" == "" ] || [ "$1" == "help" ]; then
	echo 'Help Qad-cli Fraemwork';
	echo './qad-cli install'; #TODO
	echo './qad-cli clear'; #TODO
	echo './qad-cli bulder name version title';
	exit;
fi
if [ -z "$2" ]; then exit 0; fi
if [ -z "$3" ]; then exit 0; fi

company='qwedl';
color=$(cat app/src/main/assets/www/page/$1/index.html | grep theme-color | sed 's/.*content="//g' | sed 's/".*//g');
icon=$(cat app/src/main/assets/www/page/$1/index.html | grep 'rel="icon"' | sed 's/.*href="//g' | sed 's/".*//g');

echo 'Inc: '$company;
echo 'App: '$1;
echo 'Version: '$2;
echo 'Title: '$3;
echo 'Color: '$color;
echo 'Icon: '$icon;

cd ../../
mkdir -p ~/.config/qad/page/
cp -r data ~/.config/qad/
cp -r page/$1 ~/.config/qad/page/
exit;

cp app/src/main/assets/www/page/$1/$icon app/src/main/res/drawable-hdpi/ic_launcher.png;
cp app/src/main/assets/www/page/$1/$icon app/src/main/res/drawable-mdpi/ic_launcher.png;
cp app/src/main/assets/www/page/$1/$icon app/src/main/res/drawable-xhdpi/ic_launcher.png;
cp app/src/main/assets/www/page/$1/$icon app/src/main/res/drawable-xxhdpi/ic_launcher.png;
sed -r 's/mWebView.loadUrl(.*);/mWebView.loadUrl("file:\/\/\/android_asset\/www\/page\/'$1'\/index.html");/g' app/src/main/java/com/example/app/MainActivity.java > app/src/main/java/com/example/app/MainActivity.gen.java;
mv app/src/main/java/com/example/app/MainActivity.gen.java app/src/main/java/com/example/app/MainActivity.java;
sed -r 's/applicationId ".*"/applicationId "com.'$company'.'$1'"/g' app/build.gradle  > app/build.gen.gradle;
mv app/build.gen.gradle app/build.gradle;
sed -r 's/versionName ".*"/versionName "'$2'"/g' app/build.gradle  > app/build.gen.gradle;
mv app/build.gen.gradle app/build.gradle;
sed -r 's/android:label=".*"/android:label="'$3'"/g' app/src/main/AndroidManifest.xml  > app/src/main/AndroidManifest.gen.xml;
mv app/src/main/AndroidManifest.gen.xml app/src/main/AndroidManifest.xml;
sed -r 's/stylesheet\/qad/stylesheet/g' app/src/main/assets/www/page/$1/index.html  > app/src/main/assets/www/page/$1/index.gen.html;
mv app/src/main/assets/www/page/$1/index.gen.html app/src/main/assets/www/page/$1/index.html;
sed -r 's/colorPrimary">.*</colorPrimary">'$color'</g' app/src/main/res/values/styles.xml > app/src/main/res/values/styles.gen.xml;
mv app/src/main/res/values/styles.gen.xml app/src/main/res/values/styles.xml;
sed -r 's/colorPrimaryDark">.*</colorPrimaryDark">'$color'</g' app/src/main/res/values/styles.xml > app/src/main/res/values/styles.gen.xml;
mv app/src/main/res/values/styles.gen.xml app/src/main/res/values/styles.xml;
sed -r 's/@color/'$color'/g' app/src/main/assets/www/data/qad/qad.css  > app/src/main/assets/www/data/qad/qad.gen.css;
mv app/src/main/assets/www/data/qad/qad.gen.css app/src/main/assets/www/data/qad/qad.css;
sed -r 's/@location\//..\/..\//g' app/src/main/assets/www/data/qad/qad.css  > app/src/main/assets/www/data/qad/qad.gen.css;
mv app/src/main/assets/www/data/qad/qad.gen.css app/src/main/assets/www/data/qad/qad.css;
sed -r 's/@color: meta.theme-color;//g' app/src/main/assets/www/data/qad/qad.css  > app/src/main/assets/www/data/qad/qad.gen.css;
mv app/src/main/assets/www/data/qad/qad.gen.css app/src/main/assets/www/data/qad/qad.css;
sed -i '/meta./d' app/src/main/assets/www/data/qad/qad.css;
gradle build && adb install -r app/build/outputs/apk/app-debug.apk;
