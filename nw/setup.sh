#!/bin/bash
company='qwedl';
app='@project';
version='@version';
title='@title';
if [ "$(id -u)" != "0" ]; then
	if [ -e "/usr/bin/zenity" ]; then
		zenity --error --title $title --text 'Необходимы права root' --window-icon=icon.png;
	else
		echo 'Необходимы права root'
	fi
	exit 0
fi
if [ ! -e "/opt/$company/$app" ]; then
	if [ -e "/usr/bin/zenity" ]; then
		zenity --question --title $title --text 'Установить '$title'?' --window-icon=icon.png;
		if [ "$?" -eq "1" ]; then
			exit 0;
		fi
	else
		echo -n 'Установить '$title'? [y/N] ';
		read q;
		if [ ! "$q" == "y" ] && [ ! "$q" == "Y" ]; then
			exit 0;
		fi
	fi
	mkdir -p /opt/$company/$app;
	cp -r * /opt/$company/$app/;
	cp -r * /opt/$company/$app/;
	mv /opt/$company/$app/app.desktop /usr/share/applications/$app.desktop;
	find /opt/$company -type f -exec chmod 0644 {} \;
	find /opt/$company -type d -exec chmod 0755 {} \;
	find /opt/$company -name 'app' -exec chmod 0777 {} \;
	#chmod +x /opt/$company/$app/app
	if [ -e "/usr/bin/zenity" ]; then
		zenity --info --title $title --text 'Установка '$title' завершена' --window-icon=icon.png;
	else
		echo 'Установка '$title' завершена';
	fi
else
	if [ -e "/usr/bin/zenity" ]; then
		zenity --question --title $title --text 'Удалить '$title'?' --window-icon=icon.png;
		if [ "$?" -eq "1" ]; then
			exit 0;
		fi
	else
		echo -n 'Удалить '$title'? [y/N] ';
		read q;
		if [ ! "$q" == "y" ] && [ ! "$q" == "Y" ]; then
			exit 0;
		fi
	fi
	rm -r /opt/$company/$app;
	rm /usr/share/applications/$app.desktop;
	if [ -e "/usr/bin/zenity" ]; then
		zenity --info --title $title --text 'Удаление '$title' завершено' --window-icon=icon.png;
	else
		echo 'Удаление '$title' завершено';
	fi
fi
exit 0;
