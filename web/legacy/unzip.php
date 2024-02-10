<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Rozzipování CMS</title>
</head>
<html><head><meta http-equiv="content-type" content="text/html; charset=utf-8" /><title>UnZIPovač</title><meta name="robots" content="noindex,nofollow" /><style>body { padding:2em; max-width:50em; font-family:"Trebuchet MS", "Geneva CE", lucida, sans-serif;}table {border-collapse:collapse;font-size:82%;}td, th {padding:5px;border:1px solid #ddd;}th {padding-right:10px;background-color:#eee;text-align:left; font-weight:bold;}thead th {background-color:#ddd;}a {color:inherit;}small {font-size:100%; color:#ccc; font-weight:normal;}table a {color:#000; text-decoration:underline; font-weight:bold;}table a:visited {font-weight:normal;}.messages {max-height:10em; overflow-y:auto;}.message.ok {color:green;}.message.error {color:red;}</style></head><body><?php $writable = (is_writable('.')); ?><h2><small>[<a href='./unzip.php'>Obnovit</a>]</small> Dostupné zip soubory <small>(soubor - <span class="message <?php echo ($writable) ? 'ok' : 'error' ?>"><?php echo ($writable) ? 'je' : 'není' ?> zapisovatelný</span>)</small>:</h2><?php
$file_list = array();
if ($handler = opendir(dirname(__FILE__))) {
   $i = 0;
   while (false !== ($filename = readdir($handler))) {
      if (!is_dir($filename) AND 'zip' == substr($filename, -3, 3)) {
         $file_list[$i]['name'] = $filename;
         $file_list[$i]['time'] = date('r',fileCTime($filename));
         $file_list[$i]['size'] = round(fileSize($filename)/(1024*1024),2) . ' MB';
         $i++;
      }
   }
   closedir($handler);
}
if (!count($file_list)) { ?><p>Žádné soubory k rozbalení!</p><?php
} else {
   echo '<table><thead><tr><th>Název<th>Velikost<th>Čas (vytvoření)<th> <tbody>';
   
   foreach ($file_list as $file) {
      echo "<tr><th>$file[name]"
         . " <td> $file[size]"
         . " <td> $file[time]"
         . " <td> <form action='./unzip.php' method='post'><input type='hidden' name='unpack' value='$file[name]'><input type='submit' value='Rozbaliť'></form>";
   }
   echo '</table>';
}
if (isset($_POST['unpack'])) {
   $file = $_POST['unpack']; ?><h2>Výsledky <small>(rozbalování <?php echo $file; ?>)</small></h2><?php
   if (!is_file($file)) { ?><p class="message error">Soubor <em><b><?php echo $file ?></b></em> nebyl nalezen.</p><?php
   } elseif (unzip($file)) { ?><p class="message ok">Soubor <em><b><?php echo $file ?></b></em> byl rozbalen.</p><?php
   } else { ?><p class="message error">Rozbalování souboru <em><b><?php echo $file ?></b></em> selhalo.</p><?php
   }
} ?></body></html><?php 
function unzip($src_file, $dest_dir=true, $create_zip_name_dir=true, $overwrite=true)
{
   if(function_exists("zip_open")) {
      if(!is_resource(zip_open($src_file))) { 
         $src_file=dirname($_SERVER['SCRIPT_FILENAME'])."/".$src_file; 
      }
      if (is_resource($zip = zip_open($src_file))) {         
         $splitter = ($create_zip_name_dir === true) ? "." : "/";
         if ($dest_dir === false) 
            $dest_dir = substr($src_file, 0, strrpos($src_file, $splitter))."/";
         else 
           $dest_dir = "";
         create_dirs($dest_dir);
         while ($zip_entry = zip_read($zip)) {
         $pos_last_slash = strrpos(zip_entry_name($zip_entry), "/");   
         if ($pos_last_slash !== false) {
            create_dirs($dest_dir.substr(zip_entry_name($zip_entry), 0, $pos_last_slash+1));
         }
         if (zip_entry_open($zip,$zip_entry,"r")) {
            $file_name = $dest_dir.zip_entry_name($zip_entry);
            if ($overwrite === true || $overwrite === false && !is_file($file_name)) {
               $fstream = zip_entry_read($zip_entry, zip_entry_filesize($zip_entry));
               if(!is_dir($file_name))         
               file_put_contents($file_name, $fstream );
               if(file_exists($file_name)) {
                  chmod($file_name, 0777);
                  $results[] = array($file_name, true);
               }
               else {
                  $results[] = array($file_name, false);
               }
            }
            zip_entry_close($zip_entry);
         }   
         }
         zip_close($zip);
         echo '<ol class="messages">';
         foreach ($results as $r) {
            $msg_type = ($r[1]) ? 'ok' : 'error';
            $msg = ($r[1]) ? 'Úspešné' : 'Neúspešné';
            echo "<li class='message $msg_type'><em>$msg</em> - <a href='$r[0]'>$r[0]</a>";
         }
         echo '</ol>';
         if ($msg_type == 'ok') return true; else return false;
      }
      else {
         return false;
      }
      return true;
   }
   else
   {
      if(version_compare(phpversion(), "5.2.0", "<"))
      $infoVersion="(PHP 5.2.0 alebo vyšší)";
      echo "Je potřebné nainstalovat/povolit php_zip.dll rozšíření $infoVersion"; 
   }
}
function create_dirs($path)
{
   if (!is_dir($path)) {
      $directory_path = "";
      $directories = explode("/",$path);
      array_pop($directories);

      foreach($directories as $directory) {
         $directory_path .= $directory."/";
         if (!is_dir($directory_path)) {
            mkdir($directory_path);
            chmod($directory_path, 0777);
         }
      }
   }
}
?>
<body>
</body>
</html>