<?php
/**
 * This route returns the stored songbook version timestamp. This API endpoint is not authenticated.
 **/
 
if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    http_response_code(405);
    include(dirname(__FILE__) . '/../resources/pages/405.php');
    die();
}

require(dirname(__FILE__) . '/../lib/lib_index.php');

http_response_code(200);
echo $index->getMetadata()['version_timestamp'];
?>