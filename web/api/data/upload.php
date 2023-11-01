<?php
include '../../lib/lib_auth.php';
include '../../lib/lib_save_load.php';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    auth_init();
    if ($token != null && $token->has_write_permission()) {
        header('HTTP/1.1 200 Ok');
        header('Content-Type: application/json');
        save();
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