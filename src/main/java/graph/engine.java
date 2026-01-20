/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package graph;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Stack;

import javax.swing.SwingWorker;

/**
 * 
 * @author manusoftar
 */
public class engine extends SwingWorker {
	private int cells, ancho = 0, alto = 0;
	private Stack<Dimension> celdas = new Stack();
	private cell[][] casilleros;
	private Dimension celda = new Dimension();
	private MazeGRID padre;
	private boolean showConstruction = false;
	private int delay = 200;
	private boolean abortar = false;
	private boolean debuging = true;
	private int lastDir = 0, probability = 90;

	private interface vecinos {
		static final int north = 0;
		static final int west = 1;
		static final int south = 2;
		static final int east = 3;
	}

	private interface walls {
		static final int north = 0;
		static final int west = 1;
		static final int south = 2;
		static final int east = 3;
	}

	private interface algs {
		static final int DFS = 0;
		static final int DFS2 = 1;
		static final int CS = 2; //CrawlingSnake (DFS con bias horizontales o verticales)
		static final int HK = 3; //Hunt & Kill
	}
	
	private int ways[] =  {
			  0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3,
			  0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3,
			  0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3, 0, 1, 2, 3};
	
	private int steps[] = {33,35,39,41,45,47,27,29,37,40,43,46,25,28,31,34,42,44,24,26,30,32,36,38,
			  			   52,53,55,56,58,59,49,50,54,56,57,59,48,50,51,53,57,58,48,49,51,52,54,55,
			               61,62,63,60,62,63,60,61,63,60,61,62,-1,-1,-1,-1};
	
	
	private int recursiveAux[][] = {{2, 3, 12, 13}, {10, 11, 20, 21}};
	private int run = 75;
	private int bias = 3;
	
	private int alg = 0;

	private ArrayList<Probabilidades> probabilities;

	public void setProbability(int p) {
		probability = p;
	}

	public void abort() {
		abortar = true;
		try {
			this.cancel(true);
		} catch (Exception ex) {
		}
	}

	public void setAlg(int n) {
		if (n >= 0 && n <= 2) {
			alg = n;
		}
	}

	public engine(int an, int al, MazeGRID p) {
		cells = al * an;
		ancho = an;
		alto = al;
		casilleros = new cell[alto][ancho];
		padre = p;
	}

	public engine(cell[][] juego) {
		casilleros = juego;
	}

	public engine() {
	}

	public void setPadre(MazeGRID p) {
		padre = p;
	}

	public void showConstruction(boolean s) {
		showConstruction = s;
	}

	/**
	 * Setea la demora entre celda y celda en milisegundos.-
	 * 
	 * @param d
	 */
	public void setDelay(int d) {
		delay = d;
	}

	public void setCellCount(int c) {
		cells = c;
	}

	public void setAncho(int a) {
		ancho = a;
		if (alto != 0) {
			casilleros = new cell[alto][ancho];
			cells = alto * ancho;
		}
	}

	public void setAlto(int a) {
		alto = a;
		if (ancho != 0) {
			casilleros = new cell[alto][ancho];
			cells = alto * ancho;
		}
	}

	private int isIn(ArrayList<Probabilidades> ae, int val) {
		int i = -1;
		for (i = 0; i < ae.size(); i++) {
			if (val >= ae.get(i).getMin() && val <= ae.get(i).getMax()) {
				return i;
			}
		}
		return 0;
	}

	public int getVecinosCount(int x, int y) {
		int cnt = 0;
		if (x > 0) {
			cnt++;
		}
		if (x + 1 < ancho) {
			cnt++;
		}
		if (y < alto - 1) {
			cnt++;
		}
		if (y > 0) {
			cnt++;
		}
		return cnt;
	}

	public int getVecinoX(int x, int y, int v) {
		int retorno = 0;
		// if (hasVecino(celda,x,y,v)){
		switch (v) {
		case vecinos.west:
			retorno = x - 1;
			break;
		case vecinos.east:
			retorno = x + 1;
			break;
		case vecinos.north:
			retorno = x;
			break;
		case vecinos.south:
			retorno = x;
		}
		return retorno;
		// }
		// return -1;
	}

