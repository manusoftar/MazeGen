/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MazeGRID.java
 *
 * Created on 21/03/2010, 23:20:34
 */

package graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.swing.SwingWorker;

import mazegen.IDE;



/**
 *
 * @author manusoftar
 */
public class MazeGRID extends javax.swing.JPanel {

    /** Creates new form MazeGRID */
    public MazeGRID() {
        initComponents();
        initBoard();
        super.setPreferredSize(new Dimension(ancho*celda,alto*celda));
        this.setPreferredSize(new Dimension(ancho*celda,alto*celda));
        setFocusable(true);
        requestFocusInWindow();
    }

    IDE padre = null;

    private int inifin[] = {12,13,14,21,23,24,31,32,34,41,42,43};

    private int prob = 90;

    private BufferedImage printMaze = null;

    private int solMode = 0;

    public void setPadre(IDE p){
        padre=p;
    }

    public MazeGRID(int x, int y, IDE p){
           alto=y;
           ancho=x;
           padre=p;
           initBoard();
           super.setPreferredSize(new Dimension(ancho*celda,alto*celda));
           this.setPreferredSize(new Dimension(ancho*celda,alto*celda));
    }
    private int alto=15,ancho=15, celda=20,anold,alold,cellold;
    public cell[][] tablero = new cell[15][15];

    private Stack<Dimension> solaux = new Stack<>();

    private engine juego_motor;

    private Solver sol_motor;

    private int lastDirection=0;

    private BufferedImage Maze = null;
    private BufferedImage MazeBackup = null;
    private boolean showTail = true, justLoaded=false;
    private Stack<Point> jugadas = new Stack<>();
    private Point jugada_actual = new Point(0, 0);

    private boolean showSolv = true;
    private Point inicio,salida;

    private Stack<Point> solution = new Stack<>();

    private int alg=0;
    
    //Colores por defecto
    protected Colores screenCol = new Colores(), printCol = new Colores();

    private engine motor;
    private int mode = 0;
    private boolean done = false;
    //public Graphics2D g2d=null;
    private Rectangle2D ini,fin;

    private float lineWidth=1f;

    //private Solver sol_motor;

    private boolean firstTime = true;

    public boolean isFirstTime(){
           return firstTime;
    }

    public void createGraphics(){
        firstTime=false;
    }

    public void setLineWidth(float lw){
        lineWidth = lw;
    }

    void graficar(BufferedImage mazeIMG, int mode) {
        //Así no revelo la entrada y la salida del laberinto hasta que esté completo
        switch (mode){
            case 0: 
                if (done){
                    Graphics2D mg = (Graphics2D)mazeIMG.getGraphics();
                    mg.setColor(screenCol.getEntryColor());
                    mg.fill(ini);
                    mg.setColor(screenCol.getExitColor());
                    mg.fill(fin);
                }
                Maze = mazeIMG;
                repaint();
                break;
            case 1:
                Graphics2D mg = (Graphics2D)mazeIMG.getGraphics();
                mg.setColor(printCol.getEntryColor());
                mg.fill(ini);
                mg.setColor(printCol.getExitColor());
                mg.fill(fin);
                printMaze = mazeIMG;
                //Maze = mazeIMG;
                //repaint();
                break;
                //grafico en segundo plano el mismo laberinto pero con celdas de tamaño 1 para exportarlo como archivo de texto
            case 2:
            	Graphics2D mg1 = (Graphics2D)mazeIMG.getGraphics();
                mg1.setColor(printCol.getEntryColor());
                mg1.fill(ini);
                mg1.setColor(printCol.getExitColor());
                mg1.fill(fin);
                printMaze = mazeIMG;
                //Maze = mazeIMG;
                //repaint();
                break;
        }
                
    }


    public void showTail(boolean st){
        showTail=st;
    }

    private interface walls {
            static final int west = 0;
            static final int east = 1;
            static final int south = 2;
            static final int north = 3;
    }

    private interface keys {
            static final int arriba = 38;
            static final int derecha = 39;
            static final int abajo = 40;
            static final int izquierda = 37;
    }

    private int delay=15;
    
