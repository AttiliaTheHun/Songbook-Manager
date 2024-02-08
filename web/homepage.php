<?php
// TODO: add a way to translate this perhaps through a strings.xml file so the script does not have to be modified
/**
 * This file is retrieved when the remote server homepage is visited. This script generates the HTML response
 * file. You can modify the HTML structure to alter the layout of the page or you can make changes to the stylesheet,
 * but modifying the PHP code is not recommended. The homepage feature can be disabled in the settings file.
 **/
include('./lib/lib_settings.php');

if ($settings['homepage']['enabled'] == false) {
    include './resources/pages/405.php';
    http_response_code(405);
    exit(0);
}
?>

<!DOCTYPE HTML>
<html>
<head>
<?php
	include './resources/templates/head.html';
?>
<link rel="stylesheet" href="./resources/css/style_homepage.css">
<link href='https://fonts.googleapis.com/css?family=Dekko' rel='stylesheet'>
<title>Zpěvník Hrabošů</title>
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<body>
<div class="navbar">
<img width="70" height="70" src="./resources/assets/logo.png" alt="Hraboší logo" class="navbar-logo">
<h1>Hraboší zpěvník<h1/>
</div>
<hr>
<div class="content">
<br>
<a href="pageview.php">
<span class="browse-songbook-button">
Prohlížet zpěvník
</span>
</a>
<ul>
<?php
if ($settings["plugin"]["Export"]["enabled"] == true) {
    $temp = [
        '/files/' . $settings["plugin"]["Export"]["defaultExportFileName"],
        '/files/' . $settings["plugin"]["Export"]["singlepageExportFileName"],
        '/files/' . $settings["plugin"]["Export"]["printableExportFileName"]
    ];
    echo "<h1><img width=\"24\" height=\"24\" src=\"https://img.icons8.com/sf-black/64/downloading-updates.png\" alt=\"downloading-updates\"/> Stáhnout</h1>\n";
    echo "<li><a href=\"$temp[1]\" target=\"_blank\" download>Stáhnout (A4)</a></li>\n
          <li><a href=\"$temp[0]\" target=\"_blank\" download>Stáhnout (A5)</a></li>\n
          <li><a href=\"$temp[2]\" target=\"_blank\" download>Stáhnout verzi k oboustrannému tisku (A5)</a></li>\n";
}
?>
<h1><img width="24" height="24" src="https://img.icons8.com/sf-black/64/hashtag.png" alt="hashtag"/> Další</h1>


<li><a href="">Původní zpěvník (Zpěvník Pěšinek a Ostřížů)</a></li>


<h1><img width="24" height="24" src="https://img.icons8.com/pastel-glyph/64/certificate.png" alt="certificate"/> Licenses</h1>


<li><a href="https://icons8.com"  target="_blank" >icons8</a></li>
<li><a href="https://www.needpix.com/photo/download/522268/book-blank-hardcover-spread-pages-white-diary-free-pictures-free-photos"  target="_blank" >needpix</a></li>
</ul>
</div>

<hr class="full-width-hr">
<div class="footer">
<p>Vytvořeno pomocí programu <a href="https://github.com/AttiliaTheHun/Songbook-Manager"  target="_blank" >Songbook Manager</a> • <a href="https://hrabosi.cz"  target="_blank" >Naše stránky</a></p>
</div>
</body>
</html>