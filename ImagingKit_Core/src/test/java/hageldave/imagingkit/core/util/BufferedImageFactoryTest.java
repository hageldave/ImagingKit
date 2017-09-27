package hageldave.imagingkit.core.util;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import static org.junit.Assert.*;
import org.junit.Test;

import hageldave.imagingkit.core.util.BufferedImageFactory;

public class BufferedImageFactoryTest {

	@Test
	public void testAll() {
		BufferedImage bimg = BufferedImageFactory.getINT_ARGB(1, 2);
		assertEquals(1, bimg.getWidth());
		assertEquals(2, bimg.getHeight());
		assertEquals(BufferedImage.TYPE_INT_ARGB, bimg.getType());
		
		bimg = BufferedImageFactory.getINT_ARGB(new Dimension(1, 2));
		assertEquals(1, bimg.getWidth());
		assertEquals(2, bimg.getHeight());
		assertEquals(BufferedImage.TYPE_INT_ARGB, bimg.getType());
		
		BufferedImage bimg2 = BufferedImageFactory.getINT_ARGB(bimg);
		assertFalse(bimg == bimg2);
		assertEquals(bimg.getWidth(),  bimg2.getWidth());
		assertEquals(bimg.getHeight(), bimg2.getHeight());
		assertEquals(BufferedImage.TYPE_INT_ARGB, bimg2.getType());
		
		BufferedImage bimg3 = BufferedImageFactory.get(bimg, BufferedImage.TYPE_BYTE_GRAY);
		assertFalse(bimg == bimg3);
		assertEquals(bimg.getWidth(),  bimg3.getWidth());
		assertEquals(bimg.getHeight(), bimg3.getHeight());
		assertEquals(BufferedImage.TYPE_BYTE_GRAY, bimg3.getType());
	}
	
	
}
