<?php
/**
 * This endpoint can be used to manage backups. Authentication may be required for certain actions.
 */


if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    include(dirname(__FILE__) . '/scripts/backup.php');
} else if ($_SERVER['REQUEST_METHOD'] === 'PUT') {
    include(dirname(__FILE__) . '/scripts/restore.php');
}


if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    http_response_code(405); // 405 Method not allowed
    include(dirname(__FILE__) . '/../../resources/pages/405.php');
    die();
}


require(dirname(__FILE__) . '/../../lib/lib_backup_restore.php');

$complete_backups = [];
$inverse_backups = [];

foreach (scandir($directory) as $file) {
    if (!str_ends_with('.zip')) {
        continue;
    }
    
    if (str_starts_with('complete_backup')) {
        array_push($complete_backup, $file . " " . date("F d Y H:i:s.", filemtime($file)));
    } elseif (str_starts_with('inverse_backup')) {
        array_push($inverse_backup, $file . " " . date("F d Y H:i:s.", filemtime($file)));
    }
}


$response = [
    "complete" => $complete_backups,
    "inverse" => $inverse_backups
    ];

http_response_code(200); // 200 Ok
header('Content-Type: application/json');         
header("Content-Disposition: attachment;filename=backups.json");
echo json_encode($response);
exit(0);

?>