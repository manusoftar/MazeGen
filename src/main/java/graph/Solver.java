package graph;

import java.awt.Point;
import java.util.Stack;
import javax.swing.SwingWorker;

/**
 * Maze solver for MazeGRID
 * @author manusoftar
 */
public class Solver extends SwingWorker<Void, Void> {
    private cell[][] Maze;
    private MazeGRID padre;
    private Point inicio, salida, actual;
    private int alto, ancho;
    private int delay = 20;
    private Stack<Point> moves = new Stack<>();
    private Stack<Point> backtrace = new Stack<>();
    private boolean abortar = false;
    private int mode = 0; // 0 forward, 1 backtracking

    // Wall direction indices must match cell.java: north=0, west=1, south=2, east=3
    private interface walls {
        int north = 0;
        int west = 1;
        int south = 2;
        int east = 3;
    }

    public Solver(cell[][] m, Point ini, Point fin, MazeGRID p) {
        padre = p;
        Maze = m.clone();
        alto = Maze.length;
        ancho = Maze[0].length;
        inicio = ini;
        salida = fin;
    }

    public void abort() {
        abortar = true;
    }

    @Override
    protected Void doInBackground() {
        try {
            actual = new Point(inicio.x, inicio.y);
            System.out.println("Inicio: " + inicio.toString());
            System.out.println("Fin: " + salida.toString());
            int n = 0;
            resetVisits();
            Maze[actual.y][actual.x].setVisited();
            int ciclos = 0;
            while (!hasFinished()) {
                if (abortar) {
                    break;
                }
                if (hasVecino(actual.x, actual.y, n)) {
                    if (!Maze[actual.y][actual.x].getWall(n)) {
                        int nextX = getVecinoX(actual.x, actual.y, n);
                        int nextY = getVecinoY(actual.x, actual.y, n);
                        if (!Maze[nextY][nextX].isVisited()) {
                            moves.push(actual);
                            Maze[nextY][nextX].setVisited();
                            actual = new Point(nextX, nextY);
                            moves.push(actual);
                            ciclos = 0;
                            mode = 0;
                            show();
                        } else {
                            n++;
                        }
                    } else {
                        n++;
                    }
                    if (n == 4) {
                        if (ciclos == 1) {
                            actual = moves.pop();
                            mode = 1;
                            show();
                            ciclos = 0;
                        }
                        mode = 0;
                        show();
                        n = 0;
                        ciclos++;
                    }
                } else {
                    n++;
                    if (n == 4) {
                        if (ciclos == 1) {
                            actual = moves.pop();
                            mode = 1;
                            ciclos = 0;
                            show();
                        }
                        mode = 0;
                        n = 0;
                        ciclos++;
                        show();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private void resetVisits() {
        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                Maze[y][x].clearVisited();
            }
        }
    }

    private void show() {
        if (delay > 0) {
            try {
                if (mode == 0) {
                    Thread.sleep(delay);
                } else {
                    //Thread.sleep(delay/4);
                }
            } catch (Exception ex) {
            }
        }
        padre.showSolution(moves);
    }

    private boolean findBacktrace(int x, int y) {
        boolean found = false;
        for (int n = 0; n < backtrace.size(); n++) {
            if (backtrace.get(n).x == x && backtrace.get(n).y == y) {
                found = true;
            }
        }
        return found;
    }

    private boolean isVisited(int x, int y) {
        boolean found = false;
        for (int n = 0; n < moves.size(); n++) {
            if (moves.get(n).x == x && moves.get(n).y == y) {
                found = true;
            }
        }
        return found;
    }

    public void setPadre(MazeGRID p) {
        padre = p;
    }

    private boolean hasFinished() {
        return (actual.y == salida.y) && (actual.x == salida.x);
    }

    public int getVecinoX(int x, int y, int v) {
        int retorno = x;
        switch (v) {
            case walls.west:
                retorno = x - 1;
                break;
            case walls.east:
                retorno = x + 1;
                break;
            case walls.north:
            case walls.south:
                retorno = x;
                break;
        }
        return retorno;
    }

    public int getVecinoY(int x, int y, int v) {
        int retorno = y;
        switch (v) {
            case walls.north:
                retorno = y - 1;
                break;
            case walls.south:
                retorno = y + 1;
                break;
            case walls.west:
            case walls.east:
                retorno = y;
                break;
        }
        return retorno;
    }

    public boolean hasVecino(int x, int y, int v) {
        if (x > ancho - 1 || y > alto - 1) {
            return false;
        }
        switch (v) {
            case walls.west:
                return x - 1 >= 0;
            case walls.east:
                return x + 1 < ancho;
            case walls.north:
                return y - 1 >= 0;
            case walls.south:
                return y + 1 < alto;
        }
        return false;
    }
}