    public void toPropperTxt(String path){
	    	String resultado[][] = new String[tablero.length*3][tablero[0].length*3];
	    	int offx=0, offy=0;
			for (int y=0; y<tablero.length; y++) {
				for (int x=0; x<tablero[y].length; x++) {
					String celda = tablero[y][x].toPropperText();
					String[] cars = celda.split("\n");
					
					for (int i=0; i<3; i++) {
						for (int n=0; n<3; n++) {
							 resultado[y*2+i+offy][x*2+n+offx]= cars[i].charAt(n)+"";
						}
					}
					
					offx++;
					//System.out.print(tablero[y][x].toString());
				}
				//System.out.println("");
				offy++;
				offx=0;
			}
			
			//System.out.println(Arrays.deepToString(resultado));
			
			String textoFinal = "";
			for (int y=0; y<resultado.length; y++) {
    			for (int x=0; x<resultado[y].length; x++) {
    				//System.out.print(resultado[y][x]);
    				 textoFinal+=resultado[y][x];
    			}
    			//System.out.println("");
    			textoFinal+="\n";
    		}
			
			//Hago reemplazos para corregir el dibujo ASCII
			
			// ═╔ -> ╦
			
			// ╗═ -> ╦
			
			// ═║ -> ╣
			
			// ╝═ -> ╩
			
			// ═╚ -> ╩
			
			// ║═ -> ╠
			
			
			
			/*textoFinal = textoFinal.replaceAll("╝═", "╩");
			textoFinal = textoFinal.replaceAll("═║", "╣");
			textoFinal = textoFinal.replaceAll("═╔", "╦");
			textoFinal = textoFinal.replaceAll("╗═", "╦");
			textoFinal = textoFinal.replaceAll("╗═", "╦");
			
			textoFinal = textoFinal.replaceAll("║═", "╠");
			
			textoFinal = textoFinal.replaceAll("═╚", "╩");
			*/	
			
			
			File archivo = new File(path);
			try {
				FileOutputStream fos = new FileOutputStream(archivo);
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));		
				
				bw.write(textoFinal);
	    		
