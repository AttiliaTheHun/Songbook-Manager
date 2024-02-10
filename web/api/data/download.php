<?php
/**
 * This scripts processes requests to data downloads. It is an authenticated API route.
 */

// GET is for empty request body, POST may have a request body (a load index)
if ($_SERVER['REQUEST_METHOD'] !== 'GET' && $_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    include '../../resources/pages/405.php';
    die();
}

// first we need to authenticate the user
include '../../lib/lib_auth.php';
auth_init();

// if the token is invalid, we abort
if ($token == NULL || !$token->has_read_permission()) {
    http_response_code(401);
    include '../../resources/pages/401.php';
    die();
}

// getting here means everything is ok and we can actually process the request
    
include '../../lib/lib_save_load.php';
include '../../lib/lib_action_log.php';

$request_body = file_get_contents('php://input');

// if the request body does not contain a load index, we assume the client needs the entirety of our data
if ($request_body == NULL || strlen($request_body) == 0) {
    // in this case we simply create a zip archive with all of the songbooks data and send it back
    $archive_name = create_complete_load_request_response_archive();
    // now we sent the archive to the client
    header('HTTP/1.1 200 Ok');
    header('Content-Type: application/zip');         
    header("Content-Disposition: attachment;filename=data.zip"); 
    readfile($archive_name);
    // current setup says that request_file_exists -> create new request file, so we need to cleanup afterwards
    // to prevent hoarding request files
    unlink($archive_name);
    log_action(ACTION_DOWNLOAD, $token);
    exit(0);
    
} else {
    $request_index = json_decode($request_body, true);
    $archive_name = create_partial_load_request_response_archive($request_index);
    // now we sent the archive to the client
    header('HTTP/1.1 200 Ok');
    header('Content-Type: application/zip');         
    header("Content-Disposition: attachment;filename=load_request.zip"); 
    readfile($archive_name);
    
    unlink($archive_name);
    log_action(ACTION_DOWNLOAD, $token);
    exit(0);
}
 
?>