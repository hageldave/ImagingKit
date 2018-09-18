package hageldave.imagingkit.fourier;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.function.DoubleBinaryOperator;

import org.junit.Test;

import hageldave.imagingkit.core.PixelBase;
import hageldave.imagingkit.core.io.ImageSaver;
import hageldave.imagingkit.core.scientific.ColorImg;
import hageldave.imagingkit.core.util.ImageFrame;

public class FourierTest {

	@Test
	public void test2D() {
		ColorImg img = createImg(400, 300, CIRCLE);
		ComplexImg transform = Fourier.transform(img, ColorImg.channel_r);
		final double sum = img.stream().mapToDouble(PixelBase::r_asDouble).sum();
		// test sum of values equals DC
		assertEquals(sum, transform.getDCreal(), 0.00001);
		// test inverse transform to ColorImg
		ColorImg imgInversed = Fourier.inverseTransform(null, transform, ColorImg.channel_g);
		img.forEach(px->{
			assertEquals(px.g_asDouble(), imgInversed.getValueG(px.getX(), px.getY()), 0.00001);
		});
		// test inverse transform to ColorImg of shifted Fourier
		transform.shiftCornerToCenter();
		Fourier.inverseTransform(imgInversed, transform, ColorImg.channel_r);
		img.forEach(px->{
			assertEquals(px.r_asDouble(), imgInversed.getValueR(px.getX(), px.getY()), 0.00001);
		});
		// test inverse transform to ComplexImg of shifted Fourier
		Fourier.transform(true, transform, transform);
		transform.resetShift();
		img.forEach(px->{
			assertEquals(px.b_asDouble(), transform.getValueR(px.getX(), px.getY()), 0.00001);
		});
		// test forward from complex to complex DC is the sum of values
		{
			ComplexImg transform2 = Fourier.transform(false, transform, null);
			assertEquals(sum, transform2.getDCreal(), 0.00001);
		}
	}
	
	@Test
	public void test1D() throws IOException {
		ColorImg img = createImg(201, 301, (x,y)->CIRCLE.applyAsDouble(x+0.2,y+0.2)-CIRCLE.applyAsDouble(x*4,y*4));
		// forward
		ComplexImg transform1 = Fourier.verticalTransform(img, ColorImg.channel_r);
		ComplexImg transform2 = Fourier.horizontalTransform(img, ColorImg.channel_r);
		// backward
		ColorImg inv1 = Fourier.verticalInverseTransform(null, transform1, ColorImg.channel_r);
		ColorImg inv2 = Fourier.horizontalInverseTransform(null, transform2, ColorImg.channel_r);
		// test - restored original
		img.forEach(px->{
			assertEquals(px.r_asDouble(), inv1.getValueR(px.getX(), px.getY()), 0.00001);
			assertEquals(px.r_asDouble(), inv2.getValueR(px.getX(), px.getY()), 0.00001);
		});
		// complex forward
		ComplexImg cmplxtrnsfrm1 = Fourier.horizontalTransform(false, transform1, null);
		ComplexImg cmplxtrnsfrm2 = Fourier.verticalTransform(false, transform2, null);
		// test - both transforms are the same, order of vertical and horizontal does not matter
		assertArrayEquals(cmplxtrnsfrm1.getDataReal(), cmplxtrnsfrm2.getDataReal(), 0.00001);
		assertArrayEquals(cmplxtrnsfrm1.getDataImag(), cmplxtrnsfrm2.getDataImag(), 0.00001);
		// complex backward
		Fourier.horizontalTransform(true, cmplxtrnsfrm1, cmplxtrnsfrm1);
		Fourier.verticalTransform(true, cmplxtrnsfrm2, cmplxtrnsfrm2);
		// test - retsoreed original
		transform1.forEach(px->{
			assertEquals(px.real(), cmplxtrnsfrm1.getValueR(px.getX(), px.getY()), 0.00001);
			assertEquals(px.imag(), cmplxtrnsfrm1.getValueI(px.getX(), px.getY()), 0.00001);
		});
		transform2.forEach(px->{
			assertEquals(px.real(), cmplxtrnsfrm2.getValueR(px.getX(), px.getY()), 0.00001);
			assertEquals(px.imag(), cmplxtrnsfrm2.getValueI(px.getX(), px.getY()), 0.00001);
		});
	}
	
	
	
	static ColorImg createImg(int width, int height, DoubleBinaryOperator objectFn){
		ColorImg img = new ColorImg(width,height, false);
		img.forEach(px->{
			double v = objectFn.applyAsDouble((px.getXnormalized()-0.5)*2, (px.getYnormalized()-0.5)*2);
			px.setRGB_fromDouble_preserveAlpha(v, v, v);
		});
		return img;
	}
	
	static final DoubleBinaryOperator CIRCLE = (x,y) -> (x*x+y*y) > 0.5 ? 0:1;
	
}
