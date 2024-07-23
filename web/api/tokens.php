<?php
/**
 * This is the API endpoint for management of authentication tokens. Authentication is required to use the API this way.
 */
 
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    include(dirname(__FILE__) . '/scripts/create_token.php');
}

if ($_SERVER['REQUEST_METHOD'] === 'PUT') {
    include(dirname(__FILE__) . '/scripts/freeze_token.php');
}

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    http_response_code(405);
    include(dirname(__FILE__) . '/../resources/pages/405.php');
    die();
}

// first we need to authenticate the user
require(dirname(__FILE__) . '/../lib/lib_auth.php');
auth_init();

// if the token is invalid, we abort
if ($token == NULL || !$token->has_manage_tokens_permission()) {
    http_response_code(401);
    include(dirname(__FILE__) . '/../resources/pages/401.php');
    die();
}

$stringified_tokens = [];

foreach ($GLOBALS['tokens'] as $token_mixed) {
    $token_object = get_token_object($token_mixed['token']);
    array_push($stringified_tokens, $token_object->to_string());
}

http_response_code(200); // 200 Ok
header('Content-Type: application/json');         
header("Content-Disposition: attachment;filename=tokens.json");
echo json_encode($stringified_tokens);
exit(0);

?>