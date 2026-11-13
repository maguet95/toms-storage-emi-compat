# Tom's Storage EMI Compat

Mod de cliente para Fabric 1.21.11. Devuelve a **Tom's Simple Storage** la integración con **EMI** que su versión
Fabric deja fuera (su `build.gradle` excluye `com/tom/storagemod/emi/**`, aunque el `fabric.mod.json` la sigue
declarando y el registro muestra `ClassNotFoundException: com.tom.storagemod.emi.EMIPlugin`).

Con este mod, el botón **+** de EMI en la terminal de crafteo (y en las terminales inalámbricas) llena la cuadrícula
sacando los ingredientes del almacenamiento y del inventario. Lo que falta se marca en rojo y se coloca lo que haya.
Fuera de la terminal, el + de EMI sigue igual.

Solo cliente: el llenado lo hace el servidor de Tom's con el mismo mensaje `{fill: <receta>}` que usa su integración de JEI.

## Compilar
Pon en `libs/` los jars de `toms_storage_fabric-1.21.11-2.8.0.jar` y `PF-EMI-1.21.11.jar` (solo para compilar) y
corre `./gradlew build`.

## Origen y licencia
Basado en el código de integración con EMI de Tom's Storage, de tom5454, bajo licencia MIT (`original/LICENSE`).
El original está en `original/` como referencia. Este mod también es MIT.
