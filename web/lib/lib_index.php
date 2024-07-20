<?php
/**
 * A library that allows work with songbook indices.
 **/

require_once dirname(__FILE__).'/lib_hash.php';
require_once dirname(__FILE__).'/lib_init.php';

class Index implements JsonSerializable {
    private $data = [];
    private $hashes = [];
    private $metadata = [];
    private $collections = [];
    
    public function __construct() {
        if (func_num_args() == 1) {
		    $this->set(func_get_arg(0));
		}
	}
	
	public function jsonSerialize() {
        return [
            'data' => $this->data,
            'hashes' => $this->hashes,
            'metadata' => $this->metadata,
            'collections' => $this->collections
            ];
    }
	
	public function getData() {
	    return $this->data;
	}
	
	public function addData($key, $value) {
	    $this->data[$key] = $value;
	}
	
	public function getHashes() {
	    return $this->hashes;
	}
	
	public function addHashes($key, $value) {
	    $this->hashes[$key] = $value;
	}
	
	public function getMetadata() {
	    return $this->metadata;
	}
	
	public function addMetadata($key, $value) {
	    $this->metadata[$key] = $value;
	}
	
	public function getCollections() {
	    return $this->collections;
	}
	
	public function addCollection($name, $value) {
	    $this->collections[$name] = $value;
	}
    
    public function set($data) {
        foreach ($data AS $key => $value) $this->{$key} = $value;
    }
}

/**
 * Initializes the index of the data either from a file or by generating a new one. The index is then available
 * as $GLOBALS['index']. This action must be performed before any operation that works with the index.
 **/
function init_index() {
    if (file_exists($GLOBALS['index_file_path'])) {
        $index_file_contents = file_get_contents($GLOBALS['index_file_path']);
        $GLOBALS['index'] = new Index(json_decode($index_file_contents, true));
    } else {
        regenerate_index();
    }
}

/**
 * Saves the index in $GLOBALS['index'] into a file.
 **/
function save_index() {
    file_put_contents($GLOBALS['index_file_path'], json_encode($GLOBALS['index']));
}

/**
 * Generates an index that corresponds to the current state of the songbook
 * 
 * @returns an Index object that is the index to the songbook
 **/
function generate_index() {
    $index = new Index();
    $version_timestamp = -1;
    
    // we will index every registered collection
    foreach (array_keys($GLOBALS['collections']) as $collection_name) {
        $songs = [];
        $hashes = [];
          
        // add collection file hash
        $collection_checksum = get_file_hash($GLOBALS['collection_data'][$collection_name]['file_path']);
        
        $tempstamp = filemtime($GLOBALS['collection_data'][$collection_name]['file_path']);
        $version_timestamp = max($version_timestamp, $tempstamp);
 
        // we index every song in the collection by mapping it with its hash
        // we also need to keep track of file modification dates throughout the process
        foreach ($GLOBALS['collections'][$collection_name] as $song) {
            $filename = $song['id'] . '.html';
            array_push($songs, $filename);
            array_push($hashes, get_file_hash($GLOBALS['collection_data'][$collection_name]['data_path'] . $filename));
            $tempstamp = filemtime($GLOBALS['collection_data'][$collection_name]['data_path'] . $filename);
            $version_timestamp = max($version_timestamp, $tempstamp);
            
        }
        // now we write what we found to the index
        $index->addData($collection_name, $songs);
        $index->addHashes($collection_name, $hashes);
        $index->addCollection($collection_name, $collection_checksum);
    }
    
    // finally set the timestamp
    $index->addMetadata('version_timestamp', $version_timestamp);

    return $index;
}

/**
 * What do you think? 
 **/
function regenerate_index() {
    $GLOBALS['index'] = generate_index();
    save_index();
}

init_index();

?>