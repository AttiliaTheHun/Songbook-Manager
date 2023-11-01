<?php
include '../../lib/lib_auth.php';
include '../../lib/lib_save_load.php';

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    auth_init();
    if ($token != null && $token->has_read_permission()) {
        //$filename = 'index.json';
        //TODO
        /*$file = '../../data/index.json';
        header('HTTP/1.1 200 Ok');
        header('Content-Type: application/json');         
        header("Content-Disposition: attachment;filename=$filename"); 
        readfile($file); */
    } else {
        http_response_code(401);
        include '../../resources/pages/401.php';
        die();
    }
} else {
        http_response_code(404);
        include '../../resources/pages/404.php';
        die();
}
?>