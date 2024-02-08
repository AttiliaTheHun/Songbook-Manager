<?php
include '../../lib/lib_settings.php';
$url = $settings['url'];
?>

<h1>Error 403 - Forbidden</h1>
<p>You know there was the <i>don't try this at home</i> disclaimer in that hacking tutorial, right?</p>

<?php
echo "<a href=\"$url\">Back to the homepage, shall we?</a>";
?>