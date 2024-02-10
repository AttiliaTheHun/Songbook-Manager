<?php
include dirname(__FILE__).'/lib_hash.php';
include dirname(__FILE__).'/lib_collection.php';

class Index {
    private $data = [];
    private $hashes = [];
    private $metadata = [];
    private $collections = [];
    
    public function __construct() {
        if (func_num_args() == 1) {
		    $this->set(func_get_arg(0));
		}
	}
	
	public function getData() {
	    return $this->data;
	}
	
	public function getHashes() {
	    return $this->hashes;
	}
	
	public function getMetadata() {
	    return $this->metadata;
	}
	
	public function getCollections() {
	    return $this->collections;
	}
    
    public function set($data) {
        foreach ($data AS $key => $value) $this->{$key} = $value;
    }
}

$index_file_path = dirname(__FILE__) . '/../data/index.json';
$song_data_path = dirname(__FILE__).('/../data/songbook/songs/html/');
$easter_song_data_path = dirname(__FILE__).('/../data/songbook/songs/egg/');

/**
 * Initializes the index of the data either from a file or by generating a new one. The index is then available
 * as $GLOBALS['index']. This action must be performed before any operation that works with the index.
 **/
function init_index() {
    if (file_exists($GLOBALS['index_file_path'])) {
        $index_file_contents = file_get_contents($GLOBALS['index_file_path']);
        $GLOBALS['index'] = new Index(json_decode($index_file_contents, true));
    } else {
        $GLOBALS['index'] = generate_index();
    }
}

/**
 * Saves the index in $GLOBALS['index'] into a file.
 **/
function save_index() {
    file_put_contents($index_file_path, json_encode($GLOBALS['index']));
}

function set_version_timestamp(long $version_timestamp) {
    $metadata = $GLOBALS['index']->getMetadata();
    $metadata['version_timestamp'] = $version_timestamp;
    $GLOBALS['index']->set(array("metadata" => $metadata));
}

function index_new_songs(array $songs) {
    $current_songs = $GLOBALS['index']->getData()['standard'];
    $current_songs_hashes = $GLOBALS['index']->getHashes()['standard'];
    $mixed = array_combine($current_songs, $current_songs_hashes);
    for ($x = 0; $x < len($songs); $x++) {
        $mixed[$songs[$x]] = get_file_hash($GLOBALS['song_data_path '].$songs[$x]);
    }
    ksort($mixed, SORT_NATURAL);
    $temp_data = $GLOBALS['index']->getData();
    $temp_data['standard'] = array_keys($mixed);
    $GLOBALS['index']->set(json_encode($temp_data));
    $temp_hashes = $GLOBALS['index']->getHashes();
    $temp_hashes['standard'] = array_values($mixed);
    $GLOBALS['index']->set(json_encode($temp_hashes));
}

function index_song_changes() {
    
}

function unindex_songs() {
    
}

function index_collections() {
    $collections = $GLOBALS['index']->getCollections();
    $collections['standard'] = get_file_hash($GLOBALS['collection_file_path']);
    if (file_exists($GLOBALS['easter_collection_file_path'])) {
        $collections['easter'] = get_file_hash($GLOBALS['easter_collection_file_path']);
    }
    $GLOBALS['index']->set($collections);
}

function index_new_easter_songs(array $songs) {
  
}

function index_easter_song_changes() {
    
}

function unindex_easter_songs() {
    
}

function generate_index() {
    $index = new Index();
    $GLOBALS['index_temp_version_timestamp'] = -1;
    foreach (array_keys($GLOBALS['collections']) as $collection) {
        $index->getCollections()[$collection] = get_file_hash($GLOBALS['collection_data'][$collection]['file_path']);
        $map = create_file_hash_map($GLOBALS['collection_data'][$collection]['data_path']);
        $index->getData()[$collection] = array_keys($map);
        $index->getHashes()[$collection] = array_values($map);
        if (filemtime($GLOBALS['collection_data'][$collection]['data_path']) > $GLOBALS['index_temp_version_timestamp']) {
                $GLOBALS['index_temp_version_timestamp'] = filemtime($GLOBALS['collection_data'][$collection]['data_path']);
            }
    }
    $index->getMetadata()['version_timestamp'] = $GLOBALS['index_temp_version_timestamp'];
    return $index;
}


function create_file_hash_map($path) {
    $map = array();
    while ($file = readdir($path)) {
            if ($file == '.' || $file == '..') continue;
            $map[$file] = get_file_hash($file);
            if (filemtime($file) > $GLOBALS['index_temp_version_timestamp']) {
                $GLOBALS['index_temp_version_timestamp'] = filemtime($file);
            }
    }
    return $map;
}

?>