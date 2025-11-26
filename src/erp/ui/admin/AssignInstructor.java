package erp.ui.admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import erp.ui.common.FontKit;
import erp.ui.common.RoundedPanel;

public class AssignInstructor extends AdminFrameBase {

    private static final Color BG       = new Color(246, 247, 248);
    private static final Color TEXT_600 = new Color(100, 116, 139);
    private static final Color CARD     = Color.WHITE;

    public AssignInstructor(String displayName) {
        this(null, displayName);
    }

    public AssignInstructor(String adminId, String displayName) {
        super(adminId, displayName, Page.ASSIGN);
        setTitle("IIITD ERP – Assign Instructors");
        if (metaLabel != null) {
            metaLabel.setText("System Administrator");
        }
    }

    @Override
    protected JComponent buildMainContent() {
        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);

        RoundedPanel hero = new RoundedPanel(24);
        hero.setBackground(TEAL_DARK);
        hero.setBorder(new EmptyBorder(24, 28, 24, 28));
        hero.setLayout(new BorderLayout());

        JLabel h1 = new JLabel("Assign Instructors");
        h1.setFont(FontKit.bold(24f));
        h1.setForeground(Color.WHITE);
        hero.add(h1, BorderLayout.WEST);

        JLabel right = new JLabel("Logged in as " + userDisplayName);
        right.setFont(FontKit.regular(13f));
        right.setForeground(new Color(200, 230, 225));
        hero.add(right, BorderLayout.EAST);

        main.add(hero, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(24, 8, 24, 8));

        RoundedPanel card = new RoundedPanel(20);
        card.setBackground(CARD);
        card.setBorder(new EmptyBorder(20, 24, 20, 24));
        card.setLayout(new BorderLayout());

        JLabel label = new JLabel("Instructor–section assignment UI goes here");
        label.setFont(FontKit.regular(14f));
        label.setForeground(TEXT_600);
        card.add(label, BorderLayout.CENTER);

        content.add(card);

        JScrollPane sc = new JScrollPane(content);
        sc.setBorder(null);
        sc.getViewport().setBackground(BG);
        sc.getVerticalScrollBar().setUnitIncrement(16);

        main.add(sc, BorderLayout.CENTER);
        return main;
    }
}
