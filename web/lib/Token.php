<?php
class Token {
	public const $READ_PERMISSION;
	public const $WRITE_PERMISSION;
	public const $BACKUP_PERMISSION;
	public const $RESTORE_PERMISSION;
	
	public function __construct() {
		if (func_num_args() == 1) {
			$this->READ_PERMISSION = func_get_arg(0);
			return;
		} elseif (func_num_args() == 2) {
			$this->WRITE_PERMISSION = func_get_arg(1);
			return;
		} elseif (func_num_args() == 3) {
			$this->BACKUP_PERMISSION = func_get_arg(2);
			return;
		} elseif (func_num_args() == 4) {
			$this->RESTORE_PERMISSION = func_get_arg(3);
			return;
		} 
		$this->READ_PERMISSION = true;
		$this->WRITE_PERMISSION = false;
		$this->BACKUP_PERMISSION = false;
		$this->RESTORE_PERMISSION = false;
	}
 	
}


?>