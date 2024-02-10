<?php
/**
 * This script porvides utitilities to generate checksums.
 **/

/**
 * Computes SHA-256 checksum of a file.
 * 
 * @param $path path to the file
 * @return hash of the file
 **/
function get_file_hash(string $path) {
    return hash_file("sha256", $path);
}

/**
 * Computes SHA-256 checksum of a string message.
 * 
 * @param $message the message to hash
 * @return hash of the message
 **/
function get_message_hash(string $message) {
    return hash("sha256", $message);
}

?>