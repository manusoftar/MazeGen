# Vista en Primera Persona - MazeGen 3D

## Descripción

Se ha agregado una nueva funcionalidad a MazeGen que permite visualizar y recorrer el laberinto en primera persona, similar a los juegos clásicos como Wolfenstein 3D, Doom y Duke Nukem 3D.

## Tecnología Utilizada

La implementación utiliza **Ray Casting** (también conocido como "falso 3D"), la misma técnica utilizada en los clásicos juegos de FPS de principios de los 90s.

### ¿Por qué Ray Casting?

- **Simplicidad**: No requiere librerías 3D externas (OpenGL, etc.)
- **Rendimiento**: Es muy eficiente y funciona directamente con Java2D
- **Ideal para laberintos tipo grid**: Perfecto para la estructura de datos actual del proyecto
- **Extensible**: Fácil de agregar texturas en el futuro
- **Multiplataforma**: Compatible con cualquier sistema que corra Java

## Archivos Nuevos

### `RayCastingPanel.java`
Panel que renderiza la vista en primera persona:
- Implementa el algoritmo de ray casting
- Maneja el movimiento y rotación del jugador
- Detecta colisiones con las paredes
- Aplica sombreado según la distancia (efecto de profundidad)

### `FirstPersonView.java`
Ventana principal de la vista 3D:
- Contiene el RayCastingPanel
- Muestra información de controles
- Permite actualizar el laberinto dinámicamente

## Modificaciones a Archivos Existentes

### `IDE.java`
- Agregado import de `graph.MazeObj`
- Agregado menú "Vista en Primera Persona (3D)" con atajo `Ctrl+F`
- Agregado método `jMenuItem12ActionPerformed()` para abrir la ventana 3D
- Variable `firstPersonView` para mantener la referencia a la ventana

### `MazeGRID.java`
- Agregados getters públicos:
  - `getAncho()` - Retorna el ancho del laberinto
  - `getAlto()` - Retorna el alto del laberinto  
  - `getCellSize()` - Retorna el tamaño de cada celda
  - `getInit()` - Retorna el punto de inicio
  - `getFin()` - Retorna el punto de salida

## Cómo Usar

1. **Generar un laberinto**: Use las opciones existentes del menú para generar un laberinto
2. **Abrir vista 3D**: 
   - Menú: `Laberinto → Vista en Primera Persona (3D)`
   - Atajo: `Ctrl+F`
3. **Navegar**:
   - `W` o `↑`: Mover hacia adelante
   - `S` o `↓`: Mover hacia atrás
   - `A`: Girar a la izquierda
   - `D`: Girar a la derecha
   - `Q`: Strafe (desplazamiento lateral) a la izquierda
   - `E`: Strafe (desplazamiento lateral) a la derecha

## Características Técnicas

### Ray Casting
- **FOV (Campo de Visión)**: 60 grados (π/3 radianes)
- **Resolución**: 800x600 píxeles
- **Corrección Fish-Eye**: Implementada para evitar distorsión en los bordes
- **Detección de colisiones**: Sistema de margen de 0.1 unidades desde las paredes

### Renderizado
- **Paredes verticales (Este-Oeste)**: Color más claro (RGB: 150, 150, 150)
- **Paredes horizontales (Norte-Sur)**: Color más oscuro (RGB: 100, 100, 100)
- **Piso**: Gris oscuro (RGB: 80, 80, 80)
- **Techo**: Gris muy oscuro (RGB: 50, 50, 50)
- **Sombreado dinámico**: Las paredes se oscurecen con la distancia

### Física del Jugador
- **Velocidad de movimiento**: 0.1 unidades por frame
- **Velocidad de rotación**: 0.05 radianes por frame (~2.86 grados)
- **Posición inicial**: Centro de la celda de inicio del laberinto

## Mejoras Futuras Posibles

1. **Texturas**: Reemplazar colores planos por texturas bitmap
2. **Minimap**: Mostrar un mapa pequeño en una esquina
3. **Efectos de iluminación**: Agregar antorchas o luz variable
4. **Modo OpenGL**: Opción para cambiar a renderizado 3D real
5. **Modo multijugador**: Permitir múltiples jugadores
6. **Enemigos/NPCs**: Agregar elementos móviles en el laberinto
7. **Recolectables**: Agregar objetos para recoger
8. **Vista estereoscópica**: Soporte para VR

## Personalización

Los colores pueden ser personalizados usando los métodos:

```java
firstPersonView.setWallColors(colorNS, colorEW);
firstPersonView.setFloorCeilingColors(colorPiso, colorTecho);
```

## Notas de Desarrollo

- El algoritmo es intencionalmente simple para facilitar futuras mejoras
- El código está comentado para fácil comprensión
- La arquitectura permite fácil migración a OpenGL si se desea en el futuro
- Compatible con la estructura de datos existente del proyecto sin cambios disruptivos
