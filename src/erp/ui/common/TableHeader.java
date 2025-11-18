package erp.ui.common;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;

public class TableHeader extends DefaultTableCellRenderer {

    private static final Color BG = new Color(39, 96, 92);
    private static final Color FG = Color.WHITE;

    public TableHeader() {
        setOpaque(true);
        setHorizontalAlignment(CENTER);
        setFont(FontKit.bold(16f));
        setForeground(FG);
        setBackground(BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {

        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(header.getWidth(), 50));

        return this;
    }
}
