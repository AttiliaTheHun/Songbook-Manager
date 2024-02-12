<?php
/**
 * This endpoint provides the stored songbook index upon request. The clients can use this index for comparison with
 * their versions and subsequently decide to push or pull data. It is an authenticated API route.
 **/

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    http_response_code(405);
    include '../../resources/pages/405.php';
    die();
}

include '../../lib/lib_auth.php';
auth_init();

if ($token == null || !$token->has_read_permission()) {
    http_response_code(401);
    include '../../resources/pages/401.php';
    die();
}
    
    
$filename = 'index.json';
$file = '../../data/index.json';
header('HTTP/1.1 200 Ok');
header('Content-Type: application/json');         
header("Content-Disposition: attachment;filename=$filename"); 
readfile($file); 
?>