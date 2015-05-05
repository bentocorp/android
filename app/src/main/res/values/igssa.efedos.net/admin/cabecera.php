<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<?php /*<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>*/?>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"/>
<title>::<?php global $empresa; echo $empresa; ?>::</title>
    <link href="html/css/estilos_rocimar.css" rel="stylesheet" type="text/css" />
    <link href='favicon.ico' rel='shortcut icon'/>
    <script type="text/javascript" src="html/js/jquery-latest.js"></script>
    <script language="javascript">
		/*$("button").click(function () {
		  //$("#menuListaCategoria").slideToggle("slow");
		  $("#menuListaCategoria").slideToggle("slow");
		});*/
	window.onload = (function(){try{
			//var a = 0;
		$("#desplegarCategoria").mouseover(function () {
			//a = 1;
			$("#menuListaCategoriaParamostrar").show("slow");
			//$(this).attr("class","mostrado");
			//$("#mostrar").attr("id","mostrar2");
		});
		
		$("#cerrar").click(function () {
			$("#menuListaCategoriaParamostrar").hide("slow");
			//a = 0;
			//$("#mostrar2 span").attr("class","");
			//$("#mostrar2").attr("id","mostrar");
		});
		/*$("#col_izq").mouseout(function () {
			if(a != 0)
			$("#menuListaCategoriaParamostrar").animate({height: 'toggle', opacity: 'toggle'}, 7000);
			a = 0;
			//$("#mostrar2 span").attr("class","");
			//$("#mostrar2").attr("id","mostrar");
		});*/
	
	}catch(e){}});

    </script>
</head>
<body>