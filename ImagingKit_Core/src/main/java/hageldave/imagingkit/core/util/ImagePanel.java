/*
 * Copyright 2017 David Haegele
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to 
 * deal in the Software without restriction, including without limitation the 
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or 
 * sell copies of the Software, and to permit persons to whom the Software is 
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * IN THE SOFTWARE.
 */

package hageldave.imagingkit.core.util;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.ImageObserver;
import java.util.function.Function;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import hageldave.imagingkit.core.Img;

/**
 * Panel for displaying Images.
 * The panel behaves as follows:
 * <ul><li> 
 * When not clicked, the Image is scaled to fit the panel's size. The aspect 
 * ratio is preserved and the image is centered inside the panel.
 * </li><li> 
 * When clicked (holding left mouse button), then the image is scaled to its
 * actual size (depending on the panel's size this shrinks or magnifies the 
 * images). In case of magnification, the focus point will be the click point
 * in the 'scale to fit view' on the image.
 * </li></ul>
 * 
 * @author hageldave
 * @since 1.4
 */
@SuppressWarnings("serial")
public class ImagePanel extends JPanel{
	/** First color of checkerboard (0x999999) 
	 * @since 1.4 */
	public static final Color CHECKERBOARD_COLOR_1 = new Color(0x999999);
	/** Second color of checkerboard (0x666666) 
	 * @since 1.4 */
	public static final Color CHECKERBOARD_COLOR_2 = new Color(0x666666);
	

	
	
	/** The image to be displayed, null if not set 
	 * @since 1.4 */
	protected Image img = null;
	/** The left mouse button click point, null if not currently pressed 
	 * @since 1.4 */
	protected Point clickPoint = null;
	/** whether to draw a checkerboard background or not 
	 * @since 1.4 */
	protected boolean useCheckerboardBackground = false;
	

	/** 8 by default */
	private int checkerSize;
	private Stroke checkerStroke;
	
	
	/**
	 * Constructs a new ImagePanel. 
	 * Use {@link #setImage(Image)} or {@link #setImg(Img)} to
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
		this.setBackground(CHECKERBOARD_COLOR_2);
		this.setForeground(CHECKERBOARD_COLOR_1);
		this.checkerSize = 8;
		setCheckerSize(getCheckerSize());
	}
	
	/** 
	 * Sets the size of the squares of the checkerboard background pattern.
	 * <p>
	 * This method will not trigger a call to {@link #repaint()}.
	 * @param checkerSize value greater than 0 and divisible by 2
	 * 
	 * @see #getCheckerSize()
	 * @see #enableCheckerboardBackground(boolean)
	 * @since 1.4
	 */
	protected void setCheckerSize(int checkerSize) {
		this.checkerSize = checkerSize;
		this.checkerStroke = checkerStrokeForSize(checkerSize);
	}
	
	/**
	 * Returns the size of a square of the checkerboard background pattern.
	 * @return size of a checkerboard square
	 * 
	 * @see #setCheckerSize(int)
	 * @see #enableCheckerboardBackground(boolean)
	 * @since 1.4
	 */
	protected int getCheckerSize() {
		return checkerSize;
	}
	
	/**
	 * En- or disables a checkerboard background.
	 * <br> 
	 * The colors used for the checkerboard are the back and foreground color 
	 * of this panel which are by default set to 
	 * {@link #CHECKERBOARD_COLOR_1} and {@link #CHECKERBOARD_COLOR_2}
	 * <br>
	 * (use {@link #setBackground(Color)} or {@link #setForeground(Color)} 
	 * to change them). 
	 * <p>
	 * When disabled, the background is filled with the background color.
	 * <p>
	 * This method will not trigger a call to {@link #repaint()}.
	 * @param useCheckerboardBackground true to enable, false to disable
	 * @return this for chaining
	 * 
	 * @see #isCheckerboardBackgroundEnabled()
	 * @since 1.4
	 */
	public ImagePanel enableCheckerboardBackground(boolean useCheckerboardBackground) {
		this.useCheckerboardBackground = useCheckerboardBackground;
		return this;
	}
	
	/**
	 * Returns true when checkerboard background is enabled, else false.
	 * @return true when enabled, else false
	 * @see #enableCheckerboardBackground(boolean)
	 * @since 1.4
	 */
	public boolean isCheckerboardBackgroundEnabled() {
		return useCheckerboardBackground;
	}
	
