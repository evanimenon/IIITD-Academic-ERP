package erp.ui.common;

import java.awt.*;
import javax.swing.*;
import java.awt.geom.RoundRectangle2D;

public class RoundedPanel extends JPanel {
        private final int arc;
        public RoundedPanel(int arc) { this.arc = arc; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            for (int i = 6; i >= 1; i--) {
                float a = 0.035f * (i / 6f);
                g2.setColor(new Color(0, 0, 0, a));
                g2.fill(new RoundRectangle2D.Double(6 - i, 6 - i, w - 12 + 2*i, h - 12 + 2*i, arc + i, arc + i));
            }
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Double(6, 6, w - 12, h - 12, arc, arc));
            g2.dispose();
            super.paintComponent(g);
        }
    }
