package hageldave.imagingkit.core;

import java.awt.Image;
import java.awt.image.ImageObserver;

public final class TheImageObserver implements ImageObserver{

	@Override
	public boolean imageUpdate(Image img, int infoflags, int x, int y,
			int width, int height) {
		return (infoflags & ImageObserver.ALLBITS) != 0;
	}
	
	public static final TheImageObserver INSTANCE = new TheImageObserver();
	
}