	/**
	 * Sets the image that should be displayed by this panel.
	 * This will call {@link #repaint()}.
	 * @param img to be displayed
	 * @return this for chaining
	 * 
	 * @since 1.4
	 */
	public ImagePanel setImage(Image img) {
		this.img = img;
		this.repaint();
		return this;
	}
	
	/**
	 * Sets the image that should be displayed by this panel.
	 * This will call {@link #repaint()}.
	 * @param img to be displayed
	 * @return this for chaining
	 * 
	 * @since 1.4
	 */
	public ImagePanel setImg(Img img) {
		return this.setImage(img.getRemoteBufferedImage());
	}
	
	@Override
	public void paint(Graphics g) {
		final Graphics2D g2d = (Graphics2D) g;
		if(useCheckerboardBackground){
			drawCheckerBoard(g2d);
		} else {
			g2d.setColor(getBackground());
			g2d.fillRect(0, 0, getWidth(), getHeight());
		}
		drawImage(g2d);
	}
	
	
	
	/**
	 * Draws the image to the specified graphics context
	 * @param g graphics context to draw on
	 * @since 1.4
	 */
	protected void drawImage(Graphics2D g) {
		// image observer generator
		Function<Integer, ImageObserver> obs = flags->{
			return (image, infoflags, x, y, width, height)->(infoflags & flags)!=flags;
		};
		ImageObserver obs_w = obs.apply(ImageObserver.WIDTH);
		ImageObserver obs_h = obs.apply(ImageObserver.HEIGHT);
		ImageObserver obs_allbits = obs.apply(ImageObserver.ALLBITS);
		
		Image img = this.img;
		if(img != null){
			Point clickPoint = this.clickPoint;
			if(clickPoint == null){
				double imgRatio = img.getWidth(obs_w)*1.0/img.getHeight(obs_h);
				double panelRatio = this.getWidth()*1.0/this.getHeight();
				if(imgRatio > panelRatio) {
					// image wider than panel
					int height = (int) (this.getWidth()/imgRatio);
					int y = (this.getHeight()-height)/2;
					g.drawImage(img, 0, y, this.getWidth(), y+height, 0, 0, img.getWidth(obs_w), img.getHeight(obs_h), obs_allbits);
				} else {
					// image higher than panel
					int width = (int) (this.getHeight()*imgRatio);
					int x = (this.getWidth()-width)/2;
					g.drawImage(img, x, 0, x+width, this.getHeight(), 0, 0, img.getWidth(obs_w), img.getHeight(obs_h), obs_allbits);
				}
			} else {
				float relX = clickPoint.x / (1.0f * this.getWidth());
				float relY = clickPoint.y / (1.0f * this.getHeight());
				int imgW = img.getWidth(obs_w);
				int imgH = img.getHeight(obs_h);
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
				
				// manual clipping of image drawing coordinates
				// depending on OS and window manager this may be needed
				// to prevent trail artifacts when moving the image around
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
				
				g.drawImage(img, 
						dx1, dy1, dx2, dy2, 
						sx1, sy1, sx2, sy2, 
						obs_allbits);
			}
		}
	}
	
	/**
	 * Draws the checkerboard background to the specified graphics context.
	 * @param g2d graphics context to draw on
	 * @since 1.4
	 */
	protected void drawCheckerBoard(Graphics2D g2d) {
		g2d.setColor(getBackground());
		g2d.fillRect(0, 0, getWidth(), getHeight());
		g2d.setColor(getForeground());
		
		final int checkSize = this.checkerSize;
		final int halfCheckSize = checkSize/2;
		final Stroke checker = this.checkerStroke;
		final Stroke backup = g2d.getStroke();
		g2d.setStroke(checker);
		final int width = this.getWidth()+checkSize;
		final int height = this.getHeight()+checkSize;
		for(int i = halfCheckSize; i < height; i+=checkSize*2){
			g2d.drawLine(halfCheckSize, i, width, i);
			g2d.drawLine(checkSize+halfCheckSize, i+checkSize, width, i+checkSize);
		}
		g2d.setStroke(backup);
	}
	
	/**
	 * Creates a stroke for a checkerboard pattern with specified size. <br>
	 * This is used by {@link #setCheckerSize(int)}
	 * @param size width of the stroke
	 * @return the stroke
	 * @since 1.4
	 */
	protected static final Stroke checkerStrokeForSize(int size) {
		return new BasicStroke(size, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 1, new float[]{0,size*2}, 0);
	}
	
}