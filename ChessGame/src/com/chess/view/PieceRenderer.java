package com.chess.view;

import com.chess.model.PieceColor;
import com.chess.model.PieceType;

import java.awt.*;
import java.awt.geom.*;

/**
 * Draws every chess piece purely with Java2D shapes — no font or image files needed.
 *
 * All coordinates are expressed as fractions of the square size {@code sz}.
 * {@code ox}, {@code oy} are the top-left pixel of the square.
 */
public final class PieceRenderer {

    private static final BasicStroke STROKE = new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke THIN   = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    private PieceRenderer() {}

    public static void draw(Graphics2D g2, PieceType type, PieceColor color, int ox, int oy, int sz) {
        Color fill    = (color == PieceColor.WHITE) ? new Color(255, 252, 220) : new Color(28,  28,  28);
        Color outline = (color == PieceColor.WHITE) ? new Color(55,  55,  55)  : new Color(185, 185, 185);
        Color detail  = (color == PieceColor.WHITE) ? new Color(160, 150, 110) : new Color(90,  90,  90);

        Graphics2D g = (Graphics2D) g2.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        switch (type) {
            case PAWN   -> drawPawn  (g, ox, oy, sz, fill, outline, detail);
            case ROOK   -> drawRook  (g, ox, oy, sz, fill, outline);
            case KNIGHT -> drawKnight(g, ox, oy, sz, fill, outline, detail);
            case BISHOP -> drawBishop(g, ox, oy, sz, fill, outline, detail);
            case QUEEN  -> drawQueen (g, ox, oy, sz, fill, outline);
            case KING   -> drawKing  (g, ox, oy, sz, fill, outline);
        }
        g.dispose();
    }

    // ── Individual piece drawings ─────────────────────────────────────────────

    private static void drawPawn(Graphics2D g, int ox, int oy, int sz,
                                  Color fill, Color outline, Color detail) {
        fs(g, base(ox, oy, sz),                              fill, outline);
        fs(g, rr(ox, oy, 0.36, 0.48, 0.28, 0.30, sz),       fill, outline); // stem
        fs(g, el(ox, oy, 0.27, 0.18, 0.46, 0.36, sz),        fill, outline); // head
    }

    private static void drawRook(Graphics2D g, int ox, int oy, int sz,
                                  Color fill, Color outline) {
        fs(g, base(ox, oy, sz),                              fill, outline);
        fs(g, rc(ox, oy, 0.27, 0.30, 0.46, 0.47, sz),        fill, outline); // body
        fs(g, rc(ox, oy, 0.19, 0.24, 0.62, 0.08, sz),        fill, outline); // top bar
        // Three battlements
        fs(g, rc(ox, oy, 0.19, 0.13, 0.16, 0.13, sz),        fill, outline);
        fs(g, rc(ox, oy, 0.42, 0.13, 0.16, 0.13, sz),        fill, outline);
        fs(g, rc(ox, oy, 0.65, 0.13, 0.16, 0.13, sz),        fill, outline);
    }

    private static void drawKnight(Graphics2D g, int ox, int oy, int sz,
                                    Color fill, Color outline, Color detail) {
        fs(g, base(ox, oy, sz), fill, outline);

        // Horse-head silhouette polygon
        Polygon head = poly(ox, oy, sz,
            new double[]{0.27, 0.73, 0.70, 0.72, 0.68, 0.58, 0.50, 0.40, 0.30, 0.27, 0.35, 0.30},
            new double[]{0.77, 0.77, 0.63, 0.52, 0.37, 0.20, 0.28, 0.24, 0.36, 0.46, 0.54, 0.65}
        );
        fs(g, head, fill, outline);

        // Eye
        g.setColor(outline);
        g.fillOval(p(ox, 0.42, sz), p(oy, 0.29, sz), d(0.09, sz), d(0.08, sz));

        // Nostril arc
        g.setColor(detail);
        g.setStroke(THIN);
        g.drawArc(p(ox, 0.27, sz), p(oy, 0.37, sz), d(0.11, sz), d(0.10, sz), 180, 180);
        g.setStroke(STROKE);
    }

