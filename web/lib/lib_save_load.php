<?php
include dirname(__FILE__) . '/lib_zip_util.php';
include dirname(__FILE__) . '/lib_backup_restore.php';
include dirname(__FILE__) . '/lib_settings.php';
include dirname(__FILE__) . '/lib_index.php';

$temp_path = dirname(__FILE__) . '/temp/';
$request_collection_file = dirname(__FILE__) . '/../temp/collection.json';
$request_easter_collection_file = dirname(__FILE__) . '/../temp/easter_collection.json';

function init_save_index() {
    $save_index_path = dirname(__FILE__) . '/../temp/index.json';
    $GLOBALS['save_index'] = json_decode(file_get_contents($save_index_path));
}

function verify_save_request() {
    if (!file_exists($GLOBALS['request_collection_file']) || filesize($GLOBALS['request_collection_file']) == 0) {
        return false;
    }
}

//TODO if easter files are sent, check for easter_collection.json request file before including these
function save() {
    clear_folder($GLOBALS['temp_path']);
    $request_zip_path = dirname(__FILE__) . '/../temp/request.zip';
    extract_zip_archive($request_zip_path);
    //TODO take in account max_backup_count and if reached, delete oldest backup
    if ($GLOBALS['settings']['backup']['auto_backup'] === true) {
        if ($GLOBALS['settings']['backup']['backup_requests'] === true) {
            $number = 0;
            while (file_exists(dirname(__FILE__) . "/../data/backups/save_request$number.zip")) {
                $number++;
            }
            copy($request_zip_path, file_exists(dirname(__FILE__) . "/../data/backups/save_request$number.zip"));
            unlink($request_zip_path);
        } else {
         //TODO   
        }
    }
    init_save_index();
    init_index();
    if ($GLOBALS['save_index']['version_timestamp'] === $GLOBALS['index']->get_metadata()['version_timestamp']) {
        echo "Remote songbook already up to date. Version timestamp match.";
        return 1;
    } elseif ($GLOBALS['index']->get_metadata()['version_timestamp'] === -1) {
        return save_complete();
    } else {
        return save_partial();
    }
    
}
//TODO
function save_partial() {
    echo "Remote update successful.";
    return 0;
}
//TODO
function save_complete() {
    echo "Remote update successful.";
    return 0;
}
?>