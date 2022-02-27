package com.jpexs.games.logik22;

import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 *
 * @author JPEXS
 */
public class SettingsDialog extends JDialog {

    private Settings settings;

    private static BufferedImage colorOutImages[];
    private static BufferedImage colorOutDeskImages[];
    private static BufferedImage colorInImages[];

    private Set<Color> selectedColors = new LinkedHashSet<>();

    private int hilightedColor = -1;

    public SettingsDialog(Settings settings) {
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        Container container = getContentPane();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        for (Color c : settings.colors) {
            selectedColors.add(c);
        }

        JPanel rowColsPanel = new JPanel(new FlowLayout());
        JLabel columnsLabel = new JLabel("Columns:");
        rowColsPanel.add(columnsLabel);

        final JTextField columnsField = new JTextField("" + settings.cols, 2);
        rowColsPanel.add(columnsField);

        JLabel rowsLabel = new JLabel("Rows:");
        rowColsPanel.add(rowsLabel);

        final JTextField rowsField = new JTextField("" + settings.rows, 2);
        rowColsPanel.add(rowsField);

        container.add(rowColsPanel);
        
        JLabel colorLabel = new JLabel("Used colors:");
        colorLabel.setAlignmentX(0.5f);
        container.add(colorLabel);
        
        colorOutImages = new BufferedImage[Settings.ALL_COLORS.length];
        colorOutDeskImages = new BufferedImage[Settings.ALL_COLORS.length];
        colorInImages = new BufferedImage[Settings.ALL_COLORS.length];

        for (int i = 0; i < Settings.ALL_COLORS.length; i++) {
            colorOutImages[i] = Logik22.dye(Logik22.INSTANCE.getColorOutImage(), Settings.ALL_COLORS[i]);
            colorOutDeskImages[i] = Logik22.dye(Logik22.INSTANCE.getColorOutDeskImage(), Settings.ALL_COLORS[i]);
            colorInImages[i] = Logik22.dye(Logik22.INSTANCE.getColorInImage(), Settings.ALL_COLORS[i]);
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
                    g.drawImage(img, x * colorOutImages[i].getWidth()+(isIn ? 5 : 0), y * colorOutImages[i].getHeight() + (isIn ? 20 : 0), null);
                }

            }
        };
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (hilightedColor != -1) {
                    if (selectedColors.contains(Settings.ALL_COLORS[hilightedColor])) {
                        selectedColors.remove(Settings.ALL_COLORS[hilightedColor]);
                    }else{
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

        JPanel panelButtons = new JPanel(new FlowLayout());
        JButton okButton = new JButton("OK");
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
                    JOptionPane.showMessageDialog(SettingsDialog.this, "Invalid columns value", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    settings.rows = Integer.parseInt(rowsField.getText());
                    if (settings.rows <= 0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(SettingsDialog.this, "Invalid rows value", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if(selectedColors.size() < 2) {
                    JOptionPane.showMessageDialog(SettingsDialog.this, "Invalid selected colors", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                settings.colors = new Color[selectedColors.size()];
                int i=0;
                for(Color c:selectedColors){
                    settings.colors[i] = c;
                    i++;
                }

                SettingsDialog.this.settings = settings;
                setVisible(false);
            }
        });
        JButton cancelButton = new JButton("Cancel");
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
        setTitle("Settings");
        Logik22.centerWindow(this);
    }

    public Settings getSettings() {
        return settings;
    }

}
