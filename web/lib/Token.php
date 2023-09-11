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
	
	function getToken() {
	    return $this->token;
	}
	
	function getName() {
	    return $this->name;
	}
	
	function getCreatedAt() {
	    return $this->created_at;
	}
	
	function isFrozen() {
	    return $this->frozen;
	}
	
	function hasReadPermission() {
	    return $this->READ_PERMISSION;
	}
	
	function hasWritePermission() {
	    return $this->WRITE_PERMISSION;
	}
	
	function hasBackupPermission() {
	    return $this->BACKUP_PERMISSION;
	}
	
	function hasRestorePermission() {
	    return $this->RESTORE_PERMISSION;
	}
	
	function hasManageTokensPermission() {
	    return $this->MANAGE_TOKENS_PERMISSION;
	}
	
	function freeze() {
	    $this->frozen = true;
	}
	
	public function set($data) {
        foreach ($data AS $key => $value) $this->{$key} = $value;
    }
 	
}


?>