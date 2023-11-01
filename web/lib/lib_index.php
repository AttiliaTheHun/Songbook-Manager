<?php
include dirname(__FILE__).'/lib_hash.php';
include dirname(__FILE__).'/lib_collection.php';

class Index {
    private $data = (object)[];
    private $hashes = (object)[];
    private $metadata = [];
    private $collections = (object)[];
    private $default_client_settings = (object)[];
    
    public function __construct() {
        if (func_num_args() == 1) {
		    $this->set(func_get_arg(0));
		}
	}
	
	public function get_data() {
	    return $this->$data;
	}
	
	public function get_hashes() {
	    return $this->$hashes;
	}
	
	public function get_metadata() {
	    return $this->$metadata;
	}
	
	public function get_collections() {
	    return $this->$collections;
	}
	
	public function get_default_client_settings() {
	    $this->$default_client_settings;
	}
    
    public function set($data) {
        foreach ($data AS $key => $value) $this->{$key} = $value;
    }
}

$index_file_path = dirname(__FILE__) . '/../data/index.json';
$song_data_path = dirname(__FILE__).('/../data/songbook/songs/html/');
$easter_song_data_path = dirname(__FILE__).('/../data/songbook/songs/egg/');

function init_index() {
    $index_file_contents = file_get_contents($GLOBALS['index_file_path']);
    $GLOBALS['index'] = new Index($index_file_contents);
}

function save_index() {
    file_put_contents($index_file_path, json_encode($GLOBALS['index']));
}

function set_version_timestamp(long $version_timestamp) {
    $metadata = $GLOBALS['index']->get_metadata();
    $metadata['version_timestamp'] = $version_timestamp;
    $GLOBALS['index']->set(array("metadata" => $metadata));
}

function index_new_songs(array $songs) {
    $current_songs = $GLOBALS['index']->get_data()['standard'];
    $current_songs_hashes = $GLOBALS['index']->get_hashes()['standard'];
    $mixed = array_combine($current_songs, $current_songs_hashes);
    for ($x = 0; $x < len($songs); $x++) {
        $mixed[$songs[$x]] = get_file_hash($GLOBALS['song_data_path '].$songs[$x]);
    }
    ksort($mixed, SORT_NATURAL);
    $temp_data = $GLOBALS['index']->get_data();
    $temp_data['standard'] = array_keys($mixed);
    $GLOBALS['index']->set(json_encode($temp_data));
    $temp_hashes = $GLOBALS['index']->get_hashes();
    $temp_hashes['standard'] = array_values($mixed);
    $GLOBALS['index']->set(json_encode($temp_hashes));
}

function index_song_changes() {
    
}

function unindex_songs() {
    
}

function index_collections() {
    $collections = $GLOBALS['index']->get_collections();
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

?>