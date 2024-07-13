<?php
/**
 * This endpoint lists the authentication tokens registered on the server. It is an authenticated API route.
 */

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    http_response_code(405);
    include(dirname(__FILE__) . '/../../resources/pages/405.php');
    die();
}

// first we need to authenticate the user
require(dirname(__FILE__) . '/../../lib/lib_auth.php');
auth_init();

// if the token is invalid, we abort
if ($token == NULL || !$token->has_manage_tokens_permission()) {
    http_response_code(401);
    include(dirname(__FILE__) . '/../../resources/pages/401.php');
    die();
}

$stringified_tokens = [];

foreach ($tokens as $token_mixed) {
    $token_object = get_token_object($token_mixed['token']);
    array_push($stringified_tokens, $token_object->to_string());
}

http_response_code(200); // 200 Ok
echo json_encode($stringified_tokens);
exit(0);

?>