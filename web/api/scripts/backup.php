<?php
/**
 * This script creates a complete backup of the songbook. This file is not meant to be accessed through HTTP
 * directly.
 */

// first we need to authenticate the user
require(dirname(__FILE__) . '/../../lib/lib_auth.php');
auth_init();

// if the token is invalid, we abort
if ($token == NULL || !$token->has_backup_permission()) {
    http_response_code(401);
    include(dirname(__FILE__) . '/../../resources/pages/401.php');
    die();
}

require(dirname(__FILE__) . '/../../lib/lib_backup_restore.php');
require(dirname(__FILE__) . '/../../lib/lib_action_log.php');

$filename = create_complete_backup();

log_action(ACTION_BACKUP, $token, $filename);
http_response_code(201); // 201 Created
echo '{"message": "Backup created under ' . $filename . '"}';
exit(0);

?>