<?php
include 'init_standard_collection.php';
session_start();

if (!isset($_SESSION['REPLACE_MARKS'])) {
    $_SESSION['REPLACE_MARKS'] = [
        'head_replace_mark' => '<replace "head">',
        'navbar_replace_mark' => '<replace "navbar">',
        'song1_replace_mark' => '<replace "song1">',
        'song2_replace_mark' => '<replace "song2">'
    ];

}

if (!isset($_SESSION['COLLECTION'])) {
    $_SESSION['COLLECTION'] = $collection;
}
?>