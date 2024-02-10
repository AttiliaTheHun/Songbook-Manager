<?php
/**
 * This script loads the strings. Strings are used to to change values of text strings without altering the source code
 * and they also help you translate/update the entire site by putting all the texts in one place.
 **/
$strings_file_path = dirname(__FILE__) . '/../resources/strings.json';
$strings = json_decode(file_get_contents($strings_file_path), true);
?>