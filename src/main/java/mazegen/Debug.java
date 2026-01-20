package mazegen;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.Point;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.border.EmptyBorder;
import javax.swing.SwingConstants;

public class Debug extends JFrame {
	   private Point mousePos;
	   private Point headPos;
	   
	   private JTextField mx,my,hx,hy;
	   private JLabel lmx,lmy,lhx,lhy;
	   
	   public void setMousePos(Point p){
		   mousePos = p;
		   if (mx != null && my != null){
			   mx.setText(p.x+"");
			   my.setText(p.y+"");
		   }
	   }
	   
	   public void setHeadPos(Point p){
		   headPos = p;
		   if (hx != null && hy != null){
			   hx.setText(p.x+"");
			   hy.setText(p.y+"");
		   }
	   }
	   
	   public Debug(){
	   	setTitle("Debuger");
		   	  JPanel panel = new JPanel();
		   	  panel.setBorder(new EmptyBorder(25, 25, 25, 25));
		   	  GridBagLayout gbl_panel = new GridBagLayout();
		   	  gbl_panel.columnWeights = new double[]{0.0, 1.0};
		   	  panel.setLayout(gbl_panel);
		   	  getContentPane().setLayout(new BorderLayout());
		   	  getContentPane().add(panel, BorderLayout.CENTER);
		   	  
		   	  lmx = new JLabel("Mouse X:");
		   	  
		   	  GridBagConstraints gbc_lmx = new GridBagConstraints();
		   	  gbc_lmx.insets = new Insets(0, 0, 5, 5);
		   	  gbc_lmx.gridx = 0;
		   	  gbc_lmx.gridy = 0;
		   	  panel.add(lmx, gbc_lmx);
		   	  
		   	  mx = new JTextField();
		   	  mx.setHorizontalAlignment(SwingConstants.CENTER);
		   	  mx.setEditable(false);
		   	  GridBagConstraints gbc_mx = new GridBagConstraints();
		   	  gbc_mx.fill = GridBagConstraints.HORIZONTAL;
		   	  gbc_mx.insets = new Insets(0, 0, 5, 5);
		   	  gbc_mx.gridx = 1;
		   	  gbc_mx.gridy = 0;
		   	  panel.add(mx, gbc_mx);
		   	  lmy = new JLabel("Mouse Y:");
		   	  GridBagConstraints gbc_lmy = new GridBagConstraints();
		   	  gbc_lmy.insets = new Insets(0, 0, 5, 5);
		   	  gbc_lmy.gridx = 0;
		   	  gbc_lmy.gridy = 1;
		   	  panel.add(lmy, gbc_lmy);
		   	  my = new JTextField();
		   	  my.setHorizontalAlignment(SwingConstants.CENTER);
		   	  my.setEditable(false);
		   	  GridBagConstraints gbc_my = new GridBagConstraints();
		   	  gbc_my.fill = GridBagConstraints.HORIZONTAL;
		   	  gbc_my.insets = new Insets(0, 0, 5, 5);
		   	  gbc_my.gridx = 1;
		   	  gbc_my.gridy = 1;
		   	  panel.add(my, gbc_my);
		   	  lhx = new JLabel("Head X:");
		   	  GridBagConstraints gbc_lhx = new GridBagConstraints();
		   	  gbc_lhx.insets = new Insets(0, 0, 5, 5);
		   	  gbc_lhx.gridx = 0;
		   	  gbc_lhx.gridy = 3;
		   	  panel.add(lhx, gbc_lhx);
		   	  hx = new JTextField();
		   	  hx.setHorizontalAlignment(SwingConstants.CENTER);
		   	  hx.setEditable(false);
		   	  GridBagConstraints gbc_hx = new GridBagConstraints();
		   	  gbc_hx.fill = GridBagConstraints.HORIZONTAL;
		   	  gbc_hx.insets = new Insets(0, 0, 5, 5);
		   	  gbc_hx.gridx = 1;
		   	  gbc_hx.gridy = 3;
		   	  panel.add(hx, gbc_hx);
		   	  lhy = new JLabel("Head Y:");
		   	  GridBagConstraints gbc_lhy = new GridBagConstraints();
		   	  gbc_lhy.insets = new Insets(0, 0, 0, 5);
		   	  gbc_lhy.gridx = 0;
		   	  gbc_lhy.gridy = 4;
		   	  panel.add(lhy, gbc_lhy);
		   	  hy = new JTextField();
		   	  hy.setHorizontalAlignment(SwingConstants.CENTER);
		   	  hy.setEditable(false);
		   	  GridBagConstraints gbc_hy = new GridBagConstraints();
		   	  gbc_hy.fill = GridBagConstraints.HORIZONTAL;
		   	  gbc_hy.insets = new Insets(0, 0, 0, 5);
		   	  gbc_hy.gridx = 1;
		   	  gbc_hy.gridy = 4;
		   	  panel.add(hy, gbc_hy);
		   	
	   }
}
