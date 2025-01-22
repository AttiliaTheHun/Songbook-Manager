<?php
include_once(dirname(__FILE__) . '/lib_standard_collection.php');

class EasterCollectionProvider {
    private $COLLECTION_FILE_PATH;
    private $SONG_FILE_PATH;
    const COLLECTION_NAME = "easter";
	
	private $standard_manager = null;
    
    private $collection = [];
    
    public function __construct($standard_manager, $filter = true) {
		$this->standard_manager = $standard_manager;
        $this->COLLECTION_FILE_PATH = dirname(__FILE__) . "/../data/songbook/easter_collection.json";
        $this->SONG_FILE_PATH = dirname(__FILE__) . "/../data/songbook/songs/easter/{}.html";
        
        
        
        $collection_raw = file_get_contents($this->COLLECTION_FILE_PATH);
        $collection = json_decode($collection_raw, true);
        $collection_copy = $collection;
        
        for ($x = 0; $x < count($collection); $x++) {
            $collection[$x]['name'] = iconv('UTF-8', 'ASCII//TRANSLIT', $collection[$x]['name']);
        }
        
        // sort the collection by song name (unreliable for certain languages since 1993)
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
                
        // filter out inactive songs, if needed
        if ($filter) {
            $collection = array_filter($collection, "is_song_active");
        }
        
        $this->collection = $collection;
    }
    
    public function get_collection() {
        return $this->standard_manager->get_collection();
    }
    
    public function get_song_file_path($song_id) {
		for ($x = 0; $x < count($this->collection); $x++) {
			if ($this->collection[$x]['id'] == $song_id) {
				return str_replace("{}", $song_id, $this->SONG_FILE_PATH);
			}	
		}
        return $this->standard_manager->get_song_file_path($song_id);
    }
    
}
/*
function is_song_active($song) {
    return $song['active'];
}*/


?>