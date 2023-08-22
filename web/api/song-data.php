<?php
$collection_raw = file_get_contents("../data/collection.json");
$collection = json_decode($collection_raw, true);
        
usort($collection, function($a, $b) {
            return strcmp($a["name"], $b["name"]);
});

echo json_encode($collection);
?>