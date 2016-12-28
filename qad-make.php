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
if (!empty($argv[1]))
	$_GET['qad-make'] = $argv[1];
if (!empty($_GET['page'])) {
	$method = (!empty($_GET['method']) ? $_GET['method'] : 'GET');
	$parse = preg_replace([
		'/&method='.$method.'/',
		'/method='.$method.'/',
		'/&page=/',
		'/page=/'
	],'',$_SERVER['QUERY_STRING']);
	if ($method == 'GET')
		$res = file_get_contents($parse);
	else{
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
	if ($_GET['qad-make'] == 'convert') {
		$vals = [];
		$vars = ['MySQL database','Mysql User','Password','SQLite database file'];
		if (gettype(STDIN) == 'resource')
			foreach($vars as $var) {
				echo 'Введите '.$var.':';
				if ($var=='Password')
					`stty -echo`;
				$vals[] = trim(fgets(STDIN));
				if ($var=='Password'){
					`stty echo`;
					echo "\n";
				}
			}
		else if (isset($_GET['vals']))
			$vals = $_GET['vals'];
		else{
			echo '<form><input type="hidden" name="qad-make" value="convert" />';
			foreach($vars as $var) {
				echo 'enter '.$var.':';
				echo '<input name="vals[]" />';
				echo '<br />';
			}
			echo '<input type="submit" /></form>';
			exit;
		}
		$pdoMy = new PDO('mysql:dbname='.$vals[0], $vals[1], $vals[2], array(PDO::MYSQL_ATTR_INIT_COMMAND => "SET NAMES utf8")) or die("can't connect to $myDb");
		$pdoLi = new PDO('sqlite:'.$vals[3]) or die("can't connect to $liDb");
		$creates = [];
		$tbls = [];
		$q = $pdoMy->query('SHOW TABLES');
		while($d = $q->fetch())
			$tbls[] = current($d);
		foreach($tbls as $tbl) {
			echo "creating table '$tbl'\n";
			$q = $pdoMy->query("SELECT * FROM `$tbl` LIMIT 1");
			$cols = [];
			for ($i = 0; $i < $q->columnCount(); ++$i){
				$m = (object) $q->getColumnMeta($i);
				$type = (isset($m->native_type) ? $m->native_type : '');
				$def = "\t".$m->name.' '.str_replace(['LONG'], ['integer'], $type);
				if (in_array('unique_key',$m->flags))
					$def.=" unique";
				if (in_array('primary_key',$m->flags))
					$def.=" primary key";
				if ($pdoMy->query('SHOW COLUMNS FROM `'.$tbl.'` where `Field` = "'.$m->name.'" && `Extra` = "auto_increment"')->fetch()) {
					$def .= ' autoincrement';
				}else if (in_array('not_null',$m->flags))
					$def.=" not null";
				$cols[] = $def;
			}
			query("CREATE TABLE IF NOT EXISTS $tbl(\n".implode(",\n",$cols)."\n);\n",$pdoLi);
		}
		foreach($tbls as $tbl) {
			echo "inserting data into '$tbl'\n";
			$qs = [];
			$i = 0;
			$q = $pdoMy->query("SELECT * FROM `$tbl`");
			while ($d = $q->fetch(5)) {
				$vals = $keys = [];
				foreach ($d as $k=>$v) {
					if ($k=='queryString')
						continue;
					$keys[] = $k;
					$vals[] = $pdoLi->quote($v);
				}
				$qs[]="INSERT OR IGNORE INTO $tbl (".implode(',',$keys).") VALUES  (".implode(',',$vals).");\n";
				++$i;
				if ($i>=5000) {
					query($qs,$pdoLi);
					$qs = [];
					$i = 0;
				}
			}
			query($qs,$pdoLi);
		}
	}else{
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
	}
}else{
	if (gettype(STDIN) == 'resource')
		echo 'php '.$argv[0].' convert //Конвертер Mysql -> Sqlite'."\n";
	else
		echo '<a href="?qad-make=convert"><button>Конвертер Mysql -> Sqlite</button></a><br /><br />';
	if (!$dir = opendir('qad-make/'))
		exit;
	while ($file = readdir($dir)) {
		if ($file != '.' && $file != '..' && pathinfo($file, PATHINFO_EXTENSION) == 'json') {
			$json = json_decode(file_get_contents('qad-make/'.$file), true);
			if (gettype(STDIN) == 'resource')
				echo 'php '.$argv[0].' '.pathinfo($file, PATHINFO_FILENAME).' //'.$json['title']."\n";
			else
				echo '<a href="?qad-make='.pathinfo($file, PATHINFO_FILENAME).'"><button>'.$json['title'].'</button></a>';
		}
	}
}
function query($qs,$pdo){
	if($pdo) {
		if (is_array($qs))
			$lock = 1;
		else{
			$lock = 0;
			$qs = [$qs];
		}
		if ($lock) {
			$pdo->exec('PRAGMA synchronous = 0;');
			$pdo->exec('PRAGMA journal_mode = OFF;');
			$pdo->exec('BEGIN;');
		}
		foreach ($qs as $q) {
			$pdo->exec($q);
		}
		if ($lock) {
			$pdo->exec('COMMIT;');
			$pdo->exec('PRAGMA synchronous = FULL;');
			$pdo->exec('PRAGMA journal_mode = DELETE;');
		}
		$err = $pdo->errorInfo();
		if (intval($err[0])) {
			echo "\nError: \n";
			var_dump($q,$err);
		}
	}
}
