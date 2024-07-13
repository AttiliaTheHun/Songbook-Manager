<?php
/**
 * This library is used to manage backups of the songbook.
 * 
 **/

require_once(dirname(__FILE__) . '/lib_init.php');
require_once(dirname(__FILE__) . '/lib_zip_util.php');

const BACKUP_TYPE_COMPLETE = "COMPLETE";
const BACKUP_TYPE_INVERSE = "INVERSE";


function backup($index) {
    if ($GLOBALS['settings']['backup']['auto_backup'] !== true) {
        return "";
    }
    if ($GLOBALS['settings']['backup']['backup_type'] === BACKUP_TYPE_COMPLETE) {
        return create_complete_backup();
    }
    if ($GLOBALS['settings']['backup']['backup_type'] === BACKUP_TYPE_INVERSE) {
        return create_inverse_backup($index);
    }
    return "incorrect backup configuration";
}

/**
 * Creates a backup of the entire songbook along with the index and returns the name of the backup file.
 * 
 * @returns name of the backup file (not path)
 **/
function create_complete_backup() {
    
    $archive_name = get_backup_file_name('complete_backup_');
    
    $archive = new XZipArchive();
    if ($archive->open($GLOBALS['backup_file_path'] . $archive_name, ZipArchive::CREATE) !== TRUE) {
        return "could not create the backup archive";
    }
    $archive->addFolderContent($GLOBALS['songbook_data_path']);
    // add index to preserve the original version_timestamp
    $archive->addFromString('index.json', json_encode($GLOBALS['index']));
    $archive->close();
    return $archive_name;
}

/**
 * Creates a backup that can be used to reverse the last save request.
 * 
 * @param $index the request index
 * @returns name of the backup file (not path)
 **/
function create_inverse_backup($index) {
    $archive_name = get_backup_file_name('inverse_backup_');
    
    $archive = new XZipArchive();
     if ($archive->open($GLOBALS['backup_file_path'] . $archive_name, ZipArchive::CREATE) !== TRUE) {
        return "could not create the backup archive";
    }
    fill_inverse_backup_file($index, $archive);
    $archive->close();
    return $archive_name;
}

/**
 * Adds the necessary files to the inverse backup archive
 * 
 * @param $index the request index
 * @param $archive the backup file ZipArchive object
 **/
function fill_inverse_backup_file($index, $archive) {
    
    foreach (array_keys($GLOBALS['collections']) as $collection_name) {
        
        // backup collection file, if alterations will be made
        if (in_array($collection_name, $index['collections'], true) === true) {
            $archive->addFile($GLOBALS['collection_data'][$collection_name]['file_path'], $GLOBALS['collection_data'][$collection_name]['file_name']);
        }
        
        // backup the songs that are to be deleted
        if (isset($index['deletions'][$collection_name]) && count($index['deletions'][$collection_name]) !== 0) {
            foreach ($index['deletions'][$collection_name] as $song) {
                $archive->addFile($GLOBALS['collection_data'][$collection_name]['data_path'] . $song, $GLOBALS['collection_data'][$collection_name]['relative_path'] . $song);
            }
        }
        
        // backup the original version of the songs that are to be updated
        if (isset($index['changes'][$collection_name]) && count($index['changes'][$collection_name]) !== 0) {
            foreach ($index['changes'][$collection_name] as $song) {
                $archive->addFile($GLOBALS['collection_data'][$collection_name]['data_path'] . $song, $GLOBALS['collection_data'][$collection_name]['relative_path'] .  $song);
            }
        }
    }
    // if we wanted to restore this backup, we will need to know the original version timestamp
    $index['version_timestamp'] = $GLOBALS['index']->getMetadata()['version_timestamp'];
    // finally backup the request index, it contains the version_timestamp and song additions (necessary information)
    // for reversing the backup process
    $archive->addFromString('index.json', json_encode($index));
}

/**
 * Finds a file name for the backup according to the settings.
 * 
 * @param $file_name_prefix what the file name will begin with
 * @returns string of the file name (without path)
 **/
function get_backup_file_name($file_name_prefix) {
    if ($GLOBALS['settings']['backup']['max_backup_count'] < 0 ) {
        // if no maximum number of backups was set, we will just find an available file name and use it
        // it is possible to sort files by date of modify to see the most recent backup
        $x = 0;
        $archive_name = $file_name_prefix . $x . ".zip";
        while (file_exists($GLOBALS['backup_file_path'] . $archive_name)) {
            $x++;
            $archive_name = $file_name_prefix . $x . ".zip";
        }
    } else {
      // in case we are limit in the number of files to use for backups, we are going to rotate them in such a way
      // that for a new backup we use the file that was modified the earliest, that should be the most outdated backup
      $x = 0;
      $archive_name = $file_name_prefix . $x . ".zip";
      $oldest_archive_name;
      $oldest_archive_mtime = time();
      while (file_exists($GLOBALS['backup_file_path'] . $archive_name)) {
          $current_archive_mtime = filemtime($GLOBALS['backup_file_path'] . $archive_name);
          // if the file is less recent, we will store its name and its mtime for further comparison
          if ($current_archive_mtime < $oldest_archive_mtime) {
              $oldest_archive_mtime = $current_archive_mtime;
              $oldest_archive_name = $archive_name;
          }
          // we reached the number of allowed files so we use the less recent backup file instead
          if ($x + 1 > $GLOBALS['settings']['backup']['max_backup_count']) {
              $archive_name = $oldest_archive_name;
              break;
          }
          $x++;
          $archive_name = $file_name_prefix . $x . ".zip";
      }
    }
    return $archive_name;
}

function restore_complete_backup() {
    // TODO
}

function restore_inverse_backup() {
    // TODO
}

?>