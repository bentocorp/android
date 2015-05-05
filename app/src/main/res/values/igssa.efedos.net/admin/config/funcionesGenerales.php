<?php
global $servidor, $bd, $usuario, $contrasenia;
$db = new PDO('mysql:host=' . $servidor . ';dbname=' . $bd, $usuario, $contrasenia);

function GlistarCategorias($db)
{
	$consulta = $db->prepare('SELECT * FROM galerias ORDER BY id DESC');
	$consulta->execute();
	return $consulta->fetchAll();
}
$categoriaL = GlistarCategorias($db);
//print_r($categoriaL);

function categoriaM($id)
{
	global $servidor, $bd, $usuario, $contrasenia;
	$db = new PDO('mysql:host=' . $servidor . ';dbname=' . $bd, $usuario, $contrasenia);

	$consulta = $db->prepare('SELECT * FROM galerias WHERE id = :id ORDER BY id DESC');
	$consulta->bindParam(':id', $id, PDO::PARAM_INT);
	$consulta->execute() or die (print_r($consulta->errorInfo()));
	$consulta = $consulta->fetchAll();
	echo $consulta[0]['galeria'];
}

function productosM($id)
{
	global $servidor, $bd, $usuario, $contrasenia;
	$db = new PDO('mysql:host=' . $servidor . ';dbname=' . $bd, $usuario, $contrasenia);

	$consulta = $db->prepare('SELECT count(*) pcant FROM fotos WHERE galeria = :id');
	$consulta->bindParam(':id', $id, PDO::PARAM_INT);
	$consulta->execute() or die (print_r($consulta->errorInfo()));
	$consulta = $consulta->fetch();
	echo " ( ".$consulta['pcant']." )";
}

function fabricanteM($id)
{
	global $servidor, $bd, $usuario, $contrasenia;
	$db = new PDO('mysql:host=' . $servidor . ';dbname=' . $bd, $usuario, $contrasenia);

	$consulta = $db->prepare('SELECT * FROM fabricantes WHERE id = :id');
	$consulta->bindParam(':id', $id, PDO::PARAM_INT);
	$consulta->execute() or die (print_r($consulta->errorInfo()));
	$consulta = $consulta->fetchAll();
	echo $consulta[0]['fabricante'];
}

function umedidaM($id)
{
	global $servidor, $bd, $usuario, $contrasenia;
	$db = new PDO('mysql:host=' . $servidor . ';dbname=' . $bd, $usuario, $contrasenia);

	$consulta = $db->prepare('SELECT * FROM umedidas WHERE id = :id');
	$consulta->bindParam(':id', $id, PDO::PARAM_INT);
	$consulta->execute() or die (print_r($consulta->errorInfo()));
	$consulta = $consulta->fetchAll();
	echo $consulta[0]['umedida'];
}

?>