

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import hageldave.imagingkit.core.ColorSpaceTransformation;
import hageldave.imagingkit.core.Img;
import hageldave.imagingkit.core.Pixel;

import static hageldave.imagingkit.core.TheImageObserver.*;

@SuppressWarnings("serial")
public class ImageFrame extends JFrame {
	

	public static class ImagePanel extends JPanel{
		
		Image img = null;
		Point clickPoint = null;
		boolean useCheckerboardBackground = false;
		
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
		
		public void enableCheckerboardBackground(boolean useCheckerboardBackground) {
			this.useCheckerboardBackground = useCheckerboardBackground;
		}
		
		public boolean isCheckerboardBackgroundEnabled() {
			return useCheckerboardBackground;
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
			if(useCheckerboardBackground){
				drawCheckerBoard(painter);
			} else {
				painter.setColor(getBackground());
				painter.fillRect(0, 0, getWidth(), getHeight());
			}
			
			
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
					
					int dx1,dy1,dx2,dy2, sx1,sy1,sx2,sy2;
					dx1 = dy1 = 0;
					dx2 = this.getWidth();
					dy2 = this.getHeight();
					
					sx1 = imgX-this.getWidth()/2;
					sy1 = imgY-this.getHeight()/2;
					sx2 = imgX+this.getWidth()-(this.getWidth()/2);
					sy2 = imgY+this.getHeight()-(this.getHeight()/2);
					
					if(sx1 < 0){
						dx1-=sx1; sx1=0;
					}
					if(sy1 < 0){
						dy1-=sy1; sy1=0;
					}
					if(sx2 > imgW){
						dx2-=(sx2-imgW); sx2=imgW;
					}
					if(sy2 > imgH){
						dy2-=(sy2-imgH); sy2=imgH;
					}
					
					painter.drawImage(img, 
							dx1, dy1, dx2, dy2, 
							sx1, sy1, sx2, sy2, 
							OBS_ALLBITS);
				}
			}
		}
		
		private void drawCheckerBoard(Graphics painter) {
			painter.setColor(Color.darkGray);
			painter.fillRect(0, 0, getWidth(), getHeight());
			painter.setColor(Color.lightGray);
			
			int checkSize = 10;
			int halfCheckSize = checkSize/2;
			Graphics2D g2d = (Graphics2D) painter;
			float[] dash = {0,checkSize*2};
			Stroke checker = new BasicStroke(checkSize, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 1, dash, 0);
			Stroke backup = g2d.getStroke();
			g2d.setStroke(checker);
			int width = this.getWidth()+checkSize;
			int height = this.getHeight()+checkSize;
			for(int i = halfCheckSize; i < height; i+=checkSize*2){
				g2d.drawLine(halfCheckSize, i, width, i);
				g2d.drawLine(checkSize+halfCheckSize, i+checkSize, width, i+checkSize);
				
			}
			g2d.setStroke(backup);
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
	
	public static ImageFrame display(final Image img){
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
	
	public ImagePanel getPanel() {
		return panel;
	}
	
	public static ImageFrame display(final Img img){
		return display(img.getRemoteBufferedImage());
	}

}
