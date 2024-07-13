<?php
/**
 * This file is retrieved when a user decides to visit the online preview of the songbook. This script generates
 * the HTML response file. It is advised not to modify this file in any way to prevent breaking the preview 
 * feature. 
 **/
include_once(dirname(__FILE__) . '/lib/lib_settings.php');

// if online preview feature is disabled, we will not serve any content
if ($settings['preview']['enabled'] == false) {
    if ($settings['homepage']['enabled']) {
        header("Location: " . $settings['url']);
    } else {
        include './resources/pages/204.php';
        http_response_code(204);
    }
    exit(0);
}

include_once(dirname(__FILE__) . '/lib/lib_strings.php');
include_once(dirname(__FILE__) . '/lib/init_preview_session.php');
include_once(dirname(__FILE__) . '/lib/lib_standard_collection.php');

$provider = new StandardCollectionProvider();
$_SESSION['PROVIDER'] = $provider;

$url = $_SESSION['SETTINGS']['url'];

$template_html = file_get_contents(dirname(__FILE__) . '/resources/templates/preview.html');
$template_html = str_replace($_SESSION['REPLACE_MARKS']['language_replace_mark'], $_SESSION['SETTINGS']['locale'], $template_html);


$head_html = file_get_contents('./resources/templates/head.html');
$head_html = str_replace($_SESSION['REPLACE_MARKS']['site_description_replace_mark'], $strings['site_description'], $head_html);
$head_html .= "<title>{$strings['preview']['page_title']}</title>" . PHP_EOL;

$head_html = $head_html . PHP_EOL . "<link rel=\"stylesheet\" href=\"$url/resources/css/style.css\">";

// parse URL parameters that will be provided as ?params=param1/param2/... through htaccess redirection
$params = rtrim($_GET['params'], '/');
$params = filter_var($params, FILTER_SANITIZE_URL);
$params = explode('/', $params);

// init default values
$nodecoration = false;
$nobuttons = false;

// someone can also go directly for the .php file, in that case we do not receive our params (not even empty),
// so we redirect manually
if (!isset($_GET['params'])) {
    header("Location: $url/preview/");
    exit(0);
}

// sometimes when args are actually not provided, we get the ".php" as args instead
if (count($params) == 1 && $params[0] == ".php") {
    $params = []; // trigger default
}

// for empty parameters we go with standard collection beginning (default)
if (count($params) == 0) {
    $_SESSION['PROVIDER'] = $_SESSION['providers']['standard'];
    $song1_index = 0;
    $song2_index = 1;
} else if (count($params) == 1) { // single param sets song1 of standard collection
    $_SESSION['PROVIDER'] = $_SESSION['providers']['standard'];
    $song1_index = (int)$params[0];
    $song2_index = $song1_index + 1;
} else {
    // apply params that were provided
    for ($x = 0; $x < count($params); $x++) {
        echo $params[$x];
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
                if (ctype_digit($params[$x])) {
                    if (!isset($song1_index)) {
                        $song1_index = (int)$params[$x];
                    } else if (!isset($song2_index)) {
                        $song2_index = (int)$params[$x];
                    }
                } else {
                    $_SESSION['PROVIDER'] = $_SESSION['providers'][$params[$x]];
                }
        }
    }
    
    // when our params did not give us the necessary info, we go again, with defaults
    if (!isset($_SESSION['PROVIDER'])) {
        $_SESSION['PROVIDER'] = $_SESSION['providers']['standard'];
    }
    // we wanna check $song1 status, that is why we setup $song2 first
    if (!isset($song2_index)) {
        if (!isset($song1_index)) {
            $song2_index = 1;
        } else {
            $song2_index = $song1_index + 1;
        }
    }
   
    
    if (!isset($song1_index)) {
        $song1_index = 0;
    }
    
    
}

$collection_size = count($_SESSION['PROVIDER']->get_collection());

// if song is defined but invalid, we make it nonexistent so it gets shadowed
if ($song1_index < 0 || $song1_index >= $collection_size) {
    if ($collection_size < 1) {
        $song1_index = -1;
    } else {
        $song1_index = 0;
    }
}

// and we do the same for song2
if ($song2_index < 0 || $song2_index >= $collection_size) {
    if ($collection_size < 1 || $song2_index === $song1_index + 1) {
        $song2_index = -1;
    } else {
        $song2_index = 1;
    }
}

// create the response HTML from the template
// no decoration means the background, which is addded through a stylesheet
if (!$nodecoration) {
    $head_html = $head_html . PHP_EOL . "<link rel=\"stylesheet\" href=\"$url/resources/css/style_preview.css\">";
}

// now we setup convenience for browsing: navbar with prev/next buttons (if enabled) and right/left arrow keys
$previous_song_index = "";
$next_song_index = "";
    
