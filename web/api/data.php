<?php

if ($_SERVER['REQUEST_METHOD'] === 'GET' || $_SERVER['REQUEST_METHOD'] === 'POST') {
    require(dirname(__FILE__) . '/scripts/data.php');
} else {
    http_response_code(405); // 405 Method not allowed
    include(dirname(__FILE__) . '/../../resources/pages/405.php');
    die();
}

// technically this code should never be reached
http_response_code(503); // 503 Service unavailable
die();

?>