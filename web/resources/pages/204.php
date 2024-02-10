<?php
include '../../lib/lib_settings.php';
$url = $settings['url'];
?>

<h1>204 - No Content</h1>
<p>This page has been disabled.</p>

<?php
echo "<a href=\"$url\">Back to the homepage, shall we?</a>";
?>
