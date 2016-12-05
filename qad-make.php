<?php
/*
=====================================================
Qad Make for Qad Framework (qad-make.php)
-----------------------------------------------------
https://pcmasters.ml/
-----------------------------------------------------
Copyright (c) 2016 Alex Smith
=====================================================
*/
header('Content-Type: text/html; charset=utf-8');
session_start();
if (!empty($_GET['page'])) {
	$method = (!empty($_GET['method']) ? $_GET['method'] : 'GET');
	$parse = preg_replace([
		'/&method='.$method.'/',
		'/method='.$method.'/',
		'/&page=/',
		'/page=/'
	],'',$_SERVER['QUERY_STRING']);
	if ($method == 'GET') {
		$opts = ['http' => [
			'method' => 'GET',
			'header' => 'Cookie: PHPSESSID='.$_COOKIE['session']
		]];
		$context = stream_context_create($opts);
		$res = file_get_contents($parse, 0, $context);
	}else{
		$parse = parse_url($parse);
		$opts = ['http' => [
			'method'  => $method,
			'header'  => 'Content-type: application/x-www-form-urlencoded',
			'content' => $parse['query']
		]];
		if (isset($_COOKIE['session']))
			$opts['http']['header'] = 'Cookie: PHPSESSID='.$_COOKIE['session']."\r\n".$opts['http']['header'];
		$context  = stream_context_create($opts);
		parse_str($parse['query'],$parse['params']);
		$res = file_get_contents($parse['scheme'].'://'.$parse['host'].(!empty($parse['port']) ? ':'.$parse['port'] : '').$parse['path'], false, $context);
	}
	if (!empty($http_response_header))
		foreach ($http_response_header as $hdr) {
			if (preg_match('/^Set-Cookie:\s*([^;]+)/', $hdr, $matches)) {
				parse_str($matches[1], $session);
				if ($session['PHPSESSID'])
					setcookie('session', $session['PHPSESSID'], time()+60*60*24*30, '/');
			}
		}
	echo $res;
}else if (!empty($_GET['qad-make'])) {
	$log = [];
	$json = json_decode(file_get_contents('qad-make/'.$_GET['qad-make'].'.json'), true);
	if (!empty($json['files'])) {
		$log[] = implode(" ",$json['files']);
		shell_exec('zip -r qad-make/'.$_GET['qad-make'].'.zip qad-make/'.$_GET['qad-make'].'.json '.implode(" ",$json['files']));
	}
	if (!empty($json['session']))
		foreach ($json['session'] as $k=>$v)
			$log['session'][$k] = $_SESSION[$k] = $v;
	if (!empty($json['db'])) {
		if (preg_match('/sqlite/', $json['db']['connect'])) {
			$dbh = new PDO($json['db']['connect']);
			$dbh->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
			$dbh->setAttribute(PDO::ATTR_DEFAULT_FETCH_MODE, PDO::FETCH_NAMED);
			if (!empty($json['db']['create']))
				foreach ($json['db']['create'] as $k=>$v) {
					$sql = 'create table '.$k.' (';
						$i = 1;
						$count = count($v);
						foreach ($v as $n=>$t) {
							$sql .= '`'.$n.'` '.$t.($i < $count ? ', ' : '');
							++$i;
						}
					$sql .= ')';
					try {
						$dbh->exec($sql);
						$log['create'][] = $k;
						if (!empty($json['db']['insert']) && !empty($json['db']['insert'][$k])) {
							foreach ($json['db']['insert'][$k] as $exec) {
								$sql = 'insert into '.$k.' (';
								$count = count($exec);
								for ($i = 1, $j = 0; $i <=2; ++$i)
									foreach ($exec as $n=>$t) {
										++$j;
										if ($i == 1)
											$sql .= $n;
										else
											$sql .= '"'.$t.'"';
										if ($j < $count)
											$sql .= ', ';
										else{
											if ($i == 1)
												$sql .= ') values (';
											$j = 0;
										}
									}
								$sql .= ')';
								$dbh->exec($sql);
								$log['insert'][] = $k;
							}
						}
					} catch (Exception $e) {}
				}
		}
		echo '<pre>';
		print_r($log);
		echo '</pre>';
	}
}else{
	if (!$dir = opendir('qad-make/'))
		exit;
	while ($file = readdir($dir)) {
		if ($file != '.' && $file != '..' && pathinfo($file, PATHINFO_EXTENSION) == 'json') {
			$json = json_decode(file_get_contents('qad-make/'.$file), true);
			echo '<a href="?qad-make='.pathinfo($file, PATHINFO_FILENAME).'"><button>'.$json['title'].'</button></a>';
		}
	}
}
