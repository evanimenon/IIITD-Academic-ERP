// LoginPage.java
package erp.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.InputStream;

// --- Font utilities ---------------------------------------------------------

public class LoginPage extends JFrame {

    public static final class FontKit {
        private static Font REG, SEMI, BOLD;

        public static void init() {
            REG = load("/fonts/Inter-Regular.ttf");
            SEMI = load("/fonts/Inter-SemiBold.ttf");
            BOLD = load("/fonts/Inter-Bold.ttf");

            // Global default font (size 14 looks crisp on most screens)
            setUIFont(REG.deriveFont(14f));
            // Better text rendering everywhere
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
        }

        static Font regular(float sz) {
            return REG.deriveFont(sz);
        }

        static Font semibold(float sz) {
            return SEMI.deriveFont(sz);
        }

        static Font bold(float sz) {
            return BOLD.deriveFont(sz);
        }

        private static Font load(String cp) {
            try (var in = LoginPage.class.getResourceAsStream(cp)) {
                var f = Font.createFont(Font.TRUETYPE_FONT, in);
                java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(f);
                return f;
            } catch (Exception e) {
                return new Font("SansSerif", Font.PLAIN, 14);
            }
        }

        private static void setUIFont(Font f) {
            var keys = javax.swing.UIManager.getDefaults().keys();
            while (keys.hasMoreElements()) {
                Object k = keys.nextElement();
                Object v = UIManager.get(k);
                if (v instanceof javax.swing.plaf.FontUIResource) {
                    UIManager.put(k, new javax.swing.plaf.FontUIResource(f));
                }
            }
        }
    }

    // (Removed the extra main() — entrypoint is erp.Main)

    public LoginPage() {
        setTitle("IIITD ERP – Sign in");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 720));
        setLocationRelativeTo(null);

        BackgroundPanel bg = new BackgroundPanel("/bg_login.png");
        bg.setLayout(new GridBagLayout());
        setContentPane(bg);

        LoginCard card = new LoginCard(450, 500);
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

        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        logoRow.setOpaque(false);
        JLabel logo = new JLabel(loadScaled("/iiitd_logo.png", 350, 80));
        if (logo.getIcon() != null) {
            logoRow.add(logo);
            body.add(logoRow, gc);
        }

        // Title (centered) — Inter Bold 20
        gc.gridy++;
        JLabel title = new JLabel("Sign into your ERP", SwingConstants.CENTER);
        title.setFont(FontKit.bold(20f));
        title.setForeground(new Color(29, 122, 120));
        title.setBorder(new EmptyBorder(16, 0, 8, 0));
        gc.anchor = GridBagConstraints.CENTER;
        body.add(title, gc);

        // Username label — Inter SemiBold 16
        gc.gridy++;
        gc.anchor = GridBagConstraints.WEST;
        JLabel userLbl = subtleLabel("Username");
        body.add(userLbl, gc);

        // Username field — Inter Regular 16
        gc.gridy++;
        RoundedTextField username = new RoundedTextField(30, "Enter your username");
        username.setFont(FontKit.regular(16f));
        body.add(username, gc);

        // Password label — Inter SemiBold 16
        gc.gridy++;
        gc.insets = new Insets(12, 18, 0, 18);
        JLabel passLbl = subtleLabel("Password");
        body.add(passLbl, gc);

        // Password field — Inter Regular 16
        gc.gridy++;
        RoundedPasswordField password = new RoundedPasswordField(30, "Enter your password");
        password.setFont(FontKit.regular(16f));
        body.add(password, gc);

        // Sign in button — Inter Bold 18
        gc.gridy++;
        gc.insets = new Insets(18, 18, 18, 18);
        gc.anchor = GridBagConstraints.EAST;
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0;
        RoundedButton signIn = new RoundedButton("Sign in");
        signIn.setFont(FontKit.bold(18f));
        signIn.addActionListener(e -> {
            // replace with real auth later
            SwingUtilities.invokeLater(() -> {
                new Dashboard("Student 123").setVisible(true);
                dispose(); // close login window
            });
        });
        // Allow pressing Enter to submit
        getRootPane().setDefaultButton(signIn);

        body.add(signIn, gc);
    }

    private static JLabel subtleLabel(String txt) {
        JLabel l = new JLabel(txt);
        l.setForeground(new Color(75, 85, 99));
        l.setFont(FontKit.semibold(16f)); // Inter SemiBold
        return l;
    }

    private static ImageIcon loadScaled(String path, int w, int h) {
        try (InputStream in = LoginPage.class.getResourceAsStream(path)) {
            if (in == null)
                return null;
            Image img = ImageIO.read(in).getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception e) {
            return null;
        }
    }

    // ---------- background panel (cover) ----------
    static class BackgroundPanel extends JPanel {
        private Image bg;

        BackgroundPanel(String cpPath) {
            try (InputStream in = LoginPage.class.getResourceAsStream(cpPath)) {
                if (in != null)
                    bg = ImageIO.read(in);
            } catch (Exception ignored) {
            }
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

    // ---------- card with SOFTER shadow ----------
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
                g2.fill(new RoundRectangle2D.Double(
                        pad - i, pad - i, w - 2 * pad + 2 * i, h - 2 * pad + 2 * i, arc + i, arc + i));
            }
            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Double(pad, pad, w - 2 * pad, h - 2 * pad, arc, arc));
            g2.dispose();
        }
    }

    // ---------- rounded text field ----------
    static class RoundedTextField extends JTextField {
        private final String placeholder;

        RoundedTextField(int cols, String placeholder) {
            super(cols);
            this.placeholder = placeholder;
            setOpaque(false);
            setBorder(new EmptyBorder(12, 16, 12, 16));
            setFont(FontKit.regular(16f)); // Inter Regular
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
            g2.setColor(new Color(0xD9, 0xD9, 0xD9)); // #D9D9D9
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

    // ---------- rounded password field ----------
    static class RoundedPasswordField extends JPasswordField {
        private final String placeholder;

        RoundedPasswordField(int cols, String placeholder) {
            super(cols);
            this.placeholder = placeholder;
            setOpaque(false);
            setBorder(new EmptyBorder(12, 16, 12, 16));
            setFont(FontKit.regular(16f)); // Inter Regular
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
            g2.setColor(new Color(0xD9, 0xD9, 0xD9)); // #D9D9D9
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
            g2.dispose();
            super.paintComponent(g);

            if (getPassword().length == 0) {
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

    // ---------- rounded button with hover lift ----------
    static class RoundedButton extends JButton {
        private final Color base = new Color(39, 96, 92);
        private final Color hover = new Color(28, 122, 120);

        RoundedButton(String text) {
            super(text);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setForeground(Color.WHITE);
            setFont(FontKit.bold(18f)); // Inter Bold
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(14, 28, 14, 28));
            setRolloverEnabled(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            boolean isHover = getModel().isRollover();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = 24;
            int w = getWidth(), h = getHeight();

            int shadowOffset = isHover ? 4 : 3;
            g2.fill(new RoundRectangle2D.Double(1, shadowOffset, w - 2, h - 2, arc, arc));

            g2.setColor(isHover ? hover : base);
            g2.fill(new RoundRectangle2D.Double(0, 0, w - 2, h - shadowOffset - 1, arc, arc));

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
