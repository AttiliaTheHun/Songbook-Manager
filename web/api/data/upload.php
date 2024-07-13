<?php
/**
 * This scripts processes requests for data uploads. It is an authenticated API route.
 */

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    include(dirname(__FILE__) . '/../../resources/pages/405.php');
    die();
}

// first we need to authenticate the user
require(dirname(__FILE__) . '/../../lib/lib_auth.php');
auth_init();

// if the token is invalid, we abort
if ($token == NULL || !$token->has_write_permission()) {
    http_response_code(401);
    include(dirname(__FILE__) . '/../../resources/pages/401.php');
    die();
}

$request_body = file_get_contents('php://input');

// this action is all about input, so we can as well abort when we have none
if ($request_body == NULL || strlen($request_body) == 0) {
    http_response_code(400); // 400 Bad Request
    echo "request body expected";
    die();
}

require(dirname(__FILE__) . '/../../lib/lib_save_load.php');
require(dirname(__FILE__) . '/../../lib/lib_action_log.php');

// if such a file already existed for a parallel request, we do not want to accidentally overwrite it
$num = -1;
do {
    $num += 1;
    $archive_name = $GLOBALS['temp_path'] . "save_request$num.zip";
} while (file_exists($archive_name));

// we save the request locally so we can work with it
file_put_contents($archive_name, $request_body);

$success = parse_save_request($archive_name);

// we no longer need the file
unlink($archive_name);

if (is_array($success) && $success[0] === true) {
    log_action(ACTION_UPLOAD, $token, $success[1]);
    http_response_code(201); // 201 Created
    exit(0);
} else {
    http_response_code(400); // 400 Bad Request
    if ($success === false) {
        echo "could not parse the request body";
    } else { // otherwise contains a string message
        echo $success;
    }
    die();
}

?>