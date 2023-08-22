<?php
$collection_raw = file_get_contents("../data/collection.json");
$collection = json_decode($collection_raw, true);
function id($n) {
            return $n['id'];
}
        
usort($collection, function($a, $b) {
            return strcmp($a["name"], $b["name"]);
});

//$keys = array_keys($song_data);
//$keys = array_map('id', $collection);
$keys = [];
for ($x = 0; $x < count($collection); $x++) {
    $keys[$x] = $collection[$x]['id'];
}
echo json_encode($keys);
?>