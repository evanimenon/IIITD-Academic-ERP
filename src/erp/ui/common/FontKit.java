package erp.ui.common;

import javax.swing.*;
import java.awt.*;

public final class FontKit {
    private static Font REG, SEMI, BOLD;

    private FontKit() {} // prevent instantiation

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
        try (var in = FontKit.class.getResourceAsStream(cp)) {
            if (in == null) throw new IllegalStateException("font missing: " + cp);
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
            if (v instanceof javax.swing.plaf.FontUIResource)
                UIManager.put(k, new javax.swing.plaf.FontUIResource(f));
        }
    }
}

