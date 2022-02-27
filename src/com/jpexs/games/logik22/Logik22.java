package com.jpexs.games.logik22;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author JPEXS
 */
public class Logik22 extends JFrame {

    private BufferedImage rowImage;
    private BufferedImage colorOutImage;
    private BufferedImage colorInImage;
    private BufferedImage colorOutDeskImage;
    private BufferedImage exactMatchImage;
    private BufferedImage inexactMatchImage;

    private final Color BACKGROUND_COLOR = new Color(0xff, 0, 0);

    private static final Color[] colors = new Color[]{
        Color.red,
        Color.blue,
        Color.white,
        Color.black,
        Color.green,
        Color.yellow,
        new Color(0x88, 0x00, 0x15)
    };

    private static Image colorOutImages[];
    private static Image colorInImages[];
    private static Image colorOutDeskImages[];

    private static final int ROW_HEIGHT = 50;
    private JPanel contentPanel;
    private static final int ROWS = 10;
    private static final int COLS = 6;

    private int colorOutHilight = -1;

    private int selectedColors[][] = new int[ROWS][COLS];

    private final int NO_COLOR = -1;

    private int currentCol = 0;
    private int currentRow = 0;

    private final String RESOURCE_PATH = "/com/jpexs/games/logik22/graphics";

    private int[] secretColors;

    private int[][] matches = new int[ROWS][COLS];

    private final int NO_MATCH = 0;
    private final int INEXACT_MATCH = 1;
    private final int EXACT_MATCH = 2;

    private boolean gamePaused = false;

    private final boolean DEBUG_SHOW_SECRET = false;

    private final int HOLE_WIDTH = 30;

    private final int FIRST_HOLE_LEFT = 30;

    private final int DEFAULT_COLS = 5;

    private final int SMALL_HOLE_WIDTH = 15;

    private final int SMALL_HOLE_LEFT = 12;

    private final int SMALL_HOLE_RIGHT = 32;

    private final int MATCHES_LEFT = 3;

    public Logik22() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        newGame();

        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");
        JMenuItem newGameMenuItem = new JMenuItem("New game");
        JMenuItem exitGameMenuItem = new JMenuItem("Exit game");

        newGameMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newGame();
                contentPanel.repaint();
            }
        });

        exitGameMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        gameMenu.add(newGameMenuItem);
        gameMenu.add(exitGameMenuItem);
        menuBar.add(gameMenu);

        setJMenuBar(menuBar);

        try {
            rowImage = ImageIO.read(getClass().getResource(RESOURCE_PATH + "/row.png"));
            colorInImage = ImageIO.read(getClass().getResource(RESOURCE_PATH + "/colorin.png"));
            colorOutImage = ImageIO.read(getClass().getResource(RESOURCE_PATH + "/colorout.png"));
            colorOutDeskImage = ImageIO.read(getClass().getResource(RESOURCE_PATH + "/coloroutdesk.png"));
            exactMatchImage = ImageIO.read(getClass().getResource(RESOURCE_PATH + "/black.png"));
            inexactMatchImage = ImageIO.read(getClass().getResource(RESOURCE_PATH + "/white.png"));

        } catch (IOException ex) {
            System.err.println("Cannot load image from resources");
            System.exit(1);
        }

        rowImage = dye(rowImage, BACKGROUND_COLOR);

        colorOutImages = new Image[colors.length];
        colorOutDeskImages = new Image[colors.length];
        colorInImages = new Image[colors.length];

        for (int i = 0; i < colors.length; i++) {
            colorOutImages[i] = dye(colorOutImage, colors[i]);
            colorOutDeskImages[i] = dye(colorOutDeskImage, colors[i]);
            colorInImages[i] = dye(colorInImage, colors[i]);
        }

        contentPanel = new JPanel() {

            @Override
            public void paintComponent(Graphics g) {

                Color fillColor = new Color(rowImage.getRGB(0, 0));
                g.setColor(fillColor);
                g.fillRect(0, 0, getWidth(), getHeight());
                for (int i = 0; i < ROWS; i++) {
                    g.drawImage(rowImage, 0, i * ROW_HEIGHT, FIRST_HOLE_LEFT, (i + 1) * ROW_HEIGHT,
                            0, 0, FIRST_HOLE_LEFT, ROW_HEIGHT, null);
                    for (int j = 0; j < COLS - 1; j++) {
                        g.drawImage(rowImage, FIRST_HOLE_LEFT + HOLE_WIDTH * j, i * ROW_HEIGHT, FIRST_HOLE_LEFT + HOLE_WIDTH * (j + 1), (i + 1) * ROW_HEIGHT,
                                FIRST_HOLE_LEFT, 0, FIRST_HOLE_LEFT + HOLE_WIDTH, ROW_HEIGHT, null);
                    }
                    //last col
                    g.drawImage(rowImage, FIRST_HOLE_LEFT + HOLE_WIDTH * (COLS - 1), i * ROW_HEIGHT, FIRST_HOLE_LEFT + HOLE_WIDTH * COLS, (i + 1) * ROW_HEIGHT,
                            FIRST_HOLE_LEFT + HOLE_WIDTH * (DEFAULT_COLS - 1), 0, FIRST_HOLE_LEFT + HOLE_WIDTH * DEFAULT_COLS, ROW_HEIGHT, null);

                    g.drawImage(rowImage, FIRST_HOLE_LEFT + HOLE_WIDTH * COLS, i * ROW_HEIGHT, FIRST_HOLE_LEFT + HOLE_WIDTH * COLS + SMALL_HOLE_LEFT, (i + 1) * ROW_HEIGHT,
                            FIRST_HOLE_LEFT + HOLE_WIDTH * DEFAULT_COLS, 0, FIRST_HOLE_LEFT + HOLE_WIDTH * DEFAULT_COLS + SMALL_HOLE_LEFT, ROW_HEIGHT, null);

                    for (int j = 0; j < COLS - 1; j++) {
                        g.drawImage(rowImage, FIRST_HOLE_LEFT + HOLE_WIDTH * COLS + SMALL_HOLE_LEFT + SMALL_HOLE_WIDTH * j, i * ROW_HEIGHT, FIRST_HOLE_LEFT + HOLE_WIDTH * COLS + SMALL_HOLE_LEFT + SMALL_HOLE_WIDTH * (j + 1), (i + 1) * ROW_HEIGHT,
                                FIRST_HOLE_LEFT + HOLE_WIDTH * DEFAULT_COLS + SMALL_HOLE_LEFT, 0, FIRST_HOLE_LEFT + HOLE_WIDTH * DEFAULT_COLS + SMALL_HOLE_LEFT + SMALL_HOLE_WIDTH, ROW_HEIGHT, null);
                    }
                    //last col
                    g.drawImage(rowImage, FIRST_HOLE_LEFT + HOLE_WIDTH * COLS + SMALL_HOLE_LEFT + SMALL_HOLE_WIDTH * (COLS - 1), i * ROW_HEIGHT, FIRST_HOLE_LEFT + HOLE_WIDTH * COLS + SMALL_HOLE_LEFT + SMALL_HOLE_WIDTH * COLS, (i + 1) * ROW_HEIGHT,
                            FIRST_HOLE_LEFT + HOLE_WIDTH * DEFAULT_COLS + SMALL_HOLE_LEFT + SMALL_HOLE_WIDTH * (DEFAULT_COLS - 1), 0, FIRST_HOLE_LEFT + HOLE_WIDTH * DEFAULT_COLS + SMALL_HOLE_LEFT + SMALL_HOLE_WIDTH * DEFAULT_COLS, ROW_HEIGHT, null);
                    g.drawImage(rowImage, FIRST_HOLE_LEFT + HOLE_WIDTH * COLS + SMALL_HOLE_LEFT + SMALL_HOLE_WIDTH * COLS, i * ROW_HEIGHT, FIRST_HOLE_LEFT + HOLE_WIDTH * COLS + SMALL_HOLE_LEFT + SMALL_HOLE_WIDTH * COLS + SMALL_HOLE_RIGHT, (i + 1) * ROW_HEIGHT,
                            FIRST_HOLE_LEFT + HOLE_WIDTH * DEFAULT_COLS + SMALL_HOLE_LEFT + SMALL_HOLE_WIDTH * DEFAULT_COLS, 0, FIRST_HOLE_LEFT + HOLE_WIDTH * DEFAULT_COLS + SMALL_HOLE_LEFT + SMALL_HOLE_WIDTH * DEFAULT_COLS + SMALL_HOLE_RIGHT, ROW_HEIGHT, null);

                    g.setFont(g.getFont().deriveFont(8f));
                    g.setColor(Color.black);
                    int w = g.getFontMetrics().stringWidth("" + (i + 1));
                    g.drawString("" + (i + 1), 14 - w / 2, i * ROW_HEIGHT + 30);
                    g.drawString("" + (i + 1), FIRST_HOLE_LEFT + HOLE_WIDTH * COLS + SMALL_HOLE_LEFT + SMALL_HOLE_WIDTH * COLS + 18 - w / 2, i * ROW_HEIGHT + 30);
                }

                for (int y = 0; y < ROWS; y++) {
                    for (int x = 0; x < COLS; x++) {
                        if (selectedColors[y][x] != NO_COLOR) {
                            g.drawImage(colorInImages[selectedColors[y][x]], 30 + 30 * x, rowImage.getHeight() * y + 13, null);
                        }
                    }
                }

                for (int y = 0; y < ROWS; y++) {
                    for (int x = 0; x < COLS; x++) {
                        if (matches[y][x] == EXACT_MATCH) {
                            g.drawImage(exactMatchImage, FIRST_HOLE_LEFT + HOLE_WIDTH * COLS + SMALL_HOLE_LEFT + MATCHES_LEFT + x * SMALL_HOLE_WIDTH, 12 + rowImage.getHeight() * y, null);
                        } else if (matches[y][x] == INEXACT_MATCH) {
                            g.drawImage(inexactMatchImage, FIRST_HOLE_LEFT + HOLE_WIDTH * COLS + SMALL_HOLE_LEFT + MATCHES_LEFT + x * SMALL_HOLE_WIDTH, 12 + rowImage.getHeight() * y, null);
                        }
                    }
                }

                for (int i = 0; i < colors.length; i++) {
                    if (colorOutHilight == i) {
                        g.drawImage(colorOutImages[i], getColorOutX(i), getColorOutY(i), null);
                    } else {
                        g.drawImage(colorOutDeskImages[i], getColorOutX(i), getColorOutY(i), null);
                    }
                }

                if (DEBUG_SHOW_SECRET) {
                    for (int x = 0; x < COLS; x++) {
                        g.drawImage(colorInImages[secretColors[x]], rowImage.getWidth() + 50 + x * colorInImage.getWidth(), 300, null);
                    }
                }
            }
        };
        MouseAdapter mouseAdapter = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (gamePaused) {
                    return;
                }
                if (e.getButton() == MouseEvent.BUTTON1) {
                    int selectedColor = colorOutHilight;
                    if (selectedColor != NO_COLOR) {
                        selectedColors[currentRow][currentCol] = selectedColor;
                        contentPanel.repaint();

                        int newCol = currentCol + 1;
                        int newRow = currentRow;
                        if (newCol == COLS) {
                            newCol = 0;
                            evaluate();
                            newRow++;
                            if (newRow == ROWS) {
                                newRow = 0;
                                gameOver();
                            }
                        }
                        currentCol = newCol;
                        currentRow = newRow;

                    }
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                int newHilight = NO_COLOR;

                if (!gamePaused) {
                    for (int i = 0; i < colors.length; i++) {
                        Rectangle r = new Rectangle(getColorOutX(i), getColorOutY(i), colorOutImage.getWidth(), colorOutImage.getHeight());
                        if (r.contains(e.getPoint())) {
                            newHilight = i;
                            break;
                        }
                    }
                }
                if (newHilight == NO_COLOR) {
                    contentPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                } else {
                    contentPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                }
                colorOutHilight = newHilight;
                contentPanel.repaint();
            }
        };
        contentPanel.addMouseMotionListener(mouseAdapter);
        contentPanel.addMouseListener(mouseAdapter);
        contentPanel.setPreferredSize(new Dimension(rowImage.getWidth(null) + 300, ROW_HEIGHT * ROWS));
        getContentPane().add(contentPanel);
        pack();
        setResizable(false);
        setTitle("Logik 2022");
    }

    private int getColorOutX(int i) {
        int radius = 60;
        int x = getRowWidth() + 80;
        int y = 64;

        x += radius * Math.sin(Math.toRadians(i * 360 / colors.length));

        return x;
    }
    
    private int getRowWidth() {
        return FIRST_HOLE_LEFT + HOLE_WIDTH * COLS + SMALL_HOLE_LEFT + SMALL_HOLE_WIDTH * COLS + SMALL_HOLE_RIGHT;
    }

    private int getColorOutY(int i) {
        int y = 64;
        int radius = 60;

        y += radius * Math.cos(Math.toRadians(i * 360 / colors.length));
        return y;
    }

    private static BufferedImage dye(BufferedImage image, Color color) {
        int w = image.getWidth();
        int h = image.getHeight();

        float[] hsbDye = new float[3];
        float[] hsb = new float[3];

        hsbDye = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsbDye);

        if (hsbDye[2] < 0.4f) {
            hsbDye[2] = 0.4f;
        }
        BufferedImage dyed = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int rgb = image.getRGB(x, y);
                int a = (rgb >> 24) & 0xff;

                hsb = Color.RGBtoHSB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, hsb);
                if (hsb[0] == 0.33333334f) {
                    hsb[0] = hsbDye[0];
                    hsb[1] = hsb[1] * hsbDye[1];
                    hsb[2] = hsb[2] * hsbDye[2];
                    rgb = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
                    rgb = rgb + (a << 24);
                }

                dyed.setRGB(x, y, rgb);

            }
        }
        return dyed;
    }

    private void newGame() {
        gamePaused = false;
        matches = new int[ROWS][COLS];

        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {
                selectedColors[y][x] = NO_COLOR;
                matches[y][x] = NO_MATCH;
            }
        }

        Random rnd = new Random();
        secretColors = new int[COLS];
        for (int i = 0; i < COLS; i++) {
            secretColors[i] = rnd.nextInt(colors.length);
        }
        currentCol = 0;
        currentRow = 0;

    }

    private void winGame() {
        JOptionPane.showMessageDialog(this, "Congratulation, you won!", "Win", JOptionPane.INFORMATION_MESSAGE);
        gamePaused = true;
    }

    private void gameOver() {
        JOptionPane.showMessageDialog(this, "Game over!", "Fail", JOptionPane.ERROR_MESSAGE);
        gamePaused = true;
    }

    private void evaluate() {

        boolean[] takenSecret = new boolean[COLS];
        boolean[] takenSelected = new boolean[COLS];

        //black
        int numExact = 0;
        for (int i = 0; i < COLS; i++) {
            if (selectedColors[currentRow][i] == secretColors[i]) {
                numExact++;
                takenSecret[i] = true;
                takenSelected[i] = true;
            }
        }

        //white
        int numInExact = 0;
        for (int i = 0; i < COLS; i++) {
            if (!takenSecret[i]) { //not exact match
                for (int j = 0; j < COLS; j++) {
                    if (!takenSelected[j]) {
                        if (selectedColors[currentRow][j] == secretColors[i]) {
                            numInExact++;
                            takenSecret[i] = true;
                            takenSelected[j] = true;
                            break;
                        }
                    }
                }
            }
        }

        for (int i = 0; i < numExact; i++) {
            matches[currentRow][i] = EXACT_MATCH;
        }
        for (int i = numExact; i < numExact + numInExact; i++) {
            matches[currentRow][i] = INEXACT_MATCH;
        }

        if (numExact == COLS) {
            contentPanel.repaint();
            winGame();
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.setProperty("sun.java2d.uiScale", "1.0");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException ignored) {
            //ignore
        }

        new Logik22().setVisible(true);
    }

}
