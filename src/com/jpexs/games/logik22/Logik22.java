package com.jpexs.games.logik22;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author JPEXS
 */
public class Logik22 extends JFrame {

    private static final String CONFIG_NAME = "config.bin";

    private BufferedImage defaultRowImage;
    private BufferedImage defaultSelectionImage;
    private BufferedImage rowImage;
    private BufferedImage colorOutImage;
    private BufferedImage colorInImage;
    private BufferedImage colorOutDeskImage;
    private BufferedImage exactMatchImage;
    private BufferedImage inexactMatchImage;
    private BufferedImage selectionImage;
    private BufferedImage enterImage;
    private BufferedImage enterSmallImage;
    private BufferedImage enterHilightedImage;
    private BufferedImage emptyImage;
    private BufferedImage emptyHilightedImage;

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

    public static final String RESOURCE_PATH = "/com/jpexs/games/logik22/graphics";

    private int[] secretColors;

    private int[][] matches = new int[settings.rows][settings.cols];

    private final int NO_MATCH = 0;
    private final int INEXACT_MATCH = 1;
    private final int EXACT_MATCH = 2;

    private boolean gamePaused = false;

    private final boolean DEBUG_SHOW_SECRET = false;

    public static final int HOLE_WIDTH = 30;

    public static final int FIRST_HOLE_LEFT = 30;

    public static final int SMALL_HOLE_WIDTH = 15;

    public static final int SMALL_HOLE_LEFT = 12;

    public static final int SMALL_HOLE_RIGHT = 32;

    private final int MATCHES_LEFT = 3;

    public final int IMAGE_COLS = 5;

    public static Logik22 INSTANCE;

    private JScrollPane scrollPane;

    private boolean enterClicked = false;
    private boolean enterHilighted = false;
    private boolean emptyHilighted = false;

    private JLabel statusLabel;

    public Logik22() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        loadConfig();

