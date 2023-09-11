<?php
include('./lib/init_session.php');
$template_html = file_read_contents('./resources/templates/pageview.html');
$head_replace_mark = '<replace "head">';
$navbar_replace_mark = '<replace "navbar">';
$song1_replace_mark = '<replace "song1">';
$song2_replace_mark = '<replace "song2">';
if (isset($_SESSION['song1'])) {
	$song1 = $_SESSION['song1'];
}
if (isset($_SESSION['song2'])) {
	$song2 = $_SESSION['song2'];	
}
if (isset($_SESSION['nobuttons'])) {
	$nobuttons = $_SESSION['nobuttons'];
}
if (isset($_SESSION['nodecoration'])) {
	$nodecoration = $_SESSION['nodecoration'];
}




?>