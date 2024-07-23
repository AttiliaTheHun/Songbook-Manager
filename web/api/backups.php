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
    include(dirname(__FILE__) . '/../resources/pages/405.php');
    die();
}


require(dirname(__FILE__) . '/../lib/lib_backup_restore.php');

$complete_backups = [];
$inverse_backups = [];

foreach (scandir($GLOBALS['backup_file_path']) as $file) {
    if (!str_ends_with($file, '.zip')) {
        continue;
    }
	try {
		$creation_date = date("F d Y H:i:s.", filemtime($GLOBALS['backup_file_path'].$file));
	} catch (\Throwable $t) {
		$creation_date = 'N/A';
	}
    
    if (str_starts_with($file, 'complete_backup')) {
        array_push($complete_backups, $file . " " . $creation_date);
    } elseif (str_starts_with($file, 'inverse_backup')) {
        array_push($inverse_backups, $file . " " . $creation_date);
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