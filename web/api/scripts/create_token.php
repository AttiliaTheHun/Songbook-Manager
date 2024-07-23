<?php

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
    
 $request = json_decode($request_body, true);
 
 if (count($request) !== 2 && !(isset($request['name']) && isset($request['permissions']))) {
	 http_response_code(400);
	 echo '{"message": "invalid request parameters"}';
	 exit(0);
 }

 $permissions = [];
 foreach (str_split($request['permissions']) as $bit) {
	 array_push($permissions, $bit === '1');
}
 
  if (count($permissions) === 0) {
	 http_response_code(400); // 503 Bad reqeust
	 echo '{"message": "failed to parse the permissions"}';
	 exit(0);
 }
 
 $token = Token::generate($request['name'], $permissions);
 
 if ($token === NULL) {
	 http_response_code(503); // 503 Service unavalable
	 echo '{"message": "could not generate the token"}';
	 exit(0);
 }
 
 register_token($token);
 
 http_response_code(201); // 201 Created
 echo '{"message": "'. $token->get_token() .'"}';
 exit(0);
 

?>