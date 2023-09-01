/*
 * Copyright 2023 David Haegele
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


import hageldave.imagingkit.core.Img;
import hageldave.imagingkit.core.io.ImageSaver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.ImageObserver;
import java.util.Arrays;
import java.util.function.Function;

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
public class ImagePanel extends JPanel {
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

	protected AffineTransform zoomAffineTransform = new AffineTransform();
	protected AffineTransform panningAffineTransform = new AffineTransform();
	protected int pressedKeycode = -1;
	
	/**
	 * Constructs a new ImagePanel. 
	 * Use {@link #setImage(Image)} or {@link #setImg(Img)} to
	 * set the image that should be displayed by this panel.
	 * @since 1.4
	 */
	public ImagePanel() {
        this.setFocusable(true);
		JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem saveItem = new JMenuItem("Save image");
		popupMenu.add(saveItem);

		saveItem.addActionListener(e -> {
			FileDialog saveDialog = new FileDialog(new Frame(), "Choose where to save the file.", FileDialog.SAVE);
			saveDialog.setVisible(true);
			String fileName = saveDialog.getFile();
			String directory = saveDialog.getDirectory();
			if (fileName != null) {
				for (String fileFormat: ImageSaver.getSaveableImageFileFormats()) {
					if (fileName.endsWith(fileFormat)) {
						ImageSaver.saveImage(img, directory + fileName);
						System.out.println("Image has been exported to " + directory + fileName + ".");
					}
				}
			}
			popupMenu.setVisible(false);
		});

        JMenuItem fitFrame = new JMenuItem("Fit to frame");
        popupMenu.add(fitFrame);
        fitFrame.addActionListener(e ->  {
			this.setZoomAffineTransform(new AffineTransform());
			this.setPanningAffineTransform(new AffineTransform());
        });

		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if(SwingUtilities.isLeftMouseButton(e)){
					if (pressedKeycode != KeyEvent.VK_E) {
						ImagePanel.this.clickPoint = e.getPoint();
						ImagePanel.this.repaint();
						popupMenu.setVisible(false);
					}
				} else if (SwingUtilities.isRightMouseButton(e)) {
					popupMenu.setLocation(e.getXOnScreen(), e.getYOnScreen());
					popupMenu.setVisible(true);
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

		this.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				super.keyPressed(e);
				ImagePanel.this.pressedKeycode = e.getKeyCode();
			}

			@Override
			public void keyReleased(KeyEvent e) {
				super.keyReleased(e);
				ImagePanel.this.pressedKeycode = -1;
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
			if (clickPoint == null) {
				g.transform(zoomAffineTransform);
				g.transform(panningAffineTransform);

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

	public AffineTransform getZoomAffineTransform() {
		return zoomAffineTransform;
	}

	public void setZoomAffineTransform(AffineTransform zoomAffineTransform) {
		this.zoomAffineTransform = zoomAffineTransform;
		this.repaint();
	}

	public AffineTransform getPanningAffineTransform() {
		return panningAffineTransform;
	}

	public void setPanningAffineTransform(AffineTransform panningAffineTransform) {
		this.panningAffineTransform = panningAffineTransform;
		this.repaint();
	}
	
	/**
	 * The PanelInteraction class is an abstract class that can be used to implement interactions with an {@link ImagePanel}.
	 * It implements the {@link KeyListener} interface and provides a {@link #pressedKeycode} field that can be used to
	 * check which key is currently pressed.
	 * <br>
	 * Implementations of the abstract class can be found in the {@link hageldave.imagingkit.core.interaction} package.
	 */
	public static abstract class PanelInteraction extends MouseAdapter implements KeyListener {
	    int pressedKeycode = -1;
	    @Override
	    public void keyPressed(KeyEvent e) {
	        this.pressedKeycode = e.getKeyCode();
	    }

	    @Override
	    public void keyReleased(KeyEvent e) {
	        this.pressedKeycode = -1;
	    }

	    @Override
	    public void keyTyped(KeyEvent e) {

	    }

	    /**
	     * Registers the interaction class to the image panel (e.g., calling addMouseListener() on the image panel).
	     *
	     * @return {@link PanelInteraction} this for chaining
	     */
	    public abstract PanelInteraction register();

	    /**
	     * Unregisters the interaction class from the image panel (e.g., calling removeMouseListener() on the image panel).
	     *
	     * @return {@link PanelInteraction} this for chaining
	     */
	    public abstract PanelInteraction deRegister();
	}
	
	/**
	 * This class implements panning of an {@link ImagePanel} by dragging the mouse.
	 * To activate the panning, the 'e' key has to be pressed.
	 * The panning is done by translating the panningAffineTransform {@link AffineTransform} of the image panel.
	 * <br>
	 * The ImagePanning class first has to be registered on the image panel by calling register() and
	 * passing the image panel object to the ImagePanning constructor.
	 * It can be deregistered if it isn't used anymore by calling deRegister().
	 * <br>
	 * Example use of the ImagePanning class:
	 * <pre>ImagePanning ip = new ImagePanning(imagePanel).register();</pre>
	 * <pre>ip.deRegister();</pre>
	 */
	public static class ImagePanning extends PanelInteraction  {
	    final protected ImagePanel imagePanel;
	    protected Point2D dragStart = null;

	    /**
	     * Constructs a new ImagePanning interaction for the given image panel.
	     * @param imagePanel image panel to pan
	     */
	    public ImagePanning(ImagePanel imagePanel) {
	        this.imagePanel = imagePanel;
	    }

	    @Override
	    public void mouseDragged(MouseEvent e) {
	        super.mouseDragged(e);
	        if (this.dragStart != null) {
	            double mouseTx = e.getX()-this.dragStart.getX();
	            double mouseTy = e.getY()-this.dragStart.getY();
	            double scaleX = imagePanel.getZoomAffineTransform().getScaleX();
	            double scaleY = imagePanel.getZoomAffineTransform().getScaleY();
	            mouseTx /= scaleX;
	            mouseTy /= scaleY;

	            AffineTransform transformedTrans = imagePanel.getPanningAffineTransform();
	            transformedTrans.translate(mouseTx, mouseTy);
	            imagePanel.setPanningAffineTransform(transformedTrans);
	            this.dragStart.setLocation(e.getX(), e.getY());
	        }
	    }

	    @Override
	    public void mousePressed(MouseEvent e) {
	        if (pressedKeycode == KeyEvent.VK_E) {
	            this.dragStart = e.getPoint();
	        }
	    }

	    @Override
	    public void mouseReleased(MouseEvent e) {
	        super.mouseReleased(e);
	        this.dragStart = null;
	    }

	    @Override
	    public void keyPressed(KeyEvent e) {
	        super.keyPressed(e);
	        if (e.getKeyCode() == KeyEvent.VK_E) {
	            imagePanel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
	        }
	    }

	    @Override
	    public void keyReleased(KeyEvent e) {
	        super.keyReleased(e);
	        imagePanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	    }

	    @Override
	    public ImagePanning register(){
	        if( ! Arrays.asList(imagePanel.getMouseListeners()).contains(this))
	            imagePanel.addMouseListener(this);
	        if( ! Arrays.asList(imagePanel.getMouseMotionListeners()).contains(this))
	            imagePanel.addMouseMotionListener(this);
	        if ( ! Arrays.asList(imagePanel.getKeyListeners()).contains(this))
	            imagePanel.addKeyListener(this);
	        return this;
	    }

	    @Override
	    public ImagePanning deRegister(){
	        imagePanel.removeMouseListener(this);
	        imagePanel.removeMouseMotionListener(this);
	        imagePanel.removeKeyListener(this);
	        return this;
	    }
	}
	
	/**
	 * This class implements zooming of an {@link ImagePanel} by scrolling the mouse wheel.
	 * The zooming of this class is different from that of {@link MouseFocusedZooming},
	 * as it always zooms into the center of the current viewport, not on the current mouse position.
	 * <br>
	 * To zoom on the image panel, the 'shift' key has to be pressed.
	 * The zooming is done by scaling the zoomAffineTransform {@link AffineTransform} of the image panel.
	 * <br>
	 * The ImageZooming class first has to be registered on the image panel by calling register().
	 * It can be deregistered if it isn't used anymore by calling deRegister().
	 * <br>
	 * Example use of the ImageZooming class:
	 * <pre>ImageZooming iz = new ImageZooming(imagePanel).register();</pre>
	 * <pre>iz.deRegister();</pre>
	 */
	public static class ImageZooming extends PanelInteraction  {
	    final protected ImagePanel imagePanel;

	    /**
	     * Constructs a new ImageZooming interaction for the given image panel.
	     * @param imagePanel image panel to zoom
	     */
	    public ImageZooming(ImagePanel imagePanel) {
	        this.imagePanel = imagePanel;
	    }

	    @Override
	    public void mouseWheelMoved(MouseWheelEvent e) {
	        super.mouseWheelMoved(e);
	        if (pressedKeycode == KeyEvent.VK_SHIFT) {
	            AffineTransform zoomAT = this.calcZoomAffineTransform(Math.pow(1.7, e.getWheelRotation() * 0.1));
	            AffineTransform panelAT = this.imagePanel.getZoomAffineTransform();
	            panelAT.concatenate(zoomAT);
	            this.imagePanel.setZoomAffineTransform(panelAT);
	        }
	    }

	    protected AffineTransform calcZoomAffineTransform(double zoom) {
	        double imageWidth = imagePanel.getBounds().getWidth();
	        double imageHeight = imagePanel.getBounds().getHeight();

	        AffineTransform zoomAT = new AffineTransform();
	        zoomAT.translate(imageWidth / 2.0, imageHeight / 2.0);
	        zoomAT.scale(zoom, zoom);
	        zoomAT.translate(-(imageWidth / 2.0), -(imageHeight / 2.0));
	        return zoomAT;
	    }

	    @Override
	    public ImageZooming register(){
	        if( ! Arrays.asList(imagePanel.getMouseWheelListeners()).contains(this))
	            imagePanel.addMouseWheelListener(this);
	        if ( ! Arrays.asList(imagePanel.getKeyListeners()).contains(this))
	            imagePanel.addKeyListener(this);
	        return this;
	    }

	    @Override
	    public ImageZooming deRegister(){
	        imagePanel.removeMouseWheelListener(this);
	        imagePanel.removeKeyListener(this);
	        return this;
	    }
	}
	
	/**
	 * This class implements zooming of an {@link ImagePanel} by scrolling the mouse wheel.
	 * The zooming of this class is different from that of {@link ImageZooming},
	 * as it always zooms onto the current mouse position, not into the center of the current viewport.
	 * <br>
	 * To zoom on the image panel, the 'shift' key has to be pressed.
	 * The zooming is done by scaling the zoomAffineTransform {@link AffineTransform} of the image panel.
	 * <br>
	 * The MouseFocusedZooming class first has to be registered on the image panel by calling register().
	 * It can be deregistered if it isn't used anymore by calling deRegister().
	 * <br>
	 * Example use of the MouseFocusedZooming class:
	 * <pre>MouseFocusedZooming mfz = new MouseFocusedZooming(imagePanel).register();</pre>
	 * <pre>mfz.deRegister();</pre>
	 */
	public static class MouseFocusedZooming extends PanelInteraction {
	    final protected ImagePanel imagePanel;

	    /**
	     * Constructs a new MouseFocusedZooming object for the given image panel.
	     * @param imagePanel image panel to zoom
	     */
	    public MouseFocusedZooming(ImagePanel imagePanel) {
	        this.imagePanel = imagePanel;
	    }

	    @Override
	    public void mouseWheelMoved(MouseWheelEvent e) {
	        super.mouseWheelMoved(e);
	        if (pressedKeycode == KeyEvent.VK_SHIFT) {
	            try {
	                AffineTransform zoomAT = this.calcZoomAffineTransform(e, Math.pow(1.7, e.getWheelRotation() * 0.1));
	                AffineTransform panelAT = this.imagePanel.getZoomAffineTransform();
	                panelAT.concatenate(zoomAT);
	                this.imagePanel.setZoomAffineTransform(panelAT);
	            } catch (NoninvertibleTransformException ex) {
	                throw new RuntimeException(ex);
	            }
	        }
	    }

	    protected AffineTransform calcZoomAffineTransform(MouseWheelEvent e, double zoom) throws NoninvertibleTransformException {
	        Point2D.Double mouseCoordinates = new Point2D.Double(e.getX(), e.getY());
	        mouseCoordinates = (Point2D.Double) imagePanel.getZoomAffineTransform().inverseTransform(mouseCoordinates, mouseCoordinates);

	        AffineTransform zoomAT = new AffineTransform();
	        zoomAT.translate(mouseCoordinates.getX(), mouseCoordinates.getY());
	        zoomAT.scale(zoom, zoom);
	        zoomAT.translate(-mouseCoordinates.getX(), -mouseCoordinates.getY());
	        return zoomAT;
	    }

	    @Override
	    public MouseFocusedZooming register(){
	        if( ! Arrays.asList(imagePanel.getMouseWheelListeners()).contains(this))
	            imagePanel.addMouseWheelListener(this);
	        if ( ! Arrays.asList(imagePanel.getKeyListeners()).contains(this))
	            imagePanel.addKeyListener(this);
	        return this;
	    }

	    @Override
	    public MouseFocusedZooming deRegister(){
	        imagePanel.removeMouseWheelListener(this);
	        imagePanel.removeKeyListener(this);
	        return this;
	    }
	}
	
	/**
	 * This class implements panning of an {@link ImagePanel} by scrolling the mouse wheel.
	 * To avoid conflicts with other interactions, the 'shift' key can't be pressed during the panning.
	 * The panning is either done horizontally or vertically, depending on whether the 'alt' key is pressed.
	 * <br>
	 * The MouseWheelPanning class first has to be registered on the image panel by calling register() and
	 * passing the image panel object to the MouseWheelPanning constructor.
	 * It can be deregistered if it isn't used anymore by calling deRegister().
	 * <br>
	 * Example use of the ImagePanning class:
	 * <pre>MouseWheelPanning mwp = new MouseWheelPanning(imagePanel).register();</pre>
	 * <pre>mwp.deRegister();</pre>
	 */
	public static class MouseWheelPanning extends PanelInteraction  {

	    final protected ImagePanel imagePanel;

	    /**
	     * Constructs a new MouseWheelPanning interaction for the given image panel.
	     * @param imagePanel image panel to pan
	     */
	    public MouseWheelPanning(ImagePanel imagePanel) {
	        this.imagePanel = imagePanel;
	    }

	    @Override
	    public void mouseWheelMoved(MouseWheelEvent e) {
	        super.mouseWheelMoved(e);
	        if (pressedKeycode != KeyEvent.VK_SHIFT) {
	            double scroll = e.getPreciseWheelRotation() * 1.5;
	            AffineTransform panningAT = imagePanel.getPanningAffineTransform();
	            double scaleX = imagePanel.getZoomAffineTransform().getScaleX();
	            double scaleY = imagePanel.getZoomAffineTransform().getScaleY();
	            if (pressedKeycode == KeyEvent.VK_ALT) {
	                panningAT.translate(scroll / scaleX, 0);
	            } else {
	                panningAT.translate(0, scroll / scaleY);
	            }
	            imagePanel.setPanningAffineTransform(panningAT);
	        }
	    }

	    @Override
	    public MouseWheelPanning register(){
	        if( ! Arrays.asList(imagePanel.getMouseWheelListeners()).contains(this))
	            imagePanel.addMouseWheelListener(this);
	        if ( ! Arrays.asList(imagePanel.getKeyListeners()).contains(this))
	            imagePanel.addKeyListener(this);
	        return this;
	    }

	    @Override
	    public MouseWheelPanning deRegister(){
	        imagePanel.removeMouseWheelListener(this);
	        imagePanel.removeKeyListener(this);
	        return this;
	    }
	}
}