<?php
/*
=====================================================
Mysql remote client for Qad Admin (qad-admin-mysql.php)
-----------------------------------------------------
http://qad.qwedl.com/admin.html
-----------------------------------------------------
Copyright (c) 2016 Alex Smith
=====================================================
*/
header('Content-Type: application/x-javascript');
$res = [];
$conn = (!empty($_GET['conn']) ? json_decode(urldecode($_GET['conn']), true) : []);
$action = (!empty($_GET['action']) ? $_GET['action'] : null);
$callback = (!empty($_GET['callback']) ? $_GET['callback'] : null);
try {
	$sql = new PDO('mysql:dbname='.$conn[0], $conn[1], $conn[2], [
		PDO::MYSQL_ATTR_INIT_COMMAND => 'SET NAMES utf8'
	]);
} catch (Exception $e) {
	echo $callback.'('.json_encode([
		'error' => $e->getMessage()
	]).');';
	exit;
}
if ($action) {
	$sql->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
	$sql->setAttribute(PDO::ATTR_DEFAULT_FETCH_MODE, PDO::FETCH_NAMED);
	$action = urldecode($action);
	try {
		$res = $sql->query($action);
		if (!(stripos($action, 'select') === false))
			$res = $res->fetchAll();
	} catch (Exception $e) {
		echo $callback.'('.json_encode([
			'error' => $e->getMessage()
		]).');';
		exit;
	}
	echo $callback.'('.json_encode([
		'response' => $res
	]).');';
}else{
	$q = $sql->query('SHOW TABLES')->fetchAll();
	foreach ($q as $v) {
		$res[] = $v[0];
	}
	echo $callback.'('.json_encode([
		'tables' => $res
	]).');';
}
