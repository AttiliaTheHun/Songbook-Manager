<?php
$filename = '../../data/songbook_zip_hash.txt';

if (!file_exists($fileName)) {
    echo "";
} else {
     echo file_get_contents($filename);
}
?>