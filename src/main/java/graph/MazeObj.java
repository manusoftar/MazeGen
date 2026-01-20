/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package graph;

import java.awt.Point;
import java.io.Serializable;

/**
 *
 * @author manusoftar
 */
public class MazeObj implements Serializable {
       private int ancho,alto,cellsize=15;
       private cell[][] tablero;

       private Point inicio, fin;
       private MazeGRID grid;  // Referencia al grid para sincronización

       public MazeObj(){

       }

       /**
       * Crea el objeto Maze con todos sus parámetros asignados.
        * 
        * @param w
        * @param h
        * @param cs
        * @param tbl
        * @param i
        * @param f
        * @param g
        */
       public MazeObj(int w, int h, int cs, cell[][] tbl, Point i, Point f, MazeGRID g){
              alto = h;
              ancho = w;
              cellsize = cs;
              tablero = tbl.clone();
              inicio = i;
              fin = f;
              grid = g;
       }


       public void setTablero(cell[][] tbl){
              tablero = tbl.clone();
       }

       public void setDimensiones(int w, int h){
              ancho = w;
              alto = h;
       }

       public void setCellSize(int cs){
              cellsize=cs;
       }

       public int getHeight(){
              return alto;
       }

       public int getWidth(){
              return ancho;
       }

       public int getCellSize(){
            return cellsize;
       }

       public cell[][] getBoard(){
              return tablero;
       }

       public void setInitFin(Point i, Point f){
              inicio = i;
              fin = f;
       }

       public Point getInit(){
            return inicio;
       }

       public Point getFin(){
            return fin;
       }
       
       public Point getJugadaActual() {
           if (grid != null) {
               return grid.getJugadaActual();
           }
           return inicio;  // Si no hay grid, retornar inicio por defecto
       }
       
       public void setJugadaActual(Point pos) {
           if (grid != null) {
               grid.setJugadaActual(pos);
           }
       }
       
       public void addMovimiento(Point from, Point to) {
           if (grid != null) {
               grid.addMovimientoJugador(from, to);
           }
       }

}
