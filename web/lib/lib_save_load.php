<?php
//TODO implement the backup system
require_once dirname(__FILE__) . '/lib_init.php';
require_once dirname(__FILE__) . '/lib_zip_util.php';
require_once dirname(__FILE__) . '/lib_backup_restore.php';


/**
 * Archive the entire songbook data and save it to the temp path.
 * 
 * @return path to the archive
 **/
function create_complete_load_request_response_archive() {
    $archive = new XZipArchive();
    // if multiple requests are being processed at the same time, they would overwrite each others response file
    $num = -1;
    do {
        $num += 1;
        $archive_name = $GLOBALS['temp_path'] . "load_request$num.zip";
    } while (file_exists($archive_name));
    
    if ($archive->open($archive_name, ZipArchive::CREATE)!==TRUE) {
        exit("Failed to create a response archive: <$filename>\n");
    }
    
    $archive->addFolderContent($GLOBALS['songbook_data_path']);
    $archive->close();
    return $archive_name;
}

/**
 * Creates an archive of songs and collections requested by load request index.
 * 
 * @param $index the load index
 * @return path to the archive
 **/
function create_partial_load_request_response_archive(array $load_index) {
    $archive = new XZipArchive();
    // if multiple requests are being processed at the same time, they would overwrite each others response file
    $num = -1;
    do {
        $num += 1;
        $archive_name = $GLOBALS['temp_path'] . "load_request$num.zip";
    } while (file_exists($archive_name));
    
    if ($archive->open($archive_name, ZipArchive::CREATE)!==TRUE) {
        exit("Failed to create a response archive: <$filename>\n");
    }
    
    foreach (array_keys($load_index['missing']) as $collection) {
        foreach (array_values($load_index['missing'][$collection]) as $file) {
            $archive->addFile($GLOBALS['collection_data'][$collection]['data_path'] . $file, $GLOBALS['collection_data'][$collection]['relative_path'] . $file);
        }
    }
    
    foreach (array_keys($load_index['outdated']) as $collection) {
        for ($x = 0; $x < count($load_index['outdated'][$collection]); $x++) {
            $archive->addFile($GLOBALS['collection_data'][$collection]['data_path'] . $load_index['outdated'][$collection][$x], $GLOBALS['collection_data'][$collection]['relative_path'] . $load_index['outdated'][$collection][$x]);
        }
    }
    
    for ($x = 0; $x < count($load_index['collections']); $x++) {
        $archive->addFile($GLOBALS['data_path'] . $GLOBALS['collection_data'][$load_index['collections'][$x]]['file_name'], $GLOBALS['collection_data'][$load_index['collections'][$x]]['file_name']);
    }
    
    $archive->close();
    return $archive_name;
}

/**
 * Processes a save request archive, verifies its content and eventually performs the saving.
 * 
 * @param $request_archive_path path to the request archive file
 * @returns array of true and name of the corresponding backup file on success and string with error message otherwise
 **/
function parse_save_request($request_archive_path) {
    $archive = new ZipArchive();
    $archive->open($request_archive_path);
    
    $index = $archive->getFromName("index.json");
   // var_dump($index);
    if ($index === false) {
        return "request index not found";
    } else {
        $index = json_decode($index, true);
    }
   
   if ($index === NULL) {
       return "error parsing request index";
   }
    
    // we should not allow an older version of the songbook to overwrite a newer one
    if (isset($index['version_timestamp'])) {
        if ($GLOBALS['index']->getMetadata()['version_timestamp'] > $index['version_timestamp']) {
            return "cannot push from older version to newer";
        }
        if ($GLOBALS['index']->getMetadata()['version_timestamp'] == $index['version_timestamp']) {
            return "version timestamp match";
        }
        // negative numbers can cause a great mess
        if ($index['version_timestamp'] < 0) {
            return "version timestamp cannot be negative";
        }
        // do not update the timestamp just yet, we will still need the old one for backup
        $GLOBALS['temp_save_version_timestamp'] = $index['version_timestamp'];
    } else {
        return "version timestamp not found";
    }
    
    $result = verify_collection_integrity($index, $archive);
    if ($result !== true) {
        $archive->close();
        return $result;
    }
    
    // we need to backup the original data, before performing the operation
    $backup_file = backup($index);
    
    $result = perform_save_request($index, $archive);
    
    $archive->close();
    regenerate_index();
    unset($GLOBALS['temp_save_version_timestamp']);
    if ($result === true) {
        return [$result, $backup_file];
    }
    return $result;
}

/**
 * Verifies the index data is compliant to the request file content and this save operation should not break the stored
 * songbook.
 * 
 * @param $index the index (array)
 * @param $archive ZipArchive object of the request
 * @returns true on success, false or string otherwise
 **/
