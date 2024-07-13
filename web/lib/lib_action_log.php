<?php
/**
 * This library allows the management of action log file.
 **/
const ACTION_UPLOAD = "UPLOAD";
const ACTION_DOWNLOAD = "DOWNLOAD";
const ACTION_BACKUP = "BACKUP";
const ACTION_RESTORE = "RESTORE";

$action_log_file_path = dirname(__FILE__) . "/../data/action_log.txt";

function log_action(string $action, Token $token = NULL, string $backup_file_name = "") {
    if ($backup_file_name == NULL) {
        $backup_file_name = "";
    }
    
    $date = date_create();
   
    $data = date_format($date,"[Y/m/d H:i:s]") . " " . $action ;
    if ($token !== NULL) {
        $data .= " " . $token->get_name();
    }
    $data .= " " . $backup_file_name;
    file_put_contents($GLOBALS['action_log_file_path'], $data . PHP_EOL, FILE_APPEND | LOCK_EX);
}

function clear_log() {
    file_put_contents($GLOBALS['action_log_file_path'], "");
}

?>