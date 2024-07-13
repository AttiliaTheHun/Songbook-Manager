<?php
include(dirname(__FILE__) . '/lib_settings.php');
include(dirname(__FILE__) . '/lib_standard_collection.php');
include(dirname(__FILE__) . '/lib_easter_collection.php');

$providers = [
    StandardCollectionProvider::COLLECTION_NAME => new StandardCollectionProvider()
    ];


function is_song_active($song) {
    return $song['active'];
}

?>