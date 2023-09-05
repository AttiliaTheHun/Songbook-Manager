<?php
/** auth.php
* This file contains the logic behind user authentification to allow data manipulation only to authorized users. The application
* manages a simple list of tokens of various permissions. Functions in this file can be used to retrieve a token data from the list
* and verify its permissions. This file is meant to be embedded using php 'include' utility and is relied on heavily in api endpoints.
*/

const $tokens_file_path = '../tokens.json';
$tokens_file_contents = file_get_contents($tokens_file_path);
$tokens = json_decode($tokens_file_contents, true);

$token;

/**
* Scans the local token list for the presented token. Returns its complete representation when found and null otherwise.
* @param token the authentification token's textdomain
* @return a Token object or null
*/
function get_token_data($token) {
}

?>