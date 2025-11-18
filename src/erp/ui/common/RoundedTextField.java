package erp.ui.common;
import javax.swing.*;
import javax.swing.border.EmptyBorder;  
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;

public class RoundedTextField extends JTextField {
    private final String placeholder;

    public RoundedTextField(int cols, String placeholder) {
        super(cols);
        this.placeholder = placeholder;
        setOpaque(false);
        setBorder(new EmptyBorder(12, 16, 12, 16));
        setFont(FontKit.regular(16f));
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(0xD9, 0xD9, 0xD9));
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
        g2.dispose();
        super.paintComponent(g);
        if (getText().isEmpty()) {
            Graphics2D g3 = (Graphics2D) g.create();
            g3.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g3.setColor(new Color(140, 148, 160));
            g3.setFont(getFont());
            Insets ins = getInsets();
            g3.drawString(placeholder, ins.left, getHeight() / 2 + getFont().getSize() / 2 - 3);
            g3.dispose();
        }
    }
}
