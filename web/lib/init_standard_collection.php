<?php
    setlocale(LC_ALL, 'cs_CZ');
    $filename = '../data/songbook/collection.json';
    if(!file_exists($filename)) {
        include('repair_standard_collection.php');
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
    //echo "\n\n\n\n\n";
    global $collection;
    //var_dump($collection);
?>