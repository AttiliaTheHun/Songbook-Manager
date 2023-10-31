<?php
include 'lib_collection.php';
session_start();
init_collection();

if (!isset($_SESSION['REPLACE_MARKS'])) {
    $_SESSION['REPLACE_MARKS'] = [
        'head_replace_mark' => '<replace "head">',
        'navbar_replace_mark' => '<replace "navbar">',
        'song1_replace_mark' => '<replace "song1">',
        'song2_replace_mark' => '<replace "song2">',
        'previous_song_replace_mark' => '<replace "previous-song">',
        'next_song_replace_mark' => '<replace "next-song">'
    ];

}
/* move to lib_collection
if (!isset($_SESSION['COLLECTION'])) {
    $_SESSION['COLLECTION'] = $collection;
}*/
?>