        try {
            defaultRowImage = ImageIO.read(getClass().getResource(RESOURCE_PATH + "/row.png"));
            colorInImage = ImageIO.read(getClass().getResource(RESOURCE_PATH + "/colorin.png"));
            colorOutImage = ImageIO.read(getClass().getResource(RESOURCE_PATH + "/colorout.png"));
            colorOutDeskImage = ImageIO.read(getClass().getResource(RESOURCE_PATH + "/coloroutdesk.png"));
            exactMatchImage = ImageIO.read(getClass().getResource(RESOURCE_PATH + "/black.png"));
            inexactMatchImage = ImageIO.read(getClass().getResource(RESOURCE_PATH + "/white.png"));
            defaultSelectionImage = ImageIO.read(getClass().getResource(RESOURCE_PATH + "/selection.png"));
            enterImage = ImageIO.read(getClass().getResource(RESOURCE_PATH + "/enter.png"));
            enterSmallImage = ImageIO.read(getClass().getResource(RESOURCE_PATH + "/entersmall.png"));
            enterHilightedImage = ImageIO.read(getClass().getResource(RESOURCE_PATH + "/enterhilight.png"));

            emptyImage = ImageIO.read(getClass().getResource(RESOURCE_PATH + "/empty.png"));
            emptyHilightedImage = ImageIO.read(getClass().getResource(RESOURCE_PATH + "/emptyhilight.png"));
            setIconImage(ImageIO.read(getClass().getResource(RESOURCE_PATH + "/icon.png")));

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

                g.setFont(g.getFont().deriveFont(20f));
                g.drawString(translate("game.pick_color"), FIRST_HOLE_LEFT + HOLE_WIDTH * settings.cols + SMALL_HOLE_LEFT + SMALL_HOLE_WIDTH * settings.cols + SMALL_HOLE_RIGHT + 70, 110);

                g.setFont(g.getFont().deriveFont(15f));
                g.drawString(translate("game.legend.black_pin"), FIRST_HOLE_LEFT + HOLE_WIDTH * settings.cols + SMALL_HOLE_LEFT + SMALL_HOLE_WIDTH * settings.cols + SMALL_HOLE_RIGHT + 30, 370);
                g.drawString(translate("game.legend.white_pin"), FIRST_HOLE_LEFT + HOLE_WIDTH * settings.cols + SMALL_HOLE_LEFT + SMALL_HOLE_WIDTH * settings.cols + SMALL_HOLE_RIGHT + 30, 400);

                if (!gamePaused) {
                    g.drawImage(selectionImage, FIRST_HOLE_LEFT + currentCol * HOLE_WIDTH - 5, rowImage.getHeight() * currentRow + 8, null);
                }

                for (int i = 0; i < settings.colors.length; i++) {
                    if (colorOutHilight == i) {
                        g.drawImage(colorOutImages[i], getColorOutX(i), getColorOutY(i), null);
                    } else {
                        g.drawImage(colorOutDeskImages[i], getColorOutX(i), getColorOutY(i), null);
                    }
                }

                final int enterLeft = getRowWidth() + 270;

                if (enterClicked) {
                    g.drawImage(enterSmallImage, enterLeft + 10, 205, null);
                } else if (enterHilighted) {
                    g.drawImage(enterHilightedImage, enterLeft, 200, null);
                } else {
                    g.drawImage(enterImage, enterLeft, 200, null);
                }

                if (emptyHilighted) {
                    g.drawImage(emptyHilightedImage, getRowWidth()+95, 200, null);
                } else {
                    g.drawImage(emptyImage, getRowWidth() + 100, 205, null);
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
        JMenu gameMenu = new JMenu(translate("menu_game"));
        JMenuItem newGameMenuItem = new JMenuItem(translate("menu_game_new"));
        JMenuItem settingsMenuItem = new JMenuItem(translate("menu_game_settings"));
        JMenuItem exitGameMenuItem = new JMenuItem(translate("menu_game_exit"));

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
                    saveConfig();
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

        JMenu helpMenu = new JMenu(translate("menu_help"));
        JMenuItem aboutMenuItem = new JMenuItem(translate("menu_help_about"));
        aboutMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new AboutDialog().setVisible(true);
            }
        });
        helpMenu.add(aboutMenuItem);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        MouseAdapter mouseAdapter = new MouseAdapter() {

            private int hilightFieldColumn = -1;

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (enterClicked) {
                        enterClicked = false;
                        contentPanel.repaint();
                        finishRow();
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (gamePaused) {
                    return;
                }
                if (e.getButton() == MouseEvent.BUTTON1) {

                    if (enterHilighted) {
                        enterClicked = true;
                        contentPanel.repaint();
                    }
                    if (hilightFieldColumn != -1) {
                        currentCol = hilightFieldColumn;
                        contentPanel.repaint();
                    }
                    int selectedColor = colorOutHilight;
                    if (selectedColor != NO_COLOR || emptyHilighted) {
                        selectedColors[currentRow][currentCol] = selectedColor;
                        contentPanel.repaint();

                        int newCol = currentCol + 1;
                        while (newCol < settings.cols && selectedColors[currentRow][newCol] != NO_COLOR) {
                            newCol++;
                        }
                        if (newCol == settings.cols) {
                            newCol = 0;
                            boolean processNewRow = true;
                            for (int x = 0; x < settings.cols; x++) {
                                if (selectedColors[currentRow][x] == NO_COLOR) {
                                    newCol = x;
                                    processNewRow = false;
                                    break;
                                }
                            }
                            if (processNewRow) {
                                finishRow();
                            }
                        }
                        currentCol = newCol;

                    }
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {

                if (!gamePaused) {
                    for (int x = 0; x < settings.cols; x++) {
                        Rectangle fieldRect = new Rectangle(FIRST_HOLE_LEFT + HOLE_WIDTH * x, ROW_HEIGHT * currentRow + 10, HOLE_WIDTH, ROW_HEIGHT - 15);
                        if (fieldRect.contains(e.getPoint())) {
                            setStatus(translate("hint.selectfield"));
                            contentPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                            hilightFieldColumn = x;
                            return;
                        }
                    }
                    hilightFieldColumn = -1;
                }

                if (!gamePaused) {
                    Rectangle enterRect = new Rectangle(getRowWidth() + 270, 200, enterImage.getWidth(), enterImage.getHeight());
                    enterHilighted = enterRect.contains(e.getPoint());
                    if (enterHilighted) {
                        setStatus(translate("hint.enter"));
                        emptyHilighted = false;
                        colorOutHilight = NO_COLOR;
                        contentPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        contentPanel.repaint();
                        return;
                    }
                }
               
                if (!gamePaused) {
                    Rectangle emptyRect = new Rectangle(getRowWidth()+100, 205, emptyImage.getWidth(), emptyImage.getHeight());
                    emptyHilighted = emptyRect.contains(e.getPoint());

                    if (emptyHilighted) {
                        setStatus(translate("hint.empty"));
                        colorOutHilight = NO_COLOR;
                        contentPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        contentPanel.repaint();
                        return;
                    }
                }
                
                if (!gamePaused) {
                    loopx:for (int x=0; x< settings.cols;x++){
                        for(int y=0;y<currentRow;y++){
                            Rectangle pinRect = new Rectangle(FIRST_HOLE_LEFT + settings.cols * HOLE_WIDTH + SMALL_HOLE_LEFT + x * SMALL_HOLE_WIDTH,
                            y * ROW_HEIGHT, SMALL_HOLE_WIDTH, ROW_HEIGHT);
                            if (pinRect.contains(e.getPoint())){
                                if (matches[y][x] == EXACT_MATCH){
                                    setStatus(translate("hint.black"));
                                    return;
                                }else if (matches[y][x] == INEXACT_MATCH){
                                    setStatus(translate("hint.white"));
                                    return;
                                }                                                                      
                            }
                        }
                    }
                }

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
                    if (!gamePaused) {
                        noStatus();
                    }
                } else {
                    contentPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    setStatus(translate("hint.selectcolor"));
                }
                colorOutHilight = newHilight;
                contentPanel.repaint();
            }
        };
        contentPanel.addMouseMotionListener(mouseAdapter);
        contentPanel.addMouseListener(mouseAdapter);
        scrollPane = new JScrollPane(contentPanel);
        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        container.add(scrollPane, BorderLayout.CENTER);

        JPanel statusPanel = new JPanel();
        statusPanel.setPreferredSize(new Dimension(getWidth(), 16));
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
        statusLabel = new JLabel();
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        statusPanel.add(statusLabel);
        container.add(statusPanel, BorderLayout.SOUTH);

        pack();
        //setResizable(false);        
        setTitle(translate("title"));
    }

    private void setStatus(String status) {
        if (statusLabel != null) {
            statusLabel.setText(status);
        }
    }

    private void noStatus() {
        setStatus(" ");
    }

    private void finishRow() {
        int newRow = currentRow;
        evaluate();
        if (gamePaused) { //won
            return;
        }
        newRow++;
        if (newRow == settings.rows) {
            newRow = 0;
            pauseGame();
            contentPanel.repaint();
            gameOver();
        }
        currentRow = newRow;
        currentCol = 0;
    }

    private int getColorOutX(int i) {
        int radius = 70 + 2 * settings.colors.length;
        int x = getRowWidth() + 100;
        int y = 64;

        x += radius * Math.sin(Math.toRadians(i * 360 / settings.colors.length));

        return x;
    }

    private int getColorOutY(int i) {
        int y = 200;
        int radius = 70 + 2 * settings.colors.length;

        y += radius * Math.cos(Math.toRadians(i * 360 / settings.colors.length));
        return y;
    }

    private int getRowWidth() {
        return FIRST_HOLE_LEFT + HOLE_WIDTH * settings.cols + SMALL_HOLE_LEFT + SMALL_HOLE_WIDTH * settings.cols + SMALL_HOLE_RIGHT;
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
        noStatus();
        rowImage = dye(defaultRowImage, settings.backgroundColor);
        selectionImage = dye(defaultSelectionImage, settings.backgroundColor);

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

        contentPanel.setPreferredSize(new Dimension(getRowWidth() + 350, ROW_HEIGHT * settings.rows));
        if (scrollPane != null) {
            scrollPane.revalidate();
            scrollPane.repaint();
        }
        gamePaused = false;
    }

    private void winGame() {
        setStatus(translate("hint.win"));
        JOptionPane.showMessageDialog(this, translate("game.win.message"), translate("game.win.title"), JOptionPane.INFORMATION_MESSAGE);        
    }

    private void gameOver() {
        setStatus(translate("hint.lose"));
        JOptionPane.showMessageDialog(this, translate("game.lose.message"), translate("game.lose.title"), JOptionPane.ERROR_MESSAGE);        
    }

    private void saveConfig() {
        File configFile = getConfigFile();
        try (FileOutputStream fos = new FileOutputStream(configFile);
                ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(settings);
        } catch (Exception ex) {
            //ignore
        }
    }

    private void loadConfig() {
        File configFile = getConfigFile();
        if (!configFile.exists()) {
            return;
        }
        try (FileInputStream fis = new FileInputStream(configFile);
                ObjectInputStream ois = new ObjectInputStream(fis);) {
            Settings newSettings = (Settings) ois.readObject();
            if (newSettings.settingsVersion == Settings.DEFAULT_SETTINGS_VERSION) {
                settings = newSettings;
            }
        } catch (Exception ex) {
            //ignore
        }
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
            pauseGame();
            contentPanel.repaint();
            winGame();
        }
    }

    private void pauseGame() {
        enterHilighted = false;
        enterClicked = false;
        emptyHilighted = false;
        gamePaused = true;
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

    public static void setWindowIcon(Window f) {
        try {
            f.setIconImage(ImageIO.read(Logik22.class.getResource(RESOURCE_PATH + "/icon.png")));
        } catch (IOException ex) {
            Logger.getLogger(SettingsDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
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

    public BufferedImage getDefaultRowImage() {
        return defaultRowImage;
    }

    public static String translate(String key) {
        ResourceBundle mybundle = ResourceBundle.getBundle(Logik22.class.getName());
        return mybundle.getString(key);
    }

    private static final File unspecifiedFile = new File("unspecified");

    private static File homeDirectory = unspecifiedFile;

    private enum OSId {
        WINDOWS, OSX, UNIX
    }

    private static OSId getOSId() {
        PrivilegedAction<String> doGetOSName = new PrivilegedAction<String>() {
            @Override
            public String run() {
                return System.getProperty("os.name");
            }
        };
        OSId id = OSId.UNIX;
        String osName = AccessController.doPrivileged(doGetOSName);
        if (osName != null) {
            if (osName.toLowerCase().startsWith("mac os x")) {
                id = OSId.OSX;
            } else if (osName.contains("Windows")) {
                id = OSId.WINDOWS;
            }
        }
        return id;
    }

    public static File getConfigFile() {
        String homeDir = getHomeDir();
        return new File(homeDir + CONFIG_NAME);
    }

    public static String getHomeDir() {
        if (homeDirectory == unspecifiedFile) {
            homeDirectory = null;
            String userHome = null;
            try {
                userHome = System.getProperty("user.home");
            } catch (SecurityException ignore) {
            }
            if (userHome != null) {
                String applicationId = AboutDialog.SHORT_APPLICATION_NAME;
                OSId osId = getOSId();
                if (osId == OSId.WINDOWS) {
                    File appDataDir = null;
                    try {
                        String appDataEV = System.getenv("APPDATA");
                        if ((appDataEV != null) && (appDataEV.length() > 0)) {
                            appDataDir = new File(appDataEV);
                        }
                    } catch (SecurityException ignore) {
                    }
                    String vendorId = AboutDialog.VENDOR_ID;
                    if ((appDataDir != null) && appDataDir.isDirectory()) {
                        // ${APPDATA}\{vendorId}\${applicationId}
                        String path = vendorId + "\\" + applicationId + "\\";
                        homeDirectory = new File(appDataDir, path);
                    } else {
                        // ${userHome}\Application Data\${vendorId}\${applicationId}
                        String path = "Application Data\\" + vendorId + "\\" + applicationId + "\\";
                        homeDirectory = new File(userHome, path);
                    }
                } else if (osId == OSId.OSX) {
                    // ${userHome}/Library/Application Support/${applicationId}
                    String path = "Library/Application Support/" + applicationId + "/";
                    homeDirectory = new File(userHome, path);
                } else {
                    // ${userHome}/.${applicationId}/
                    String path = "." + applicationId + "/";
                    homeDirectory = new File(userHome, path);
                }
            } else {
                //no home, then use application directory
                homeDirectory = new File(".");
            }
        }
        if (!homeDirectory.exists()) {
            if (!homeDirectory.mkdirs()) {
                if (!homeDirectory.exists()) {
                    homeDirectory = new File("."); //fallback to current directory
                }
            }
        }
        String ret = homeDirectory.getAbsolutePath();
        if (!ret.endsWith(File.separator)) {
            ret += File.separator;
        }
        return ret;
    }
}
