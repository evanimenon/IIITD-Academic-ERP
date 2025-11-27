package erp.ui.auth;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.mindrot.jbcrypt.BCrypt;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import erp.ui.common.FontKit;
import erp.db.DatabaseConnection;

import erp.ui.common.RoundedButton;
import erp.ui.common.RoundedPasswordField;

public class ChangePassword extends JFrame {

    private final JLabel errorLabel; // inline error

    public ChangePassword(String username) {
        setTitle("IIITD ERP – Change Password");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        setMinimumSize(new Dimension(1100, 720));
        setSize(1200, 800);

        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);

        BackgroundPanel bg = new BackgroundPanel("/resources/bg_login.png");
        bg.setLayout(new GridBagLayout());
        setContentPane(bg);

        LoginCard card = new LoginCard(450, 520);
        GridBagConstraints rootGc = new GridBagConstraints();
        bg.add(card, rootGc);

        JPanel body = card.getBody();
        body.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.insets = new Insets(8, 18, 0, 18);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        // Logo
        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        logoRow.setOpaque(false);
        JLabel logo = new JLabel(loadScaled("/resources/iiitd_logo.png", 350, 80));
        if (logo.getIcon() != null) {
            logoRow.add(logo);
            body.add(logoRow, gc);
        }

        // Title
        gc.gridy++;
        JLabel title = new JLabel("Change Your Password", SwingConstants.CENTER);
        title.setFont(FontKit.bold(20f));
        title.setForeground(new Color(29, 122, 120));
        title.setBorder(new EmptyBorder(16, 0, 8, 0));
        gc.anchor = GridBagConstraints.CENTER;
        body.add(title, gc);

        // Password
        gc.gridy++;
        gc.anchor = GridBagConstraints.WEST;
        JLabel userLbl = subtleLabel("New Password");
        body.add(userLbl, gc);

        gc.gridy++;
        RoundedPasswordField passwordField = new RoundedPasswordField(30, "Enter password");
        passwordField.setFont(FontKit.regular(16f));
        body.add(passwordField, gc);

        // Confirm Password
        gc.gridy++;
        gc.insets = new Insets(12, 18, 0, 18);
        JLabel passLbl = subtleLabel("Password");
        body.add(passLbl, gc);

        gc.gridy++;
        RoundedPasswordField confirmField = new RoundedPasswordField(30, "Confirm password");
        confirmField.setFont(FontKit.regular(16f));
        body.add(confirmField, gc);

        // Inline error (hidden by default)
        gc.gridy++;
        gc.insets = new Insets(6, 18, 0, 18);
        errorLabel = new JLabel(" "); // keep height even when empty
        errorLabel.setForeground(new Color(185, 28, 28)); // red-600
        errorLabel.setFont(FontKit.semibold(13f));
        body.add(errorLabel, gc);

        // Sign in button
        gc.gridy++;
        gc.insets = new Insets(18, 18, 18, 18);
        gc.anchor = GridBagConstraints.EAST;
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0;
        RoundedButton signIn = new RoundedButton("Set Password");
        signIn.setFont(FontKit.bold(18f));
        body.add(signIn, gc);
        getRootPane().setDefaultButton(signIn);

        // Action
        signIn.addActionListener(e -> {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            signIn.setEnabled(false);
            showError(null); // clear any old message

            String pass = new String(passwordField.getPassword());
            String confirm = new String(confirmField.getPassword());

            if (!pass.equals(confirm)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
                setCursor(Cursor.getDefaultCursor());
                signIn.setEnabled(true);
                return;
            }
            if (pass.length() < 8) {
                JOptionPane.showMessageDialog(this,"Password should be at least 8 characters.","Weak Password",JOptionPane.ERROR_MESSAGE);
                setCursor(Cursor.getDefaultCursor());
                signIn.setEnabled(true);
                return;
            }
            boolean set = setNewPassword(pass, username);
            if(set){
                JOptionPane.showMessageDialog(this,"Password updated successfully. Please log in with your new password.");
                new LoginPage().setVisible(true);
                dispose();
            }
            else{
                new ChangePassword(username);
            }
        });
    }

    private boolean setNewPassword(String pwd, String usr){

        // Hash password
        String hash = BCrypt.hashpw(pwd, BCrypt.gensalt(12));
        String changePwdSQL = "UPDATE users_auth SET password_hash = ?, last_login = NOW() WHERE username = ?";

        try (Connection conn = DatabaseConnection.auth().getConnection();
            PreparedStatement stmt = conn.prepareStatement(changePwdSQL)) {

            stmt.setString(1, hash);
            stmt.setString(2, usr);
            stmt.executeUpdate();
            return true;

        } 
        catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,"Error updating password:\n" + e.getMessage(),"Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void showError(String msg) {
        if (msg == null || msg.isBlank()) {
            errorLabel.setText(" ");
        } else {
            errorLabel.setText(msg);
        }
        errorLabel.revalidate();
        errorLabel.repaint();
    }

    // ---- helpers ----
    private static JLabel subtleLabel(String txt) {
        JLabel l = new JLabel(txt);
        l.setForeground(new Color(75, 85, 99));
        l.setFont(FontKit.semibold(16f));
        return l;
    }

    private static ImageIcon loadScaled(String path, int w, int h) {
        try (InputStream in = LoginPage.class.getResourceAsStream(path)) {
            if (in == null)
                return null;
            Image img = ImageIO.read(in).getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception e) {
            System.err.println("Error loading image: " + path);
            e.printStackTrace();
            return null;
        }
    }

    // ---- inner UI bits ----
    static class BackgroundPanel extends JPanel {
        private Image bg;

        BackgroundPanel(String cpPath) {
            try (InputStream in = LoginPage.class.getResourceAsStream(cpPath)) {
                if (in != null) {
                    bg = ImageIO.read(in);
                }
            } catch (Exception e) {
                System.err.println("Error loading background image: " + cpPath);
                e.printStackTrace();
            }
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bg == null)
                return;
            int pw = getWidth(), ph = getHeight();
            int iw = bg.getWidth(null), ih = bg.getHeight(null);
            if (iw <= 0 || ih <= 0)
                return;
            double scale = Math.max(pw / (double) iw, ph / (double) ih);
            int dw = (int) (iw * scale), dh = (int) (ih * scale);
            int dx = (pw - dw) / 2, dy = (ph - dh) / 2;
            g.drawImage(bg, dx, dy, dw, dh, null);
        }
    }

    static class LoginCard extends JComponent {
        private final int prefW, prefH;
        private final JPanel body = new JPanel();

        LoginCard(int w, int h) {
            prefW = w;
            prefH = h;
            setLayout(new GridBagLayout());
            body.setOpaque(false);
            add(body, new GridBagConstraints());
            setOpaque(false);
        }

        JPanel getBody() {
            return body;
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(prefW, prefH);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int arc = 26, pad = 16;
            int w = getWidth(), h = getHeight();
            for (int i = 6; i >= 1; i--) {
                float alpha = 0.035f * (i / 6f);
                g2.setColor(new Color(0, 0, 0, alpha));
                g2.fill(new RoundRectangle2D.Double(pad - i, pad - i, w - 2 * pad + 2 * i, h - 2 * pad + 2 * i, arc + i,
                        arc + i));
            }
            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Double(pad, pad, w - 2 * pad, h - 2 * pad, arc, arc));
            g2.dispose();
        }
    }

    // Quick manual launch
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        FontKit.init();
        erp.db.DatabaseConnection.init();
        SwingUtilities.invokeLater(() -> new LoginPage().setVisible(true));
    }
}
