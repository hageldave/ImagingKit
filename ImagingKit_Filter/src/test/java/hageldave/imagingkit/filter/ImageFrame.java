package hageldave.imagingkit.filter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import hageldave.imagingkit.core.TheImageObserver;

@SuppressWarnings("serial")
public class ImageFrame extends JFrame {
	

	public static class ImagePanel extends JPanel{
		
		Image img = null;
		Point clickPoint = null;
		ImageObserver obs = hageldave.imagingkit.core.TheImageObserver.OBS_ALLBITS;
		
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
		}
		
		public void setImg(Image img) {
			this.img = img;
			this.repaint();
		}
		
		@Override
		public void paint(Graphics painter) {
			super.paint(painter);
			if(img != null){
				if(clickPoint == null){
					painter.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), 0, 0, img.getWidth(obs), img.getHeight(obs), obs);
				} else {
					float relX = clickPoint.x / (1.0f * this.getWidth());
					float relY = clickPoint.y / (1.0f * this.getHeight());
					int imgW = img.getWidth(obs);
					int imgH = img.getHeight(obs);
					int imgX = (int) (relX*imgW);
					int imgY = (int) (relY*imgH);
					
					painter.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), imgX-this.getWidth()/2, imgY-this.getHeight()/2, imgX+this.getWidth()-(this.getWidth()/2), imgY+this.getHeight()-(this.getHeight()/2), obs);
				}
			}
		}
		
	}
	
	ImagePanel panel;
	
	public ImageFrame() {
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.panel = new ImagePanel();
		this.panel.setBackground(Color.magenta);
		this.getContentPane().add(this.panel);
		this.setMinimumSize(new Dimension(300,300));
		this.pack();
	}
	
	public void setImg(Image img){
		panel.setImg(img);
		this.setTitle(img.getWidth(TheImageObserver.OBS_WIDTHHEIGHT) + " x " + img.getHeight(TheImageObserver.OBS_WIDTHHEIGHT) + " Pixels");
	}
	
	public void setPanelBGColor(Color color){
		this.panel.setBackground(color);
	}
	
	public static ImageFrame display(final BufferedImage img){
		ImageFrame frame = new ImageFrame();
		SwingUtilities.invokeLater( new Runnable() {
			@Override
			public void run() {
				frame.setImg(img);
				frame.setVisible(true);
			}
		});
		return frame;
	}

}
