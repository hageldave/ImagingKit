package hageldave.imagingkit.core;

import static org.junit.Assert.*;

import java.awt.Image;
import java.awt.image.ImageObserver;

import org.junit.Test;

public class TheImageObserverTest {

	@Test
	public void testAll() {
		Image img = BufferedImageFactory.getINT_ARGB(100, 100);
		boolean update = TheImageObserver.OBS_ALLBITS.imageUpdate(img, ImageObserver.SOMEBITS, 0, 0, 10, 10);
		assertTrue(update);
		update = TheImageObserver.OBS_ALLBITS.imageUpdate(img, ImageObserver.ALLBITS, 0, 0, 10, 10);
		assertFalse(update);
		
		update = TheImageObserver.OBS_WIDTHHEIGHT.imageUpdate(img, ImageObserver.SOMEBITS, 0, 0, 10, 10);
		assertTrue(update);
		update = TheImageObserver.OBS_WIDTHHEIGHT.imageUpdate(img, ImageObserver.ALLBITS, 0, 0, 10, 10);
		assertTrue(update);
		update = TheImageObserver.OBS_WIDTHHEIGHT.imageUpdate(img, ImageObserver.WIDTH, 0, 0, 10, 10);
		assertTrue(update);
		update = TheImageObserver.OBS_WIDTHHEIGHT.imageUpdate(img, ImageObserver.HEIGHT, 0, 0, 10, 10);
		assertTrue(update);
		update = TheImageObserver.OBS_WIDTHHEIGHT.imageUpdate(img, ImageObserver.WIDTH|ImageObserver.HEIGHT, 0, 0, 10, 10);
		assertFalse(update);
	}
	
}
