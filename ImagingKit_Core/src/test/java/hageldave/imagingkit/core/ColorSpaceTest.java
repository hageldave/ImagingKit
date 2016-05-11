package hageldave.imagingkit.core;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ColorSpaceTest {

	static final int alphaTestImg = 0x88000000;
	static final int greyErrorThreshold = 7;
	static final int white = 0xffffffff;
	static final int black = 0xff000000;
	
	@Test
	public void test(){
		Img reference = getTestImg();

		
		// test RGB <-> HSV
		{
			ColorSpaceTransformation forward = ColorSpaceTransformation.RGB_2_HSV;
			ColorSpaceTransformation backward = ColorSpaceTransformation.HSV_2_RGB;
			// test image transformation (image covers all RGB values)
			Img img = reference.copy();
			img.forEachParallel(forward.get());
			img.forEachParallel(backward.get());
			int[] error = getMaxGreyError(reference, img);
			assertTrue(error[0] >= 0);
			assertTrue(String.format("LAB Error: %d %s %s", error[0], 
					Integer.toHexString(error[1]),
					Integer.toHexString(error[2])), 
					error[0] < greyErrorThreshold);
			
			// test alpha preservation
			for(int color: img.getData()){
				assertEquals(alphaTestImg, color & 0xff000000);
			}

			// test black and white transformation accuracy
			int color; 
			color = forward.transform(white);
			color = backward.transform(color);
			assertEquals(white, color);

			color = forward.transform(black);
			color = backward.transform(color);
			assertEquals(black, color);
		}
		
		// test RGB <-> LAB
		{
			ColorSpaceTransformation forward = ColorSpaceTransformation.RGB_2_LAB;
			ColorSpaceTransformation backward = ColorSpaceTransformation.LAB_2_RGB;
			// test image transformation (image covers all RGB values)
			Img img = reference.copy();
			img.forEachParallel(forward.get());
			img.forEachParallel(backward.get());
			int[] error = getMaxGreyError(reference, img);
			assertTrue(error[0] >= 0);
			assertTrue(String.format("LAB Error: %d %s %s", error[0], 
					Integer.toHexString(error[1]),
					Integer.toHexString(error[2])), 
					error[0] < greyErrorThreshold);
			
			// test alpha preservation
			for(int color: img.getData()){
				assertEquals(alphaTestImg, color & 0xff000000);
			}
			
			// test black and white transformation accuracy
			int color; 
			color = forward.transform(white);
			color = backward.transform(color);
			assertEquals(white, color);

			color = forward.transform(black);
			color = backward.transform(color);
			assertEquals(black, color);
		}

	}

	static Img getTestImg(){
		Img img = new Img(5000,5000);
		img.forEach(px->{
			px.setValue(alphaTestImg | (px.getIndex()&0xffffff));
		});
		return img;
	}

	static int[] getMaxGreyError(Img img1, Img img2){
		assertEquals(img1.getDimension(), img2.getDimension());
		assertNotEquals(img1, img2);
		int maxError = -1;
		int col1 = 0;
		int col2 = 0;
		int size = img1.numValues();
		for(int i = 0; i < size; i++){
			int color1 = img1.getData()[i];
			int color2 = img2.getData()[i];
			int err = Math.abs(Pixel.getGrey(color1, 1,1,1)-Pixel.getGrey(color2, 1,1,1));
			if(err > maxError){
				maxError = err;
				col1 = color1;
				col2 = color2;
			}
		}

		return new int[]{maxError, col1, col2};
	}

}
