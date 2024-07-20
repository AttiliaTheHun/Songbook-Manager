<?php
/**
 * This script should be run when the server is first being setup to generate the proper directory structure and files.
 * All configuration shoold be already in place. It is not recommended this script is executed later on as it may trigger
 * some unexpected behavior within other components. It is safe to delete this file afterwards.
 **/
$temp_dir = dirname(__FILE__) . '../temp/';
$data_dir = dirname(__FILE__) . '../data/';
$backup_dir = $data_dir . '/backup/';
$songbook_dir = $data_dir . '/songbook/';
$resources_zip_file = dirname(__FILE__) . '/files/resources.zip';
$client_downloadable_resources_dir = dirname(__FILE__) . '/resources';

// TODO: make it automatically compile resources.zip

if (!file_exists($temp_dir)) {
    mkdir($temp_dir);
}

if (!file_exists($data_dir)) {
    mkdir($data_dir);
}

if (!file_exists($backup_dir)) {
    mkdir($backup_dir);
}

if (!file_exists($songbook_dir)) {
    mkdir($songbook_dir);
}

if (!file_exists($resources_zip_file)) {
    mkdir($songbook_dir);
}



?>