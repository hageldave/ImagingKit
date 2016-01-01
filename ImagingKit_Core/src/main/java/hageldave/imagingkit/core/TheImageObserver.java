package hageldave.imagingkit.core;

import java.awt.Image;
import java.awt.image.ImageObserver;

/**
 * {@link ImageObserver} implementation evaluating the infoflags.
 * @author hageldave
 */
public final class TheImageObserver implements ImageObserver {
	
	private final int testbits;
	
	/**
	 * Creates an ImageObserver that returns false when all specified testbits 
	 * are present in the infoflags of {@link #imageUpdate(Image, int, int, int, int, int)}
	 * @param testbits to be tested against infoflags
	 */
	public TheImageObserver(int testbits) {
		this.testbits = testbits;
	}

	@Override
	public boolean imageUpdate(Image img, int infoflags, int x, int y,
			int width, int height) {
		return (infoflags & testbits) != testbits;
	}
	
	/** Observer that tests infoflags against {@link ImageObserver#ALLBITS} */
	public static final TheImageObserver OBS_ALLBITS = new TheImageObserver(ImageObserver.ALLBITS);
	
	/** Observer that tests infoflags against bitwise OR of 
	 * {@link ImageObserver#WIDTH} and  {@link ImageObserver#HEIGHT} 
	 */
	public static final TheImageObserver OBS_WIDTHHEIGHT = new TheImageObserver(ImageObserver.WIDTH | ImageObserver.HEIGHT);
}
