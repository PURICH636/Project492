<?php
	$DB_HOST = '';
	$DB_USER = '';
	$DB_PASS = '';
	$DB_NAME = '';
	$name = $_GET['name'];
	$mysqli = mysqli_connect($DB_HOST, $DB_USER, $DB_PASS, $DB_NAME);
	$sql = mysqli_query($mysqli, "DELETE FROM `wifi_data` WHERE username = '$name';");
	echo "<script window.location.reload() language='javascript'>history.go(-1);</script>";
?>