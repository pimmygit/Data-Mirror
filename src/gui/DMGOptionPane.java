package gui;

import java.awt.Frame;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;

public class DMGOptionPane extends JOptionPane
{
    
    public DMGOptionPane( )
    {
	    Frame frame = new Frame();
        ImageIcon img = new ImageIcon("images/generator.gif");
        frame.setIconImage(img.getImage());
        setRootFrame(frame);
    }
}



