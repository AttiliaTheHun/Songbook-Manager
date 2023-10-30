<?php
$tokens_file_path = '../../../data/tokens.json';
include '../../../lib/lib_auth.php';

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    auth_init();
    if ($token->has_read_permission()) {
        $filename = 'index.json';
        $file = '../../../data/index.json';
        header('Content-Type: application/json');         
        header("Content-Disposition: attachment;filename=$filename"); 
        readfile($file); 
    } else {
        http_response_code(403);
        include "../../../resources/pages/403.php";
        die();
    }
} else {
        http_response_code(404);
        include "../../../resources/pages/404.php";
        die();
}
?>