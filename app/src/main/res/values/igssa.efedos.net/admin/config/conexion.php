<?php
error_reporting(0);
switch($_SERVER["SERVER_NAME"]){
	case 'igssa.efedos.local';
		$servidor = 'localhost';
		$bd = 'efedos_igssa';
		$usuario = 'root';
		$contrasenia = '';
		break;
	default:
		$servidor = 'localhost';
		$bd = 'efedos_igssa';
		$usuario = 'efedos_igssa';
		$contrasenia = '$T#bHDEORb*?';
		break;
}
/*
$bd = 'wsd_igssa';
$usuario = 'wsd_igssa';
$contrasenia = '!@igssa';
*/
global $empresa;
$empresa = 'IGSSA-ADMIN';

?>