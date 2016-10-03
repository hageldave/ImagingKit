package hageldave.imagingkit.core;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import static hageldave.imagingkit.core.TheImageObserver.*;

@SuppressWarnings("serial")
public class ImageFrame extends JFrame {
	

	public static class ImagePanel extends JPanel{
		
		Image img = null;
		Point clickPoint = null;
		
		public ImagePanel() {
			this.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					if(e.getButton() == MouseEvent.BUTTON1){
						ImagePanel.this.clickPoint = e.getPoint();
						ImagePanel.this.repaint();
					}
				}
				@Override
				public void mouseReleased(MouseEvent e) {
					if(e.getButton() == MouseEvent.BUTTON1){
						ImagePanel.this.clickPoint = null;
						ImagePanel.this.repaint();
					}
				}
			});
			this.addMouseMotionListener(new MouseAdapter() {
				@Override
				public void mouseDragged(MouseEvent e) {
					if(clickPoint != null){
						ImagePanel.this.clickPoint = e.getPoint();
						ImagePanel.this.repaint();
					}
				}
			});
			this.setBackground(Color.darkGray);
		}
		
		public void setImg(Image img) {
			this.img = img;
			this.repaint();
		}
		
		public void setImg(Img img) {
			this.setImg(img.getRemoteBufferedImage());
		}
		
		@Override
		public void paint(Graphics painter) {
			painter.setColor(getBackground());
			painter.fillRect(0, 0, getWidth(), getHeight());
			if(img != null){
				if(clickPoint == null){
					double imgRatio = img.getWidth(OBS_WIDTHHEIGHT)*1.0/img.getHeight(OBS_WIDTHHEIGHT);
					double panelRatio = this.getWidth()*1.0/this.getHeight();
					if(imgRatio > panelRatio) {
						// image wider than panel
						int height = (int) (this.getWidth()/imgRatio);
						int y = (this.getHeight()-height)/2;
						painter.drawImage(img, 0, y, this.getWidth(), y+height, 0, 0, img.getWidth(OBS_WIDTHHEIGHT), img.getHeight(OBS_WIDTHHEIGHT), OBS_ALLBITS);
					} else {
						// image higher than panel
						int width = (int) (this.getHeight()*imgRatio);
						int x = (this.getWidth()-width)/2;
						painter.drawImage(img, x, 0, x+width, this.getHeight(), 0, 0, img.getWidth(OBS_WIDTHHEIGHT), img.getHeight(OBS_WIDTHHEIGHT), OBS_ALLBITS);
					}
				} else {
					float relX = clickPoint.x / (1.0f * this.getWidth());
					float relY = clickPoint.y / (1.0f * this.getHeight());
					int imgW = img.getWidth(OBS_WIDTHHEIGHT);
					int imgH = img.getHeight(OBS_WIDTHHEIGHT);
					int imgX = (int) (relX*imgW);
					int imgY = (int) (relY*imgH);
					
					painter.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), imgX-this.getWidth()/2, imgY-this.getHeight()/2, imgX+this.getWidth()-(this.getWidth()/2), imgY+this.getHeight()-(this.getHeight()/2), OBS_ALLBITS);
				}
			}
		}
		
	}
	
	ImagePanel panel;
	
	public ImageFrame() {
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.panel = new ImagePanel();
		this.getContentPane().add(this.panel);
		this.setMinimumSize(new Dimension(300,300));
		this.pack();
	}
	
	public void setImg(Image img){
		panel.setImg(img);
		this.setTitle(img.getWidth(OBS_WIDTHHEIGHT) + " x " + img.getHeight(OBS_WIDTHHEIGHT) + " Pixels");
	}
	
	public void setImg(Img img){
		this.setImg(img.getRemoteBufferedImage());
	}
	
	public void setPanelBGColor(Color color){
		this.panel.setBackground(color);
	}
	
	public static void display(final Image img){
		SwingUtilities.invokeLater( new Runnable() {
			
			@Override
			public void run() {
				ImageFrame frame = new ImageFrame();
				frame.setImg(img);
				frame.setVisible(true);
			}
		});
	}
	
	public ImagePanel getPanel() {
		return panel;
	}
	
	public static void display(final Img img){
		display(img.getRemoteBufferedImage());
	}

}
