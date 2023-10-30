<?php
$settings_file_path = dirname(__FILE__) . '../settings.json';
$settings_file_contents = file_get_contents($settings_file_path);
$settings = json_decode($settings_file_contents, true);
?>