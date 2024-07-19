<?php
include(dirname(__FILE__) . '/../../lib/lib_settings.php');
$url = $settings['url'];
?>

<h1>Error 401 - Unauthorized</h1>
<p>You are not allowed to access this page without previous authentication.</p>

<?php
echo "<a href=\"$url\">Back to the homepage, shall we?</a>";
?>


