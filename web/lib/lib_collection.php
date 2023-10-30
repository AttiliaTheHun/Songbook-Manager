<?php
include dirname(__FILE__) . '/lib_settings.php';
function init_standard_collection() {
    setlocale(LC_ALL, $settings->locale);
    $filename = dirname(__FILE__) . './data/songbook/collection.json';
    if(!file_exists($filename)) {
        repair_standard_collection();
    }
    $collection_raw = file_get_contents($filename);
    $collection = json_decode($collection_raw, true);
    
        
    $collection_copy = $collection;
        
    for ($x = 0; $x < count($collection); $x++) {
        $collection[$x]['name'] = iconv('UTF-8', 'ASCII//TRANSLIT', $collection[$x]['name']);
    }
        
    usort($collection, function($a, $b) {
        return strcmp($a["name"], $b["name"]);
    });
        
    //var_dump($collection);

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
    
    $collection = array_filter($collection, "is_song_active");
    
    //echo "\n\n\n\n\n";
    //global $collection;
    //var_dump($collection);
}

function init_easter_collection() {
    
}

function repair_standard_collection() {
    
}

function repair_easter_collection() {

}

?>