package com.jpexs.games.logik22;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
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
import javax.swing.JScrollPane;
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

    private static Image colorOutImages[];
    private static Image colorInImages[];
    private static Image colorOutDeskImages[];

    private static final int ROW_HEIGHT = 50;
    private JPanel contentPanel;

    private Settings settings = new Settings();

    private int colorOutHilight = -1;

    private int selectedColors[][] = new int[settings.rows][settings.cols];

    private final int NO_COLOR = -1;

    private int currentCol = 0;
    private int currentRow = 0;

    private final String RESOURCE_PATH = "/com/jpexs/games/logik22/graphics";

    private int[] secretColors;

    private int[][] matches = new int[settings.rows][settings.cols];

    private final int NO_MATCH = 0;
    private final int INEXACT_MATCH = 1;
    private final int EXACT_MATCH = 2;

    private boolean gamePaused = false;

    private final boolean DEBUG_SHOW_SECRET = false;

    private final int HOLE_WIDTH = 30;

    private final int FIRST_HOLE_LEFT = 30;

    private final int SMALL_HOLE_WIDTH = 15;

    private final int SMALL_HOLE_LEFT = 12;

    private final int SMALL_HOLE_RIGHT = 32;

    private final int MATCHES_LEFT = 3;

    public final int IMAGE_COLS = 5;

    public static Logik22 INSTANCE;

    private JScrollPane scrollPane;

    public Logik22() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);

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
        contentPanel = new JPanel() {

            @Override
            public void paintComponent(Graphics g) {

                Color fillColor = new Color(rowImage.getRGB(0, 0));
                g.setColor(fillColor);
                g.fillRect(0, 0, getWidth(), getHeight());
                for (int i = 0; i < settings.rows; i++) {
                    g.drawImage(rowImage, 0, i * ROW_HEIGHT, FIRST_HOLE_LEFT, (i + 1) * ROW_HEIGHT,
                            0, 0, FIRST_HOLE_LEFT, ROW_HEIGHT, null);
                    for (int j = 0; j < settings.cols - 1; j++) {
                        g.drawImage(rowImage, FIRST_HOLE_LEFT + HOLE_WIDTH * j, i * ROW_HEIGHT, FIRST_HOLE_LEFT + HOLE_WIDTH * (j + 1), (i + 1) * ROW_HEIGHT,
                                FIRST_HOLE_LEFT, 0, FIRST_HOLE_LEFT + HOLE_WIDTH, ROW_HEIGHT, null);
                    }
                    //last col
                    g.drawImage(rowImage, FIRST_HOLE_LEFT + HOLE_WIDTH * (settings.cols - 1), i * ROW_HEIGHT, FIRST_HOLE_LEFT + HOLE_WIDTH * settings.cols, (i + 1) * ROW_HEIGHT,
                            FIRST_HOLE_LEFT + HOLE_WIDTH * (IMAGE_COLS - 1), 0, FIRST_HOLE_LEFT + HOLE_WIDTH * IMAGE_COLS, ROW_HEIGHT, null);

                    g.drawImage(rowImage, FIRST_HOLE_LEFT + HOLE_WIDTH * settings.cols, i * ROW_HEIGHT, FIRST_HOLE_LEFT + HOLE_WIDTH * settings.cols + SMALL_HOLE_LEFT, (i + 1) * ROW_HEIGHT,
                            FIRST_HOLE_LEFT + HOLE_WIDTH * IMAGE_COLS, 0, FIRST_HOLE_LEFT + HOLE_WIDTH * IMAGE_COLS + SMALL_HOLE_LEFT, ROW_HEIGHT, null);

                    for (int j = 0; j < settings.cols - 1; j++) {
                        g.drawImage(rowImage, FIRST_HOLE_LEFT + HOLE_WIDTH * settings.cols + SMALL_HOLE_LEFT + SMALL_HOLE_WIDTH * j, i * ROW_HEIGHT, FIRST_HOLE_LEFT + HOLE_WIDTH * settings.cols + SMALL_HOLE_LEFT + SMALL_HOLE_WIDTH * (j + 1), (i + 1) * ROW_HEIGHT,
                                FIRST_HOLE_LEFT + HOLE_WIDTH * IMAGE_COLS + SMALL_HOLE_LEFT, 0, FIRST_HOLE_LEFT + HOLE_WIDTH * IMAGE_COLS + SMALL_HOLE_LEFT + SMALL_HOLE_WIDTH, ROW_HEIGHT, null);
                    }
                    //last col
                    g.drawImage(rowImage, FIRST_HOLE_LEFT + HOLE_WIDTH * settings.cols + SMALL_HOLE_LEFT + SMALL_HOLE_WIDTH * (settings.cols - 1), i * ROW_HEIGHT, FIRST_HOLE_LEFT + HOLE_WIDTH * settings.cols + SMALL_HOLE_LEFT + SMALL_HOLE_WIDTH * settings.cols, (i + 1) * ROW_HEIGHT,
                            FIRST_HOLE_LEFT + HOLE_WIDTH * IMAGE_COLS + SMALL_HOLE_LEFT + SMALL_HOLE_WIDTH * (IMAGE_COLS - 1), 0, FIRST_HOLE_LEFT + HOLE_WIDTH * IMAGE_COLS + SMALL_HOLE_LEFT + SMALL_HOLE_WIDTH * IMAGE_COLS, ROW_HEIGHT, null);
                    g.drawImage(rowImage, FIRST_HOLE_LEFT + HOLE_WIDTH * settings.cols + SMALL_HOLE_LEFT + SMALL_HOLE_WIDTH * settings.cols, i * ROW_HEIGHT, FIRST_HOLE_LEFT + HOLE_WIDTH * settings.cols + SMALL_HOLE_LEFT + SMALL_HOLE_WIDTH * settings.cols + SMALL_HOLE_RIGHT, (i + 1) * ROW_HEIGHT,
                            FIRST_HOLE_LEFT + HOLE_WIDTH * IMAGE_COLS + SMALL_HOLE_LEFT + SMALL_HOLE_WIDTH * IMAGE_COLS, 0, FIRST_HOLE_LEFT + HOLE_WIDTH * IMAGE_COLS + SMALL_HOLE_LEFT + SMALL_HOLE_WIDTH * IMAGE_COLS + SMALL_HOLE_RIGHT, ROW_HEIGHT, null);

                    g.setFont(g.getFont().deriveFont(8f));
                    g.setColor(Color.black);
                    int w = g.getFontMetrics().stringWidth("" + (i + 1));
                    g.drawString("" + (i + 1), 14 - w / 2, i * ROW_HEIGHT + 30);
                    g.drawString("" + (i + 1), FIRST_HOLE_LEFT + HOLE_WIDTH * settings.cols + SMALL_HOLE_LEFT + SMALL_HOLE_WIDTH * settings.cols + 18 - w / 2, i * ROW_HEIGHT + 30);
                }

                for (int y = 0; y < settings.rows; y++) {
                    for (int x = 0; x < settings.cols; x++) {
                        if (selectedColors[y][x] != NO_COLOR) {
                            g.drawImage(colorInImages[selectedColors[y][x]], 30 + 30 * x, rowImage.getHeight() * y + 13, null);
                        }
                    }
                }

                for (int y = 0; y < settings.rows; y++) {
                    for (int x = 0; x < settings.cols; x++) {
                        if (matches[y][x] == EXACT_MATCH) {
                            g.drawImage(exactMatchImage, FIRST_HOLE_LEFT + HOLE_WIDTH * settings.cols + SMALL_HOLE_LEFT + MATCHES_LEFT + x * SMALL_HOLE_WIDTH, 12 + rowImage.getHeight() * y, null);
                        } else if (matches[y][x] == INEXACT_MATCH) {
                            g.drawImage(inexactMatchImage, FIRST_HOLE_LEFT + HOLE_WIDTH * settings.cols + SMALL_HOLE_LEFT + MATCHES_LEFT + x * SMALL_HOLE_WIDTH, 12 + rowImage.getHeight() * y, null);
                        }
                    }
                }

                for (int i = 0; i < settings.colors.length; i++) {
                    if (colorOutHilight == i) {
                        g.drawImage(colorOutImages[i], getColorOutX(i), getColorOutY(i), null);
                    } else {
                        g.drawImage(colorOutDeskImages[i], getColorOutX(i), getColorOutY(i), null);
                    }
                }

                if (DEBUG_SHOW_SECRET) {
                    for (int x = 0; x < settings.cols; x++) {
                        g.drawImage(colorInImages[secretColors[x]], rowImage.getWidth() + 50 + x * colorInImage.getWidth(), 300, null);
                    }
                }
            }
        };
        newGame();

        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");
        JMenuItem newGameMenuItem = new JMenuItem("New game");
        JMenuItem settingsMenuItem = new JMenuItem("Settings");
        JMenuItem exitGameMenuItem = new JMenuItem("Exit game");

        newGameMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newGame();
                contentPanel.repaint();
            }
        });

        settingsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SettingsDialog settingsDialog = new SettingsDialog(settings);
                settingsDialog.setVisible(true);
                Settings newSettings = settingsDialog.getSettings();
                if (newSettings != null) {
                    Logik22.this.settings = newSettings;
                    newGame();
                }
            }
        });

        exitGameMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        gameMenu.add(newGameMenuItem);
        gameMenu.add(settingsMenuItem);
        gameMenu.add(exitGameMenuItem);
        menuBar.add(gameMenu);

        setJMenuBar(menuBar);

        rowImage = dye(rowImage, BACKGROUND_COLOR);

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
                        if (newCol == settings.cols) {
                            newCol = 0;
                            evaluate();
                            newRow++;
                            if (newRow == settings.rows) {
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
                    for (int i = 0; i < settings.colors.length; i++) {
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
        scrollPane = new JScrollPane(contentPanel);
        getContentPane().add(scrollPane);
        pack();
        //setResizable(false);        
        setTitle("Logik 2022");
    }

    private int getColorOutX(int i) {
        int radius = 60 + 2 * settings.colors.length;
        int x = getRowWidth() + 80;
        int y = 64;

        x += radius * Math.sin(Math.toRadians(i * 360 / settings.colors.length));

        return x;
    }

    private int getRowWidth() {
        return FIRST_HOLE_LEFT + HOLE_WIDTH * settings.cols + SMALL_HOLE_LEFT + SMALL_HOLE_WIDTH * settings.cols + SMALL_HOLE_RIGHT;
    }

    private int getColorOutY(int i) {
        int y = 200;
        int radius = 60 + 2 * settings.colors.length;

        y += radius * Math.cos(Math.toRadians(i * 360 / settings.colors.length));
        return y;
    }

    public static BufferedImage dye(BufferedImage image, Color color) {
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
        colorOutImages = new Image[settings.colors.length];
        colorOutDeskImages = new Image[settings.colors.length];
        colorInImages = new Image[settings.colors.length];

        for (int i = 0; i < settings.colors.length; i++) {
            colorOutImages[i] = dye(colorOutImage, settings.colors[i]);
            colorOutDeskImages[i] = dye(colorOutDeskImage, settings.colors[i]);
            colorInImages[i] = dye(colorInImage, settings.colors[i]);
        }

        selectedColors = new int[settings.rows][settings.cols];
        matches = new int[settings.rows][settings.cols];

        for (int x = 0; x < settings.cols; x++) {
            for (int y = 0; y < settings.rows; y++) {
                selectedColors[y][x] = NO_COLOR;
                matches[y][x] = NO_MATCH;
            }
        }

        Random rnd = new Random();
        secretColors = new int[settings.cols];
        for (int i = 0; i < settings.cols; i++) {
            secretColors[i] = rnd.nextInt(settings.colors.length);
        }
        currentCol = 0;
        currentRow = 0;

        contentPanel.setPreferredSize(new Dimension(getRowWidth() + 250, ROW_HEIGHT * settings.rows));
        if (scrollPane != null) {
            scrollPane.revalidate();
            scrollPane.repaint();
        }
        gamePaused = false;
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

        boolean[] takenSecret = new boolean[settings.cols];
        boolean[] takenSelected = new boolean[settings.cols];

        //black
        int numExact = 0;
        for (int i = 0; i < settings.cols; i++) {
            if (selectedColors[currentRow][i] == secretColors[i]) {
                numExact++;
                takenSecret[i] = true;
                takenSelected[i] = true;
            }
        }

        //white
        int numInExact = 0;
        for (int i = 0; i < settings.cols; i++) {
            if (!takenSecret[i]) { //not exact match
                for (int j = 0; j < settings.cols; j++) {
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

        if (numExact == settings.cols) {
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

        INSTANCE = new Logik22();
        INSTANCE.setVisible(true);
        centerWindow(INSTANCE);
    }

    public static void centerWindow(Window f) {
        GraphicsDevice[] allDevices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        int topLeftX, topLeftY, screenX, screenY, windowPosX, windowPosY;

        int screen = 0;

        if (screen < allDevices.length && screen > -1) {
            topLeftX = allDevices[screen].getDefaultConfiguration().getBounds().x;
            topLeftY = allDevices[screen].getDefaultConfiguration().getBounds().y;

            screenX = allDevices[screen].getDefaultConfiguration().getBounds().width;
            screenY = allDevices[screen].getDefaultConfiguration().getBounds().height;

            Insets bounds = Toolkit.getDefaultToolkit().getScreenInsets(allDevices[screen].getDefaultConfiguration());
            screenX = screenX - bounds.right;
            screenY = screenY - bounds.bottom;
        } else {
            topLeftX = allDevices[0].getDefaultConfiguration().getBounds().x;
            topLeftY = allDevices[0].getDefaultConfiguration().getBounds().y;

            screenX = allDevices[0].getDefaultConfiguration().getBounds().width;
            screenY = allDevices[0].getDefaultConfiguration().getBounds().height;

            Insets bounds = Toolkit.getDefaultToolkit().getScreenInsets(allDevices[0].getDefaultConfiguration());
            screenX = screenX - bounds.right;
            screenY = screenY - bounds.bottom;
        }

        windowPosX = ((screenX - f.getWidth()) / 2) + topLeftX;
        windowPosY = ((screenY - f.getHeight()) / 2) + topLeftY;

        f.setLocation(windowPosX, windowPosY);
    }

    public BufferedImage getColorInImage() {
        return colorInImage;
    }

    public BufferedImage getColorOutImage() {
        return colorOutImage;
    }

    public BufferedImage getColorOutDeskImage() {
        return colorOutDeskImage;
    }

}
