package hageldave.imagingkit.fourier;

import org.junit.Test;
import static org.junit.Assert.*;

public class ComplexPixelTest {

	@Test
	public void test() {
		ComplexImg img = new ComplexImg(11,5);
		img.setComplex(3,3, 0.4, 0.2);
		img.forEach(px -> {
			if(px.getX() == 3 && px.getY()==3){
				assertEquals(0.4, px.real(),0);
				assertEquals(0.2, px.imag(),0);
				assertEquals(0.4*0.4+0.2*0.2, px.computePower().power(), 0);
				px.conjugate();
				assertEquals(-0.2, px.imag(), 0);
				px.add(0.6, 0.2);
				assertEquals(1.0, px.computePower().power(), 0);
				px.mult(2, 1);
				assertEquals(2, px.real(), 0);
				assertEquals(1, px.imag(), 0);
				px.subtract(1, 1);
				assertEquals(1, px.real(), 0);
				assertEquals(0, px.imag(), 0);
				px.setReal(0);
				assertEquals(0, px.real(), 0);
			} else {
				assertEquals(0.0, px.real(),0);
				assertEquals(0.0, px.imag(),0);
			}
		});
		img.shiftCornerToCenter();
		img.forEach(px->{
			assertEquals(px.getX()-5, px.getXFrequency());
			assertEquals(px.getY()-2, px.getYFrequency());
		});
		
		ComplexPixel pixel = img.getPixel(2, 3);
		assertEquals(2, pixel.getX());
		assertEquals(3, pixel.getY());
		assertEquals(3*img.getWidth()+2, pixel.getIndex());
	}

} 
