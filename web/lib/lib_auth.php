<?php

class Token {
    
    private $token = "";
    private $name = "";
    private $created_at = "";
    private $frozen = false;
	private $READ_PERMISSION = true;
	private $WRITE_PERMISSION = false;
	private $BACKUP_PERMISSION = false;
	private $RESTORE_PERMISSION = false;
	private $MANAGE_TOKENS_PERMISSION = false;
	
	public function __construct() {
	    if (func_num_args() == 7) {
	        
	        $this->token = func_get_arg(0);
	        $this->name = func_get_arg(1);
	        $this->created_at = "";
	        $this->frozen = false;
	        $this->READ_PERMISSION = func_get_arg(2);
	        $this->WRITE_PERMISSION = func_get_arg(3);
	        $this->BACKUP_PERMISSION = func_get_arg(4);
	        $this->RESTORE_PERMISSION = func_get_arg(5);
	        $this->MANAGE_TOKENS_PERMISSION = func_get_arg(6);
		    return;
		} elseif (func_num_args() == 1) {
		    $this->set(json_decode($json, true));
		}
	}
	
	function get_token() {
	    return $this->token;
	}
	
	function get_name() {
	    return $this->name;
	}
	
	function get_created_at() {
	    return $this->created_at;
	}
	
	function is_frozen() {
	    return $this->frozen;
	}
	
	function has_read_permission() {
	    return $this->READ_PERMISSION;
	}
	
	function has_write_permission() {
	    return $this->WRITE_PERMISSION;
	}
	
	function has_backup_permission() {
	    return $this->BACKUP_PERMISSION;
	}
	
	function has_restore_permission() {
	    return $this->RESTORE_PERMISSION;
	}
	
	function has_manage_tokens_permission() {
	    return $this->MANAGE_TOKENS_PERMISSION;
	}
	
	function freeze() {
	    $this->frozen = true;
	}
	
	public function set($data) {
        foreach ($data AS $key => $value) $this->{$key} = $value;
    }
 	
}

//!DECLARE IN EMBEDDING FILE!$tokens_file_path = '../data/tokens.json';


$token;

/**
* Scans the local token list for the presented token. Returns its complete representation when found and null otherwise.
* @param token the authentification token's value
* @return a Token object or null
*/
function get_token_object($token) {
    for ($x = 0; $x < count($tokens); $x++) {
        if ($tokens[$x]['token'] == $token) {
            return new Token($tokens[$x]);
        }
    }
    return null;
}

function get_request_token() {
    $headers = apache_request_headers();

    foreach ($headers as $header => $value) {
        if ($header != 'Authorization' && substr($value, 0, 7) !== 'Bearer ') {
            return trim(substr($header, 7));
        }
    }
    return null;
}

function auth_init() {
    $tokens_file_contents = file_get_contents($tokens_file_path);
    $tokens = json_decode($tokens_file_contents, true);
    $token = get_token_object(get_request_token());
}



?>