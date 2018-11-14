package flappysquare;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.Timer;
import javax.swing.JFrame;

public class FlappySquare implements ActionListener, MouseListener, KeyListener {

    public static FlappySquare flappySquare;

    public final int WIDTH = 1000, HEIGHT = 900;

    public Renderer renderer;

    public Rectangle square;

    public ArrayList<Rectangle> columns;

    public int ticks, yMotion, score;

    public boolean gameOver, started;

    public Random rand;

    public FlappySquare() {
        JFrame jframe = new JFrame();
        Timer timer = new Timer(20, this); // this se odnosi na ActionListener

        renderer = new Renderer();
        rand = new Random();

        jframe.add(renderer);
        jframe.setTitle("Flappy Square");
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setSize(WIDTH, HEIGHT);
        jframe.addMouseListener(this);
        jframe.addKeyListener(this);
        jframe.setResizable(false);
        jframe.setVisible(true);

        square = new Rectangle(WIDTH / 2 - 10, HEIGHT / 2 - 10, 20, 20);
        columns = new ArrayList<Rectangle>(); // lista stubova (koloni)

        //dodajemo 4 stuba za celu igru posto ce se posle stari brisati i praviti novi
        addColumn(true);
        addColumn(true);
        addColumn(true);
        addColumn(true);

        timer.start();
    }

    private void addColumn(boolean start) {
        int space = 260;
        int width = 100;
        int height = 50 + rand.nextInt(500);

        if (start) { // stubovi na pocetku
            columns.add(new Rectangle(WIDTH + width + columns.size() * 300, HEIGHT - height - 120, width, height));
            columns.add(new Rectangle(WIDTH + width + (columns.size() - 1) * 300, 0, width, HEIGHT - height - space));
        } else { // stubevi nakon pocetka
            columns.add(new Rectangle(columns.get(columns.size() - 1).x + 600, HEIGHT - height - 120, width, height)); // dodaj stub sa kraja liste stubova
            columns.add(new Rectangle(columns.get(columns.size() - 1).x, 0, width, HEIGHT - height - space));
        }
    }

    public void paintColumn(Graphics g, Rectangle column) {
        g.setColor(Color.green.darker());
        g.fillRect(column.x, column.y, column.width, column.height);
    }

    public void jump() {
        if (gameOver) {
            square = new Rectangle(WIDTH / 2 - 10, HEIGHT / 2 - 10, 20, 20); // koordinate startne pozicije kvadratica i dimenzije
            columns.clear(); // obrisi celu listu
            yMotion = 0; // zaustavi pomeranje
            score = 0; // resetuj skor
            
            // dodaj kolone
            addColumn(true);
            addColumn(true);
            addColumn(true);
            addColumn(true);

            gameOver = false;
        }

        if (!started) {
            started = true;
        } else if (!gameOver) {
            if (yMotion > 0) {
                yMotion = 0;
            }
            yMotion -= 10;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) { // metod celog scenarija
        int speed = 12;

        ticks++;

        if (started) {
            for (int i = 0; i < columns.size(); i++) { // generisanje kolona
                Rectangle column = columns.get(i);
                column.x -= speed; // pomeranje stubova na levo
            }

            if (ticks % 2 == 0 && yMotion < 15) {
                yMotion += 2; // gravitacija, kretanje na dole
            }

            for (int i = 0; i < columns.size(); i++) {
                Rectangle column = columns.get(i);

                if (column.x + column.width < 0) { // pbrisi kolonu ako je otisla skroz levo
                    columns.remove(column);

                    if (column.y == 0) { // ako je prva kolona u pitanju jer je y uvek 0 za nju
                        addColumn(false);
                    }
                }
            }

            square.y += yMotion;

            for (Rectangle column : columns) {
                if (column.y == 0 && square.x + square.width / 2 > column.x + column.width / 2 - 5 && square.x + square.width / 2 < column.x + column.width / 2 + 5 && !gameOver) {
                    score++; // skor se uvecava kada se prodje prva kolona naredna i kada se prodje prostor izmedju delova stuba i ako igra nije gotova
                }

                if (column.intersects(square)) {
                    gameOver = true;
                    
                    if (square.x <= column.x) {
                        square.x = column.x - square.width; // da se kvdadrat zadrzi kod poslenjeg stuba
                        
                    } else {
                        
                        if (column.y != 0) { // ako nije prva kolona
                            square.y = column.y - square.height; // spusti je na donju ivicu stuba
                            
                        } else if (square.y < column.height) { // ako nije doslo do dna stuba spusti je niz stub do zemlje
                            square.y = column.height;
                        }
                    }
                }
            }

            if (square.y > HEIGHT - 120 || square.y < 0) { // kraj igre ako ode skroz gore ili dole
                gameOver = true;
            }

            if (square.y + yMotion >= HEIGHT - 120) { // ako je kvadrat dosao do zemlje
                square.y = HEIGHT - 120 - square.height;
                gameOver = true;
            }
        }
        
        renderer.repaint();
    }

    public void repaint(Graphics g) {
        g.setColor(Color.cyan);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        g.setColor(Color.ORANGE);
        g.fillRect(0, HEIGHT - 120, WIDTH, 150);

        g.setColor(Color.green);
        g.fillRect(0, HEIGHT - 120, WIDTH, 20);

        g.setColor(Color.red);
        g.fillRect(square.x, square.y, square.width, square.height);

        for (Rectangle column : columns) { // oboji sve stubove u listi
            paintColumn(g, column);
        }

        g.setColor(Color.white);
        g.setFont(new Font("Arial", 1, 100));

        if (!started) {
            g.drawString("Klikni za pocetak!", 75, HEIGHT / 2 - 50);
        }

        if (gameOver) {
            g.drawString("Igra zavrsena!", 100, HEIGHT / 2 - 50);
            g.drawString("Rezultat:" + String.valueOf(score), 100, HEIGHT / 2 + 40);

        }

        if (!gameOver && started) {
            g.drawString(String.valueOf(score), WIDTH / 2 - 25, 100);
        }
    }

    public static void main(String[] args) {
        flappySquare = new FlappySquare();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        jump();
    }

    @Override
    public void keyReleased(KeyEvent e) {

        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            jump();
        }

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

}
