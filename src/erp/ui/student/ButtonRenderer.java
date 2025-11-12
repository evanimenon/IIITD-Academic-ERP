package erp.ui.student;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

import erp.ui.auth.LoginPage;
import erp.ui.common.FontKit;

public class ButtonRenderer extends JButton implements TableCellRenderer {
    private static final Color TEAL_DARK = new Color(39, 96, 92);
    
    public ButtonRenderer() {
        setOpaque(true);
        setFont(FontKit.semibold(14f));
        setForeground(Color.WHITE);
        setBackground(TEAL_DARK);
        setFocusPainted(false);
        setBorderPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // Add rounded corners visually
        setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        setMargin(new Insets(0, 0, 0, 0));
    }
    
    // Custom painting for rounded corners
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        // Use a round rect to simulate rounded corners
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16); 
        g2.setColor(getForeground());
        
        // Draw the text
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(getText())) / 2;
        int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(getText(), x, y);
        g2.dispose();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        setText((value == null) ? "" : value.toString());
        
        // Ensure the button fits the cell and looks good
        if (isSelected) {
            setBackground(TEAL_DARK.darker());
        } else {
            setBackground(TEAL_DARK);
        }
        return this;
    }
}