	public int getVecinoY(int x, int y, int v) {
		int retorno = 0;
		// if (hasVecino(celda,x,y,v)){
		switch (v) {
		case vecinos.south:
			retorno = y + 1;
			break;
		case vecinos.north:
			retorno = y - 1;
			break;
		case vecinos.west:
			retorno = y;
			break;
		case vecinos.east:
			retorno = y;
			break;

		}
		return retorno;
		// }

		// return -1;
	}

	public int getOpossingWall(int w) {
		switch (w) {
		case vecinos.east:
			return vecinos.west;
		case vecinos.west:
			return vecinos.east;
		case vecinos.north:
			return vecinos.south;
		case vecinos.south:
			return vecinos.north;

		}
		return -1;
	}

	public boolean hasVecino(int x, int y, int v) {
		if (x > ancho - 1 || y > alto - 1) {
			return false;
		}
		switch (v) {
		case vecinos.west:
			if (x - 1 >= 0) {
				return true;
			}
			break;
		case vecinos.east:
			if (x + 1 < ancho) {
				return true;
			}
			break;
		case vecinos.north:
			if (y - 1 >= 0) {
				return true;
			}
			break;
		case vecinos.south:
			if (y < alto - 1) {
				return true;
			}
			break;
		}
		return false;
	}

	public int countVecinosLibres(int x, int y) {
		int cnt = 0;
		for (int n = 0; n < 4; n++) {
			int xv, yv;
			xv = getVecinoX(x, y, n);
			yv = getVecinoY(x, y, n);
			if (hasVecino(x, y, n)) {
				if (!casilleros[yv][xv].isVisited()) {
					cnt++;
				}
			}

		}
		return cnt;
	}

	private interface WallType {
		static final int Vertical = 0;
		static final int Horizontal = 1;
	}

	public int getCellNumber(int x, int y) {
		return ancho * y + x + 1;
	}

	@Override
	public synchronized Void doInBackground() {
		//System.out.println("Entre en el doInBackground");
		//this.debuging = true;
		if (probabilities == null){
			probabilities = new ArrayList<Probabilidades>();
		}
		if (probabilities.size() == 0) {
			probabilities.add(new Probabilidades(0, 45, walls.west));
			probabilities.add(new Probabilidades(46, 56, walls.south));
			probabilities.add(new Probabilidades(57, 95, walls.east));
			probabilities.add(new Probabilidades(96, 100, walls.north));
		}
		System.out.println(probabilities);
		switch (alg){
			case algs.DFS:
				genDFS();
				break;
			case algs.DFS2:
				genDFS2();
				break;
			case algs.CS:
				genCS();
				break;
		}
		
			
		return null;
	}

