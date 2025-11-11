// LoginPage.java
package erp.ui.auth;

import erp.ui.common.Dashboard;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.InputStream;

public class LoginPage extends JFrame {

    // ---------------------- FontKit ----------------------
    public static final class FontKit {
        private static Font REG, SEMI, BOLD;

        public static void init() {
            REG = load("/fonts/Inter-Regular.ttf");
            SEMI = load("/fonts/Inter-SemiBold.ttf");
            BOLD = load("/fonts/Inter-Bold.ttf");

            setUIFont(REG.deriveFont(14f));
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
        }

        public static Font regular(float sz) { return REG.deriveFont(sz); }
        public static Font semibold(float sz) { return SEMI.deriveFont(sz); }
        public static Font bold(float sz) { return BOLD.deriveFont(sz); }

        private static Font load(String cp) {
            try (var in = LoginPage.class.getResourceAsStream(cp)) {
                var f = Font.createFont(Font.TRUETYPE_FONT, in);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(f);
                return f;
            } catch (Exception e) {
                return new Font("SansSerif", Font.PLAIN, 14);
            }
        }

        private static void setUIFont(Font f) {
            var keys = UIManager.getDefaults().keys();
            while (keys.hasMoreElements()) {
                Object k = keys.nextElement();
                Object v = UIManager.get(k);
                if (v instanceof javax.swing.plaf.FontUIResource) {
                    UIManager.put(k, new javax.swing.plaf.FontUIResource(f));
                }
            }
        }
    }

