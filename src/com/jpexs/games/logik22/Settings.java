package com.jpexs.games.logik22;

import java.awt.Color;
import java.io.Serializable;

/**
 *
 * @author JPEXS
 */
public class Settings implements Serializable {
    public int rows = 10;
    public int cols = 5;
    
    public static int DEFAULT_SETTINGS_VERSION = 3;
    
    public int settingsVersion = DEFAULT_SETTINGS_VERSION;
    
        
    public static final Color[] ALL_COLORS = new Color[]{
        Color.red,
        Color.blue,
        Color.white,
        Color.black,
        Color.green,
        Color.yellow,
        new Color(0x88, 0x00, 0x15), // brown
        Color.magenta,
        Color.orange,
        new Color(0x99,0x00,0xff) //fuchsia
    };
    
    
    public Color backgroundColor = ALL_COLORS[0];
    
    
    public Color[] colors = new Color[]{
        ALL_COLORS[0],
        ALL_COLORS[1],
        ALL_COLORS[2],
        ALL_COLORS[3],
        ALL_COLORS[4],
        ALL_COLORS[5],
        ALL_COLORS[6]
    };
}
