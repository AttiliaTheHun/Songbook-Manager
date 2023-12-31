<?php
include('./lib/lib_settings.php');

if ($settings['homepage']['enabled'] == false) {
    http_response_code(204);
    include './resources/pages/204.html';
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
<h1><img width="24" height="24" src="https://img.icons8.com/sf-black/64/downloading-updates.png" alt="downloading-updates"/> Stáhnout</h1>
<li><a href="" target="_blank" download>Stáhnout (A4)</a></li>
<li><a href="" target="_blank" download>Stáhnout (A5)</a></li>
<li><a href="" target="_blank" download>Stáhnout verzi k oboustrannému tisku (A5)</a></li>
<br>

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