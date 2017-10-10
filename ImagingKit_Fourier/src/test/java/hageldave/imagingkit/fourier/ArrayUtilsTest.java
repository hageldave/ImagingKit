package hageldave.imagingkit.fourier;

import org.junit.Test;

import static org.junit.Assert.*;

public class ArrayUtilsTest {

	@Test
	public void testShift2D(){
		for(int w = 1; w < 16; w++)
			for(int h = 1; h < 16; h++)
				testShift2Dwith(w, h);
		
	}
			
	static void testShift2Dwith(final int w, final int h){
		double[] sourceArray = new double[w*h];
		initArray(sourceArray);
		double[] array = new double[sourceArray.length];
		
		for(int x = -w; x<w*2; x++){
			for(int y =-h; y < h*2; y++){
				System.arraycopy(sourceArray, 0, array, 0, sourceArray.length);
				ArrayUtils.shift2D(array, w, h, x, y);
				for(int i = 0; i < sourceArray.length; i++){
					if(sourceArray[i] != array[shiftedIndex(i, w, h, x, y)]){
						String msg = String.format("error at w=%d h=%d, sx=%d sy=%d index=%d (x=%d y=%d)", w,h, x,y, i,i%w,i/w);
						assertEquals(msg,sourceArray[i], array[shiftedIndex(i, w, h, x, y)], 0);
					}
				}
			}
		}
	}
	
	static void initArray(double[] a){
		for(int i = 0; i < a.length; i++){
			a[i]=i;
		}
	}
	
	static int shiftedIndex(int i, int w, int h, int xs, int ys){
		while(xs < 0) xs +=w;
		while(ys < 0) ys +=h; 
		int x = ((i%w)+xs)%w;
		int y = ((i/w)+ys)%h;
		return y*w+x;
	}
	
}
