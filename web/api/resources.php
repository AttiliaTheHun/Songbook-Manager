<?php
/**
 * This endpoint returns the resources file. Upon first start, every client automatically tries to download a resources file.
 * It makes sense to configure the clients to use this URL to do so. This route is not authenticated, because on first start,
 * the client usually does not have any token available and obtaining one could be cumbersome. Moreover, it is not possible to
 * change the state of the server from here and also the resources are supposed to contain very little data for leakage,
 * since the software is open-sourced and the standard templates are freely available. To disable this route, just delete the file.
 **/
 
if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    http_response_code(405);
    include(dirname(__FILE__) . '/../../resources/pages/405.php');
    die();
}

$filename = 'resources.zip';
$file = dirname(__FILE__) . '/../../files/resources.zip';
header('HTTP/1.1 200 Ok');
header('Content-Type: application/zip');         
header("Content-Disposition: attachment;filename=$filename"); 
readfile($file);
?>