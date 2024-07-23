<?php
/**
 * This script processed the actual data related requests. 
 **/

require_once(dirname(__FILE__) . '/../../lib/lib_auth.php');
auth_init();

function starts_with($string, $prefix) {
    return ($string === $prefix) ? true : substr($string, 0, strlen($prefix)) === $prefix;
}

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    handle_data_download();
} else {

    // we check the HTTP headers
    $content_type = "";
    $content_disposition = "";
    $headers = apache_request_headers();

    foreach ($headers as $header => $value) {
        if ($header == 'Content-Type') {
            if (starts_with($value, 'application/zip')) {
                $content_type = 'upload';
            } else if (starts_with($value, 'application/json')) {
                $content_type = 'download';
            }
            continue;
        }
        if ($header == 'Content-Disposition') {
            if ($value == 'attachment;filename=save_request.zip' || $value == 'attachment; filename=save_request.zip') {
                $content_disposition = 'upload';
            } else if ($value == 'attachment;filename=index.json' || $value == 'attachment; filename=index.json') {
                $content_disposition = 'download';
            }
            continue;
        }
    }
    
     // headers are weird, abort
    if ($content_type !== $content_disposition) {
        http_response_code(400); // Bad request
        echo '{"message": "invalid request headers"}';
        die();
    }
   
    if ($content_type === 'save') {
        handle_data_upload();
    } else {
        handle_data_download(true);
    }
}

function handle_data_download($has_index = false) {
    // if the token is invalid, we abort
    if ($GLOBALS['token'] == NULL || !$GLOBALS['token']->has_read_permission()) {
        http_response_code(401);
        include(dirname(__FILE__) . '/../../resources/pages/401.php');
        die();
    }
	
    require_once(dirname(__FILE__) . '/../../lib/lib_save_load.php');
    require_once(dirname(__FILE__) . '/../../lib/lib_action_log.php');
    
    if (!$has_index) {
        // in this case we simply create a zip archive with all of the songbooks data and send it back
        $archive_name = create_complete_load_request_response_archive();
		
		if (!file_exists($archivename)) {
			http_response_code(500) // 500 Internal server error
			die('{"message": "failed to create the response archive file"}');
		}
		
        // now we sent the archive to the client
        header('HTTP/1.1 200 Ok');
        header('Content-Type: application/zip');         
        header("Content-Disposition: attachment;filename=data.zip"); 
        readfile($archive_name);
        // current setup says that request_file_exists -> create new request file, so we need to cleanup afterwards
        // to prevent hoarding request files
        unlink($archive_name);
        log_action(ACTION_DOWNLOAD, $GLOBALS['token']);
        exit(0);
    }
    
    // here we are dealing with a customized load request that should have its index
    $request_body = file_get_contents('php://input');
    
    $request_index = json_decode($request_body, true);
    
    // the index can not be parsed to an associated array, abort
    if (!is_array($request_index)) {
        http_response_code(400); // Bad request
        echo '{"message": "invalid request index"}';
        die();
    }
    
    $archive_name = create_partial_load_request_response_archive($request_index);
	
	if (!file_exists($archive_name)) {
		http_response_code(500)
		die('{"message": "failed to create the response archive file"}');
	}
	
    // now we sent the archive to the client
    header('HTTP/1.1 200 Ok');
    header('Content-Type: application/zip');         
    header("Content-Disposition: attachment;filename=load_request.zip"); 
    readfile($archive_name);
    // no need to store the file locally after it is sent
    unlink($archive_name);
    log_action(ACTION_DOWNLOAD, $GLOBALS['token']);
    exit(0);
    
}

function handle_data_upload() {
    // if the token is invalid, we abort
    if ($GLOBALS['token'] == NULL || !$GLOBALS['token']->has_write_permission()) {
        http_response_code(401);
        include(dirname(__FILE__) . '/../../resources/pages/401.php');
        die();
    }

    $request_body = file_get_contents('php://input');

    // this action is all about input, so we can as well abort when we have none
    if ($request_body == NULL || strlen($request_body) == 0) {
        http_response_code(400); // 400 Bad Request
        echo '{"message": "request body expected"}';
        die();
    }

    require(dirname(__FILE__) . '/../../lib/lib_save_load.php');
    require(dirname(__FILE__) . '/../../lib/lib_action_log.php');

    // if such a file already existed for a parallel request, we do not want to accidentally overwrite it
    $num = -1;
    do {
        $num += 1;
        $archive_name = $GLOBALS['temp_path'] . "save_request$num.zip";
    } while (file_exists($archive_name));

    // we save the request locally so we can work with it
    file_put_contents($archive_name, $request_body);

    $success = parse_save_request($archive_name);

    // we no longer need the file
    unlink($archive_name);

    if (is_array($success) && $success[0] === true) {
        log_action(ACTION_UPLOAD, $GLOBALS['token'], $success[1]);
        http_response_code(201); // 201 Created
        exit(0);
    } else {
        http_response_code(400); // 400 Bad Request
        if ($success === false) {
            echo '{"message": "could not parse the request body"}';
        } else { // otherwise contains a string message
            echo '{"message": "' . $success . '"}';
        }
        die();
    }
}



?>