package mazegen;

import graph.MazeObj;
import java.awt.BorderLayout;
import javax.swing.JSplitPane;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * Ventana que muestra la vista en primera persona del laberinto
 * Utiliza ray casting para renderizar una vista 3D estilo Wolfenstein 3D
 * 
 * @author manusoftar
 */
public class FirstPersonView extends JFrame {
    
    private RayCastingPanel rayCastingPanel;
    private RayCastingControlPanel controlPanel;
    private MazeObj maze;
    
    public FirstPersonView(MazeObj maze) {
        this.maze = maze;
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Vista en Primera Persona - MazeGen 3D");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        

        // Crear el panel de ray casting y el panel de control
        rayCastingPanel = new RayCastingPanel(maze);
        controlPanel = new RayCastingControlPanel(rayCastingPanel);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, rayCastingPanel, controlPanel);
        splitPane.setResizeWeight(1.0); // El panel 3D ocupa todo el espacio salvo el control
        splitPane.setDividerLocation(0.8); // 80% para el render, 20% para controles
        add(splitPane, BorderLayout.CENTER);
        
        // Panel de información en la parte superior
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(new Color(40, 40, 40));
        JLabel infoLabel = new JLabel("Vista en Primera Persona (Ray Casting - Estilo Wolfenstein 3D)");
        infoLabel.setForeground(Color.WHITE);
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infoPanel.add(infoLabel);
        add(infoPanel, BorderLayout.NORTH);
        
        // Panel de ayuda en la parte inferior
        JPanel helpPanel = new JPanel();
        helpPanel.setBackground(new Color(40, 40, 40));
        helpPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        JLabel helpLabel = new JLabel(
            "<html><center>" +
            "<b>Controles:</b> W/↑ = Adelante | S/↓ = Atrás | A = Girar Izq | D = Girar Der | Q = Strafe Izq | E = Strafe Der" +
            "</center></html>"
        );
        helpLabel.setForeground(Color.LIGHT_GRAY);
        helpLabel.setHorizontalAlignment(SwingConstants.CENTER);
        helpPanel.add(helpLabel);
        add(helpPanel, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(null);
        setResizable(true);
        
        // Asegurarse de que el panel tenga el foco para los controles de teclado
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                rayCastingPanel.requestFocusInWindow();
            }
            
            @Override
            public void windowClosing(WindowEvent e) {
                // Detener el game loop cuando se cierra la ventana
                if (rayCastingPanel != null) {
                    rayCastingPanel.stopGameLoop();
                }
            }
        });
    }
    
    /**
     * Actualiza el laberinto mostrado en la vista
     * @param newMaze El nuevo laberinto a mostrar
     */
    public void updateMaze(MazeObj newMaze) {
        this.maze = newMaze;
        
        // Detener el game loop del panel anterior
        if (rayCastingPanel != null) {
            rayCastingPanel.stopGameLoop();
        }
        
        remove(rayCastingPanel);
        rayCastingPanel = new RayCastingPanel(newMaze);
        add(rayCastingPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
        rayCastingPanel.requestFocusInWindow();
    }
    
    /**
     * Permite personalizar los colores de las paredes
     * @param northSouth Color para paredes norte-sur
     * @param eastWest Color para paredes este-oeste
     */
    public void setWallColors(Color northSouth, Color eastWest) {
        rayCastingPanel.setWallColors(northSouth, eastWest);
    }
    
    /**
     * Permite personalizar los colores del piso y techo
     * @param floor Color del piso
     * @param ceiling Color del techo
     */
    public void setFloorCeilingColors(Color floor, Color ceiling) {
        rayCastingPanel.setFloorCeilingColors(floor, ceiling);
    }
}
