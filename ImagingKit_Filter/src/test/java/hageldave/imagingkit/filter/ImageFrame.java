package hageldave.imagingkit.filter;

import java.awt.Dimension;
import java.awt.Image;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import hageldave.imagingkit.core.Img;

import static hageldave.imagingkit.core.TheImageObserver.*;

/**
 * {@link JFrame} for displaying an image utilizing an {@link ImagePanel}.
 * This class is merely meant for debugging purposes when you want
 * to quickly view an image without the need to export it as an
 * image file.
 * <p>
 * Use the static method {@link #display(Image)} or {@link #display(Img)}
 * to quickly obtain an ImageFrame that displays the specified image.
 * <p>
 * Use {@link #getPanel()} to access the frame's ImagePanel.
 * 
 * @author hageldave
 * @since 1.4
 */
@SuppressWarnings("serial")
public class ImageFrame extends JFrame {
	
	/** the {@link ImagePanel} used by this ImageFrame 
	 * @since 1.4 */
	protected ImagePanel panel;
	
	/**
	 * Constructs a new ImagePanel.<br>
	 * The Frame is initialized via the argument less JFrame constructor
	 * {@link JFrame#JFrame()}.<br>
	 * An {@link ImagePanel} is added to the frame's content pane.
	 * The ImagePanel can be accessed using {@link #getPanel()}.
	 * <p>
	 * For a quick setup of this frame the {@link #useDefaultSettings()}
	 * method can be used to make this frame ready to go.
	 * 
	 * @since 1.4
	 */
	public ImageFrame() {
		super();
		this.panel = new ImagePanel();
		this.getContentPane().add(this.panel);
	}
	
	/**
	 * Sets default close operation to {@link JFrame#EXIT_ON_CLOSE}, <br>
	 * sets the minimum size to 300x300 <br>
	 * and calls {@link #pack()}.
	 * @return this for chaining.
	 * 
	 * @since 1.4
	 */
	public ImageFrame useDefaultSettings(){
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setMinimumSize(new Dimension(300,300));
		this.pack();
		return this;
	}
	
	/**
	 * Sets the image to be displayed by this frame.
	 * This will update the panel (calls repaint()).
	 * @param img to be displayed
	 * 
	 * @see #setImg(Img)
	 * @since 1.4
	 */
	public void setImage(final Image img){
		panel.setImage(img);
		this.setTitle(img.getWidth(OBS_WIDTHHEIGHT) + " x " + img.getHeight(OBS_WIDTHHEIGHT) + " Pixels");
	}
	
	/**
	 * Sets the image to be displayed by this frame.
	 * This will update the panel (calls repaint()).
	 * @param img to be displayed
	 * 
	 * @see #setImage(Image)
	 * @since 1.4
	 */
	public void setImg(final Img img){
		this.setImage(img.getRemoteBufferedImage());
	}
	
	/**
	 * Displays the specified image in a new ImageFrame instance.
	 * <p>
	 * Creates a new ImageFrame, sets the specified image, and schedules a
	 * call to <tt>setVisible(true)</tt> on the AWT event dispatching thread.
	 * @param img to be displayed
	 * @return the frame that displays the image.
	 * 
	 * @see #display(Img)
	 * @since 1.4
	 */
	public static ImageFrame display(final Image img){
		ImageFrame frame = new ImageFrame().useDefaultSettings();
		SwingUtilities.invokeLater( () -> {
			frame.setImage(img);
			frame.setVisible(true);
		});
		return frame;
	}
	
	/**
	 * Displays the specified image in a new ImageFrame instance.
	 * <p>
	 * Creates a new ImageFrame, sets the specified image, and schedules a
	 * call to <tt>setVisible(true)</tt> on the AWT event dispatching thread.
	 * @param img to be displayed
	 * @return the frame that displays the image.
	 * 
	 * @see #display(Image)
	 * @since 1.4
	 */
	public static ImageFrame display(final Img img){
		return display(img.getRemoteBufferedImage());
	}
	
	/**
	 * Returns the {@link ImagePanel} of this frame.
	 * @return the image panel
	 * @since 1.4
	 */
	public ImagePanel getPanel() {
		return panel;
	}

}
