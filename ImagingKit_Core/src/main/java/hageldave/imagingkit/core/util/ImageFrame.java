package hageldave.imagingkit.core.util;

import java.awt.Dimension;
import java.awt.Image;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import hageldave.imagingkit.core.Img;

import static hageldave.imagingkit.core.TheImageObserver.*;

@SuppressWarnings("serial")
public class ImageFrame extends JFrame {
	
	ImagePanel panel;
	
	public ImageFrame() {
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.panel = new ImagePanel();
		this.getContentPane().add(this.panel);
		this.setMinimumSize(new Dimension(300,300));
		this.pack();
	}
	
	public void setImg(final Image img){
		panel.setImage(img);
		this.setTitle(img.getWidth(OBS_WIDTHHEIGHT) + " x " + img.getHeight(OBS_WIDTHHEIGHT) + " Pixels");
	}
	
	public void setImg(final Img img){
		this.setImg(img.getRemoteBufferedImage());
	}
	
	public static ImageFrame display(final Image img){
		ImageFrame frame = new ImageFrame();
		SwingUtilities.invokeLater( () -> {
			frame.setImg(img);
			frame.setVisible(true);
		});
		return frame;
	}
	
	public static ImageFrame display(final Img img){
		return display(img.getRemoteBufferedImage());
	}
	
	public ImagePanel getPanel() {
		return panel;
	}

}
