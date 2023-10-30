<?php
$ACTION_UPLOAD = "UPLOAD";
$ACTION_DOWNLOAD = "DOWNLOAD";
$ACTION_BACKUP = "BACKUP";
$ACTION_RESTORE = "RESTORE";


function log_action(string $action, mixed $token, string $backup_file_name) {
    //$action_log_file_path = "../data/action_log.txt";

    $date=date_create();
   
    $data = date_format($date,"[Y/m/d H:i:s]") . " " . $action . $token->getName() . " " . $backup_file_name;
    file_put_contents($action_log_file_path, $data, FILE_APPEND | LOCK_EX);
}


?>