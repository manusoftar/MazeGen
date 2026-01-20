package mazegen;

import graph.MazeObj;
import graph.cell;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Panel que renderiza una vista en primera persona del laberinto usando ray casting
 * Similar a Wolfenstein 3D / Doom
 * OPTIMIZADO para mejor rendimiento con texturas pre-calculadas
 * 
 * @author manusoftar
 */
public class RayCastingPanel extends JPanel {
    
    private MazeObj maze;
    private double playerX;  // Posición del jugador en coordenadas del mundo
    private double playerY;
    private double playerAngle;  // Ángulo de visión en radianes
    private Point lastCellPosition;  // Última celda visitada para rastrear movimientos
    private double wallThickness = 0.15;  // Grosor de las paredes (ajustable)
    private double passageWidth = 0.8;    // Ancho de pasillo (ajustable)
        public double getPassageWidth() { return passageWidth; }
        public void setPassageWidth(double v) { passageWidth = v; repaint(); }
    
    // Dimensiones iniciales, pero ahora se usará getWidth()/getHeight()
    private static final int DEFAULT_PANEL_WIDTH = 800;
    private static final int DEFAULT_PANEL_HEIGHT = 600;
    private static final double FOV = Math.PI / 3.0;  // Campo de visión 60 grados
    private static final double MOVE_SPEED = 0.04;
    private static final double ROTATION_SPEED = 0.025;
    
    // Texturas pre-calculadas (OPTIMIZACIÓN)
    private static final int TEXTURE_SIZE = 64;
    private BufferedImage brickTexture;
    private BufferedImage floorTexture;
    private BufferedImage ceilingTexture;
    
    // Buffer para renderizado (OPTIMIZACIÓN CRÍTICA)
    private BufferedImage screenBuffer;
    
    // Colores para el renderizado
    private Color wallColorNS = new Color(100, 100, 100);
    private Color wallColorEW = new Color(150, 150, 150);
    private Color floorColor = new Color(180, 140, 80);  // Dorado/arena
    private Color ceilingColor = new Color(80, 80, 80);  // Gris
    
    // Sistema de teclas presionadas para movimiento suave
    private Set<Integer> keysPressed = new HashSet<>();
    private Timer gameLoop;
    
    // Debug visual para paredes por lado
    private boolean debugWallColors = false;

    // ...existing code...

    // --- Parámetros ajustables para debug/control ---
    private double esquinaUmbral = 0.08;
    private double fov = FOV;
    private double darkDistance = 10.0;

    public double getEsquinaUmbral() { return esquinaUmbral; }
    public void setEsquinaUmbral(double v) { esquinaUmbral = v; repaint(); }
    public double getFov() { return fov; }
    public void setFov(double v) { fov = v; repaint(); }
    public double getDarkDistance() { return darkDistance; }
    public void setDarkDistance(double v) { darkDistance = v; repaint(); }
    public double getWallThickness() { return wallThickness; }
    public void setWallThickness(double v) { wallThickness = v; repaint(); }
    public boolean isDebugWallColors() { return debugWallColors; }
    public void setDebugWallColors(boolean v) { debugWallColors = v; repaint(); }


