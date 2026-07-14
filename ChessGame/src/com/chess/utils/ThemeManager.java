package com.chess.utils;

import java.awt.Color;

public class ThemeManager {

    public enum Theme { LIGHT, DARK }

    private static Theme currentTheme = Theme.LIGHT;

    // Board square colors
    private static final Color LIGHT_SQUARE_LIGHT = new Color(240, 217, 181);
    private static final Color DARK_SQUARE_LIGHT  = new Color(181, 136,  99);
    private static final Color LIGHT_SQUARE_DARK  = new Color( 93, 111, 128);
    private static final Color DARK_SQUARE_DARK   = new Color( 54,  71,  86);

    // Highlight colors
    private static final Color SELECT_HIGHLIGHT   = new Color(106, 168,  79, 180);
    private static final Color MOVE_HIGHLIGHT     = new Color(106, 168,  79, 120);
    private static final Color CHECK_HIGHLIGHT    = new Color(220,  50,  50, 180);
    private static final Color LAST_MOVE_LIGHT    = new Color(205, 210,  92, 160);

    // Panel / UI colors
    private static final Color PANEL_BG_LIGHT     = new Color(238, 238, 238);
    private static final Color PANEL_BG_DARK      = new Color( 40,  44,  52);
    private static final Color TEXT_LIGHT         = new Color( 33,  33,  33);
    private static final Color TEXT_DARK          = new Color(220, 220, 220);

    public static void setTheme(Theme t)  { currentTheme = t; }
    public static Theme getCurrentTheme() { return currentTheme; }
    public static boolean isDark()        { return currentTheme == Theme.DARK; }

    public static Color getLightSquareColor() {
        return isDark() ? LIGHT_SQUARE_DARK : LIGHT_SQUARE_LIGHT;
    }
    public static Color getDarkSquareColor() {
        return isDark() ? DARK_SQUARE_DARK  : DARK_SQUARE_LIGHT;
    }
    public static Color getSelectHighlight()  { return SELECT_HIGHLIGHT; }
    public static Color getMoveHighlight()    { return MOVE_HIGHLIGHT; }
    public static Color getCheckHighlight()   { return CHECK_HIGHLIGHT; }
    public static Color getLastMoveHighlight(){ return LAST_MOVE_LIGHT; }

    public static Color getPanelBackground() {
        return isDark() ? PANEL_BG_DARK : PANEL_BG_LIGHT;
    }
    public static Color getTextColor() {
        return isDark() ? TEXT_DARK : TEXT_LIGHT;
    }
    public static Color getWhitePieceColor() { return new Color(255, 255, 255); }
    public static Color getBlackPieceColor() { return new Color( 30,  30,  30); }
    public static Color getPieceOutlineColor(){ return new Color(0, 0, 0, 100); }
}
