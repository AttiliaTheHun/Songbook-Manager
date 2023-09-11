<?php
$output_filename = '../data/songbook_zip_hash.txt';
$filename = '../data/songbook.zip';

if (!file_exists($fileName)) {
    $output = "";
} else {
     $ctx = hash_init('sha256');

    echo("INFO : Reading file $fileName ...");
    $file = fopen($fileName, 'r');
    while(!feof($file)){
        $buffer = fgets($file, 1024);
        hash_update($ctx, $buffer);
    }
    echo(" DONE!" . PHP_EOL);

    echo("INFO : Calculating SHA256 hash of $fileName ...");
    $hashRaw = hash_final($ctx, false); // Here, set the $raw_output to false
    echo(" DONE!" . PHP_EOL);

    echo ($hash . " INPUT " . PHP_EOL);
    echo ($hashRaw . " OUTPUT " . PHP_EOL);

    if($hash == $hashRaw)
        echo("INFO : Hash Comparison: OK!" . PHP_EOL);
    else
        echo("WARN : Hash Comparison: MISMATCH!" . PHP_EOL);
    echo("END" . PHP_EOL);
    
    file_put_contents($output_filename, $hash);
}
?>