package hageldave.imagingkit.core;

import org.junit.Test;
import static org.junit.Assert.*;

import java.awt.image.BufferedImage;

public class ImgTest {

	@Test
	public void channelMethods_test(){
		int color = 0xffaa1244;
		assertEquals(0xff, Img.a(color));
		assertEquals(0xaa, Img.r(color));
		assertEquals(0x12, Img.g(color));
		assertEquals(0x44, Img.b(color));
		assertEquals(0xa124, Img.ch(color, 4, 16));
		assertEquals(0x44, Img.ch(color, 0, 8));
		
		assertEquals(0x01001234, Img.argb_fast(0x01, 0x00, 0x12, 0x34));
		assertEquals(0xff543210, Img.rgb_fast(0x54, 0x32, 0x10));
		assertEquals(0xff00ff54, Img.rgb_bounded(-12, 260, 0x54));
		assertEquals(0xffffffff, Img.rgb(0x15ff, 0xaff, 0x5cff));
		assertEquals(0b10101110, Img.combineCh(2, 0b10, 0b10, 0b11, 0b10));
	}
	
	@Test
	public void boundaryModes_test(){
		Img img = new Img(4, 4, new int[]
				{
						0,1,2,3,
						4,5,6,7,
						8,9,9,9,
						9,9,9,9
				});
		for(int mode: new int[]{Img.boundary_mode_zero, Img.boundary_mode_mirror, Img.boundary_mode_repeat_edge, Img.boundary_mode_repeat_image}){
			// test corners
			assertEquals(0, img.getPixel(0, 0, mode));
			assertEquals(3, img.getPixel(3, 0, mode));
			assertEquals(9, img.getPixel(0, 3, mode));
			assertEquals(9, img.getPixel(3, 3, mode));
		}
		assertEquals(0, img.getPixel(-1, 0, Img.boundary_mode_zero));
		assertEquals(0, img.getPixel(4, 0, Img.boundary_mode_zero));
		assertEquals(0, img.getPixel(0, -1, Img.boundary_mode_zero));
		assertEquals(0, img.getPixel(0, 4, Img.boundary_mode_zero));
		
		assertEquals(0, img.getPixel(-2, 0, Img.boundary_mode_repeat_edge));
		assertEquals(3, img.getPixel(3, -2, Img.boundary_mode_repeat_edge));
		assertEquals(9, img.getPixel(-10, 10, Img.boundary_mode_repeat_edge));
		assertEquals(3, img.getPixel(10, -10, Img.boundary_mode_repeat_edge));
		
		for(int y = 0; y < 4; y++)
		for(int x = 0; x < 4; x++){
			assertEquals(img.getPixel(x, y), img.getPixel(x+4, y, Img.boundary_mode_repeat_image));
			assertEquals(img.getPixel(x, y), img.getPixel(x, y+4, Img.boundary_mode_repeat_image));
			assertEquals(img.getPixel(x, y), img.getPixel(x-4, y, Img.boundary_mode_repeat_image));
			assertEquals(img.getPixel(x, y), img.getPixel(x, y-4, Img.boundary_mode_repeat_image));
			assertEquals(img.getPixel(x, y), img.getPixel(x+8, y+8, Img.boundary_mode_repeat_image));
			assertEquals(img.getPixel(x, y), img.getPixel(x-8, y-8, Img.boundary_mode_repeat_image));
		}
		
		for(int y = 0; y < 4; y++)
		for(int x = 0; x < 4; x++){
			assertEquals(img.getPixel(x, y), img.getPixel(x+8, y+8, Img.boundary_mode_mirror));
			assertEquals(img.getPixel(x, y), img.getPixel(7-x, 7-y, Img.boundary_mode_mirror));
			assertEquals(img.getPixel(x, y), img.getPixel(-8+x, -8+y, Img.boundary_mode_mirror));
			assertEquals(img.getPixel(x, y), img.getPixel(-1-x, -1-y, Img.boundary_mode_mirror));
		}
	}
	
	@Test
	public void pixelRetrieval_test(){
		Img img = new Img(4, 3, new int[]
				{
						0,1,2,3,
						4,5,6,7,
						8,9,9,5
				});
		
		assertEquals(img.getData().length, img.getWidth()*img.getHeight());
		assertEquals(img.getData().length, img.numPixels());
		
		int i = 0;
		for(int y = 0; y < img.getHeight(); y++)
		for(int x = 0; x < img.getWidth(); x++){
			assertEquals(img.getData()[i], img.getPixel(x, y));
			i++;
		}
		
		// test interpolation
		img = new Img(5,3, new int[]
				{
					0,1,2,3,4,
					2,3,4,5,6,
					4,5,6,7,8
				});
		
		assertEquals(img.getPixel(0, 0), img.interpolatePixel(0, 0));
		assertEquals(img.getPixel(img.getWidth()-1, img.getHeight()-1), img.interpolatePixel(1, 1));
		assertEquals(img.getPixel(0, img.getHeight()-1), img.interpolatePixel(0, 1));
		assertEquals(img.getPixel(img.getWidth()-1, 0), img.interpolatePixel(1, 0));
		assertEquals(img.getPixel(2, 1), img.interpolatePixel(0.5f, 0.5f));
		
		// test copypixels
		Img img2 = new Img(2,2);
		img.copyPixels(0, 0, 2, 2, img2, 0, 0);
		assertEquals(img.getPixel(0, 0), img2.getPixel(0, 0));
		assertEquals(img.getPixel(1, 1), img2.getPixel(1, 1));
		img.copyPixels(1, 1, 2, 2, img2, 0, 0);
		assertEquals(img.getPixel(1, 1), img2.getPixel(0, 0));
		assertEquals(img.getPixel(2, 2), img2.getPixel(1, 1));
		img.copyPixels(4, 2, 1, 1, img2, 1, 0);
		assertEquals(img.getPixel(4, 2), img2.getPixel(1, 0));
	}
	
	@Test
	public void buffimg_test(){
		BufferedImage bimg = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
		bimg.setRGB(0, 0, 1, 1, new int[]
				{
					0,1,2,3,
					4,5,6,7,
					8,9,0,1,
					2,3,4,5
				}, 0, 4);
		Img img = new Img(bimg);
		// test same pixels
		for(int y = 0; y < 4; y++)
		for(int x = 0; x < 4; x++){
			assertEquals(bimg.getRGB(x, y), img.getPixel(x, y));
		}
		// test remoteness
		Img img2 = Img.fromBufferedImage(bimg);
		for(int y = 0; y < 4; y++)
		for(int x = 0; x < 4; x++){
			img.setPixel(x, y, -2000-x-y);
			assertEquals(bimg.getRGB(x, y), img.getPixel(x, y));
			assertNotEquals(bimg.getRGB(x, y), img2.getPixel(x, y));
		}
		
		
	}
	
	
}
