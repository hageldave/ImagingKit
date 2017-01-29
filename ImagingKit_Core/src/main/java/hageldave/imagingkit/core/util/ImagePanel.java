package hageldave.imagingkit.core.util;

import static hageldave.imagingkit.core.TheImageObserver.OBS_ALLBITS;
import static hageldave.imagingkit.core.TheImageObserver.OBS_WIDTHHEIGHT;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import hageldave.imagingkit.core.Img;

/**
 * Panel for displaying Images.
 * The panel behaves as follows:
 * <li> 
 * When not clicked, the Image is scaled to fit the panel's size. The aspect 
 * ratio is preserved and the image is centered inside the panel.
 * </li><li> 
 * When clicked (holding left mouse button), then the image is scaled to its
 * actual size (depending on the panel's size this shrinks or magnifies the 
 * images). In case of magnification, the focus point will be the click point
 * in the 'scale to fit view' on the image.
 * </li>
 * 
 * @author hageldave
 * @since 1.4
 */
@SuppressWarnings("serial")
public class ImagePanel extends JPanel{
	static final Color checkerColor1 = new Color(0x999999);
	static final Color checkerColor2 = new Color(0x666666);
	
	Image img = null;
	Point clickPoint = null;
	boolean useCheckerboardBackground = false;
	
	/**
	 * Constructs a new ImagePanel. Use {@link #setImage(Image)} to
	 * set the image that should be displayed by this panel.
	 * @since 1.4
	 */
	public ImagePanel() {
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if(SwingUtilities.isLeftMouseButton(e)){
					ImagePanel.this.clickPoint = e.getPoint();
					ImagePanel.this.repaint();
				}
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				if(SwingUtilities.isLeftMouseButton(e)){
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
	
	/**
	 * En- or 
	 * @param useCheckerboardBackground
	 * @return
	 */
	public ImagePanel enableCheckerboardBackground(boolean useCheckerboardBackground) {
		this.useCheckerboardBackground = useCheckerboardBackground;
		return this;
	}
	
	public boolean isCheckerboardBackgroundEnabled() {
		return useCheckerboardBackground;
	}
	
	public ImagePanel setImage(Image img) {
		this.img = img;
		this.repaint();
		return this;
	}
	
	
	public ImagePanel setImg(Img img) {
		return this.setImage(img.getRemoteBufferedImage());
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
		painter.setColor(checkerColor1);
		painter.fillRect(0, 0, getWidth(), getHeight());
		painter.setColor(checkerColor2);
		
		final int checkSize = 8;
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