    public RayCastingPanel(MazeObj maze) {
        this.maze = maze;
        
        // Pre-calcular texturas una sola vez (GRAN OPTIMIZACIÓN)
        generateTextures();
        
        // Crear buffer de pantalla (OPTIMIZACIÓN CRÍTICA)
        screenBuffer = new BufferedImage(DEFAULT_PANEL_WIDTH, DEFAULT_PANEL_HEIGHT, BufferedImage.TYPE_INT_RGB);
        
        // Inicializar posición del jugador desde la posición actual en 2D
        Point jugadaActual = maze.getJugadaActual();
        if (jugadaActual != null && (jugadaActual.x != 0 || jugadaActual.y != 0)) {
            // Si hay una posición ya establecida, usarla
            playerX = jugadaActual.x + 0.5;
            playerY = jugadaActual.y + 0.5;
            lastCellPosition = new Point(jugadaActual.x, jugadaActual.y);
        } else {
            // Si no, usar la posición inicial
            Point init = maze.getInit();
            if (init != null) {
                playerX = init.x + 0.5;
                playerY = init.y + 0.5;
                lastCellPosition = new Point(init.x, init.y);
                maze.setJugadaActual(init);
            } else {
                playerX = 1.5;
                playerY = 1.5;
                lastCellPosition = new Point(1, 1);
                maze.setJugadaActual(new Point(1, 1));
            }
        }
        
        playerAngle = 0;  // Mirando hacia el este
        
        setPreferredSize(new Dimension(DEFAULT_PANEL_WIDTH, DEFAULT_PANEL_HEIGHT));
        setFocusable(true);
        setBackground(Color.BLACK);
        // Recuperar foco al hacer click
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                requestFocusInWindow();
            }
        });
        
        // Sistema de teclas para permitir múltiples teclas presionadas simultáneamente
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                keysPressed.add(e.getKeyCode());
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                keysPressed.remove(e.getKeyCode());
            }
        });
        
        // Game loop para actualizar posición basado en teclas presionadas
        gameLoop = new Timer(16, e -> {  // ~60 FPS
            updatePlayerPosition();
            repaint();
        });
        gameLoop.start();
    }
    
    /**
     * Pre-genera texturas una sola vez para máximo rendimiento
     */
    private void generateTextures() {
        // Textura de ladrillos rojos
        brickTexture = new BufferedImage(TEXTURE_SIZE, TEXTURE_SIZE, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < TEXTURE_SIZE; y++) {
            for (int x = 0; x < TEXTURE_SIZE; x++) {
                // Patrón de ladrillos
                int brickH = 8;  // Altura de ladrillo
                int brickW = 16; // Ancho de ladrillo
                
                int row = y / brickH;
                int col = x / brickW;
                int offset = (row % 2) * (brickW / 2);
                int adjX = (x + offset) % TEXTURE_SIZE;
                
                // Mortero (líneas grises)
                if (y % brickH == 0 || adjX % brickW == 0) {
                    brickTexture.setRGB(x, y, new Color(200, 200, 200).getRGB());
                } else {
                    // Ladrillo rojo con variación
                    int variation = (int)((Math.sin(x * 0.5) + Math.cos(y * 0.3)) * 10);
                    int red = Math.min(255, Math.max(0, 160 + variation));
                    int green = Math.min(255, Math.max(0, 50 + variation / 2));
                    int blue = Math.min(255, Math.max(0, 40 + variation / 2));
                    brickTexture.setRGB(x, y, new Color(red, green, blue).getRGB());
                }
            }
        }
        
        // Textura de piso dorado
        floorTexture = new BufferedImage(TEXTURE_SIZE, TEXTURE_SIZE, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < TEXTURE_SIZE; y++) {
            for (int x = 0; x < TEXTURE_SIZE; x++) {
                double noise = Math.sin(x * 0.3) * Math.cos(y * 0.3) * 0.1;
                int baseColor = (int)(180 + noise * 30);
                int r = Math.min(255, Math.max(0, baseColor + 20));
                int g = Math.min(255, Math.max(0, baseColor - 20));
                int b = Math.min(255, Math.max(0, baseColor - 80));
                floorTexture.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }
        
        // Textura de techo gris con baldosas
        ceilingTexture = new BufferedImage(TEXTURE_SIZE, TEXTURE_SIZE, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < TEXTURE_SIZE; y++) {
            for (int x = 0; x < TEXTURE_SIZE; x++) {
                int tileSize = TEXTURE_SIZE / 4;
                int tileX = x / tileSize;
                int tileY = y / tileSize;
                int baseGray = ((tileX + tileY) % 2 == 0) ? 90 : 100;
                ceilingTexture.setRGB(x, y, new Color(baseGray, baseGray, baseGray).getRGB());
            }
        }
    }
    
    private void updatePlayerPosition() {
        if (keysPressed.isEmpty()) {
            return; // No hay teclas presionadas, no hacer nada
        }
        
        double newX = playerX;
        double newY = playerY;
        boolean moved = false;
        
        // Rotación
        if (keysPressed.contains(KeyEvent.VK_A)) {
            playerAngle -= ROTATION_SPEED;
            moved = true;
        }
        if (keysPressed.contains(KeyEvent.VK_D)) {
            playerAngle += ROTATION_SPEED;
            moved = true;
        }
        
        // Movimiento adelante/atrás
        if (keysPressed.contains(KeyEvent.VK_W) || keysPressed.contains(KeyEvent.VK_UP)) {
            newX = playerX + Math.cos(playerAngle) * MOVE_SPEED;
            newY = playerY + Math.sin(playerAngle) * MOVE_SPEED;
            moved = true;
        }
        if (keysPressed.contains(KeyEvent.VK_S) || keysPressed.contains(KeyEvent.VK_DOWN)) {
            newX = playerX - Math.cos(playerAngle) * MOVE_SPEED;
            newY = playerY - Math.sin(playerAngle) * MOVE_SPEED;
            moved = true;
        }
        
        // Strafe (movimiento lateral)
        if (keysPressed.contains(KeyEvent.VK_Q)) {
            newX = playerX + Math.cos(playerAngle - Math.PI/2) * MOVE_SPEED;
            newY = playerY + Math.sin(playerAngle - Math.PI/2) * MOVE_SPEED;
            moved = true;
        }
        if (keysPressed.contains(KeyEvent.VK_E)) {
            newX = playerX + Math.cos(playerAngle + Math.PI/2) * MOVE_SPEED;
            newY = playerY + Math.sin(playerAngle + Math.PI/2) * MOVE_SPEED;
            moved = true;
        }
        
        // Verificar colisión antes de mover
        if (moved && !checkCollision(newX, newY)) {
            playerX = newX;
            playerY = newY;
            
            // SINCRONIZACIÓN: Actualizar posición en la vista 2D
            int cellX = (int) playerX;
            int cellY = (int) playerY;
            Point newCellPosition = new Point(cellX, cellY);
            
            // Si cambió de celda, registrar el movimiento
            if (!newCellPosition.equals(lastCellPosition)) {
                maze.addMovimiento(lastCellPosition, newCellPosition);
                lastCellPosition = newCellPosition;
            }
            
            maze.setJugadaActual(newCellPosition);
        }
    }
    
    private boolean checkCollision(double x, double y) {
        int cellX = (int) x;
        int cellY = (int) y;
        
        // Verificar límites del laberinto
        if (cellX < 0 || cellX >= maze.getWidth() || cellY < 0 || cellY >= maze.getHeight()) {
            return true;
        }
        
        cell[][] board = maze.getBoard();
        if (board == null || board[cellY][cellX] == null) {
            return false;
        }
        
        cell currentCell = board[cellY][cellX];
        
        // Calcular la posición dentro de la celda (0.0 a 1.0)
        double cellLocalX = x - cellX;
        double cellLocalY = y - cellY;
        
        // Margen para el jugador + grosor de pared
        double margin = 0.1 + wallThickness;
        
        // Verificar colisión con cada pared
        if (cellLocalY < margin && currentCell.getWall(0)) return true;  // Norte
        if (cellLocalX < margin && currentCell.getWall(1)) return true;  // Oeste
        if (cellLocalY > 1.0 - margin && currentCell.getWall(2)) return true;  // Sur
        if (cellLocalX > 1.0 - margin && currentCell.getWall(3)) return true;  // Este
        
        return false;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int panelWidth = getWidth();
        int panelHeight = getHeight();

        // Si el tamaño cambió, recrear el buffer
        if (screenBuffer.getWidth() != panelWidth || screenBuffer.getHeight() != panelHeight) {
            screenBuffer = new BufferedImage(panelWidth, panelHeight, BufferedImage.TYPE_INT_RGB);
        }

        cell[][] board = maze.getBoard();
        if (board == null) return;

        // Obtener posiciones de entrada y salida
        Point entrada = maze.getInit();
        Point salida = maze.getFin();

        // Dibujar en el buffer en lugar de directamente en pantalla (OPTIMIZACIÓN CRÍTICA)
        int[] pixels = ((java.awt.image.DataBufferInt) screenBuffer.getRaster().getDataBuffer()).getData();

        // Preparar colores especiales para entrada (verde) y salida (rojo)
        Color entradaColor = new Color(100, 200, 100);  // Verde claro
        Color salidaColor = new Color(200, 100, 100);   // Rojo claro

        // Limpiar buffer - piso y techo con colores simples (o especiales si es entrada/salida)
        int currentCellX = (int) playerX;
        int currentCellY = (int) playerY;

        Color currentFloorColor = floorColor;
        if (entrada != null && currentCellX == entrada.x && currentCellY == entrada.y) {
            currentFloorColor = entradaColor;
        } else if (salida != null && currentCellX == salida.x && currentCellY == salida.y) {
            currentFloorColor = salidaColor;
        }

        int floorRGB = currentFloorColor.getRGB();
        int ceilingRGB = ceilingColor.getRGB();

        // Sombreado por distancia para piso y techo
        for (int y = 0; y < panelHeight; y++) {
            int baseIndex = y * panelWidth;
            for (int x = 0; x < panelWidth; x++) {
                double relY = (double)y / panelHeight;
                // Calcular distancia "falsa" para piso/techo (más lejos cuanto más cerca del fondo)
                double fakeDist = (y < panelHeight / 2)
                    ? (panelHeight / 2 - y) / (double)(panelHeight / 2) * darkDistance
                    : (y - panelHeight / 2) / (double)(panelHeight / 2) * darkDistance;
                float brightness = Math.max(0.3f, Math.min(1.0f, (float)(1.0 - fakeDist / (float)darkDistance)));
                // Invertir: más lejos, más oscuro
                brightness = Math.max(0.3f, Math.min(1.0f, (float)(1.0 - Math.abs(relY - 0.5) * 2)));
                int color = (y < panelHeight / 2) ? ceilingRGB : floorRGB;
                Color c = new Color(color);
                int r = (int)(c.getRed() * brightness);
                int gcol = (int)(c.getGreen() * brightness);
                int b = (int)(c.getBlue() * brightness);
                pixels[baseIndex + x] = (r << 16) | (gcol << 8) | b;
            }
        }

        // Renderizar paredes columna por columna
        for (int x = 0; x < panelWidth; x++) {
            double cameraX = 2 * x / (double)panelWidth - 1;
            double rayAngle = playerAngle + Math.atan(cameraX * Math.tan(fov / 2));

            double rayDirX = Math.cos(rayAngle);
            double rayDirY = Math.sin(rayAngle);

            RayHit hit = castRay(playerX, playerY, rayDirX, rayDirY);

            if (hit != null) {
                double perpDistance = hit.distance * Math.cos(rayAngle - playerAngle);

                int wallHeight = (int) (panelHeight / perpDistance);
                int wallTop = (panelHeight - wallHeight) / 2;
                int wallBottom = wallTop + wallHeight;

                // Limitar a los bordes de la pantalla
                int drawTop = Math.max(0, wallTop);
                int drawBottom = Math.min(panelHeight, wallBottom);

                // Sombreado por distancia
                float brightness = Math.max(0.3f, Math.min(1.0f, 1.0f - (float)(perpDistance / (float)darkDistance)));

                double wallX = hit.wallX;
                if (wallX < 0.001) wallX = 0.0;
                if (wallX > 0.999) wallX = 1.0;
                int texX = (int)(wallX * (TEXTURE_SIZE - 1));

                for (int y = drawTop; y < drawBottom; y++) {
                    int texY = (int)(((y - wallTop) * TEXTURE_SIZE) / wallHeight) & (TEXTURE_SIZE - 1);
                    int rgb = brickTexture.getRGB(texX, texY);
                    Color c = new Color(rgb);
                    int r = (int)(c.getRed() * brightness);
                    int gcol = (int)(c.getGreen() * brightness);
                    int b = (int)(c.getBlue() * brightness);

                    if (debugWallColors) {
                        // DEBUG VISUAL: Colores por lado
                        Color debugColor = Color.MAGENTA;
                        if (hit.isVertical) {
                            if (rayDirX > 0) debugColor = Color.BLUE;    // Oeste
                            else debugColor = Color.CYAN;                // Este
                        } else {
                            if (rayDirY > 0) debugColor = Color.RED;     // Norte
                            else debugColor = Color.GREEN;               // Sur
                        }
                        // Mezclar color de debug (50%)
                        r = (r + debugColor.getRed()) / 2;
                        gcol = (gcol + debugColor.getGreen()) / 2;
                        b = (b + debugColor.getBlue()) / 2;
                    }
                    pixels[y * panelWidth + x] = (r << 16) | (gcol << 8) | b;
                }
            }
        }

        // Dibujar el buffer completo en la pantalla de una sola vez (MÁXIMA OPTIMIZACIÓN)
        g.drawImage(screenBuffer, 0, 0, panelWidth, panelHeight, null);

        // Info de debug
        g.setColor(Color.WHITE);
        g.drawString(String.format("Pos: (%.2f, %.2f)", playerX, playerY), 10, 20);
        g.drawString("Optimizado - Windows 98 Style", 10, panelHeight - 20);
    }
    
    // Raycasting mejorado: sub-stepping en esquinas para evitar escalones
    private RayHit castRay(double startX, double startY, double dirX, double dirY) {
        cell[][] board = maze.getBoard();
        double rayX = startX;
        double rayY = startY;
        double stepSize = 0.005;  // Pasos más finos para precisión
        double maxDistance = 20.0;
        int lastWall = -1;
        double hitX = 0, hitY = 0;
        
        for (int step = 0; step < maxDistance / stepSize; step++) {
            double prevX = rayX;
            double prevY = rayY;
            
            rayX += dirX * stepSize;
            rayY += dirY * stepSize;
            
            int cellX = (int) rayX;
            int cellY = (int) rayY;
            if (cellX < 0 || cellX >= maze.getWidth() || cellY < 0 || cellY >= maze.getHeight()) {
                break;
            }
            
            cell currentCell = board[cellY][cellX];
            if (currentCell == null) continue;
            
            double localX = rayX - cellX;
            double localY = rayY - cellY;
            
            // Detectar colisión con cada pared
            boolean hitNorth = localY < wallThickness && currentCell.getWall(0);
            boolean hitWest  = localX < wallThickness && currentCell.getWall(1);
            boolean hitSouth = localY > 1.0 - wallThickness && currentCell.getWall(2);
            boolean hitEast  = localX > 1.0 - wallThickness && currentCell.getWall(3);
            
            int hitCount = (hitNorth ? 1 : 0) + (hitWest ? 1 : 0) + (hitSouth ? 1 : 0) + (hitEast ? 1 : 0);
            
            if (hitCount > 0) {
                // Si hay colisión, usar sub-stepping para encontrar la pared exacta
                if (hitCount == 1) {
                    // Una sola pared - uso directo
                    hitX = rayX;
                    hitY = rayY;
                    if (hitNorth) lastWall = 0;
                    else if (hitWest) lastWall = 1;
                    else if (hitSouth) lastWall = 2;
                    else if (hitEast) lastWall = 3;
                } else {
                    // Múltiples paredes - usar interpolación para encontrar el punto exacto
                    double bestT = 1.0;  // Parámetro de interpolación [0,1]
                    int bestWall = -1;
                    
                    // Interpolar linealmente entre prevX,prevY y rayX,rayY
                    for (int subStep = 1; subStep <= 20; subStep++) {
                        double t = subStep / 20.0;
                        double interpX = prevX + (rayX - prevX) * t;
                        double interpY = prevY + (rayY - prevY) * t;
                        
                        int iCellX = (int) interpX;
                        int iCellY = (int) interpY;
                        
                        if (iCellX < 0 || iCellX >= maze.getWidth() || iCellY < 0 || iCellY >= maze.getHeight()) {
                            break;
                        }
                        
                        cell iCell = board[iCellY][iCellX];
                        if (iCell == null) continue;
                        
                        double iLocalX = interpX - iCellX;
                        double iLocalY = interpY - iCellY;
                        
                        // Detectar qué pared se golpea primero
                        boolean iNorth = iLocalY < wallThickness && iCell.getWall(0);
                        boolean iWest  = iLocalX < wallThickness && iCell.getWall(1);
                        boolean iSouth = iLocalY > 1.0 - wallThickness && iCell.getWall(2);
                        boolean iEast  = iLocalX > 1.0 - wallThickness && iCell.getWall(3);
                        
                        if (iNorth) {
                            bestT = t;
                            bestWall = 0;
                            break;
                        }
                        if (iWest) {
                            bestT = t;
                            bestWall = 1;
                            break;
                        }
                        if (iSouth) {
                            bestT = t;
                            bestWall = 2;
                            break;
                        }
                        if (iEast) {
                            bestT = t;
                            bestWall = 3;
                            break;
                        }
                    }
                    
                    if (bestWall >= 0) {
                        hitX = prevX + (rayX - prevX) * bestT;
                        hitY = prevY + (rayY - prevY) * bestT;
                        lastWall = bestWall;
                    } else {
                        // Fallback si no encontró nada en interpolación
                        hitX = rayX;
                        hitY = rayY;
                        // Elegir por ángulo del rayo
                        double rayAngle = Math.atan2(dirY, dirX);
                        while (rayAngle < 0) rayAngle += Math.PI * 2;
                        
                        if (rayAngle < Math.PI / 4 || rayAngle >= 7 * Math.PI / 4) {
                            lastWall = hitEast ? 3 : (hitNorth ? 0 : (hitSouth ? 2 : 1));
                        } else if (rayAngle < 3 * Math.PI / 4) {
                            lastWall = hitNorth ? 0 : (hitEast ? 3 : (hitWest ? 1 : 2));
                        } else if (rayAngle < 5 * Math.PI / 4) {
                            lastWall = hitWest ? 1 : (hitSouth ? 2 : (hitNorth ? 0 : 3));
                        } else {
                            lastWall = hitSouth ? 2 : (hitWest ? 1 : (hitEast ? 3 : 0));
                        }
                    }
                }
                break;
            }
        }
        
        if (lastWall == -1) return null;
        
        double dx = hitX - startX;
        double dy = hitY - startY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        double wallX = 0;
        int cellX = (int) hitX;
        int cellY = (int) hitY;
        double localX = hitX - cellX;
        double localY = hitY - cellY;
        
        switch (lastWall) {
            case 0: wallX = localX; break;
            case 1: wallX = localY; break;
            case 2: wallX = 1.0 - localX; break;
            case 3: wallX = 1.0 - localY; break;
        }
        
        wallX = Math.max(0.0, Math.min(1.0, wallX));
        return new RayHit(distance, lastWall == 1 || lastWall == 3, wallX);
    }
    
    // Clase auxiliar para almacenar información del rayo
    private static class RayHit {
        double distance;
        boolean isVertical;
        double wallX;  // Posición exacta del impacto en la pared (0.0 a 1.0)
        
        RayHit(double distance, boolean isVertical, double wallX) {
            this.distance = distance;
            this.isVertical = isVertical;
            this.wallX = wallX;
        }
    }
    
    public void setWallColors(Color northSouth, Color eastWest) {
        this.wallColorNS = northSouth;
        this.wallColorEW = eastWest;
    }
    
    public void setFloorCeilingColors(Color floor, Color ceiling) {
        this.floorColor = floor;
        this.ceilingColor = ceiling;
    }
    
    /**
     * Detiene el game loop - debe llamarse al cerrar la ventana
     */
    public void stopGameLoop() {
        if (gameLoop != null && gameLoop.isRunning()) {
            gameLoop.stop();
        }
    }
}
