<?php
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

function init_index() {
    $index_file_contents = file_get_contents($GLOBALS['index_file_path']);
    $GLOBALS['index'] = new Index($GLOBALS['index_file_contents']);
}

?>