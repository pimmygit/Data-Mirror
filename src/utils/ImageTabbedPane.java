package utils;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: GB031042
 * Date: 22-Sep-2006
 * Time: 13:23:40
 * To change this template use File | Settings | File Templates.
 */
public class ImageTabbedPane extends JTabbedPane
{
    // Display properties
    private Image m_tabBackground;
    private Image m_paneBackground;

    public ImageTabbedPane(ImageIcon tabBackground, ImageIcon paneBackground) {
        m_tabBackground = tabBackground.getImage();
        m_paneBackground = paneBackground.getImage();
        setUI((ImageTabbedPaneUI) ImageTabbedPaneUI.createUI(this));
    }

    public void setTabBackground(Image i) {
        m_tabBackground = i;
    }

    public void setPaneBackground(Image i) {
        m_paneBackground = i;
    }

    public Image getTabBackground() {
        return m_tabBackground;
    }

    public Image getPaneBackground() {
        return m_paneBackground;
    }
}