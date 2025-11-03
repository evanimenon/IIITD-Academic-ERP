package erp.ui;

import javax.swing.*;
import java.awt.*;

public class Dashboard extends JFrame {

    public Dashboard() {
        setTitle("IIITD ERP Dashboard");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel welcome = new JLabel("Welcome to IIITD ERP System!", SwingConstants.CENTER);
        welcome.setFont(new Font("SansSerif", Font.BOLD, 20));
        add(welcome);
    }
}
