<?php
/**
 * This script should be run when the server is first being setup to generate the proper directory structure and files.
 * All configuration shuold be already in place. It is not recommended this script is executed later on as it may trigger
 * some unexpected behavior within other components. It is safe to delete this file afterwards.
 **/
$temp_dir = dirname(__FILE__) . '../temp/';

if (!file_exists($temp_dir)) {
    mkdir($temp_dir);
}

?>