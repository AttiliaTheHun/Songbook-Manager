<?php

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    process_request();
} else {
    http_response_code(405); // 405 Method not allowed
    include(dirname(__FILE__) . '/../resources/pages/405.php');
    die();
}

// technically this code should never be reached
http_response_code(503); // 503 Service unavailable
die();

function process_request() {
	require_once(dirname(__FILE__) . '/../lib/lib_auth.php');
	auth_init();
	
	 if ($GLOBALS['token'] == NULL || !($GLOBALS['token']->has_restore_permission() || $GLOBALS['token']->has_manage_tokens_permission())) {
        http_response_code(401);
        include(dirname(__FILE__) . '/../resources/pages/401.php');
        die();
    }
	require_once(dirname(__FILE__) . '/../lib/lib_action_log.php');
	
	http_response_code(200);
	
	if (!file_exists($GLOBALS['action_log_file_path']) || filesize($GLOBALS['action_log_file_path']) === 0) {
		echo '{"message": "action log not available"}';
		exit(0);
	}
	
	header('Content-Type: application/txt');         
    header("Content-Disposition: attachment;filename=action_log.txt");
	readfile($GLOBALS['action_log_file_path']);
	exit(0);
}

?>