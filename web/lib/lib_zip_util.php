<?php
/**
 * @source https://stackoverflow.com/questions/4914750/how-to-zip-a-whole-folder-using-php (modified)
 * */
class FlxZipArchive extends ZipArchive {
 public function addDir($location, $name, boolean $include_source_dir) {
     if ($include_source_dir === true) {
         $this->addEmptyDir($name);
     }
       
       $this->addDirDo($location, $name);
 } 
 private function addDirDo($location, $name) {
    $name .= '/';
    $location .= '/';
    $dir = opendir ($location);
    while ($file = readdir($dir)) {
        if ($file == '.' || $file == '..') continue;
        $do = (filetype( $location . $file) == 'dir') ? 'addDir' : 'addFile';
        $this->$do($location . $file, $name . $file);
    }
 } 
}



$temp_path = dirname(__FILE__) . '/temp/';

function clear_folder($folder) {
    $files = glob(join($folder, '/*')); // get all file names
    foreach($files as $file) { // iterate files
        if(is_file($file)) {
            unlink($file); // delete file
        }
    }
}

function extract_zip_archive($zip_path) {
    clear_folder($GLOBALS['temp_path']);
    $zip = new ZipArchive;
    if ($zip->open($zip_path) === TRUE) {
        $zip->extractTo($GLOBALS['temp_path']);
        $zip->close();
        return true;
    } else {
        return false;
    }
}

function assemble_zip_archive_from_temp($archive_name) {
    clear_folder($GLOBALS['temp_path']);
    
    $za = new FlxZipArchive;
    $res = $za->open($archive_name, ZipArchive::CREATE);
    if($res === TRUE) {
        $za->addDir($GLOBALS['temp_path'], '/');
        $za->close();
    } else {
        return false;
    }
    return true;
}


?>