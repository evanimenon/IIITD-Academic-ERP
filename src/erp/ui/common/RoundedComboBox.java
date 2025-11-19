package erp.ui.common;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class RoundedComboBox<E> extends JComboBox<E> {
    private final Color bg = new Color(0xD9, 0xD9, 0xD9);
    private final Color text = new Color(24, 30, 37);
    private final Color placeholderColor = new Color(140, 148, 160);
    private String placeholder = "";

    public RoundedComboBox() {
        super();
        setup();
    }

    public RoundedComboBox(E[] items) {
        super(items);
        setup();
    }

    private void setup() {
        setOpaque(false);
        setBorder(new EmptyBorder(12, 16, 12, 16));
        setFont(FontKit.regular(16f));
        setForeground(text);
        setBackground(bg);

        setUI(new BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                JButton arrow = new JButton("▼");
                arrow.setBorder(null);
                arrow.setFont(FontKit.regular(12f));
                arrow.setOpaque(false);
                arrow.setContentAreaFilled(false);
                arrow.setFocusPainted(false);
                arrow.setForeground(new Color(100, 116, 139));
                return arrow;
            }

            @Override public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                // no grey highlight
            }
        });
        setBorder(new EmptyBorder(12, 16, 12, 16));
    }

    public void setPlaceholder(String text) {
        this.placeholder = text;
        repaint();
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Rounded background
        g2.setColor(bg);
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));

        g2.dispose();
        super.paintComponent(g);

        // Placeholder
        if (getSelectedIndex() == -1 || getSelectedItem() == null || getSelectedItem().toString().trim().isEmpty()) {
            Graphics2D g3 = (Graphics2D) g.create();
            g3.setColor(placeholderColor);
            g3.setFont(getFont());
            Insets ins = getInsets();
            g3.drawString(placeholder, ins.left, getHeight() / 2 + getFont().getSize() / 2 - 3);
            g3.dispose();
        }
    }
}
