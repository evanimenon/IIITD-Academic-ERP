package erp.ui.auth;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.InputStream;

import erp.auth.AuthService;
import erp.ui.common.FontKit;
import erp.ui.student.StudentDashboard;
import erp.ui.instructor.InstructorDashboard;
import erp.ui.admin.AdminDashboard;
import java.util.Locale;
import erp.auth.AuthContext;
import erp.auth.Role;
import erp.ui.student.StudentDashboard;
import erp.ui.student.StudentLookupService;

import erp.ui.common.RoundedButton;
import erp.ui.common.RoundedTextField;
import erp.ui.common.RoundedPasswordField;

public class LoginPage extends JFrame {

    private final JLabel errorLabel; // inline error

    public LoginPage() {
        setTitle("IIITD ERP – Sign in");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 720));
        setLocationRelativeTo(null);

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
        JLabel title = new JLabel("Sign into your ERP", SwingConstants.CENTER);
        title.setFont(FontKit.bold(20f));
        title.setForeground(new Color(29, 122, 120));
        title.setBorder(new EmptyBorder(16, 0, 8, 0));
        gc.anchor = GridBagConstraints.CENTER;
        body.add(title, gc);

        // Username
        gc.gridy++;
        gc.anchor = GridBagConstraints.WEST;
        JLabel userLbl = subtleLabel("Username");
        body.add(userLbl, gc);

        gc.gridy++;
        RoundedTextField username = new RoundedTextField(30, "Enter your username");
        username.setFont(FontKit.regular(16f));
        body.add(username, gc);

        // Password
        gc.gridy++;
        gc.insets = new Insets(12, 18, 0, 18);
        JLabel passLbl = subtleLabel("Password");
        body.add(passLbl, gc);

        gc.gridy++;
        RoundedPasswordField password = new RoundedPasswordField(30, "Enter your password");
        password.setFont(FontKit.regular(16f));
        body.add(password, gc);

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
        RoundedButton signIn = new RoundedButton("Sign in");
        signIn.setFont(FontKit.bold(18f));
        body.add(signIn, gc);
        getRootPane().setDefaultButton(signIn);

        // Action
        signIn.addActionListener(e -> {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            signIn.setEnabled(false);
            showError(null); // clear any old message

            new Thread(() -> {
                String user = username.getText().trim();
                char[] pw = password.getPassword();
                String pwStr = new String(pw);
                java.util.Arrays.fill(pw, '\0');

                try {
                    var session = new AuthService().login(user, pwStr);

                    // store session centrally for auth checks elsewhere
                    AuthContext.setSession(session);

                    SwingUtilities.invokeLater(() -> {
                        // route user to the appropriate dashboard based on normalized role
                        Role r = Role.from(session.role());
                        String display = session.username() + " • " + session.role();

                        switch (r) {
                            case STUDENT -> {
                                String authUsername = session.username();

                                StudentLookupService.StudentInfo info = StudentLookupService
                                        .loadByAuthUsername(authUsername);

                                String studentId = authUsername; // start with something non-null
                                String displayName = authUsername;

                                if (info != null) {
                                    // 1) Pick a canonical studentId: prefer student_id, else roll_no, else username
                                    if (info.getStudentId() != null && !info.getStudentId().isBlank()) {
                                        studentId = info.getStudentId();
                                    } else if (info.getRollNo() != null && !info.getRollNo().isBlank()) {
                                        studentId = info.getRollNo();
                                    }

                                    // 2) Pick a nice display name
                                    if (info.getFullName() != null && !info.getFullName().isBlank()) {
                                        displayName = info.getFullName();
                                    } else if (info.getRollNo() != null && !info.getRollNo().isBlank()) {
                                        displayName = info.getRollNo();
                                    }
                                }

                                System.out.println("[DEBUG] Final studentId used in UI = '" + studentId + "'");

                                new StudentDashboard(studentId, displayName).setVisible(true);
                                dispose();
                            }

                            case INSTRUCTOR -> {
                                // InstructorDashboard expects (instrID, displayName) — use userId as instrID
                                // placeholder
                                new InstructorDashboard(String.valueOf(session.userId()),
                                        display).setVisible(true);
                                dispose();
                            }
                            case ADMIN -> {
                                new AdminDashboard(display).setVisible(true);
                                dispose();
                            }
                            default -> {
                                // Unknown role — clear session and show error
                                AuthContext.clear();
                                showError("Your account is not authorized. Contact admin.");
                            }
                        }
                    });

                } catch (AuthService.AuthException ex) {
                    SwingUtilities.invokeLater(() -> showError(ex.getMessage()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> showError("Incorrect username or password."));
                } finally {
                    SwingUtilities.invokeLater(() -> {
                        signIn.setEnabled(true);
                        setCursor(Cursor.getDefaultCursor());
                    });
                }
            }, "login-thread").start();
        });
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
