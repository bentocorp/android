<?php include("config/funcionesGenerales.php"); ?>
<div id="col_izq">
	<h2>MENU PRINCIPAL</h2>
	<ul>
    	<li><a href="./" class="pri<?php if(empty($_GET['controlador'])) echo "_activo"; ?>">FOTOS</a></li>
    	<li><a href="?controlador=categorias" class="pri<?php if($_GET['controlador'] == "categorias") echo "_activo"; ?>"><span id="desplegarCategoria">[&bull;]</span> GALERIAS</a></li>
            <div id="menuListaCategoriaParamostrar" <?php if($_GET['controlador'] != "categorias") echo 'style="display:none;"'; ?>>
                <ul>
                <?php foreach($categoriaL as $categoria){ 
				if($categoria['id'] == $_GET['id']){
				?>
                    <li><a href="?controlador=galerias&accion=ver&id=<?php echo $categoria['id']?>" title="Ver productos con esta categoría" class="activo"><?php categoriaM($categoria['id']); productosM($categoria['id']);?></a></li>
                <?php }else{ ?>
                    <li><a href="?controlador=galerias&accion=ver&id=<?php echo $categoria['id']?>" title="Ver productos con esta categoría" class="sub"><?php categoriaM($categoria['id']); productosM($categoria['id']);?></a></li>
                <?php }
				} ?>
                </ul>
                <div id="cerrar"> [ ^ ] Cerrar [ ^ ] </div>
      </div>
    	<?php /*?><li><a href="?controlador=fabricantes" class="pri<?php if($_GET['controlador'] == "fabricantes") echo "_activo"; ?>">FABRICANTES</a></li>
    	<li><a href="?controlador=umedidas" class="pri<?php if($_GET['controlador'] == "umedidas") echo "_activo"; ?>">UNIDADES DE MEDIDA</a></li><?php */?>
  </ul>
  <div id="buscador">
  	<h2>Buscar</h2>
      <form method="get">
        <input type="hidden" name="controlador" value="buscar" />
      	<input type="text" name="buscar" onfocus="if(this.value=='buscar...'){ this.value=''; this.style.backgroundColor='#FFFFCC';}" onblur="if(this.value==''){ this.style.backgroundColor=''; this.value='buscar...';}" onmouseover="this.style.backgroundColor='#FFFFCC'" <?php if(empty($_GET['buscar'])){ ?> onmouseout="if(this.value!=''){ this.style.backgroundColor='';}"<?php } ?> value="<?php if(!empty($_GET['buscar'])){echo $_GET['buscar'];}else{echo 'buscar...';}?>" <?php if(!empty($_GET['buscar'])) echo 'style="background:#FFFFCC"'; ?> />
      </form>
  </div>
</div>