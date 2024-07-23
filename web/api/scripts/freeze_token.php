<?php
/**
 * This script handles the freezing of authentication tokens. This file is not meant to be accessed through HTTP
 * directly.
 */

// first we need to authenticate the user
require(dirname(__FILE__) . '/../../lib/lib_auth.php');
auth_init();

// if the token is invalid, we abort
if ($token == NULL || !$token->has_manage_tokens_permission()) {
    http_response_code(401);
    include(dirname(__FILE__) . '/../../resources/pages/401.php');
    die();
}

$request_body = file_get_contents('php://input');

// this action is all about input, so we can as well abort when we have none
if ($request_body == NULL || strlen($request_body) == 0) {
    http_response_code(400); // 400 Bad Request
    echo '{"message": "request body expected"}';
    die();
}

$request = json_decode($request_body, true);

foreach ($request['freeze'] as $index) {
    $GLOBALS['tokens'][$index]['frozen']['frozen'] = true;
}
save_tokens();

http_response_code(200); // 200 Ok
exit(0);

?>