	private void genCS() {
		int cnt = cells - 1;
		init(0);
		int x = 0, y = 0, x1 = 0, y1 = 0;
		int rndRun = 0;
		int rndDir = -1;
		int id = 0, cRunRnd = 0;
		x = (int) (Math.random() * ancho);
		y = (int) (Math.random() * alto);
		celda = new Dimension(x, y);
		celdas.push(celda);
		casilleros[y][x].setVisited();
		Stack<Integer> dirs = new Stack<Integer>();
		//int decision;
		//int lastDir = -1; //Para tener referencia de la �ltima direcci�n en que se busc� celda candidata
		
		//Para empezar voy a probar con un 60% de probabilidades de continuar en la misma direcci�n (siempre que sea posible)
		
		try {
			
			while ( cnt > 0 && !abortar ){
				
				
				if (cRunRnd > 0){
					cRunRnd--;
				} else {
					if (run>0){
						cRunRnd = (int)(Math.random()*run);
					}
					rndDir = (int)(Math.random()*24+bias*4);
				
				    if (rndDir>=24){
				    	id = recursiveAux[(bias > 0) ? 1 : 0][rndDir&3];
				    } else {
				    	id = rndDir;
				    }
				}
			  
					    int n = 0;
					    
					    //int n = (int) (Math.random() * 4);
						//int n = ways[id];
						/*if (n == -1) {
							n = 0;
						}*/
						
						System.out.println("Direcci�n -> " + n);
						// int n = vecinos.north;
						casilleros[y][x].setVisited();
						boolean done = false;
						boolean carved = false;
						boolean kill = false;
						while (!done) {
							if (kill){
								break;
							}
							if (id!=-1){
								n = ways[id];
							} else {
								n = (int)(Math.random()*4);
							}
							if (hasVecino(x, y, n)) {
								if (!casilleros[getVecinoY(x, y, n)][getVecinoX(x, y, n)].isVisited()) {
									x1 = getVecinoX(x, y, n);
									y1 = getVecinoY(x, y, n);
									done = true;
									//lastDir = n;
									dirs.push(id);
									
									casilleros[y][x].clearWALL(n);
									// x=x1;
									// y=y1;
									if (debuging) {
										System.out.println("Elegí el vecino X:" + x1
											+ " Y:" + y1 + " Nº:"
											+ getCellNumber(x1, y1));
									}
									casilleros[y1][x1].clearWALL(getOpossingWall(n));
									celda = new Dimension(x1, y1);
									celdas.push(celda);
									cnt--;
									x = x1;
									y = y1;
									if (showConstruction) {
										padre.draw(casilleros);
										if (delay > 0) {
											try {
												Thread.sleep(delay);
											} catch (Exception ex) {
			
											}
			
										}
									}
									if (debuging) {
										System.out.println("Celdas restantes: " + cnt);
									}
									
									
									carved = true;
									break;
								} 
							}
							
							do {
								 if (id>-1){
									 id = steps[id];
								 }
								 if (id >= 0){
									 break;
								 }
								 
								 
								 if (dirs.isEmpty()){
									 if (celdas.isEmpty()){
										 //cnt3=0;
										 done = true;
										 carved = false;
										 kill = true;
										 break;
									 } else {
										 
										 kill = false;
										 done = false;
										 break;
									 }
								 }
								 id = dirs.pop();
								 if (id>-1){
									 n =  ways[id];
								 } else {
									 n = (int)(Math.random()*4);
								 }
									 
								 celda = new Dimension(celdas.pop());
								 x = (int) celda.getWidth();
								 y = (int) celda.getHeight();
							} while (true);
							
						}

						
						
						
							
					/*} else {*/
						
						
				
					//}
				
				}
				
			    padre.draw(casilleros);
			
		} catch(Exception ex){
			ex.printStackTrace();
		}
		padre.setDone();
	}

	private void genCS(Dimension d1, Dimension d2) {
		int a = 0, b = 0;
		padre.draw(casilleros);
		if ((d2.width - d1.width) > 0) {
			a = (int) (Math.random() % (d2.width - d1.width));
		}
		if ((d2.height - d1.height) > 0) {
			b = (int) (Math.random() % (d2.height - d1.height));
		}
		if (a != 0 && b != 0) {
			setWall(a, WallType.Vertical);
			setWall(b, WallType.Horizontal);
		}
		padre.draw(casilleros);

	}

	/**
	 * Crea una pared que cruza todo el laberinto en la posici�n indicada, el
	 * par�mentro type indica si la pared debe ser vertical(0) u horizontal(1)
	 * 
	 * @param pos
	 * @param type
	 */
	private void setWall(int pos, int type) {
		switch (type) {
		case WallType.Vertical:
			if (pos > 0 && pos < casilleros.length) {
				for (int y = 0; y < casilleros.length; y++) {
					casilleros[y][pos - 1].setWall(walls.east);
					casilleros[y][pos].setWall(walls.west);

				}
			}
			break;
		case WallType.Horizontal:
			if (pos > 0 && pos < casilleros[0].length) {
				for (int x = 0; x < casilleros.length; x++) {
					casilleros[pos - 1][x].setWall(walls.south);
					casilleros[pos][x].setWall(walls.north);

				}
			}
			break;
		}
	}

	public void setDebuging(boolean d) {
		debuging = d;
	}

	public void init(int mode) {
		if (mode == 0) {
			for (int y = 0; y < alto; y++) {
				for (int x = 0; x < ancho; x++) {
					casilleros[y][x] = new cell();
				}
			}
		} else if (mode == 1) {
			for (int y = 0; y < alto; y++) {
				for (int x = 0; x < ancho; x++) {
					if (y == 0) {
						casilleros[y][x] = new cell(false, false, false, true);
					} else if (y == alto - 1) {
						casilleros[y][x] = new cell(false, false, true, false);
					} else if (x == 0) {
						casilleros[y][x] = new cell(true, false, false, false);
					} else if (x == ancho - 1) {
						casilleros[y][x] = new cell(false, true, false, false);
					} else {
						casilleros[y][x] = new cell(true, true, true, true);
					}
				}
			}
		}
	}

