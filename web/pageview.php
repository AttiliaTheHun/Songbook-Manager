<?php
// TODO: implement empty song shadowing and prevent crashes for odd number of songs (or zero)
// TODO: go for more edge cases and add corresponfing error or warning messages (when casting for example)
/**
 * This file is retrieved when a user decides to visit the online preview of the songbook. This script generates
 * the HTML response file. It is advised not to modify this file in any way to prevent breaking the preview 
 * feature. 
 **/
include('./lib/lib_settings.php');

// if online preview feature is disabled, we will not serve any content
// 405 Method Not Allowed
if ($settings['preview']['enabled'] == false) {
    http_response_code(405);
    include './resources/pages/405.php';
    exit(0);
}

include('./lib/init_preview_session.php');

$url = $_SESSION['SETTINGS']['url'];

$template_html = file_get_contents('./resources/templates/pageview.html');

$head_html = file_get_contents('./resources/templates/head.html');
$head_html = $head_html . PHP_EOL . "<link rel=\"stylesheet\" href=\"$url/resources/css/style.css\">";

// parse URL parameters that will be provided as ?params=param1/param2/...
$params = rtrim($_GET['params'], '/');
$params = filter_var($params, FILTER_SANITIZE_URL);
$params = explode('/', $params);

// init some default values
$collection_name = "";
$nodecoration = false;
$nobuttons = false;

// someone can also go directly for the .php file, in that case we do not receive our params (not even empty),
// so we redirect manually
if (!isset($_GET['params'])) {
    header("Location: $url/pageview/");
    exit();
}

// sometimes when args are actually not provided, we get the ".php" as args instead
if (count($params) == 1 && $params[0] == ".php") {
    $params = []; // trigger default
}

// for empty parameters we go with default collection beginning (default)
if (count($params) == 0) {
    $_SESSION['COLLECTION'] = $_SESSION['collections']['standard'];
    $song1_index = 0;
    $song2_index = 1;
} else if (count($params) == 1) { // single param sets song1 of standard collection
    $_SESSION['COLLECTION'] = $_SESSION['collections']['standard'];
    $song1_index = (int)$params[0];
    $song2_index = $song1_index + 1;
} else {
    // apply params that were provided
    for ($x = 0; $x < count($params); $x++) {
        switch ($params[$x]) {
            case 'print':
                $nobuttons = true;
                $nodecoration = true;
                break;
            case 'nobuttons':
                $nobuttons = true;
                break;
            case 'nodecoration':
                $nodecoration = true;
                break;
            default:
                // first priority is the collection, then song1 and finally song2
                if ($collection_name == "") {
                    $_SESSION['COLLECTION'] = $_SESSION['collections'][$params[$x]];
                    $collection_name = $params[$x] . "/";
                } else if (!isset($song1_index)) {
                    $song1_index = (int)$params[$x];
                } else if (!isset($song2_index)) {
                    $song2_index = (int)$params[$x];
                }
        }
    }
    
    // when our params did not give us the necessary info, we go again, with defaults
    if (!isset($_SESSION['COLLECTION'])) {
        $_SESSION['COLLECTION'] = $_SESSION['collections']['standard'];
    }
    // we wanna check $song1 status, that is why we setup $song2 first
    if (!isset($song2_index)) {
        if (!isset($song1_index)) {
            $song2_index = 1;
        } else {
            $song2_index = $song1_index + 1; // TODO: check for overflow
        }
    }
    
    if (!isset($song1_index)) {
        $song1_index = 0;
    }
    
}


// create the response HTML from the template

// no decoration means thebackground, which is addded through a stylesheet
if (!$nodecoration) {
    $head_html = $head_html . PHP_EOL . "<link rel=\"stylesheet\" href=\"$url/resources/css/style_pageview.css\">";
}

// when buttons are allowed and the songs displayed are subsequent, it makes sense to add "next" and "last" buttons
if (!$nobuttons && abs($song2_index - $song1_index) == 1) {
    $head_html = $head_html . PHP_EOL . "<link rel=\"stylesheet\" href=\"$url/resources/css/style_navbar.css\">";
    $previous_song_index = "";
    $next_song_index = "";
    
    if ($song1_index == 0) {
        $previous_song_index = $song1_index;
    } else {
        $previous_song_index = $song1_index - 2;
    }
    
    
    if ($song2_index == count($_SESSION["COLLECTION"])) {
        $next_song_index = $song2_index;
    } else {
        $next_song_index = $song2_index + 1;
    }
    
    // replace next and previous page links inside the lfet and right arrow key press events
    $template_html = str_replace($_SESSION['REPLACE_MARKS']['previous_song_replace_mark'], $url . "/pageview/" . $collection_name . $previous_song_index, $template_html);
    $template_html = str_replace($_SESSION['REPLACE_MARKS']['next_song_replace_mark'], $url . "/pageview/" . $collection_name . $next_song_index, $template_html);
   
    // init the navbar
    $navbar_html = file_get_contents('./resources/templates/navbar.html');
    $navbar_html = str_replace($_SESSION['REPLACE_MARKS']['previous_song_replace_mark'], $url . "/pageview/" . $collection_name . $previous_song_index, $navbar_html);
    $navbar_html = str_replace($_SESSION['REPLACE_MARKS']['next_song_replace_mark'], $url .  "/pageview/" . $collection_name . $next_song_index, $navbar_html);
    $template_html = str_replace($_SESSION['REPLACE_MARKS']['navbar_replace_mark'], $navbar_html, $template_html);
} else {
    $template_html = str_replace($_SESSION['REPLACE_MARKS']['previous_song_replace_mark'], $_SERVER['REQUEST_URI'], $template_html);
    $template_html = str_replace($_SESSION['REPLACE_MARKS']['next_song_replace_mark'], $_SERVER['REQUEST_URI'], $template_html);
   
}

$template_html = str_replace($_SESSION['REPLACE_MARKS']['head_replace_mark'], $head_html, $template_html);

// now we need to prepare the actual songs to be added to the template
$song1_html = '<div class="song"></div>';
$song1_id = $_SESSION['COLLECTION'][$song1_index]['id'];

if (file_exists("./data/songbook/songs/html/$song1_id.html")) {
    $song1_html = file_get_contents("./data/songbook/songs/html/$song1_id.html");
}

$song2_html = '<div class="song"></div>';
$song2_id = $_SESSION['COLLECTION'][$song2_index]['id'];

if (file_exists("./data/songbook/songs/html/$song2_id.html")) {
    $song2_html = file_get_contents("./data/songbook/songs/html/$song2_id.html");
}

// finally inject the song HTML
$template_html = str_replace($_SESSION['REPLACE_MARKS']['song1_replace_mark'], $song1_html, $template_html);
$template_html = str_replace($_SESSION['REPLACE_MARKS']['song2_replace_mark'], $song2_html, $template_html);


echo $template_html;
?>