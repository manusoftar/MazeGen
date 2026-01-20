package mazegen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Panel de control para ajustar parámetros de RayCastingPanel en tiempo real.
 */
public class RayCastingControlPanel extends JPanel {
    public RayCastingControlPanel(RayCastingPanel rayPanel) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder("Controles Raycasting"));

        // Umbral de esquina
        SpinnerNumberModel esquinaModel = new SpinnerNumberModel(rayPanel.getEsquinaUmbral(), 0.0, 0.5, 0.01);
        JSpinner esquinaSpinner = new JSpinner(esquinaModel);
        add(new JLabel("Umbral esquina:"));
        add(esquinaSpinner);
        esquinaSpinner.addChangeListener(e -> {
            rayPanel.setEsquinaUmbral(((Number) esquinaSpinner.getValue()).doubleValue());
            rayPanel.requestFocusInWindow();
        });

        // FOV
        SpinnerNumberModel fovModel = new SpinnerNumberModel(Math.toDegrees(rayPanel.getFov()), 10.0, 180.0, 1.0);
        JSpinner fovSpinner = new JSpinner(fovModel);
        add(new JLabel("FOV (grados):"));
        add(fovSpinner);
        fovSpinner.addChangeListener(e -> {
            double fovRad = Math.toRadians(((Number) fovSpinner.getValue()).doubleValue());
            rayPanel.setFov(fovRad);
            rayPanel.requestFocusInWindow();
        });

        // Distancia de oscurecimiento
        SpinnerNumberModel darkModel = new SpinnerNumberModel(rayPanel.getDarkDistance(), 1.0, 50.0, 0.1);
        JSpinner darkSpinner = new JSpinner(darkModel);
        add(new JLabel("Dist. oscurecimiento:"));
        add(darkSpinner);
        darkSpinner.addChangeListener(e -> {
            rayPanel.setDarkDistance(((Number) darkSpinner.getValue()).doubleValue());
            rayPanel.requestFocusInWindow();
        });

        // Grosor de paredes
        SpinnerNumberModel wallModel = new SpinnerNumberModel(rayPanel.getWallThickness(), 0.01, 0.5, 0.01);
        JSpinner wallSpinner = new JSpinner(wallModel);
        add(new JLabel("Grosor pared:"));
        add(wallSpinner);
        wallSpinner.addChangeListener(e -> {
            rayPanel.setWallThickness(((Number) wallSpinner.getValue()).doubleValue());
            rayPanel.requestFocusInWindow();
        });

        // Ancho de pasillo
        SpinnerNumberModel passageModel = new SpinnerNumberModel(rayPanel.getPassageWidth(), 0.2, 1.0, 0.01);
        JSpinner passageSpinner = new JSpinner(passageModel);
        add(new JLabel("Ancho pasillo:"));
        add(passageSpinner);
        passageSpinner.addChangeListener(e -> {
            rayPanel.setPassageWidth(((Number) passageSpinner.getValue()).doubleValue());
            rayPanel.requestFocusInWindow();
        });

        // Debug colores
        JCheckBox debugBox = new JCheckBox("Colores debug", rayPanel.isDebugWallColors());
        add(debugBox);
        debugBox.addActionListener(e -> {
            rayPanel.setDebugWallColors(debugBox.isSelected());
            rayPanel.requestFocusInWindow();
        });
    }
}