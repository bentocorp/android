<?php
require 'config/conexion.php';
include("login/seguridad.php");

//VARIABLES
$carpetaControladores = "mvc/controladores/";
$controladorPredefinido = "productos";
$accionPredefinida = "listar";

//CONTROLADOR (DEFINICION)
if(! empty($_GET['controlador']))
      $controlador = $_GET['controlador'];
else
      $controlador = $controladorPredefinido;

//ACCION (DEFINICION)
if(! empty($_GET['accion']))
      $accion = $_GET['accion'];
else
      $accion = $accionPredefinida;
	  $controlador = $carpetaControladores . $controlador . 'Controlador.php';

//INCLUIR CONTROLADOR
if(is_file($controlador))
      require_once($controlador);
else
      die('El controlador no existe - 404 not found');

//ACCION
if(is_callable($accion))
      $accion();
else
      die('La accion no existe - 404 not found');
?>