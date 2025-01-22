<?php
include_once(dirname(__FILE__) . '/lib_settings.php');
include_once(dirname(__FILE__) . '/lib_standard_collection.php');
include_once(dirname(__FILE__) . '/lib_easter_collection.php');

$standard_manager = new StandardCollectionProvider();
$providers = [
    StandardCollectionProvider::COLLECTION_NAME => $standard_manager,
	EasterCollectionProvider::COLLECTION_NAME => new EasterCollectionProvider($standard_manager)
    ];


function is_song_active($song) {
    return $song['active'];
}

?>