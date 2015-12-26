package hageldave.imagingkit.core;

import java.awt.Image;
import java.awt.image.ImageObserver;

public final class TheImageObserver implements ImageObserver {
	
	private final int testbits;
	
	public TheImageObserver(int testbits) {
		this.testbits = testbits;
	}

	@Override
	public boolean imageUpdate(Image img, int infoflags, int x, int y,
			int width, int height) {
		return (infoflags & testbits) != testbits;
	}
	
	public static final TheImageObserver OBS_ALLBITS = new TheImageObserver(ImageObserver.ALLBITS);
	public static final TheImageObserver OBS_WIDTHHEIGHT = new TheImageObserver(ImageObserver.WIDTH | ImageObserver.HEIGHT);
}
