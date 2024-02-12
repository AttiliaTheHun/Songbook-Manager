<?php
/**
 * Initializes the $_SESSION variable to map all necessary values and defines all necessary fields
 * for the online preview of the songbook.
 */
 
include 'lib_collection.php';
include_once 'lib_settings.php';

session_start();

init_collections();

/**
 * $_SESSION['collections'] contains a map of "collection_name" => collection (Object) entries for every
 * available collection in the songbook.
 **/
$_SESSION['collections'] = $GLOBALS['collections'];

/**
 * $_SESSION['collection_data'] contains a map of "collection_name" => collection_data (Object) entries for every
 * available collection in the songbook. The collection_data contains additional data such as file paths.
 **/
$_SESSION['collection_data'] = $GLOBALS['collection_data'];

/**
 * $_SESSION['COLLECTION'] contains directly the current collection ('standard' by default). It is capitalised
 * to differentiate it visually from $_SESSION['collections'].
 **/
$_SESSION['COLLECTION'] = $_SESSION['collections']['standard'];

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

// we mapped these values to the $_SESSION variable so we can free up some memory now
unset($GLOBALS['collections']);
unset($GLOBALS['collection_data']);

?>