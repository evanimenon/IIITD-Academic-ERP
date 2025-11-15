package erp.ui.common;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class RoundedButton extends JButton {

    private final Color base  = new Color(39, 96, 92);
    private final Color hover = new Color(28, 122, 120);

    public RoundedButton(String text) {
        super(text);

        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setRolloverEnabled(true);

        setForeground(Color.WHITE);
        setFont(FontKit.bold(16f));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(new EmptyBorder(10, 22, 10, 22));
    }

    @Override
    protected void paintComponent(Graphics g) {

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        boolean isHover = getModel().isRollover();
        int arc = 24;
        int w = getWidth();
        int h = getHeight();

        // --- Button fill ---
        g2.setColor(isHover ? hover : base);
        g2.fill(new RoundRectangle2D.Double(
                0, 0,
                w - 2, h - 2,
                arc, arc
        ));

        g2.dispose();
        super.paintComponent(g);
    }
}