	private boolean esPrimo(int n) {
		for (int x = 2; x <= (int) Math.sqrt(n); x++) {
			if (n % x == 0 && n != x) {
				return false;
			}
		}
		return true;
	}

	private boolean findIn(Stack<Integer> st, int n) {
		for (int e = 0; e < st.size(); e++) {
			if (st.get(e) == n) {
				return true;
			}
		}
		return false;
	}
	
	private void genDFS(){
		System.out.println("Entr� en el if con alg == 0");
		int cnt = cells - 1;
		init(0);
		int x = 0, y = 0, x1 = 0, y1 = 0;
		x = (int) (Math.random() * ancho);
		y = (int) (Math.random() * alto);
		celda = new Dimension(x, y);
		celdas.push(celda);
		casilleros[y][x].setVisited();
		try {
			while (cnt > 0 && !abortar) {
				if (debuging) {
					System.out.println("Celda X:" + x + " Celda Y: " + y + " Celda N�: " + getCellNumber(x, y));
				}
				int cnt3 = 0;
				for (int c = 0; c < 4; c++) {
					if (debuging) {
						System.out.print("Vecino X: " + getVecinoX(x, y, c)  + " Vecino Y:"	+ getVecinoY(x, y, c) + " Vecino N�:" + getCellNumber(getVecinoX(x, y, c), getVecinoY(x, y, c)));
					}
					if (hasVecino(x, y, c)) {
						if (debuging) {
							System.out.print(" OK");
						}
						if (!casilleros[getVecinoY(x, y, c)][getVecinoX(x, y, c)].isVisited()) {
							if (debuging) {
								System.out.println(" Disponible");
							}
							cnt3++;
						} else {
							if (debuging) {
								System.out.println(" Visitado");
							}
						}
					} else {
						if (debuging) {
							System.out.println(" No tiene este vecino");
						}
					}
				}
				if (cnt3 >= 1) {
					int n = (int) (Math.random() * 4);
					int d = (int) (Math.random() * 101);
					//n = isIn(probabilities, d);
					if (n == -1) {
						n = 0;
					}
					System.out.println("Direcci�n -> " + n);
					// int n = vecinos.north;
					casilleros[y][x].setVisited();
					boolean done = false;
					while (!done) {
						
						if (hasVecino(x, y, n)) {
							if (!casilleros[getVecinoY(x, y, n)][getVecinoX(x, y, n)].isVisited()) {
								x1 = getVecinoX(x, y, n);
								y1 = getVecinoY(x, y, n);
								done = true;
								break;
							}

						}
						d = (int) (Math.random() * 101);
						//n = isIn(probabilities, d);
						n = (int)(Math.random()*4);
						if (n == -1) {
							n = 0;
						}
					}

					casilleros[y][x].clearWALL(n);
					// x=x1;
					// y=y1;
					if (debuging) {
						    System.out.println("Elegí el vecino X:" + x1
							    + " Y:" + y1 + " Nº:"
							    + getCellNumber(x1, y1));
					}
					casilleros[y1][x1].clearWALL(getOpossingWall(n));
					celda = new Dimension(x1, y1);
					celdas.push(celda);
					cnt--;
					x = x1;
					y = y1;
					if (showConstruction) {
						padre.draw(casilleros);
						if (delay > 0) {
							try {
								Thread.sleep(delay);
							} catch (Exception ex) {

							}

						}
					}
					if (debuging) {
						System.out.println("Celdas restantes: " + cnt);
					}
				} else {
				
					celda = new Dimension(celdas.pop());
					x = (int) celda.getWidth();
					y = (int) celda.getHeight();
			
				}

			}
			if (debuging) {
				System.out.println("Ya gener� el laberinto");
			}
			padre.draw(casilleros);
		} catch (Exception ex) {
			if (debuging) {
				System.out.println(ex.toString() + "\n" + ex.getCause());
				ex.printStackTrace();
			}
		}
		padre.setDone();
	}
	
