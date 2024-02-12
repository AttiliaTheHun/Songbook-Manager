<?php
include dirname(__FILE__) . '/lib_settings.php';

$songbook_data_path = dirname(__FILE__) . '/../data/songbook/';

function init_collections($filter = true) {
    setlocale(LC_ALL, $GLOBALS['settings']['locale']);
    $collection_file_suffix = '_collection.json';
    
    // iteratate over all collection files and setup a collection for each
    $dir = opendir ($GLOBALS['songbook_data_path']);
        while ($file = readdir($dir)) {
            if ($file == '.' || $file == '..') continue;
            if (ends_with($file, $collection_file_suffix)) {
                $collection_name = substr($file, 0, -strlen($collection_file_suffix));
                
                $collection_raw = file_get_contents($GLOBALS['songbook_data_path'] . $file);
                $collection = json_decode($collection_raw, true);
                $collection_copy = $collection;
        
                for ($x = 0; $x < count($collection); $x++) {
                    $collection[$x]['name'] = iconv('UTF-8', 'ASCII//TRANSLIT', $collection[$x]['name']);
                }
        
                // sort the collection by song name
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
                
                // filter out inactive songs, if need be
                if ($filter) {
                    $collection = array_filter($collection, "is_song_active");
                }
    
                // set up the necessary variables so we can access the result of our work
                $GLOBALS['collections'][$collection_name] = $collection;
                $GLOBALS['collection_data'][$collection_name]['data_path'] = $GLOBALS['songbook_data_path'] . "songs/$collection_name/";
                $GLOBALS['collection_data'][$collection_name]['relative_path'] = "songs/$collection_name/";
                $GLOBALS['collection_data'][$collection_name]['file_path'] = $GLOBALS['songbook_data_path'] . $file;
                $GLOBALS['collection_data'][$collection_name]['file_name'] = $file;

            }
            
        }
    closedir($dir);
    easter_followup_init($filter);
}


function easter_followup_init($filter) {
    // TODO:
}

// yeah, PHP 8+ has str_ends_with(), I don't
function ends_with($string, $wannabe_suffix) {
    $length = strlen($wannabe_suffix);
    if(!$length) {
        return true;
    }
    return substr( $string, -$length ) === $wannabe_suffix;
}

function is_song_active($song) {
    return $song['active'];
}

?>