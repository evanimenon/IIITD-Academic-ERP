package erp.ui.student;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import erp.ui.student.CourseRegistrationDialog;

public class ButtonEditor extends DefaultCellEditor {
    protected JButton button;
    private String label;
    private boolean clicked;

    public ButtonEditor(JCheckBox checkBox) {
        super(checkBox);
        button = new JButton();
        
        // Use the same renderer styling for the editor button
        button.setOpaque(true);
        button.setFont(new ButtonRenderer().getFont()); // Use font from Renderer
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(39, 96, 92)); 
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        
        // Add action listener to stop editing and show message
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                fireEditingStopped();
            }
        });
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        label = (value == null) ? "" : value.toString();
        button.setText(label);
        clicked = true;
        
        // Custom painting logic from Renderer needs to be applied here for visual consistency
        button.repaint(); 
        
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        if (clicked) {
            // Instead of JOptionPane, open your CourseRegistrationDialog
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(button); // get parent frame
            CourseRegistrationDialog dialog = new CourseRegistrationDialog(
                parentFrame,
                label,                  // courseCode
                "Course Title Here",    // courseTitle (replace with real value from table)
                "50",                   // capacity
                "Room 101",             // room
                "CS"                    // acronym
            );
            dialog.setVisible(true);
        }
        clicked = false;
        return label;
    }


    @Override
    public boolean stopCellEditing() {
        clicked = false;
        return super.stopCellEditing();
    }
}