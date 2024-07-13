<?php
/**
 * This library is used to authenticate HTTP requests on the API. It features basic token authentication.
 **/
 
class Token {
    
    private $token = "";
    private $name = "";
    private $created_at = "";
    private $frozen = false;
    private $permissions = [
        "READ_PERMISSION" => true,
        "WRITE_PERMISSION" => false,
        "BACKUP_PERMISSION" => false,
        "RESTORE_PERMISSION" => false,
        "MANAGE_TOKENS_PERMISSION" => false
        ];
	
	public function __construct() {
	    if (func_num_args() == 9) {
	        
	        $this->token = func_get_arg(0);
	        $this->name = func_get_arg(1);
	        $this->created_at = func_get_arg(2);
	        $this->frozen = func_get_arg(3);
	        $this->permissions['READ_PERMISSION'] = func_get_arg(4);
	        $this->permissions['WRITE_PERMISSION'] = func_get_arg(5);
	        $this->permissions['BACKUP_PERMISSION'] = func_get_arg(6);
	        $this->permissions['RESTORE_PERMISSION'] = func_get_arg(7);
	        $this->permissions['MANAGE_TOKENS_PERMISSION'] = func_get_arg(8);
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
	    return $this->frozen ? false : $this->permissions['READ_PERMISSION'];
	}
	
	function has_write_permission() {
	    return $this->frozen ? false : $this->permissions['WRITE_PERMISSION'];
	}
	
	function has_backup_permission() {
	    return $this->frozen ? false : $this->permissions['BACKUP_PERMISSION'];
	}
	
	function has_restore_permission() {
	    return $this->frozen ? false : $this->permissions['RESTORE_PERMISSION'];
	}
	
	function has_manage_tokens_permission() {
	    return $this->frozen ? false : $this->permissions['MANAGE_TOKENS_PERMISSION'];
	}
	
	function freeze() {
	    $this->frozen = true;
	}
	
	public function set($data) {
        foreach ($data AS $key => $value) $this->{$key} = $value;
    }
    
    public static function generate(string $secret = NULL, string $name, mixed $permissions) {
        if ($secret == NULL) {
            $secret = base64_encode(random_bytes(10));
        } else {
            $secret = base64_encode($secret);
        }
        return new Token($secret, $name, date("d-m-Y"), $permissions[0], $permissions[1], $permissions[2], $permissions[3], $permissions[4]);
    }
    
    public function to_string() {
        $output = $this->name . " " . $this->created_at;
        if ($this->frozen) {
            $output .= " frozen";
        }
        $output .= " ";
        foreach($this->permissions as $permission => $value) {
            if (value) {
                $output .= '1';
            } else {
                $output .= '0';
            }
        }
        return $output;
    }
 	
}

$tokens_file_path = dirname(__FILE__) . '/../data/tokens.json';

/**
* Scans the local token list for the presented token. Returns its complete representation when found and null otherwise.
* 
* @param $token the authentification token's value
* @return a Token object or null
*/
function get_token_object($token) {
    for ($x = 0; $x < count($GLOBALS['tokens']); $x++) {
        if ($GLOBALS['tokens'][$x]['token'] == $token) {
            return new Token(json_decode(json_encode($GLOBALS['tokens'][$x]), true));
        }
    }
    return NULL;
}

/**
 * Extracts the token from HTTP request and if found returns its string value.
 * 
 * @return token value string of the request token or null
 **/
function get_request_token() {
    $headers = apache_request_headers();

    foreach ($headers as $header => $value) {
        if ($header == 'Authorization' && substr($value, 0, 7) === 'Bearer ') {
            return trim(substr($value, 7));
        }
    }
    return NULL;
}

/**
 * Initializes the local token database into $GLOBALS['tokens']. Must be called in advance to any other functions
 * from this library. It also automatically initializes the token of the current request into $GLOBALS['token'].
 **/
function auth_init() {
    $tokens_file_contents = file_get_contents($GLOBALS['tokens_file_path']);
    $GLOBALS['tokens'] = json_decode($tokens_file_contents, true);
    $GLOBALS['token'] = get_token_object(get_request_token());
}

/**
 * Registers a new token to the database.
 * 
 * @param $token the token to register
 **/
function register_token(mixed $token) {
    array_push($GLOBALS['tokens'], $token);
    save_tokens();
}

/**
 * Saves the token database.
 **/
function save_tokens() {
    file_put_contents($GLOBALS['tokens_file_path'], json_encode($GLOBALS['tokens']));
}
?>