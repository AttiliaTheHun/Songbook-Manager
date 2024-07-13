<?php
// TODO: add a way to translate this perhaps through a strings.xml file so the script does not have to be modified
/**
 * This file is retrieved when the remote server homepage is visited. This script generates the HTML response
 * file. You can modify the HTML structure to alter the layout of the page or you can make changes to the stylesheet,
 * but modifying the PHP code is not recommended. The homepage feature can be disabled in the settings file.
 **/
include_once(dirname(__FILE__) . '/lib/lib_settings.php');

if ($settings['homepage']['enabled'] == false) {
    if ($settings['preview']['enabled']) {
        header("Location: " . $settings['url'] . "/preview/");
        exit(0);
    } else {
        include(dirname(__FILE__) . '/resources/pages/204.php');
        http_response_code(204);
        exit(0);
    }
}
?>

<!DOCTYPE HTML>
<html>
<head>
<?php
    include_once(dirname(__FILE__) . '/lib/lib_strings.php');
    include_once(dirname(__FILE__) . '/lib/init_preview_session.php');
    $head_html = file_get_contents(dirname(__FILE__) . '/resources/templates/head.html');
    $head_html = str_replace($_SESSION['REPLACE_MARKS']['site_description_replace_mark'], $strings['site_description'], $head_html);
    echo $head_html;
?>
<link rel="stylesheet" href="./resources/css/style_homepage.css">
<link href='https://fonts.googleapis.com/css?family=Dekko' rel='stylesheet'>
<title><?php echo $strings['homepage']['page_title']; ?></title>
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<body>
<div class="navbar">
<img width="70" height="70" src="./resources/assets/logo.png" alt="<?php echo $strings['homepage']['header_logo_alt']; ?>" class="navbar-logo">
<h1><?php echo $strings['homepage']['header_text']; ?><h1/>
</div>
<hr>
<div class="content">
<br>

<?php
// when preview is disabled, there is no point in showing a link to it
if ($settings['preview']['enabled']) {
    echo '<a href="preview.php">';
    echo '<span class="browse-songbook-button">';
    echo $strings['homepage']['browse_songbook_button_text'];
    echo '</a>';
    echo '</span>';
}
?>

<ul>
<?php
if ($settings["plugin"]["Export"]["enabled"] == true) {
    $temp = [
        '/files/' . $settings["plugin"]["Export"]["defaultExportFileName"],
        '/files/' . $settings["plugin"]["Export"]["singlepageExportFileName"],
        '/files/' . $settings["plugin"]["Export"]["printableExportFileName"]
    ];
    echo "<h1><img width=\"24\" height=\"24\" src=\"https://img.icons8.com/sf-black/64/downloading-updates.png\" alt=\"downloading-updates\"/> Stáhnout</h1>\n";
    echo "<li><a href=\"$temp[1]\" target=\"_blank\" download>{$strings['homepage']['download_singlepage_A4']}</a></li>\n
          <li><a href=\"$temp[0]\" target=\"_blank\" download>{$strings['homepage']['download_default_A5']}</a></li>\n
          <li><a href=\"$temp[2]\" target=\"_blank\" download>{$strings['homepage']['download_printable_A5']}</a></li>\n";
}
?>
<h1><img width="24" height="24" src="https://img.icons8.com/sf-black/64/hashtag.png" alt="hashtag"/> Další</h1>


<li><a href="">Původní zpěvník (Zpěvník Pěšinek a Ostřížů)</a></li>


<h1><img width="24" height="24" src="https://img.icons8.com/pastel-glyph/64/certificate.png" alt="certificate"/> <?php echo $strings['homepage']['licenses']; ?></h1>


<li><a href="https://icons8.com"  target="_blank" >icons8</a></li>
<li><a href="https://www.needpix.com/photo/download/522268/book-blank-hardcover-spread-pages-white-diary-free-pictures-free-photos"  target="_blank" >needpix</a></li>
</ul>
</div>

<hr class="full-width-hr">
<div class="footer">
<p><?php echo $strings['homepage']['footer_credits_text']; ?> <a href="https://github.com/AttiliaTheHun/Songbook-Manager"  target="_blank" >Songbook Manager</a> • <a href="https://hrabosi.cz"  target="_blank" >Naše stránky</a></p>
</div>
</body>
</html>