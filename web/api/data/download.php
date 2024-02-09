<?php
/**
 * This scripts processes requests to data downloads. It is an authenticated API route.
 */

// this is supposed to be a GET operation, for any other HTTP method we abort
if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    http_response_code(404);
    include '../../resources/pages/404.php';
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

$request_body = file_get_contents('php://input');

// if the request body does not contain a load index, we assume the client needs the entirety of our data
if ($request_body == NULL || strlen($request_body) == 0) {
    // in this case we simply create a zip archive with all of the songbooks data and send it back
    $archive = new XZipArchive();
    // if multiple requests are being processed at the same time, they would overwrite each others response file
    $num = -1;
    do {
        $num += 1;
        $archive_name = $temp_path . "load_request$num.zip";
    } while (file_exists($archive_name));
    
    if ($archive->open($archive_name, ZipArchive::CREATE)!==TRUE) {
        exit("Failed to create a response archive: <$filename>\n");
    }
    
    $archive->addFolderContent($data_path);
    $archive->close();
    
    // now we sent the archive to the client
    header('HTTP/1.1 200 Ok');
    header('Content-Type: application/zip');         
    header("Content-Disposition: attachment;filename=load_request.zip"); 
    readfile($archive_name);
    // current setup says that request_file_exists -> create new request file, so we need to cleanup afterwards
    unlink($archive_name); 
    exit(0);
    
} else {
    $request_index = json_decode($request_body);
    
    // TODO compare indices and generate response archive
    
    /*
    // now we sent the archive to the client
    header('HTTP/1.1 200 Ok');
    header('Content-Type: application/zip');         
    header("Content-Disposition: attachment;filename=load_request.zip"); 
    readfile($archive_name);
    // current setup says that request_file_exists -> create new request file, so we need to cleanup afterwards
    unlink($archive_name); 
    exit(0);*/
}
 
?>