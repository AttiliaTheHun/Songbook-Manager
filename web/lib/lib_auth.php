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
	    if (func_num_args() == 9) {
	        
	        $this->token = func_get_arg(0);
	        $this->name = func_get_arg(1);
	        $this->created_at = func_get_arg(2);;
	        $this->frozen = func_get_arg(3);
	        $this->READ_PERMISSION = func_get_arg(4);
	        $this->WRITE_PERMISSION = func_get_arg(5);
	        $this->BACKUP_PERMISSION = func_get_arg(6);
	        $this->RESTORE_PERMISSION = func_get_arg(7);
	        $this->MANAGE_TOKENS_PERMISSION = func_get_arg(8);
		    return;
		} elseif (func_num_args() == 1) {
		    $this->set(func_get_arg(0));
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
    
    public static function generate(string $secret, string $name, mixed $permissions, boolean $phrase) {
        if (len($secret) == 0) {
            $secret = base64_encode(random_bytes(10));
        }
        if ($phrase === true) {
            $secret = base64_encode($secret);
        }
        return new Token($secret, $name, date("d-m-Y"), $permissions[0], $permissions[1], $permissions[2], $permissions[3], $permissions[4]);
    }
 	
}

$tokens_file_path = dirname(__FILE__) . '/../data/tokens.json';

/**
* Scans the local token list for the presented token. Returns its complete representation when found and null otherwise.
* @param token the authentification token's value
* @return a Token object or null
*/
function get_token_object($token) {
    for ($x = 0; $x < count($GLOBALS['tokens']); $x++) {
        if ($GLOBALS['tokens'][$x]['token'] == $token) {
            return new Token(json_decode(json_encode($GLOBALS['tokens'][$x]), true));
        }
    }
    return null;
}

function get_request_token() {
    $headers = apache_request_headers();

    foreach ($headers as $header => $value) {
        if ($header == 'Authorization' && substr($value, 0, 7) === 'Bearer ') {
            return trim(substr($value, 7));
        }
    }
    return null;
}

function auth_init() {
    $tokens_file_contents = file_get_contents($GLOBALS['tokens_file_path']);
    $GLOBALS['tokens'] = json_decode($tokens_file_contents, true);
    $GLOBALS['token'] = get_token_object(get_request_token());
}

function register_token(mixed $token) {
    array_push($GLOBALS['tokens'], $token);
    file_put_contents($GLOBALS['tokens_file_path'], $GLOBALS['tokens']);
    save_tokens();
}

function save_tokens() {
    file_put_contents($GLOBALS['tokens_file_path'], json_encode($GLOBALS['tokens']));
}


?>