	private void genDFS2(){
		int cnt = cells - 1;
		init(0);
		int x = 0, y = 0, x1 = 0, y1 = 0;
		x = (int) (Math.random() * ancho);
		y = (int) (Math.random() * alto);
		celda = new Dimension(x, y);
		celdas.push(celda);
		casilleros[y][x].setVisited();
		try {
			while (cnt > 0 && !abortar) {

				if (debuging) {
					    System.out.println("Celda X:" + x + " Celda Y: " + y
						    + " Celda Nº: " + getCellNumber(x, y));
				}

				int cnt3 = 0;
				for (int c = 0; c < 4; c++) {
					if (debuging) {
						System.out.print("Vecino X: "
							+ getVecinoX(x, y, c)
							+ " Vecino Y:"
							+ getVecinoY(x, y, c)
							+ " Vecino Nº:"
							+ getCellNumber(getVecinoX(x, y, c),
								getVecinoY(x, y, c)));
					}
					if (hasVecino(x, y, c)) {
						if (debuging) {
							System.out.print(" OK");
						}
						if (!casilleros[getVecinoY(x, y, c)][getVecinoX(x, y, c)].isVisited()) {
							if (debuging) {
								System.out.println(" Disponible");
							}
							cnt3++;
						} else {
							if (debuging) {
								System.out.println(" Visitado");
							}
						}
					} else {
						if (debuging) {
							System.out.println(" No tiene este vecino");
						}
					}
				}
				if (cnt3 >= 1) {
					int n = (int) (Math.random() * 4);
					casilleros[y][x].setVisited();
					boolean done = false;
					while (!done) {
						int a = (int) (Math.random() * 100) + 1;
						if (a <= probability) {
							/*
							 * switch (lastDir){ case 0: case 1: lastDir =
							 * (int)(Math.random()*1)+2; break; default:
							 * lastDir = (int)(Math.random()*1); break; }
							 */
							if (hasVecino(x, y, lastDir)) {
								if (!casilleros[getVecinoY(x, y, lastDir)][getVecinoX(
										x, y, lastDir)].isVisited()) {
									x1 = getVecinoX(x, y, lastDir);
									y1 = getVecinoY(x, y, lastDir);
									done = true;
									n = lastDir;
									break;
								}

							}
						}
						if (hasVecino(x, y, n)) {
							if (!casilleros[getVecinoY(x, y, n)][getVecinoX(
									x, y, n)].isVisited()) {
								x1 = getVecinoX(x, y, n);
								y1 = getVecinoY(x, y, n);
								done = true;
								lastDir = n;
								break;
							}

						}
						n++;
						if (n == 4) {
							n = (int) (Math.random() * 4);
						}

					}

					casilleros[y][x].clearWALL(n);
					// x=x1;
					// y=y1;
					if (debuging) {
						System.out.println("Elegí el vecino X:" + x1
								+ " Y:" + y1 + " Nº:"
								+ getCellNumber(x1, y1));
					}
					casilleros[y1][x1].clearWALL(getOpossingWall(n));
					celda = new Dimension(x1, y1);
					celdas.push(celda);
					cnt--;
					x = x1;
					y = y1;
					if (showConstruction) {
						padre.draw(casilleros);
						if (delay > 0) {
							try {
								Thread.sleep(delay);
							} catch (Exception ex) {
							}
						}
					}
					if (debuging) {
						System.out.println("Celdas restantes: " + cnt);
					}
				} else {
					celda = new Dimension(celdas.pop());
					x = (int) celda.getWidth();
					y = (int) celda.getHeight();
				}

			}
			if (debuging) {
				System.out.println("Ya generé el laberinto");
			}
			padre.draw(casilleros);
		} catch (Exception ex) {
			if (debuging) {
				System.out.println(ex.toString() + "\n" + ex.getCause());
				ex.printStackTrace();
			}
		}
		padre.setDone();
}

	
}	
/**
 * Clase para identificar la probabilidad de intentar con cada pared
 * 
 * @author Manusoftar
 * 
 */
class Probabilidades {
	int min;
	int max;
	int wall;

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public int getWall() {
		return wall;
	}

	public void setWall(int wall) {
		this.wall = wall;
	}

	public Probabilidades(int mi, int ma, int w) {
		min = mi;
		max = ma;
		wall = w;
	}

	public Probabilidades() {
		min = 0;
		max = 0;
		wall = -1;
	}

}
