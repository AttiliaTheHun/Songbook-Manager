<?php
/**
 * @source https://stackoverflow.com/questions/4914750/how-to-zip-a-whole-folder-using-php (modified)
 **/
class XZipArchive extends ZipArchive {
    /**
     * Adds a folder (with its content) to the zip file.
     * 
     * @param $folder path to the folder
     * @param $name what will the folder be called in the archive
     **/
    public function addFolder($folder, $name) {
        if (filetype( $folder) == 'dir') {
           $this->addEmptyDir($name);
           $this->addFolderToArchive($folder, $name);
        } else {
            $this->addFile($folder, $name);
        }
    }
    
    /**
     * Adds content of a folder to the root of the archive.
     * 
     * @param $folder path to the folder
     **/
    public function addFolderContent($folder) {
        if (filetype( $folder ) == 'dir') {
           $dir = opendir ($folder);
        while ($file = readdir($dir)) {
            if ($file == '.' || $file == '..') continue;
            $do = (filetype( $folder . $file) == 'dir') ? 'addFolder' : 'addFile';
            $this->$do($folder . $file, $file);
        }
           
        } else {
            $this->addFile($folder);
        }
    }
    
    /**
     * Recursively adds folder and its content to the archive.
     * 
     * @param $folder path to the folder
     * @param $name what will the folder be called in the archive
     **/
    private function addFolderToArchive($location, $name) {
        $name .= '/';
        $location .= '/';
        $dir = opendir ($location);
        while ($file = readdir($dir)) {
            if ($file == '.' || $file == '..') continue;
            $do = (filetype( $location . $file) == 'dir') ? 'addFolder' : 'addFile';
            $this->$do($location . $file, $name . $file);
        }
    } 
}



//$temp_path = dirname(__FILE__) . '/temp/';
/*
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
}*/
/*
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
*/

?>