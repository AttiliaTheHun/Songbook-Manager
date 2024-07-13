<?php
/**
 * This scripts processes requests for data downloads. It is an authenticated API route.
 */

// GET is for empty request body, POST may have a request body (a load index)
if ($_SERVER['REQUEST_METHOD'] !== 'GET' && $_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    include(dirname(__FILE__) . '/../../resources/pages/405.php');
    die();
}

// first we need to authenticate the user
require(dirname(__FILE__) . '../../lib/lib_auth.php');
auth_init();

// if the token is invalid, we abort
if ($token == NULL || !$token->has_read_permission()) {
    http_response_code(401);
    include(dirname(__FILE__) . '/../../resources/pages/401.php');
    die();
}

// getting here means everything is ok and we can actually process the request
    
require(dirname(__FILE__) . '/../../lib/lib_save_load.php');
require(dirname(__FILE__) . '/../../lib/lib_action_log.php');

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
    // we need to verify the data we received now
    // first we check the HTTP headers
    $content_type_correct = false;
    $content_disposition_correct = false;
    $headers = apache_request_headers();

    foreach ($headers as $header => $value) {
        if ($header == 'Content-Type' && $value == 'application/json') {
            $content_type_correct = true;
            continue;
        }
        if ($header == 'Content-Disposition' && $value == 'attachment;filename=index.json') {
            $content_disposition_correct = true;
            continue;
        }
    }
    // headers are weird, abort
    if (!$content_type_correct || !$content_disposition_correct) {
        http_response_code(400); // Bad request
        echo "invalid request headers";
        die();
    }
    
    $request_index = json_decode($request_body, true);
    
    // the index can not be parsed to an associated array, abort
    if (!is_array($request_index)) {
        http_response_code(400); // Bad request
        echo "invalid request index";
        die();
    }
    
    $archive_name = create_partial_load_request_response_archive($request_index);
    // now we sent the archive to the client
    header('HTTP/1.1 200 Ok');
    header('Content-Type: application/zip');         
    header("Content-Disposition: attachment;filename=load_request.zip"); 
    readfile($archive_name);
    // no need to store the file locally
    unlink($archive_name);
    log_action(ACTION_DOWNLOAD, $token);
    exit(0);
}
 
?>