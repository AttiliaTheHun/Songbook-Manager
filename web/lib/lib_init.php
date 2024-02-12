<?php
/**
 * Initializes variables and loads libraries necesary for most of the other libraries.
 **/

require_once dirname(__FILE__) . '/lib_env_var.php';
require_once dirname(__FILE__) . '/lib_settings.php';
require_once dirname(__FILE__) . '/lib_collection.php';
require_once dirname(__FILE__) . '/lib_index.php';


init_collections(false);
init_index();

?>