    private static void drawBishop(Graphics2D g, int ox, int oy, int sz,
                                    Color fill, Color outline, Color detail) {
        fs(g, base(ox, oy, sz), fill, outline);

        // Curved body
        Path2D.Double body = new Path2D.Double();
        body.moveTo(p(ox, 0.50, sz), p(oy, 0.17, sz));
        body.curveTo(p(ox, 0.76, sz), p(oy, 0.22, sz),
                     p(ox, 0.70, sz), p(oy, 0.60, sz),
                     p(ox, 0.65, sz), p(oy, 0.76, sz));
        body.lineTo(p(ox, 0.35, sz), p(oy, 0.76, sz));
        body.curveTo(p(ox, 0.30, sz), p(oy, 0.60, sz),
                     p(ox, 0.24, sz), p(oy, 0.22, sz),
                     p(ox, 0.50, sz), p(oy, 0.17, sz));
        body.closePath();
        fs(g, body, fill, outline);

        // Waist band
        g.setColor(detail); g.setStroke(THIN);
        g.drawLine(p(ox, 0.30, sz), p(oy, 0.54, sz), p(ox, 0.70, sz), p(oy, 0.54, sz));
        g.setStroke(STROKE);

        // Top ball
        fs(g, el(ox, oy, 0.39, 0.10, 0.22, 0.18, sz), fill, outline);

        // Finial pin (triangle)
        fs(g, poly(ox, oy, sz,
                new double[]{0.46, 0.54, 0.50},
                new double[]{0.14, 0.14, 0.05}), fill, outline);
    }

    private static void drawQueen(Graphics2D g, int ox, int oy, int sz,
                                   Color fill, Color outline) {
        fs(g, base(ox, oy, sz), fill, outline);

        // Tapered body
        fs(g, poly(ox, oy, sz,
                new double[]{0.22, 0.78, 0.68, 0.32},
                new double[]{0.76, 0.76, 0.38, 0.38}), fill, outline);

        // Crown band
        fs(g, rc(ox, oy, 0.22, 0.32, 0.56, 0.08, sz), fill, outline);

        // Five crown balls
        double[] bx = {0.21, 0.32, 0.44, 0.56, 0.68};
        for (double x : bx) {
            fs(g, el(ox, oy, x, 0.17, 0.12, 0.12, sz), fill, outline);
        }
    }

    private static void drawKing(Graphics2D g, int ox, int oy, int sz,
                                  Color fill, Color outline) {
        fs(g, base(ox, oy, sz), fill, outline);

        // Tapered body
        fs(g, poly(ox, oy, sz,
                new double[]{0.24, 0.76, 0.65, 0.35},
                new double[]{0.76, 0.76, 0.40, 0.40}), fill, outline);

        // Cross — vertical bar
        fs(g, rc(ox, oy, 0.44, 0.07, 0.12, 0.36, sz), fill, outline);
        // Cross — horizontal bar
        fs(g, rc(ox, oy, 0.30, 0.17, 0.40, 0.12, sz), fill, outline);
    }

    // ── Shape helpers ─────────────────────────────────────────────────────────

    /** Fill then stroke a shape. */
    private static void fs(Graphics2D g, Shape s, Color fill, Color outline) {
        g.setStroke(STROKE);
        g.setColor(fill);   g.fill(s);
        g.setColor(outline); g.draw(s);
    }

    /** Pixel position: origin + fraction × sz. */
    private static int p(int origin, double frac, int sz) {
        return (int) Math.round(origin + frac * sz);
    }

    /** Pixel dimension: fraction × sz (minimum 1). */
    private static int d(double frac, int sz) {
        return Math.max(1, (int) Math.round(frac * sz));
    }

    /** Rounded rectangle. */
    private static RoundRectangle2D.Double rr(int ox, int oy,
            double x, double y, double w, double h, int sz) {
        return new RoundRectangle2D.Double(p(ox,x,sz), p(oy,y,sz), d(w,sz), d(h,sz), 8, 8);
    }

    /** Ellipse. */
    private static Ellipse2D.Double el(int ox, int oy,
            double x, double y, double w, double h, int sz) {
        return new Ellipse2D.Double(p(ox,x,sz), p(oy,y,sz), d(w,sz), d(h,sz));
    }

    /** Axis-aligned rectangle. */
    private static Rectangle2D.Double rc(int ox, int oy,
            double x, double y, double w, double h, int sz) {
        return new Rectangle2D.Double(p(ox,x,sz), p(oy,y,sz), d(w,sz), d(h,sz));
    }

    /** The standard wide flat base all pieces share. */
    private static RoundRectangle2D.Double base(int ox, int oy, int sz) {
        return rr(ox, oy, 0.13, 0.75, 0.74, 0.20, sz);
    }

    /** Polygon from parallel fractional x/y arrays. */
    private static Polygon poly(int ox, int oy, int sz, double[] fx, double[] fy) {
        int n = fx.length;
        int[] xs = new int[n], ys = new int[n];
        for (int i = 0; i < n; i++) { xs[i] = p(ox, fx[i], sz); ys[i] = p(oy, fy[i], sz); }
        return new Polygon(xs, ys, n);
    }
}
