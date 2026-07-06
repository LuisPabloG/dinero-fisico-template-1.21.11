# Ratonera

Mod de economia fisica para Minecraft Fabric 1.21.11 con billetes en quetzales.

## Requisitos

- Minecraft 1.21.11
- Fabric Loader
- Fabric API
- Java 21

## Compilar

Desde la raiz del proyecto ejecuta:

```powershell
.\gradlew.bat build
```

El jar generado para pruebas queda en `build/libs/ratonera-1.0.5.jar`.

## Instalacion

1. Instala Fabric Loader para Minecraft 1.21.11.
2. Coloca Fabric API en la carpeta `mods` de tu instancia.
3. Copia `build/libs/ratonera-1.0.5.jar` dentro de la carpeta `mods`.
4. Inicia Minecraft con el perfil de Fabric.

## Como usarlo

La version actual del mod incluye 7 billetes fisicos:

- `ratonera:billete_1`
- `ratonera:billete_5`
- `ratonera:billete_10`
- `ratonera:billete_20`
- `ratonera:billete_50`
- `ratonera:billete_100`
- `ratonera:billete_200`

### Uso dentro del juego

1. Obtiene billetes con comandos vanilla como `/give`.
2. Guarda los billetes en tu inventario.
3. Abre el inventario del jugador.
4. En la esquina superior izquierda veras el total acumulado en quetzales.
5. Debajo del total aparece el boton `Transferir`.

## Estado actual

Lo que ya funciona:

- Registro de los 7 billetes fisicos.
- Texturas y modelos de item.
- Calculo del total de quetzales en el inventario del jugador.
- Pantalla de transferencia desde el inventario.
- Solicitudes de transferencia entre jugadores con confirmacion.
- Avisos de transferencia por chat y confirmacion/rechazo.

Lo que aun no esta implementado:

- Comandos propios del mod.

## Comandos disponibles

Por ahora el mod no registra comandos propios como `/ratonera` o `/transferir`.

Los comandos utiles en esta fase son comandos vanilla de Minecraft usando los IDs del mod.

### Dar billetes

```mcfunction
/give @p ratonera:billete_1 1
/give @p ratonera:billete_5 1
/give @p ratonera:billete_10 1
/give @p ratonera:billete_20 1
/give @p ratonera:billete_50 1
/give @p ratonera:billete_100 1
/give @p ratonera:billete_200 1
```

### Ejemplos de prueba

```mcfunction
/give @p ratonera:billete_200 3
/give @p ratonera:billete_50 2
/give @p ratonera:billete_10 4
```

Ese ejemplo deberia mostrar un total de `430 Q` al abrir el inventario.

### Limpiar billetes

```mcfunction
/clear @p ratonera:billete_1
/clear @p ratonera:billete_5
/clear @p ratonera:billete_10
/clear @p ratonera:billete_20
/clear @p ratonera:billete_50
/clear @p ratonera:billete_100
/clear @p ratonera:billete_200
```

## Estructura relevante

- `src/main/java/com/ratonera/RatoneraMod.java`: inicializador principal del mod.
- `src/main/java/com/ratonera/registry/ModItems.java`: registro de billetes.
- `src/main/java/com/ratonera/util/QuetzalInventoryHelper.java`: suma el dinero del inventario.
- `src/client/java/com/ratonera/client/mixin/InventoryScreenMixin.java`: pinta el total y agrega el boton en el inventario.
- `src/main/resources/assets/ratonera/textures/item/`: texturas de los billetes.

## Licencia

Este proyecto mantiene la licencia `CC0-1.0` definida en el repositorio.