function verify_collection_integrity($index, $archive) {
    // for every registered collection we check the information in the request adds up
    foreach (array_keys($GLOBALS['collections']) as $collection_name) {
        // for some operations such as adding it is necessary the client supplies an updated collection file
        $has_collection_file = false;
        $new_collection_state = $archive->getFromName($GLOBALS['collection_data'][$collection_name]['file_name']);
        if ($new_collection_state !== false) {
            $new_collection_state = json_decode($new_collection_state, true);
            // we shall perform said operations on our collection and in the end our collection must be identical
            // to the one the client gave us
            $current_collection_state = $GLOBALS['collections'][$collection_name];
            $has_collection_file = true;
            // this is a problem since the backup library functions do not have access to the request archive, but rather
            // to the request index
            if (!in_array( $collection_name, $index['collections'], true)) {
                return "collection file is provided but not registered in the index";
            }
        }
        
        // first we check song additions
        if (isset($index['additions'][$collection_name]) && count($index['additions'][$collection_name]) !== 0) {
            if (!$has_collection_file) {
                return "collection file must be included when adding songs";
            }
            
            // we need to check that each new songs was provided with its .html file
            foreach ($index['additions'][$collection_name] as $song) {
                array_push($current_collection_state, song($song));
                $song_content = $archive->getFromName($GLOBALS['collection_data'][$collection_name]['relative_path'] . $song);
                if ($song_content === false) {
                    return "song file must be included when adding a song";
                }
            }
        }
        
        // now we check song deletions
        if (isset($index['deletions'][$collection_name]) && count($index['deletions'][$collection_name]) !== 0) {
            // technically we do not need to receive client's copy of the collection for deleting songs, but the one he
            // gives us must match with the operations we are supposed to perform upon the colelction
            if ($has_collection_file) {
                foreach ($index['deletions'][$collection_name] as $song) {
                    $GLOBALS['save_temp_song'] = $song;
                    $current_collection_state = array_filter($current_collection_state, function($value) {
                        return $value['id'] !== (int)str_replace(".html", "", $GLOBALS['save_temp_song']);
                    });
                }
                
            }
            unset($GLOBALS['save_temp_song']);
        }
        
        // finally we check changes to songs
        if (isset($index['changes'][$collection_name]) && count($index['changes'][$collection_name]) !== 0) {
            foreach ($index['changes'][$collection_name] as $song) {
                // again, we must ensure that songs that have been updated (changed) were shipped with their updated .html files
                $song_content = $archive->getFromName($GLOBALS['collection_data'][$collection_name]['relative_path'] . $song);
                if ($song_content === false) {
                    return "song file must be included when updating a song";
                }
            }
        }
        
        // now we check if the provided collection will reflect with the changes we are about to make, if we are supposed to replace
        // our collection file with it. Otherwise we might have a collection file that does not mirror the actual collecting.
        // This can lead to some serious fun while debugging :)
        if ($has_collection_file) {
        
            // we will be comparing only song ids, because on our side we can not know whether a name or url has not been changed
            // and in that case we would not be able to compare these collections at all. Luckily, we don't need to
            $current_collection_state_ids = array_map(function($value) {
                return $value['id'];
            }, $current_collection_state);
            $new_collection_state_ids = array_map(function($value) {
                return $value['id'];
            }, $new_collection_state);
            
            // now we check both arrays are of teh same length and there are no extra elements in the second one compared to the first one
            if (count($current_collection_state_ids) !== count($new_collection_state_ids) || !count(array_diff(array_values($current_collection_state_ids), array_values($new_collection_state_ids))) === 0) {
                return "included collection file is incompatible with the changes made to the collection";
            }
    
        }
    
    }
    
    return true;
}

/**
 * Does the actual saving and overwriting of the data.
 * 
 * @param $index the request index
 * @param $archive the request archive
 * @return true on success
 **/
function perform_save_request($index, $archive) {
    $version_timestamp = $index['version_timestamp'];
    // again we go one collection at a time
    foreach (array_keys($GLOBALS['collections']) as $collection_name) {
        
        // we delete what we ought to delete
        if (isset($index['deletions'][$collection_name]) && count($index['deletions'][$collection_name]) !== 0) {
            foreach ($index['deletions'][$collection_name] as $song) {
                unlink($GLOBALS['collection_data'][$collection_name]['data_path'] . $song);
            }
        }
        
        // handle additions
        if (isset($index['additions'][$collection_name]) && count($index['additions'][$collection_name]) !== 0) {
            foreach ($index['additions'][$collection_name] as $song) {
                $song_content = $archive->getFromName($GLOBALS['collection_data'][$collection_name]['relative_path'] . $song);
                if ($song_content === false) {
                    return "failed resolving additions";
                }
                file_put_contents($GLOBALS['collection_data'][$collection_name]['data_path'] . $song, $song_content, LOCK_EX);
                touch($GLOBALS['collection_data'][$collection_name]['data_path'] . $song, $GLOBALS['temp_save_version_timestamp']);
            }
        }
        
        // handle changes
        if (isset($index['changes'][$collection_name]) && count($index['changes'][$collection_name]) !== 0) {
            foreach ($index['changes'][$collection_name] as $song) {
                $song_content = $archive->getFromName($GLOBALS['collection_data'][$collection_name]['relative_path'] . $song);
                if ($song_content === false) {
                    return "failed resolving changes";
                }
                file_put_contents($GLOBALS['collection_data'][$collection_name]['data_path'] . $song, $song_content, LOCK_EX);
                touch($GLOBALS['collection_data'][$collection_name]['data_path'] . $song, $GLOBALS['temp_save_version_timestamp']);
                
            }
        }
        
        // finally, add the collection file if available
        $new_collection = $archive->getFromName($GLOBALS['collection_data'][$collection_name]['file_name']);
        if ($new_collection !== false) {
            file_put_contents($GLOBALS['collection_data'][$collection_name]['file_path'], $new_collection, LOCK_EX);
            touch($GLOBALS['collection_data'][$collection_name]['file_path'], $GLOBALS['temp_save_version_timestamp']);
            
        }
    }
    
    return true;
}

/**
 * Creates a dummy song object that represents a song without actually knowing anything about it but the id.
 * 
 * @param $id the id the song will have
 * @returns a dummy song object
 **/
function song($id) {
    $id = (int)str_replace(".html", "", $id);
    return ["id" => $id, "name" => "song", "active" => true, "url" => ""];
}
?>