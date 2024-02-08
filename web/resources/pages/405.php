<?php
include '../../lib/lib_settings.php';
$url = $settings['url'];
?>

<h1>405 - Method Not Allowed</h1>
<p>This feature has been disabled by the SongbookManager server administrator.</p>

<?php
echo "<a href=\"$url\">Back to the homepage, shall we?</a>";
?>
