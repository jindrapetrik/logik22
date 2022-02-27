package com.jpexs.games.logik22;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author JPEXS
 */
public class AboutDialog extends JDialog {
    
    private static final String VERSION = "1.0.0";
    private static final String AUTHOR = "JPEXS";
    private static final String YEAR = "2022";
    
    public AboutDialog() {
        Container container = getContentPane();        
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.add(new JLabel(Logik22.translate("about.version")
                .replace("%version%", VERSION)
                ));
        container.add(new JLabel(Logik22.translate("about.createdby")
                .replace("%author%", AUTHOR)
                .replace("%year%", YEAR)));
        
        container.add(new JLabel(""));
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JButton okButton=new JButton(Logik22.translate("about.ok"));
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        buttonsPanel.add(okButton);
        
        container.add(buttonsPanel);
        pack();
        setTitle(Logik22.translate("about.title"));
        Logik22.centerWindow(this);
        Logik22.setWindowIcon(this);
        setModal(true);
        setResizable(false);
    }
}
