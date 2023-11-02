<?php
include '../../lib/lib_index.php';

init_index();

echo $index->get_metadata()['version_timestamp'];
?>