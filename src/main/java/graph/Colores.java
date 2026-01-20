/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package graph;

import java.awt.Color;

/**
 *
 * @author manusoftar
 */
public class Colores {
       private Color back = Color.BLACK;
       private Color lines = Color.LIGHT_GRAY;
       private Color entrada = Color.GREEN;
       private Color salida = Color.RED;
       private Color phead = Color.YELLOW;
       private Color pbody = new Color(163,73,164);
       private Color solvc = new Color(163,73,164);

       private float lineWidth = 1.0f;

       public Colores(){
       }

       public void setLineWidth(float lw){
           lineWidth = lw;
       }

       public float getLineWidth(){
           return lineWidth;
       }

       public void setBackColor(Color bc){
              back=bc;
       }

       public void setLineColor(Color lc){
           lines=lc;
       }

       public void setEntryColor(Color ec){
           entrada=ec;
       }

       public void setExitColor(Color exc){
           salida=exc;
       }

       public void setPlayerHeadColor(Color phc){
           phead=phc;
       }

       public void setPlayerBodyColor(Color pbc){
           pbody = pbc;
       }

       public void setSolvColor(Color sc){
           solvc = sc;
       }

       public Color getBackColor(){
           return back;
       }

       public Color getLinesColor(){
           return lines;
       }

       public Color getEntryColor(){
           return entrada;
       }

       public Color getExitColor(){
           return salida;
       }
       public Color getPlayerHeadColor(){
           return phead;
       }

       public Color getPlayerBodyColor(){
           return pbody;
       }

       public Color getSolvColor(){
           return solvc;
       }
}
