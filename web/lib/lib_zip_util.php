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

?>