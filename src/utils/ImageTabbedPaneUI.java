package utils;

import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.plaf.ComponentUI;
import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: GB031042
 * Date: 22-Sep-2006
 * Time: 13:38:21
 * To change this template use File | Settings | File Templates.
 */
public class ImageTabbedPaneUI extends BasicTabbedPaneUI
{
    private Image m_image;

    public static ComponentUI createUI(JComponent c) {
        return new ImageTabbedPaneUI();
    }

    public void update(Graphics g, JComponent c) {
        if (c instanceof ImageTabbedPane)
        {
            Image paneImage = ((ImageTabbedPane) c).getPaneBackground();
            int w = c.getWidth();
            int h = c.getHeight();
            int iw = paneImage.getWidth(tabPane);
            int ih = paneImage.getHeight(tabPane);
            if (iw > 0 && ih > 0)
            {
                for (int j=0; j < h; j += ih)
                {
                    for (int i=0; i < w; i += iw)
                    {
                        g.drawImage(paneImage,i,j,tabPane);
                    }
                }
            }
        }
        paint(g,c);
    }

    public void paint(Graphics g, JComponent c) {
        if (c instanceof ImageTabbedPane)
            m_image = ((ImageTabbedPane) c).getTabBackground();
        super.paint(g,c);
    }

    protected void paintTabBackground(Graphics g, int tabPlacement,
                                      int tabIndex, int x, int y, int w, int h, boolean isSelected ) {
        Color tp = tabPane.getBackgroundAt(tabIndex);
        switch(tabPlacement) {
            case LEFT:
                g.drawImage(m_image, x+1, y+1, (w-2)+(x+1), (y+1)+(h-3),
                        0, 0, w, h, tp, tabPane);
                break;
            case RIGHT:
                g.drawImage(m_image, x, y+1, (w-2)+(x), (y+1)+(h-3),
                        0, 0, w, h, tp, tabPane);
                break;
            case BOTTOM:
                g.drawImage(m_image, x+1, y, (w-3)+(x+1), (y)+(h-1),
                        0, 0, w, h, tp, tabPane);
                break;
            case TOP:
                g.drawImage(m_image, x+1, y+1, (w-3)+(x+1), (y+1)+(h-1),
                        0, 0, w, h, tp, tabPane);
        }
    }
}