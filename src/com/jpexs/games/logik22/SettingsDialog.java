package com.jpexs.games.logik22;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author JPEXS
 */
public class SettingsDialog extends JDialog {

    private Settings settings;

    private static BufferedImage colorOutImages[];
    private static BufferedImage colorOutDeskImages[];
    private static BufferedImage colorInImages[];

    private static BufferedImage rowImages[];

    private Set<Color> selectedColors = new LinkedHashSet<>();

    private int selectedBackgroundColor = -1;

    private int hilightedColor = -1;

    public SettingsDialog(Settings settings) {
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        Container container = getContentPane();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        for (Color c : settings.colors) {
            selectedColors.add(c);
        }

        for (int i = 0; i < Settings.ALL_COLORS.length; i++) {
            if (Settings.ALL_COLORS[i].equals(settings.backgroundColor)) {
                selectedBackgroundColor = i;
                break;
            }
        }

        JPanel rowColsPanel = new JPanel(new FlowLayout());
        JLabel columnsLabel = new JLabel(Logik22.translate("settings.columns"));
        rowColsPanel.add(columnsLabel);

        final JTextField columnsField = new JTextField("" + settings.cols, 2);
        rowColsPanel.add(columnsField);

        JLabel rowsLabel = new JLabel(Logik22.translate("settings.rows"));
        rowColsPanel.add(rowsLabel);

        final JTextField rowsField = new JTextField("" + settings.rows, 2);
        rowColsPanel.add(rowsField);

        container.add(rowColsPanel);

        JLabel colorLabel = new JLabel(Logik22.translate("settings.colors"));
        colorLabel.setAlignmentX(0.5f);
        container.add(colorLabel);

        colorOutImages = new BufferedImage[Settings.ALL_COLORS.length];
        colorOutDeskImages = new BufferedImage[Settings.ALL_COLORS.length];
        colorInImages = new BufferedImage[Settings.ALL_COLORS.length];
        rowImages = new BufferedImage[Settings.ALL_COLORS.length];

        for (int i = 0; i < Settings.ALL_COLORS.length; i++) {
            colorOutImages[i] = Logik22.dye(Logik22.INSTANCE.getColorOutImage(), Settings.ALL_COLORS[i]);
            colorOutDeskImages[i] = Logik22.dye(Logik22.INSTANCE.getColorOutDeskImage(), Settings.ALL_COLORS[i]);
            colorInImages[i] = Logik22.dye(Logik22.INSTANCE.getColorInImage(), Settings.ALL_COLORS[i]);
            rowImages[i] = Logik22.dye(Logik22.INSTANCE.getDefaultRowImage(), Settings.ALL_COLORS[i]);
        }

        final JPanel colorsPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
                for (int i = 0; i < Settings.ALL_COLORS.length; i++) {
                    int x = i % 5;
                    int y = i / 5;
                    boolean isIn = selectedColors.contains(Settings.ALL_COLORS[i]);
                    Image img;
                    if (isIn) {
                        img = colorInImages[i];
                    } else {
                        if (hilightedColor == i) {
                            img = colorOutDeskImages[i];
                        } else {
                            img = colorOutImages[i];
                        }
                    }
                    g.drawImage(img, x * colorOutImages[i].getWidth() + (isIn ? 5 : 0), y * colorOutImages[i].getHeight() + (isIn ? 20 : 0), null);
                }

            }
        };
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (hilightedColor != -1) {
                    if (selectedColors.contains(Settings.ALL_COLORS[hilightedColor])) {
                        selectedColors.remove(Settings.ALL_COLORS[hilightedColor]);
                    } else {
                        selectedColors.add(Settings.ALL_COLORS[hilightedColor]);
                    }
                    colorsPanel.repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                int newHilightedColor = -1;
                int x = e.getX() / Logik22.INSTANCE.getColorOutImage().getWidth();
                int y = e.getY() / Logik22.INSTANCE.getColorOutImage().getHeight();
                newHilightedColor = y * 5 + x;
                if (newHilightedColor >= 0 && newHilightedColor < Settings.ALL_COLORS.length) {
                    if (newHilightedColor != hilightedColor) {
                        hilightedColor = newHilightedColor;
                        colorsPanel.repaint();
                    }
                }
            }

        };
        colorsPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        colorsPanel.addMouseMotionListener(mouseAdapter);
        colorsPanel.addMouseListener(mouseAdapter);
        colorsPanel.setPreferredSize(new Dimension(5 * Logik22.INSTANCE.getColorOutImage().getWidth(), Logik22.INSTANCE.getColorOutImage().getHeight() * (Settings.ALL_COLORS.length / 5)));
        container.add(colorsPanel);

        JLabel backgroundColorLabel = new JLabel(Logik22.translate("settings.background_color"));
        backgroundColorLabel.setAlignmentX(0.5f);
        container.add(backgroundColorLabel);

        final int BACKGROUND_INSET = 10;
        final int SELECTED_INSET = 5;

        JPanel backgroundColorsPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
                for (int i = 0; i < Settings.ALL_COLORS.length; i++) {
                    int x = i % 5;
                    int y = i / 5;
                    g.drawImage(rowImages[i],
                            BACKGROUND_INSET + x * (Logik22.HOLE_WIDTH + BACKGROUND_INSET), BACKGROUND_INSET + y * (Logik22.HOLE_WIDTH + BACKGROUND_INSET), BACKGROUND_INSET + x * (Logik22.HOLE_WIDTH + BACKGROUND_INSET) + Logik22.HOLE_WIDTH, BACKGROUND_INSET + y * (Logik22.HOLE_WIDTH + BACKGROUND_INSET) + Logik22.HOLE_WIDTH,
                            Logik22.FIRST_HOLE_LEFT, 12, Logik22.FIRST_HOLE_LEFT + Logik22.HOLE_WIDTH, 12 + Logik22.HOLE_WIDTH,
                            null);
                    if (selectedBackgroundColor == i) {
                        g.setColor(getForeground());
                        Graphics2D g2d=(Graphics2D)g;
                        g2d.setStroke(new BasicStroke(2));
                        g2d.drawRect(BACKGROUND_INSET + x * (Logik22.HOLE_WIDTH + BACKGROUND_INSET) - SELECTED_INSET, BACKGROUND_INSET + y * (Logik22.HOLE_WIDTH + BACKGROUND_INSET) - SELECTED_INSET, Logik22.HOLE_WIDTH + 2 * SELECTED_INSET, Logik22.HOLE_WIDTH + 2 * SELECTED_INSET);
                    }
                }
            }
        };
        backgroundColorsPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int newBackgroundColor = -1;
                int x = (e.getX()-BACKGROUND_INSET) / (Logik22.HOLE_WIDTH+BACKGROUND_INSET);
                int y = (e.getY() - BACKGROUND_INSET) / (Logik22.HOLE_WIDTH+BACKGROUND_INSET);
                newBackgroundColor = y * 5 + x;
                if (newBackgroundColor >= 0 && newBackgroundColor < Settings.ALL_COLORS.length) {
                    if (newBackgroundColor != selectedBackgroundColor) {
                        selectedBackgroundColor = newBackgroundColor;
                        backgroundColorsPanel.repaint();
                    }
                }
            }            
        });
        backgroundColorsPanel.setPreferredSize(new Dimension(BACKGROUND_INSET + 5 * (Logik22.HOLE_WIDTH + BACKGROUND_INSET), BACKGROUND_INSET + (int) Math.ceil(Settings.ALL_COLORS.length / 5.0) * (Logik22.HOLE_WIDTH + BACKGROUND_INSET)));
        backgroundColorsPanel.setMaximumSize(backgroundColorsPanel.getPreferredSize());
        backgroundColorsPanel.setAlignmentX(0.5f);
        backgroundColorsPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        container.add(backgroundColorsPanel);

        JPanel panelButtons = new JPanel(new FlowLayout());
        JButton okButton = new JButton(Logik22.translate("settings.ok"));
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Settings settings = new Settings();
                try {
                    settings.cols = Integer.parseInt(columnsField.getText());
                    if (settings.cols <= 2) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(SettingsDialog.this, Logik22.translate("settings.invalid.columns"), Logik22.translate("settings.error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    settings.rows = Integer.parseInt(rowsField.getText());
                    if (settings.rows <= 0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(SettingsDialog.this, Logik22.translate("settings.invalid.rows"), Logik22.translate("settings.error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (selectedColors.size() < 2) {
                    JOptionPane.showMessageDialog(SettingsDialog.this, Logik22.translate("settings.invalid.colors"), Logik22.translate("settings.error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
                settings.colors = new Color[selectedColors.size()];
                int i = 0;
                for (Color c : selectedColors) {
                    settings.colors[i] = c;
                    i++;
                }
                
                settings.backgroundColor = Settings.ALL_COLORS[selectedBackgroundColor];

                SettingsDialog.this.settings = settings;
                setVisible(false);
            }
        });
        JButton cancelButton = new JButton(Logik22.translate("settings.cancel"));
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        panelButtons.add(okButton);
        panelButtons.add(cancelButton);

        container.add(panelButtons);
        setModal(true);
        pack();
        setTitle(Logik22.translate("settings.title"));
        Logik22.centerWindow(this);
        Logik22.setWindowIcon(this);
    }

    public Settings getSettings() {
        return settings;
    }

}
