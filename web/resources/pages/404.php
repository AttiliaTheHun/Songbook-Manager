<?php
include(dirname(__FILE__) . '/../../lib/lib_settings.php');
$url = $settings['url'];
?>

<h1>Error 404 - File not found</h1>
<p>What the heck were you typing into that search bar?</p>

<?php
echo "<a href=\"$url\">Back to the homepage, shall we?</a>";
?>