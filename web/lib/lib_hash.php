<?php
function get_file_hash(string $path) {
    return hash_file("sha256", $path);
}

function get_message_hash(string $message) {
    return hash("sha256", $message);
}


?>