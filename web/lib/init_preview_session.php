<?php
/**
 * Initializes the $_SESSION variable to map all necessary values and defines all necessary fields
 * for the online preview of the songbook.
 */
 

include_once(dirname(__FILE__) . '/lib_settings.php');
include_once(dirname(__FILE__) . '/lib_collection.php');

session_start();


/**
 * $_SESSION['providers'] contains a map of "collection_name" => provider (Object) entries for every
 * available collection in the songbook.
 **/
$_SESSION['providers'] = $GLOBALS['providers'];

/**
 * $_SESSION['PROVIDER'] contains the current collection provider ('standard' by default). It is capitalised
 * to differentiate it visually from $_SESSION['providers'].
 **/
$_SESSION['PROVIDER'] = $_SESSION['providers'][StandardCollectionProvider::COLLECTION_NAME];

/**
 * $_SESSION['SETTINGS'] contains the setings of the songbook.
 **/
$_SESSION['SETTINGS'] = $settings;

// these "marks" are used when the page is being generated from the template
$_SESSION['REPLACE_MARKS'] = [
    'language_replace_mark' => '<replace "language">',
    'head_replace_mark' => '<replace "head">',
    'navbar_replace_mark' => '<replace "navbar">',
    'song1_replace_mark' => '<replace "song1">',
    'song2_replace_mark' => '<replace "song2">',
    'previous_song_replace_mark' => '<replace "previous-song">',
    'next_song_replace_mark' => '<replace "next-song">',
    'previous_song_button_text_replace_mark' => '<replace "previous-song-button-text">',
    'next_song_button_text_replace_mark' => '<replace "next-song-button-text">',
    'site_description_replace_mark' => '<replace "site-description">'
];

unset($GLOBALS['providers']);

?>