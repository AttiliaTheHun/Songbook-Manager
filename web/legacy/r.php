<?php
$path_to_backup_file = "file.zip";
echo proc_open("gunzip -c -t $path_to_backup_file 2>&1");
?>