<?php
 $filename = 'resources.zip';
 $file = '../../files/resources.zip';
 header('Content-Type: application/zip');         
 header("Content-Disposition: attachment;filename=$filename"); 
 readfile($file);
 ?>