
<?php
if ($_FILES['file']['name'] != null) { //<-- Saber si selecciono un archivo
    $ArchivoDestino = "fotos/".$_POST['id'].".jpg"; //<-- asi se llamara el archivo en tu servidor, yo utilice POST[id] para nombrarlo, tu elige el que necesites
    if(move_uploaded_file($_FILES['file']['tmp_name'], $ArchivoDestino )) { //<-- Movemos el Temporal del servidor  y lo renombramos con nombre elegido
        
        //Informamos
        header("Content-type: application/json");
        $Nodo = new stdClass;
        $Nodo ->Exito = "TRUE";
        $Nodo ->Info = "Se Grabo con exito ".$ArchivoDestino;
        $Nodo ->Error = "";
        $elJSON = json_encode($Nodo); 
        echo $elJSON;

    } else{

        //Si hay problemas mandamos el error
        header("Content-type: application/json");
        $Nodo = new stdClass;
        $Nodo ->Exito = "TRUE";
        $Nodo ->Info = "";
        $Nodo ->Error = "Hubo un error al intentar grabar";
        $elJSON = json_encode($Nodo); 
        echo $elJSON;
    }

} else { //<-- sino selecciono un archivo le indicara
    header("Content-type: application/json");
    $Nodo = new stdClass;
    $Nodo ->Exito = "TRUE";
    $Nodo ->Info = "sin archivo";
    $Nodo ->Error = "Hubo un error al intentar grabar";
    $elJSON = json_encode($Nodo); 
    echo $elJSON;
}




?>