// if it is the first song, we simply let the page turned backwards action collapse back to this song,
// otherwise we init index of the previous page song1
if ($song1_index === 0) {
    $previous_song_index = $song1_index;
} else {
    $previous_song_index = $song1_index - 2;
}
    
// if it is the last song, we simply let the page turned forward action collapse back to this song,
// otherwise we init index of the upcoming page song1
if ($song2_index === $collection_size - 1) {
    $next_song_index = $song1_index;
} else {
    if ($song2_index === -1) {
        $next_song_index = $song1_index;
    } else {
        $next_song_index = $song2_index + 1;
    }
}


// when the songs displayed are subsequent, it makes sense to add "next" and "last" buttons
// the second part of the condition applies for the end of the songbook at an odd number of songs
if (abs($song2_index - $song1_index) === 1 || ($song1_index !== -1 && $song2_index === -1)) {
    // only if the buttons are not disabled tho
    if (!$nobuttons) {
        $head_html = $head_html . PHP_EOL . "<link rel=\"stylesheet\" href=\"$url/resources/css/style_navbar.css\">";

        // init the navbar
        $navbar_html = file_get_contents('./resources/templates/navbar.html');
        $navbar_html = str_replace($_SESSION['REPLACE_MARKS']['previous_song_button_text_replace_mark'], $strings['preview']['previous_song_button_text'], $navbar_html);
        $navbar_html = str_replace($_SESSION['REPLACE_MARKS']['next_song_button_text_replace_mark'], $strings['preview']['next_song_button_text'], $navbar_html);
        $template_html = str_replace($_SESSION['REPLACE_MARKS']['navbar_replace_mark'], $navbar_html, $template_html);
    }
    
    
    // replace next and previous page links inside the left and right arrow key press events and buttons
    $template_html = str_replace($_SESSION['REPLACE_MARKS']['previous_song_replace_mark'], construct_url($previous_song_index), $template_html);
    $template_html = str_replace($_SESSION['REPLACE_MARKS']['next_song_replace_mark'], construct_url($next_song_index), $template_html);
} else {
    // disable the arrow key redirects
    $template_html = str_replace($_SESSION['REPLACE_MARKS']['previous_song_replace_mark'], '";return;var x="', $template_html);
    $template_html = str_replace($_SESSION['REPLACE_MARKS']['next_song_replace_mark'], '";return;var x="', $template_html);
}


$template_html = str_replace($_SESSION['REPLACE_MARKS']['head_replace_mark'], $head_html, $template_html);

// now we need to prepare the actual songs to be added to the template
$song1_html = '<div class="song">' . $strings['preview']['no_song_found'] . '</div>';
if ($song1_index != -1) {
    $song1_id = $provider->get_collection()[$song1_index]['id'];
}

// -1 means the song does not exist, so we leave the shadow (empty) song as the HTML
if ($song1_index != -1 && file_exists($provider->get_song_file_path($song1_id))) {
    $song1_html = file_get_contents($provider->get_song_file_path($song1_id));
}

$song2_html = '<div class="song"></div>';
if ($song2_index != -1) {
    $song2_id = $provider->get_collection()[$song2_index]['id'];
}


// -1 means the song does not exist, so we leave the shadow (empty) song as the HTML
if ($song2_index != -1 && file_exists($provider->get_song_file_path($song2_id))) {
    $song2_html = file_get_contents($provider->get_song_file_path($song2_id));
}

// finally inject the song HTML
$template_html = str_replace($_SESSION['REPLACE_MARKS']['song1_replace_mark'], $song1_html, $template_html);
$template_html = str_replace($_SESSION['REPLACE_MARKS']['song2_replace_mark'], $song2_html, $template_html);


echo $template_html;


function construct_url($song1_index = null, $song2_index = null) {
    //https://beta-hrabozpevnik.clanweb.eu/preview/0
    //https://beta-hrabozpevnik.clanweb.eu/preview/easter/0
    //https://beta-hrabozpevnik.clanweb.eu/preview/0/nodecoration
    //https://beta-hrabozpevnik.clanweb.eu/preview/0/4/print
    
    $url = $GLOBALS['url'] . "/preview";
    
    if ($_SESSION['PROVIDER']::COLLECTION_NAME != StandardCollectionProvider::COLLECTION_NAME) {
        $url .= "/" . $_SESSION['PROVIDER']::COLLECTION_NAME;
    }
    
    if ($song1_index !== null) {
        $url .= "/" . $song1_index;
    }
    
    if ($song2_index !== null) {
        $url .= "/" . $song2_index;
    }
    
    if ($GLOBALS['nobuttons']) {
        if ($GLOBALS['nodecoration']) {
            $url .= "/print";
        } else {
            $url .= "/nobuttons";
        }
    }
    
    if ($GLOBALS['nodecoration']) {
            $url .= "/nodecoration";
    } 
    
    $url .= "/";
    
    return $url;
}

?>