<?php
/**
 * Initializes variables and loads libraries necesary for most of the other libraries.
 **/

require_once dirname(__FILE__) . '/lib_env_var.php';
require_once dirname(__FILE__) . '/lib_settings.php';

// yeah, PHP 8+ has str_ends_with(), I don't
function ends_with($string, $wannabe_suffix) {
    $length = strlen($wannabe_suffix);
    return $length > 0 ? substr( $string, -$length ) === $wannabe_suffix : true;
}

function init() {
    $collections = [];
    $collection_data = [];
    
    $files = scandir($GLOBALS['songbook_data_path']);
    foreach($files as $file) {
        if (ends_with($file, '_collection.json')) {
            $dummy_collection_data = [];
            $name = substr($file, 0, strlen($file) - strlen('_collection.json'));
            $dummy_collection_data['file_path'] = $GLOBALS['songbook_data_path'].$file;
            $dummy_collection_data['relative_path'] = "songs/$name/";
            $dummy_collection_data['data_path'] = $GLOBALS['songbook_data_path'].$dummy_collection_data['relative_path'];
            $dummy_collection_data['file_name'] = $file;
            $collection_data[$name] = $dummy_collection_data;
            $collection_data_json = file_get_contents($collection_data[$name]['file_path']);
            $collections[$name] = json_decode($collection_data_json, true);
        }
    }
    $GLOBALS['collections'] = $collections;
    $GLOBALS['collection_data'] = $collection_data;
}

init();


?>