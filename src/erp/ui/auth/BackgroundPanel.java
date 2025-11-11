package erp.ui.auth;
import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import javax.imageio.ImageIO;

public class BackgroundPanel extends JPanel {
    private Image backgroundImage;
    public BackgroundPanel(String imagePath) {
        try (InputStream in = getClass().getResourceAsStream(imagePath)) {
            if (in != null) {
                backgroundImage = ImageIO.read(in);
                System.out.println("✅ Background image loaded successfully from " + imagePath);
            } else {
                System.err.println("❌ Could not find image at " + imagePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        setLayout(new BorderLayout());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(Color.GRAY);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.RED);
            g.drawString("No background image found", 20, 20);
        }
    }
}
