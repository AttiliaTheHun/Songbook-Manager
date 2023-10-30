<?php


include('./lib/init_session.php');
$template_html = file_get_contents('./resources/templates/pageview.html');

$head_html = file_get_contents('./resources/templates/head.html');
$head_html = $head_html . PHP_EOL . '<link rel="stylesheet" href="./resources/css/style.css">';

if (isset($_GET['song1'])) {
	$song1 = $_GET['song1'];
} else {
    $song1 = $_SESSION['COLLECTION'][0]['id'];
}

if (isset($_GET['song2'])) {
	$song2 = $_GET['song2'];	
} else {
    $song2 = $_SESSION['COLLECTION'][1]['id'];
}

if (isset($_GET['nobuttons'])) {
	$nobuttons = $_GET['nobuttons'];
} else {
    $nobuttons = false;    
}

if (isset($_GET['nodecoration'])) {
	$nodecoration = $_GET['nodecoration'];
} else {
    $nodecoration = false;
}

if ($nodecoration == false) {
    $head_html = $head_html . PHP_EOL . '<link rel="stylesheet" href="./resources/css/style_pageview.css">';
}

if ($nobuttons == false || abs($song1 - $song2) == 1) {
    $head_html = $head_html . PHP_EOL . '<link rel="stylesheet" href="./resources/css/style_navbar.css">';
    $previous_song = "";
    $next_song = "";
    
    if ($song1 != 0) {
        $previous_song = 0;
    }
    
    
    $navbar_html = file_get_contents('./resources/templates/navbar.html');
    $navbar_html = str_replace($_SESSION['REPLACE_MARKS']['previous_song_replace_mark'], "", $navbar_html);
    $navbar_html = str_replace($_SESSION['REPLACE_MARKS']['next_song_replace_mark'], "", $navbar_html);
    $template_html = str_replace($_SESSION['REPLACE_MARKS']['navbar_replace_mark'], $navbar_html, $template_html);
}

$template_html = str_replace($_SESSION['REPLACE_MARKS']['head_replace_mark'], $head_html, $template_html);


$song1_html = "";

if (file_exists("./data/songbook/songs/html/$song1.html")) {
    $song1_html = file_get_contents("./data/songbook/songs/html/$song1.html");
}

$song2_html = "";

if (file_exists("./data/songbook/songs/html/$song2.html")) {
    $song2_html = file_get_contents("./data/songbook/songs/html/$song2.html");
}

$template_html = str_replace($_SESSION['REPLACE_MARKS']['song1_replace_mark'], $song1_html, $template_html);
$template_html = str_replace($_SESSION['REPLACE_MARKS']['song2_replace_mark'], $song2_html, $template_html);


echo $template_html;
?>