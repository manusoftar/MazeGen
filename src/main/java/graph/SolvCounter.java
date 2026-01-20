/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package graph;

import java.awt.Point;
import java.util.Stack;
import javax.swing.SwingWorker;

/**
 *
 * @author manusoftar
 */
public class SolvCounter extends SwingWorker {
       private cell[][] Maze;
       private MazeGRID padre;
       private Point inicio, salida, actual;
       private int alto,ancho;
       private Stack<Point> moves = new Stack();
       private Stack<Point> backtrace = new Stack();
       private boolean abortar = false;

       private interface walls {
            static final int west = 0;
            static final int east = 1;
            static final int south = 2;
            static final int north = 3;
       }


       public SolvCounter(cell[][] m, MazeGRID p){
              padre=p;
              Maze = m.clone();
              alto = Maze.length;
              ancho = Maze[0].length;
              //inicio = ini;
              //salida = fin;
              //System.out.println(inicio.toString());
             //System.out.println(salida.toString());
       }

       public void setIni(Point i){
           inicio = new Point(i.x, i.y);
       }

       public void setFin(Point f){
           salida = new Point(f.x, f.y);
       }

       public void abort(){
              abortar=true;
       }
    @Override
    protected Void doInBackground() {
        try {
            actual = new Point(inicio.x, inicio.y);
            //System.out.println(actual.toString());
            System.out.println("Inicio: " + inicio.toString());
            System.out.println("Fin: " + salida.toString());
            int n = 0;
            resetVisits();
            Maze[actual.y][actual.x].setVisited();
            int ciclos = 0;
            while (!hasFinished()){
                  if (abortar){
                      break;
                  }


                      if (hasVecino(actual.x,actual.y,n)){
                          if (!Maze[actual.y][actual.x].getWall(n)){
                                     if (!Maze[getVecinoY(actual.x,actual.y,n)][getVecinoX(actual.x,actual.y,n)].isVisited()){
                                          moves.push(actual);
                                          Maze[getVecinoY(actual.x,actual.y,n)][getVecinoX(actual.x,actual.y,n)].setVisited();
                                          actual = new Point(getVecinoX(actual.x,actual.y,n),getVecinoY(actual.x,actual.y,n));
                                          //if (actual.height != salida.height && actual.width != salida.width){
                                              moves.push(actual);
                                          //}
                                          ciclos=0;
                                          padre.showSolution(moves);
                                      } else {
                                          n++;
                                          if (n==4){
                                              if (ciclos == 1){
                                                  //backtrace.push(moves.peek());
                                                  actual = moves.pop();
                                                  padre.showSolution(moves);
                                                  ciclos = 0;
                                              }
                                              n=0;
                                              ciclos++;
                                          }
                                      }
                                 //}

                         } else {
                            n++;
                            if (n==4){
                                if (ciclos == 1){
                                    //backtrace.push(moves.peek());
                                    actual = moves.pop();
                                    padre.showSolution(moves);
                                    ciclos = 0;
                                }
                                n=0;
                                ciclos++;
                            }
                         }
                         //padre.showSolution(moves);
                         //System.out.println(actual.toString());
                      } else {
                        n++;
                        if (n==4){
                            if (ciclos == 1){
                                //backtrace.push(moves.peek());
                                actual = moves.pop();
                                padre.showSolution(moves);
                                ciclos = 0;
                            }
                            n=0;
                            ciclos++;
                        }
                    }
            }
            //padre.showSolution(moves);
        } catch (Exception ex){
            //ex.printStackTrace();
        }
        return null;
    }


    private void resetVisits(){
            for (int y=0; y<alto; y++){
                for (int x=0; x<ancho; x++){
                    Maze[y][x].clearVisited();
                }
            }
    }

    private boolean findBacktrace(int x,int y){
            boolean found = false;
            int pos = -1;
            for (int n=0; n<backtrace.size(); n++){
                if (backtrace.get(n).x == x && backtrace.get(n).y == y){
                   found = true;
                }
            }
            return found;
    }

    private boolean isVisited(int x, int y){
            boolean found = false;
            for (int n=0; n<moves.size(); n++){
                if (moves.get(n).x == x && moves.get(n).y == y){
                   found = true;
                }
            }
            return found;
    }



    public void setPadre(MazeGRID p){
        padre = p;
    }


    private boolean hasFinished(){
            if ((actual.y == salida.y) && (actual.x == salida.x)){
               return true;
            } else {
               return false;
            }
    }

    private interface vecinos {
            static final int west = 0;
            static final int east = 1;
            static final int south = 2;
            static final int north = 3;
    }

    public int getVecinoX(int x, int y, int v){
              int retorno=0;
              //if (hasVecino(celda,x,y,v)){
                  switch (v){
                      case vecinos.west:
                           retorno = x-1;
                           break;
                      case vecinos.east:
                           retorno = x+1;
                           break;
                      case vecinos.north:
                           retorno = x;
                           break;
                      case vecinos.south:
                           retorno = x;
                  }
                  return retorno;
              //}
              //return -1;
    }

    public int getVecinoY(int x, int y, int v){
              int retorno = 0;
              //if (hasVecino(celda,x,y,v)){
                  switch(v){
                      case vecinos.south:
                           retorno = y+1;
                           break;
                      case vecinos.north:
                           retorno = y-1;
                           break;
                      case vecinos.west:
                           retorno = y;
                           break;
                      case vecinos.east:
                           retorno = y;
                           break;

                  }
                  return retorno;
              //}

              //return -1;
   }

   public boolean hasVecino(int x, int y, int v){
          if (x>ancho-1 || y>alto-1){
                  return false;
              }
              switch (v){
                  case vecinos.west:
                       if (x-1>=0){
                           return true;
                       }
                       break;
                  case vecinos.east:
                      if (x+1<ancho){
                          return true;
                      }
                      break;
                  case vecinos.north:
                      if (y-1>=0){
                          return true;
                      }
                      break;
                  case vecinos.south:
                      if (y<alto-1){
                          return true;
                      }
                      break;
              }
              return false;
   }
}
