package erp.ui.common;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class NavButton extends JButton {
    private final boolean selected;
    public NavButton(String text, boolean selected) {
        super(text);
        this.selected = selected;
        setHorizontalAlignment(LEFT);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setForeground(Color.WHITE);
        setFont(FontKit.semibold(16f));
        setBorder(new EmptyBorder(10, 14, 10, 14));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (getModel().isRollover() || selected) {
            g2.setColor(new Color(255, 255, 255, selected ? 60 : 30));
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 14, 14));
        }
        g2.dispose();
        super.paintComponent(g);
    }
}