	    		bw.close();
	    		fos.close();
	    		//repaint();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
    }
    
    
    public void toTxt(String path) {
    		String resultado[][] = new String[tablero.length*2+1][tablero[0].length*2+1];
    		for (int y=0; y<tablero.length; y++) {
    			for (int x=0; x<tablero[y].length; x++) {
    				String celda = tablero[y][x].toString();
    				String[] cars = celda.split("\n");
    				
    				for (int i=0; i<3; i++) {
    					for (int n=0; n<3; n++) {
    						 resultado[y*2+i][x*2+n]= cars[i].charAt(n)+"";
    					}
    				}
    				
    				//System.out.print(tablero[y][x].toString());
    			}
    			//System.out.println("");
    		}
    		
    		//System.out.println(Arrays.deepToString(resultado));
    		
    		File archivo = new File(path);
    		try {
    			FileWriter fw = new FileWriter(archivo);
				BufferedWriter bw = new BufferedWriter(fw);		
				
	    		for (int y=0; y<resultado.length; y++) {
	    			for (int x=0; x<resultado[y].length; x++) {
	    				//System.out.print(resultado[y][x]);
	    				bw.write(resultado[y][x]);
	    			}
	    			//System.out.println("");
	    			bw.write("\n");
	    		}
	    		
	    		bw.close();
	    		fw.close();
	    		//repaint();
    		} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    }

    public void init(){
        for (int y=0; y<tablero.length; y++){
             for (int x=0; x<tablero[y].length; x++){
                  tablero[y][x]= new cell(true,true,true,true);
             }
        }
    }

    public boolean isBusy(){
           return !done;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @Override
    public void paintComponent(Graphics g) {
        //System.out.println("paintComponent: ini=" + ini + ", fin=" + fin);
           super.setPreferredSize(new Dimension(ancho*celda,alto*celda));
           setPreferredSize(new Dimension(ancho*celda,alto*celda));
           //padre.setDatos(alto, ancho, delay, celda);
           super.paintComponent(g);
           //System.err.println("Entré en paintComponent");
        // Dibuja el fondo con el color configurado
        g.setColor(screenCol.getBackColor());
        g.fillRect(0, 0, ancho * celda, alto * celda);

        // Dibuja la grilla y las paredes del laberinto con color y grosor configurables
        g.setColor(screenCol.getLinesColor());
        float lw = lineWidth;
        if (screenCol.getLineWidth() > 0) {
            lw = screenCol.getLineWidth();
        }
        ((Graphics2D)g).setStroke(new BasicStroke(lw));
        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                cell c = tablero[y][x];
                int px = x * celda;
                int py = y * celda;
                // Norte
                if (c.getNorth()) {
                    ((Graphics2D)g).drawLine(px, py, px + celda, py);
                }
                // Sur
                if (c.getSouth()) {
                    ((Graphics2D)g).drawLine(px, py + celda, px + celda, py + celda);
                }
                // Oeste
                if (c.getWest()) {
                    ((Graphics2D)g).drawLine(px, py, px, py + celda);
                }
                // Este
                if (c.getEast()) {
                    ((Graphics2D)g).drawLine(px + celda, py, px + celda, py + celda);
                }
            }
        }

        // Dibuja la solución si corresponde
        if (showSolv && !solution.isEmpty()) {
            ((Graphics2D)g).setStroke(new BasicStroke(8));
            g.setColor(screenCol.getSolvColor());
            try {
                for (int m = 1; m < solution.size(); m++) {
                    int x0 = solution.get(m - 1).x * celda + celda / 2;
                    int y0 = solution.get(m - 1).y * celda + celda / 2;
                    int x1 = solution.get(m).x * celda + celda / 2;
                    int y1 = solution.get(m).y * celda + celda / 2;
                    Line2D mov1 = new Line2D.Double(x0, y0, x1, y1);
                    ((Graphics2D)g).draw(mov1);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // Dibuja los puntos de entrada y salida
        if (ini != null && done) {
            g.setColor(screenCol.getEntryColor());
            ((Graphics2D)g).fill(ini);
        }
        if (fin != null && done) {
            g.setColor(screenCol.getExitColor());
            ((Graphics2D)g).fill(fin);
        }

           if (done) {
               // Dibuja el trazo del jugador (líneas conectando centros de celdas)
               if (showTail && !jugadas.isEmpty()) {
                   System.out.println("Dibujando trazo. Stack size: " + jugadas.size());
                   ((Graphics2D)g).setStroke(new BasicStroke(8));
                   g.setColor(screenCol.getPlayerBodyColor());
                   
                   // Construir lista con todos los puntos: inicio -> jugadas -> actual
                   java.util.List<Point> trazoPuntos = new java.util.ArrayList<>();
                   trazoPuntos.add(inicio);
                   for (int i = 0; i < jugadas.size(); i++) {
                       trazoPuntos.add(jugadas.get(i));
                   }
                   trazoPuntos.add(jugada_actual);
                   
                   // Dibujar líneas conectando puntos consecutivos (igual a como se dibuja la solución)
                   for (int m = 1; m < trazoPuntos.size(); m++) {
                       int x0 = trazoPuntos.get(m - 1).x * celda + celda / 2;
                       int y0 = trazoPuntos.get(m - 1).y * celda + celda / 2;
                       int x1 = trazoPuntos.get(m).x * celda + celda / 2;
                       int y1 = trazoPuntos.get(m).y * celda + celda / 2;
                       Line2D trazo = new Line2D.Double(x0, y0, x1, y1);
                       ((Graphics2D)g).draw(trazo);
                   }
               }
               
               // Dibuja la cabeza del jugador como un pequeño círculo
               g.setColor(screenCol.getPlayerHeadColor());
               double centerX = jugada_actual.x * celda + celda / 2.0;
               double centerY = jugada_actual.y * celda + celda / 2.0;
               double headSize = celda / 4.0;  // Cabeza más delgada
               ((Graphics2D)g).fillOval((int)(centerX - headSize/2), (int)(centerY - headSize/2), (int)headSize, (int)headSize);
           }

           
         
    }

    @Override
    public void update(Graphics g){
        //try {
          
        repaint();
        /*} catch (Exception ex){
              ex.printStackTrace();
        }*/
    }

    public Colores getScreenColors(){
           return screenCol;
    }

    public Colores getPrintColors(){
            return printCol;
    }

    public boolean saveMaze(String path){
           try {
               if (path.length() > 0){
                   ObjectOutputStream ost = new ObjectOutputStream( new FileOutputStream(path));
                   ost.writeObject(new MazeObj(ancho,alto,celda,tablero,inicio,salida,this));
                   ost.close();
                   return true;
               } else {
                   return false;
               }
           } catch (Exception ex){
               return false;
           }
    }

    public void openMaze(String path){
           try {
               if (path.length()>0){
                   ObjectInputStream ois = new ObjectInputStream( new FileInputStream(path));
                   MazeObj mo  =  (MazeObj) ois.readObject();
                   tablero = mo.getBoard().clone();
                   ancho = mo.getWidth();
                   alto = mo.getHeight();
                   celda = mo.getCellSize();
                   inicio = mo.getInit();
                   salida = mo.getFin();
                   
                   //super.setSize(ancho*celda, alto*celda);
                   //setPreferredSize(new Dimension(ancho*celda,alto*celda));
                   ois.close();
                   justLoaded=true;
                   //IDE miPadre = (IDE)this.getParent();
                   if (padre!=null){
                       padre.setDatos(alto, ancho, delay, celda);
                   }
                   setIni(getCornerNum(inicio));
                   setFin(getCornerNum(salida));
                   rePintar(0);
                   done=true;
                   
                   repaint();
               }
           } catch (Exception ex){
           }
    }

    public boolean exportMaze(String path){
           if (path.length()>0){
               String ext = path.substring(path.lastIndexOf(".")+1);
               try {
                   // Save as Imagen
                   System.out.println("Archivo a exportar: " + path + " Extension: " + ext);
                   File file = new File(path);
                   /*if (printMaze == null){
                       rePintar(1);
                       while (printMaze == null){}
                   }*/
                   ImageIO.write(printMaze, ext, file);
                   return true;
               } catch (Exception ex) {
                   return false;
               }
           }
           return false;
    }

    
    public void showSolution(boolean ss){
           showSolv=ss;
           repaint();
    }

    public void setAncho(int a){
        anold=ancho;
        ancho=a;
        if (alto!=0){
            tablero = new cell[alto][ancho];
            initBoard();
            //super.setSize(alto*celda, ancho*celda);
            //this.getGraphics().setColor(Color.black);
            //this.getGraphics().clearRect(0, 0, ancho, alto);
            rePintar(0);
            repaint();
        }
    }

    private void initBoard(){
            for (int y=0; y<tablero.length; y++){
                for (int x=0; x<tablero[y].length; x++){
                    tablero[y][x] = new cell(true,true,true,true);
                }
            }
    }

    public void setAlto(int a){
        alold=alto;
        alto=a;
        if (ancho!=0){
            tablero = new cell[alto][ancho];
            initBoard();
            //this.getGraphics().setColor(Color.black);
            //this.getGraphics().clearRect(0, 0, ancho, alto);
            //super.setSize(alto*celda, ancho*celda);
            rePintar(0);
            repaint();
        }
    }

    public void setCellSize(int cs){
        if (cs>=4){
            cellold=celda;
            celda=cs;
        }
        if (ancho!=0 && alto!=0){
            rePintar(0);
            repaint();
        }
    }

    /**
    * Establece el tiempo en milisegundos que se demorará el pasaje entre casilleros,
     * si se establece en 0 se muestra el laberinto generado instantaneamente.-
     * @param d
     */

    public void setDelay(int d){
           delay = d;
           if (motor != null){
               motor.setDelay(d);
           }
    }

    public void Solve(){
           Solver sol_motor1 = new Solver(tablero, new Point(jugada_actual.x, jugada_actual.y), salida, this);
           sol_motor1.setPadre(this);
           sol_motor1.execute();
    }

    public void updateScreenColors(Colores cl){
        screenCol.setBackColor(cl.getBackColor());
        screenCol.setEntryColor(cl.getEntryColor());
        screenCol.setExitColor(cl.getExitColor());
        screenCol.setLineColor(cl.getLinesColor());
        screenCol.setPlayerBodyColor(cl.getPlayerBodyColor());
        screenCol.setPlayerHeadColor(cl.getPlayerHeadColor());
        screenCol.setSolvColor(cl.getSolvColor());
        rePintar(0);
        repaint();
    }

    public void updatePrintColors(Colores cl){
        printCol.setBackColor(cl.getBackColor());
        printCol.setEntryColor(cl.getEntryColor());
        printCol.setExitColor(cl.getExitColor());
        printCol.setLineColor(cl.getLinesColor());
        //printCol.setPlayerBodyColor(cl.getPlayerBodyColor());
        //printCol.setPlayerHeadColor(cl.getPlayerHeadColor());
        //printCol.setSolvColor(cl.getSolvColor());
        rePintar(1);
    }
   
    public Point keyPressed(int keyCode){
           System.out.println("KeyPressed llamado. done=" + done + " keyCode=" + keyCode);
           if (done){
               System.out.println("done es true, procesando movimiento");
               int x = 0,y = 0;
               switch (keyCode){
                   case keys.arriba:
                        y = jugada_actual.y;
                        x = jugada_actual.x;
                        if (y-1>=0 && !tablero[y][jugada_actual.x].getNorth()){
                           if (!jugadas.isEmpty()){
                               Point lastPos = jugadas.peek();
                               if (lastPos.y == y-1 ){
                                      jugada_actual=jugadas.pop();
                                      System.out.println("Volviendo atrás, stack size: " + jugadas.size());
                                      repaint();
                                      break;
                               }
                           }
                           if (showTail) {
                                   jugadas.push(new Point(jugada_actual.x, jugada_actual.y));
                                   System.out.println("Movimiento grabado ARRIBA. Stack size: " + jugadas.size() + " Pos guardada: " + jugada_actual.x + "," + jugada_actual.y);
                           }
                           y--;
                           jugada_actual = new Point(x,y);
                           System.out.println("Nueva posición ARRIBA: " + jugada_actual.x + "," + jugada_actual.y);
                           repaint();
                        }
                        break;
                   case keys.abajo:
                       y = jugada_actual.y;
                       x = jugada_actual.x;
                       if (y+1<alto && !tablero[y][x].getSouth()){

                           if (!jugadas.isEmpty()){
                               Point lastPos = jugadas.peek();
                               if (lastPos.y == y+1){
                                   jugada_actual=jugadas.pop();
                                   System.out.println("Volviendo atrás, stack size: " + jugadas.size());
                                   repaint();
                                   break;
                               }
                           }
                           if (showTail){
                               jugadas.push(new Point(jugada_actual.x, jugada_actual.y));
                               System.out.println("Movimiento grabado ABAJO. Stack size: " + jugadas.size() + " Pos guardada: " + jugada_actual.x + "," + jugada_actual.y);
                           }
                           y++;
                           lastDirection=keys.abajo;
                           jugada_actual = new Point(x,y);
                           System.out.println("Nueva posición ABAJO: " + jugada_actual.x + "," + jugada_actual.y);
                           repaint();
                           
                       }
                       break;
                   case keys.derecha:
                       y = jugada_actual.y;
                       x = jugada_actual.x;
                       if (x+1<ancho && !tablero[y][x].getEast()){

                           if (!jugadas.isEmpty()){
                               Point lastPos = jugadas.peek();
                               if (lastPos.x == x+1){
                                   jugada_actual=jugadas.pop();
                                   System.out.println("Volviendo atrás, stack size: " + jugadas.size());
                                   repaint();
                                   break;
                               }
                           }
                           if (showTail){
                               jugadas.push(new Point(jugada_actual.x, jugada_actual.y));
                               System.out.println("Movimiento grabado DERECHA. Stack size: " + jugadas.size() + " Pos guardada: " + jugada_actual.x + "," + jugada_actual.y);
                           }
                           x++;
                           lastDirection=keys.derecha;
                           jugada_actual = new Point(x,y);
                           System.out.println("Nueva posición DERECHA: " + jugada_actual.x + "," + jugada_actual.y);
                           repaint();
                           
                       }
                       break;
                   case keys.izquierda:
                       y = jugada_actual.y;
                       x = jugada_actual.x;
                       if (x-1>=0 && !tablero[y][x].getWest()){

                           if (!jugadas.isEmpty()){
                               Point lastPos = jugadas.peek();
                               if (lastPos.x == x-1){
                                   jugada_actual=jugadas.pop();
                                   System.out.println("Volviendo atrás, stack size: " + jugadas.size());
                                   repaint();
                                   break;
                               }
                           }
                           if (showTail){
                               jugadas.push(new Point(jugada_actual.x, jugada_actual.y));
                               System.out.println("Movimiento grabado IZQUIERDA. Stack size: " + jugadas.size() + " Pos guardada: " + jugada_actual.x + "," + jugada_actual.y);
                           }
                           x--;
                           lastDirection=keys.izquierda;
                           jugada_actual = new Point(x,y);
                           System.out.println("Nueva posición IZQUIERDA: " + jugada_actual.x + "," + jugada_actual.y);
                           repaint();
                           
                       }
                       break;
               }
               return new Point(jugada_actual.x*celda,jugada_actual.y*celda);
           }
           return null;
    }

    public void setAlgorithm(int a){
           if (a==0 || a==1 || a==2){
               alg = a;
           }
    }

    public void setProbability(int p){
        prob=p;
        if (motor!=null){
            motor.setProbability(p);
        }
    }

    public void gen(){
           /*if (Maze != null){
               Maze.getGraphics().setColor(Color.black);
               Maze.getGraphics().clearRect(0, 0, alto*celda, ancho*celda);
               repaint();
               //Maze = null;
           }*/
           

           System.out.println("Jugada actual --> X=" + jugada_actual.x + " Y=" + jugada_actual.y);

           done = false;
           if (!jugadas.isEmpty()){
               jugadas.clear();
           }

           if (!solution.isEmpty()){
               solution.clear();
           }

           if (sol_motor != null){
               sol_motor.abort();
           }
           
           if (motor != null){
               motor.abort();
           }

           motor = new engine(ancho,alto,this);
           motor.setDelay(delay);
           motor.setProbability(prob);
           motor.setDebuging(false);
           motor.showConstruction((delay!=0));
           motor.setAlg(alg); 
           //System.out.println("Generar el laberinto");
           motor.execute();
          
           //rePintar();
           //repaint();
           /*int f1 = (int)(Math.random()*4);
           int i1 = (int)(Math.random()*4);
           while (i1==f1){
                i1 = (int)(Math.random()*4);
           }*/
         
           
           
          
    }


    private void setEntranceExit(){
        int maxsol = 0;
           int ini2=0,fin2=1;
           SolvCounter sCounter = new SolvCounter(tablero,this);

           for (int n=0; n<inifin.length; n++){
               setIni(((int)(inifin[n]/10))-1);
               setFin(((int)(inifin[n]%10))-1);
               solMode=1;
               while (jugada_actual==null){
               }
               while(salida==null){
               }

               sCounter = new SolvCounter(tablero,this);
               sCounter.setIni(new Point(jugada_actual.x, jugada_actual.y));
               sCounter.setFin(salida);
               sCounter.execute();
               while(!sCounter.isDone()){
               }
               if (solaux.size() > maxsol){
                   maxsol=solaux.size();
                   ini2=(int)(inifin[n]/10);
                   ini2--;
                   fin2=(int)(inifin[n]%10);
                   fin2--;
               }

               solaux.clear();
           }
           solMode=0;
           setIni(ini2);
           setFin(fin2);
           //setIni(i1);
           //setFin(f1);
    }

    public void setIni(int i1){
        //Rectangle2D ini;
        // Centra el rectángulo en la celda de entrada
        double size = celda * 0.6;
        double offset = celda * 0.2;
        switch(i1){
            case 0:
            ini = new Rectangle2D.Double(0 * celda + offset, 0 * celda + offset, size, size);
            jugada_actual = new Point(0,0);
            inicio = new Point(0, 0);
            break;
            case 1:
            ini = new Rectangle2D.Double((ancho-1) * celda + offset, 0 * celda + offset, size, size);
            jugada_actual = new Point(ancho-1,0);
            inicio = new Point(ancho-1, 0);
            break;
            case 2:
            ini = new Rectangle2D.Double(0 * celda + offset, (alto-1) * celda + offset, size, size);
            jugada_actual = new Point(0,alto-1);
            inicio = new Point(0, alto-1);
            break;
            case 3:
            ini = new Rectangle2D.Double((ancho-1) * celda + offset, (alto-1) * celda + offset, size, size);
            jugada_actual = new Point(ancho-1,alto-1);
            inicio = new Point(ancho-1, alto-1);
            break;
        }
           //return ini;
    }

    public void setFin(int f1){
        // Centra el rectángulo en la celda de salida
        double size = celda * 0.6;
        double offset = celda * 0.2;
        switch(f1){
            case 0:
            fin = new Rectangle2D.Double(0 * celda + offset, 0 * celda + offset, size, size);
            salida = new Point(0, 0);
            break;
            case 1:
            fin = new Rectangle2D.Double((ancho-1) * celda + offset, 0 * celda + offset, size, size);
            salida = new Point(ancho-1, 0);
            break;
            case 2:
            fin = new Rectangle2D.Double(0 * celda + offset, (alto-1) * celda + offset, size, size);
            salida = new Point(0, alto-1);
            break;
            case 3:
            fin = new Rectangle2D.Double((ancho-1) * celda + offset, (alto-1) * celda + offset, size, size);
            salida = new Point(ancho-1, alto-1);
            break;
        }
    }

    public int getCornerNum(Point corner){
           int num=0;
           if (corner.y == 0 && corner.x == 0){
              num=0;
           } else if (corner.y == 0 && corner.x == ancho - 1) {
              num = 1;
           } else if (corner.y == alto - 1 && corner.x == 0){
              num = 2;
           } else {
              num = 3;
           }

           return num;
    }


    public Color getRandomColor(){
           return new Color((int)(Math.random()*255),(int)(Math.random()*255),(int)(Math.random()*255),(int)(Math.random()*255)) ;
    }

    public void showSolution(Stack<Point> movs){
           if (solMode==0){
               
               solution = (Stack<Point>) movs.clone();
               repaint();
           } else {
               
               solaux = (Stack<Dimension>) movs.clone();
           }
    }

    public void abort(){
           if (motor!=null){
               motor.abort();
               done = true;
           }
    }
    
    public void setDone(){
           done=true;
           setEntranceExit();
           juego_motor = new engine(tablero);
           //pSupport.firePropertyChange("done", false, true);
           rePintar(0);
           rePintar(1);
    }

    public void draw(cell[][] mapa){
           tablero = mapa.clone();
           rePintar(0);
    }


    public void rePintar(int mode){
            if (alto!=0 && ancho!=0 && celda!=0){
               //Dimension size = super.getSize();
               if (justLoaded){
                   //if ((size.height != alto*celda || size.width != ancho*celda)){
                       super.setSize(ancho*celda, alto*celda);
                       //padre.setDatos(alto,ancho,delay,celda);
                   //}
                   justLoaded=false;
               }
               
               trabajador worker = new trabajador();
               worker.setAlto(alto);
               worker.setAncho(ancho);
               worker.setCellSize(celda);
               worker.setLineWidth(lineWidth);
               worker.setGraphics(getGraphics());
               worker.setMode(mode);
               //worker.setTimeout(0);
               worker.setPadre(this);
               //System.out.println("Worker iniciado -> " + new Date());
               //Este if es para bypasear un bug de la version 1.6u20 de JAVA que no permite más de un SwingWorker trabajando en simultáneo
               if (System.getProperty("java.version").toString().equals("1.6.0_20")){
                    Maze=worker.doInBackground();
               } else {
                    worker.execute();
               }
               //Maze=worker.doInBackground();
               
           }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                formKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyReleased
        // TODO add your handling code here:

    }//GEN-LAST:event_formKeyReleased

    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
        // TODO add your handling code here:
        System.out.println("Tecla presionada: " + evt.getKeyCode());
    }//GEN-LAST:event_formKeyPressed

    // Getters para acceder a las propiedades del laberinto desde otras clases
    public int getAncho() {
        return ancho;
    }
    
    public int getAlto() {
        return alto;
    }
    
    public int getCellSize() {
        return celda;
    }
    
    public Point getInit() {
        return inicio;
    }
    
    public Point getFin() {
        return salida;
    }
    
    public Point getJugadaActual() {
        return jugada_actual;
    }
    
    public void setJugadaActual(Point pos) {
        jugada_actual = pos;
        repaint();
    }
    
    public void addMovimientoJugador(Point from, Point to) {
        // Agregar movimiento al stack de jugadas
        if (showTail && !from.equals(to)) {
            // Verificar si no es un movimiento hacia atrás
            if (!jugadas.isEmpty()) {
                Point lastPos = jugadas.peek();
                if (lastPos.equals(to)) {
                    // Estamos volviendo atrás, hacer pop
                    jugadas.pop();
                    return;
                }
            }
            // Es un movimiento nuevo, hacer push
            jugadas.push(new Point(from.x, from.y));
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}
class trabajador extends SwingWorker{
    private MazeGRID padre;
    private int ancho, alto, celda;
    private float lineWidth;
    private Stack<Boolean> paredesh= new Stack(), paredesv = new Stack();
    private BufferedImage mazeIMG;
    private Graphics g;
    private int mode = 0;

    //private Canvas c;
    //public Graphics2D g2d = null;

    @Override
    protected BufferedImage doInBackground() {
        if (alto!=0 && ancho!=0 && celda!=0 & padre!=null){
               //Graphics2D g2d2 = (Graphics2D)padre.getGraphics();
               setParedes();
               int cnt=0;
               Line2D l2d;

               //if (padre.isFirstTime()){
               
               mazeIMG=(BufferedImage)padre.createImage(padre.getWidth(), padre.getHeight());
               //mazeIMG = new BufferedImage(padre.getWidth(),padre.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
               
               Graphics2D  g2d = (Graphics2D)mazeIMG.createGraphics();
                   //padre.createGraphics();
              // }
               
               switch (mode){
                   case 0: 
                           g2d.setBackground(padre.screenCol.getBackColor());
                           g2d.setColor(padre.screenCol.getBackColor());
                           g2d.setColor(padre.screenCol.getLinesColor());
                           break;
                   case 1:
                           g2d.setBackground(padre.printCol.getBackColor());
                           g2d.setColor(padre.printCol.getBackColor());
                           g2d.setColor(padre.printCol.getLinesColor());
                           break;
               }

               //if (padre.isFirstTime()){
               g2d.clearRect(0, 0, ancho*celda, alto*celda);
                   //padre.createGraphics();
               //}
               
               g2d.setStroke(new BasicStroke(lineWidth));
               for (int y=0; y<=alto*celda; y+=celda){
                   for (int x=0; x<ancho*celda; x+=celda){
                       //g2d.setColor(getRandomColor());
                       l2d = new Line2D.Double(x, y, x+celda, y);
                       //if (padre.tablero[y][x].getNorth()){
                       //System.out.println("Pared h:"+paredesh.peek());
                       if (paredesh.pop()){
                           g2d.draw(l2d);
                           cnt++;
                       }
                       //}
                       
                   }
               }
               g2d.setStroke(new BasicStroke(lineWidth));
               for (int y=0; y<alto*celda; y+=celda){
                   for (int x=0; x<=ancho*celda; x+=celda){
                        //g2d.setColor(getRandomColor());
                        //g2d.drawLine(y, x, y, x+celda);
                        l2d = new Line2D.Double(x, y, x, y+celda);
                        //System.out.println("Pared v:"+paredesv.peek());
                        if (paredesv.pop()){
                            g2d.draw(l2d);
                            //g2d.drawString(String.valueOf(cnt), x+(celda/2), y+(celda/2));
                            cnt++;
                        }
                        
                   }
               }
               padre.graficar(mazeIMG,mode);
               
               //System.out.println("Dibujé " + cnt + " líneas");
        }
        return mazeIMG;
    }

    public void setMode(int m){
        if (m==0 || m==1){
            mode = m;
        }
    }

    public void setAlto(int a){
            alto=a;
    }

    public void setAncho(int a){
            ancho=a;
    }

    public void setCellSize(int cs){
            celda=cs;
    }

    public void setPadre(MazeGRID mg){
            padre=mg;
    }

    public void setGraphics(Graphics gr){
        g = gr;
    }

    public void setLineWidth(float lw) {
           lineWidth = lw;
    }


    /*
    public void setTimeout(int to){
        timeout=to;
    }*/

    public void setParedes(){
           paredesh.clear();
           paredesv.clear();
           Stack<Boolean> ph = new Stack(), pv = new Stack();
           int x,y;
           for (y=0; y<alto; y++){
               for (x=0; x<ancho; x++){
                       ph.push(padre.tablero[y][x].getNorth());
               }
           }
           for (x=0; x<ancho; x++){
                ph.push(true);    
           }

           for (y=0; y<alto; y++){
               pv.push(true);
               for (x=0; x<ancho-1; x++){
                   pv.push(padre.tablero[y][x].getEast());
               }
               pv.push(true);
           }

           paredesh = (Stack<Boolean>) ph.clone();
           paredesv = (Stack<Boolean>) pv.clone();
           while (!ph.isEmpty()){
                 paredesh.push(ph.pop());
           }
           while (!pv.isEmpty()){
               paredesv.push(pv.pop());
           }

    }

}



