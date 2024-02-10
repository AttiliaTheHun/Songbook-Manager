<?php
include '../../lib/lib_settings.php';
$url = $settings['url'];
?>

<h1>405 - Method Not Allowed</h1>
<p>The HTTP method is not applicable on this path.</p>

<?php
echo "<a href=\"$url\">Back to the homepage, shall we?</a>";
?>