    // ---------------------- MAIN ----------------------
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception ignored) {}
        FontKit.init();
        SwingUtilities.invokeLater(() -> new LoginPage().setVisible(true));
    }

    // ---------------------- Constructor ----------------------
    public LoginPage() {
        setTitle("IIITD ERP – Sign in");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 720));
        setLocationRelativeTo(null);

        BackgroundPanel bg = new BackgroundPanel("/erp/ui/auth/bg_login.png");
        bg.setLayout(new GridBagLayout());
        setContentPane(bg);

        LoginCard card = new LoginCard(440, 520);
        GridBagConstraints rootGc = new GridBagConstraints();
        bg.add(card, rootGc);

        JPanel body = card.getBody();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(36, 42, 36, 42));

        // ---------------------- Logo ----------------------
        JLabel logo = new JLabel(loadScaled("/erp/ui/auth/iiitd_logo.png", 400, 100));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(logo);

        // Divider line
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(new Color(225, 225, 225));
        sep.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(Box.createRigidArea(new Dimension(0, 20)));
        body.add(sep);
        body.add(Box.createRigidArea(new Dimension(0, 20)));

        // Title
        JLabel title = new JLabel("Sign in to your ERP", SwingConstants.CENTER);
        title.setFont(FontKit.bold(15f));
        title.setForeground(new Color(29, 122, 120));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(title);

        body.add(Box.createRigidArea(new Dimension(0, 24)));

        // Username
        RoundedTextField username = new RoundedTextField(30, "Enter your username");
        username.setFont(FontKit.regular(16f));
        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));
        userPanel.setOpaque(false);
        userPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel userLbl = subtleLabel("Username");
        userLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        username.setAlignmentX(Component.LEFT_ALIGNMENT);

        userPanel.add(userLbl);
        userPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        userPanel.add(username);

        body.add(userPanel);
        body.add(Box.createRigidArea(new Dimension(0, 18)));

        //password

        RoundedPasswordField password = new RoundedPasswordField(30, "Enter your password");
        password.setFont(FontKit.regular(16f));

        JPanel passPanel = new JPanel();
        passPanel.setLayout(new BoxLayout(passPanel, BoxLayout.Y_AXIS));
        passPanel.setOpaque(false);
        passPanel.setAlignmentX(Component.CENTER_ALIGNMENT); // center panel in card

        JLabel passLbl = subtleLabel("Password");
        passLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        password.setAlignmentX(Component.LEFT_ALIGNMENT);

        passPanel.add(passLbl);
        passPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        passPanel.add(password);

        body.add(passPanel);
        body.add(Box.createRigidArea(new Dimension(0, 28)));

        // Sign in button
        RoundedButton signIn = new RoundedButton("Sign in");
        signIn.setFont(FontKit.bold(18f));
        signIn.setAlignmentX(Component.CENTER_ALIGNMENT);
        signIn.addActionListener(e -> {
            new Dashboard("Student 123").setVisible(true);
            dispose();
        });
        body.add(signIn);
    }

    // ---------------------- Helpers ----------------------
    private static JLabel subtleLabel(String txt) {
        JLabel l = new JLabel(txt);
        l.setForeground(new Color(90, 98, 110));
        l.setFont(FontKit.semibold(15f));
        return l;
    }

    private static ImageIcon loadScaled(String path, int w, int h) {
        try (InputStream in = LoginPage.class.getResourceAsStream(path)) {
            if (in == null) return null;
            Image img = ImageIO.read(in).getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception e) {
            return null;
        }
    }

    // ---------------------- Background ----------------------
    static class BackgroundPanel extends JPanel {
        private Image bg;
        BackgroundPanel(String cpPath) {
            try (InputStream in = LoginPage.class.getResourceAsStream(cpPath)) {
                if (in != null)
                    bg = ImageIO.read(in);
            } catch (Exception ignored) {}
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bg == null) return;
            int pw = getWidth(), ph = getHeight();
            int iw = bg.getWidth(null), ih = bg.getHeight(null);
            double scale = Math.max(pw / (double) iw, ph / (double) ih);
            int dw = (int) (iw * scale), dh = (int) (ih * scale);
            int dx = (pw - dw) / 2, dy = (ph - dh) / 2;
            g.drawImage(bg, dx, dy, dw, dh, null);
        }
    }

    // ---------------------- LoginCard ----------------------
    static class LoginCard extends JComponent {
        private final int prefW, prefH;
        private final JPanel body = new JPanel();

        LoginCard(int w, int h) {
            prefW = w; prefH = h;
            setLayout(new GridBagLayout());
            body.setOpaque(false);
            add(body, new GridBagConstraints());
            setOpaque(false);
        }

        JPanel getBody() { return body; }

        @Override public Dimension getPreferredSize() { return new Dimension(prefW, prefH); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = 28;
            int w = getWidth(), h = getHeight();

            // Soft shadow
            for (int i = 6; i >= 1; i--) {
                float alpha = 0.035f * (i / 6f);
                g2.setColor(new Color(0, 0, 0, alpha));
                g2.fill(new RoundRectangle2D.Double(i, i, w - 2 * i, h - 2 * i, arc, arc));
            }

            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Double(0, 0, w - 2, h - 2, arc, arc));
            g2.dispose();
        }
    }

    // ---------------------- RoundedTextField ----------------------
    static class RoundedTextField extends JTextField {
        private final String placeholder;
        RoundedTextField(int cols, String placeholder) {
            super(cols);
            this.placeholder = placeholder;
            setOpaque(false);
            setBorder(new EmptyBorder(12, 16, 12, 16));
            setFont(FontKit.regular(16f));
            addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { repaint(); }
                @Override public void focusLost(FocusEvent e) { repaint(); }
            });
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0xEE, 0xEE, 0xEE));
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
            g2.dispose();
            super.paintComponent(g);

            if (getText().isEmpty()) {
                Graphics2D g3 = (Graphics2D) g.create();
                g3.setColor(new Color(140, 148, 160));
                Insets ins = getInsets();
                g3.setFont(getFont());
                g3.drawString(placeholder, ins.left, getHeight() / 2 + getFont().getSize() / 2 - 3);
                g3.dispose();
            }
        }
    }

    // ---------------------- RoundedPasswordField ----------------------
    static class RoundedPasswordField extends JPasswordField {
        private final String placeholder;
        RoundedPasswordField(int cols, String placeholder) {
            super(cols);
            this.placeholder = placeholder;
            setOpaque(false);
            setBorder(new EmptyBorder(12, 16, 12, 16));
            setFont(FontKit.regular(16f));
            addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { repaint(); }
                @Override public void focusLost(FocusEvent e) { repaint(); }
            });
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0xEE, 0xEE, 0xEE));
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
            g2.dispose();
            super.paintComponent(g);

            if (getPassword().length == 0) {
                Graphics2D g3 = (Graphics2D) g.create();
                g3.setColor(new Color(140, 148, 160));
                Insets ins = getInsets();
                g3.setFont(getFont());
                g3.drawString(placeholder, ins.left, getHeight() / 2 + getFont().getSize() / 2 - 3);
                g3.dispose();
            }
        }
    }

    // ---------------------- RoundedButton ----------------------
    static class RoundedButton extends JButton {
        private final Color base = new Color(39, 96, 92);
        private final Color hover = new Color(28, 122, 120);
        RoundedButton(String text) {
            super(text);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setForeground(Color.WHITE);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(14, 28, 14, 28));
            setRolloverEnabled(true);
        }
        @Override
        protected void paintComponent(Graphics g) {
            boolean isHover = getModel().isRollover();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isHover ? hover : base);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 22, 22));
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
