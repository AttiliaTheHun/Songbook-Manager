<?php
include dirname(__FILE__) . '/lib_settings.php';

$collection_file_path = dirname(__FILE__) . '/../data/songbook/collection.json';
$easter_collection_file_path = dirname(__FILE__) . '/../data/songbook/easter_collection.json';

function init_standard_collection($filter = true) {
    setlocale(LC_ALL, $GLOBALS['settings']['locale']);
 
    if(!file_exists($GLOBALS['collection_file_path'])) {
        repair_standard_collection();
    }
    $collection_raw = file_get_contents($GLOBALS['collection_file_path']);
    $collection = json_decode($collection_raw, true);
    
        
    $collection_copy = $collection;
        
    for ($x = 0; $x < count($collection); $x++) {
        $collection[$x]['name'] = iconv('UTF-8', 'ASCII//TRANSLIT', $collection[$x]['name']);
    }
        
    usort($collection, function($a, $b) {
        return strcmp($a["name"], $b["name"]);
    });
        

    for ($x = 0; $x < count($collection); $x++) {
        for ($y = 0; $y < count($collection); $y++) {
            if ($collection[$x]['id'] == $collection_copy[$y]['id']) {
                $name = $collection_copy[$y]['name'];
            }
        }
        $collection[$x]['name'] = $name;
    }
    
    function is_song_active($var) {
        return $var['active'];
    }
    if ($filter) {
        $collection = array_filter($collection, "is_song_active");
    }
    
    $GLOBALS['collections']['standard'] = $collection;
    $GLOBALS['collection_data']['standard']['data_path'] = dirname(__FILE__) . '/../data/songbook/songs/html/';
    $GLOBALS['collection_data']['standard']['relative_data_path'] = 'songs/html/';
    $GLOBALS['collection_data']['standard']['file_path'] = $GLOBALS['collection_file_path'];
    $GLOBALS['collection_data']['standard']['file_name'] = "collection.json";
}

function init_easter_collection($filter = true) {
    
}

function repair_standard_collection() {
    
}

function repair_easter_collection() {

}

?>