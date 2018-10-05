package hageldave.imagingkit.core.img;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.util.function.Consumer;

public interface BufferedImageWrapable {

	/**
	 * Creates a {@link BufferedImage} that shares the data of this image. Changes in
	 * this image are reflected in the created BufferedImage and vice versa.
	 * The {@link ColorModel} and {@link Raster} of the resulting BufferedImage
	 * are implementation dependent.
	 * 
	 * @return BufferedImage sharing this Img's data.
	 * @throws UnsupportedOperationException if this implementation of {@link ImgBase}
	 * does not support this method.
	 * 
	 * @see #toBufferedImage(BufferedImage)
	 * @see #toBufferedImage()
	 */
	public BufferedImage getRemoteBufferedImage();
	
	/**
	 * Creates a {@link Graphics2D}, which can be used to draw into this image.
	 * <br>
	 * This operation may not be supported by an implementation of {@link ImgBase}
	 * and will then throw an {@link UnsupportedOperationException}. Use
	 * {@link #supportsRemoteBufferedImage()} to check if this operation is
	 * supported.
	 * 
	 * @return Graphics2D object to draw into this image.
	 * @throws UnsupportedOperationException if this implementation of {@link ImgBase}
	 * does not support this method.
	 * 
	 * @see #supportsRemoteBufferedImage()
	 * @see #paint(Consumer)
	 */
	public default Graphics2D createGraphics(){
		return getRemoteBufferedImage().createGraphics();
	}

	/**
	 * Uses the specified paintInstructions to draw into this image.
	 * This method will pass a {@link Graphics2D} object of this image to the
	 * specified {@link Consumer}. The {@link Consumer#accept(Object)} method
	 * can then draw into this image. When the accept method returns, the
	 * Graphics2D object is disposed.
	 * <p>
	 * This operation may not be supported by an implementation of {@link ImgBase}
	 * and will then throw an {@link UnsupportedOperationException}. Use
	 * {@link #supportsRemoteBufferedImage()} to check if this operation is
	 * supported.
	 * <p>
	 * Example (using lambda expression for Consumers accept method):
	 * <pre>
	 * {@code
	 * Img img = new Img(100, 100);
	 * img.paint( g2d -> { g2d.drawLine(0, 0, 100, 100); } );
	 * }
	 * </pre>
	 * 
	 * @param paintInstructions to be executed on a Graphics2D object of this image
	 * of this Img.
	 * @throws UnsupportedOperationException if this implementation of {@link ImgBase}
	 * does not support this method.
	 * 
	 * @see #createGraphics()
	 * @since 1.3
	 */
	public default void paint(Consumer<Graphics2D> paintInstructions){
		Graphics2D g2d = createGraphics();
		paintInstructions.accept(g2d);
		g2d.dispose();
	}
	
}
