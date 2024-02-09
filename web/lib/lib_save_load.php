<?php
//TODO fill in the code for easter songs
//TODO implements the backup system

include dirname(__FILE__) . '/lib_zip_util.php';
include dirname(__FILE__) . '/lib_backup_restore.php';
include dirname(__FILE__) . '/lib_settings.php';
include dirname(__FILE__) . '/lib_index.php';

$data_path = dirname(__FILE__) . '/../data/songbook/';
$temp_path = dirname(__FILE__) . '/../temp/';
$request_collection_file = dirname(__FILE__) . '/../temp/collection.json';
$request_easter_collection_file = dirname(__FILE__) . '/../temp/easter_collection.json';

function dir_is_empty($dir) {
  $handle = opendir($dir);
  while (false !== ($entry = readdir($handle))) {
    if ($entry != "." && $entry != "..") {
      closedir($handle);
      return false;
    }
  }
  closedir($handle);
  return true;
}

function recurse_copy($src, $dst) {

  $dir = opendir($src);
  $result = ($dir === false ? false : true);

  if ($result !== false) {
    $result = mkdir($dst);

    if ($result === true) {
      while(false !== ( $file = readdir($dir)) ) { 
        if (( $file != '.' ) && ( $file != '..' ) && $result) { 
          if ( is_dir($src . '/' . $file) ) { 
            $result = recurse_copy($src . '/' . $file, $dst . '/' . $file); 
          }     else { 
            $result = copy($src . '/' . $file, $dst . '/' . $file); 
          } 
        } 
      } 
    }
    closedir($dir);
  }
  return $result;
}

function init_save_index() {
    $save_index_path = dirname(__FILE__) . '/../temp/index.json';
    $GLOBALS['save_index'] = json_decode(file_get_contents($save_index_path));
}

function verify_save_request() {
    if (!file_exists($GLOBALS['request_collection_file']) || filesize($GLOBALS['request_collection_file']) == 0) {
        return false;
    }
    
    if (file_exists($GLOBALS['temp_path'].'songs/egg/') && !is_dir_empty($GLOBALS['temp_path'].'songs/egg/') && !(file_exists($GLOBALS['request_easter_collection_file']) && filesize($GLOBALS['request_easter_collection_file']) != 0)) {
        return false;
    }
    return true;
}

function save() {
    clear_folder($GLOBALS['temp_path']);
    $request_zip_path = dirname(__FILE__) . '/../temp/request.zip';
    extract_zip_archive($request_zip_path);
    unlink($request_zip_path);
    if (!verify_save_request()) {
        echo '{"error": "One of the collection files is corrupt; you may delete and regenerate the collection and try again."}';
        return 2;
    }
    init_save_index();
    init_index();
    if ($GLOBALS['save_index']['version_timestamp'] === $GLOBALS['index']->get_metadata()['version_timestamp']) {
        echo '{"message": "Remote songbook already up to date."}';
        return 1;
    } elseif ($GLOBALS['index']->get_metadata()['version_timestamp'] === -1) {
        return save_complete();
    } else {
        return save_partial();
    }
    
}
//TODO
function save_partial() {
    echo '{"message": "Remote update successful."}';
    return 0;
}

function save_complete() {
    if (!recurse_copy($GLOBALS['temp_path'], $GLOBALS['songbook_data_folder'])) {
        echo '{"error": "Something went wrong."}';
        return 3;
    }
    if (!index_new_songs($GLOBALS['save_index']['additions']['standard'])) {
        echo '{"error": "Something went wrong."}';
        return 4;
    }
    if (!index_collections()) {
        echo '{"error": "Something went wrong."}';
        return 5;
    }
    if (!save_index()) {
        echo '{"error": "Something went wrong."}';
        return 6;
    }
    echo '{"message": "Remote update successful."}';
    return 0;
}

function add_songs(array $songs) {
    foreach ($songs as $song) {
        rename($temp_path."songs/html/$song", $song_data_path.$song);
    }
}

function delete_songs(array $songs) {
    foreach ($songs as $song) {
        unlink($song_data_path.$song);
    }
}

function update_collections() {
    $songbook_data_path = dirname(__FILE__).'/../data/songbook/';
    rename($temp_path."collection.json", $songbook_data_path."collection.json");
    if (file_exists($temp_path."easter_collection.json")) {
        rename($temp_path."easter_collection.json", $songbook_data_path."easter_collection.json");
    }
}
?>