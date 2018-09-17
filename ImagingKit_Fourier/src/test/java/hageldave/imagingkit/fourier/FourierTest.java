package hageldave.imagingkit.fourier;

import static org.junit.Assert.assertEquals;

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
		
		ComplexImg transform1 = Fourier.verticalTransform(img, ColorImg.channel_r);
		ComplexImg transform2 = Fourier.horizontalTransform(img, ColorImg.channel_r);
		
		Fourier.horizontalTransform(false, transform1, transform1);
		Fourier.verticalTransform(false, transform2, transform2);
		
		// order does not really matter transforms are the same but complex conjugated
		transform2.forEach(ComplexPixel::conjugate);
		transform1.forEach(px->{
			assertEquals(px.real(), transform2.getValueR(px.getX(), px.getY()),0.00001);
			assertEquals(px.imag(), transform2.getValueI(px.getX(), px.getY()),0.00001);
		});
		
		// test 2D inverse is equal to horizontal then vertical inverse
		ColorImg imgInversed1 = Fourier.inverseTransform(null, transform1, ColorImg.channel_r);
		ComplexImg horizontalInv = Fourier.horizontalTransform(true, transform2, null);
		ColorImg imgInversed2 = Fourier.verticalInverseTransform(null, horizontalInv, ColorImg.channel_r);
		imgInversed1.forEach(px->{
			assertEquals(px.r_asDouble(), imgInversed2.getValueR(px.getX(), px.getY()), 0.00001);
		});
		// also vertical first then horizontal equals 2D inverse
		ComplexImg verticalInv = Fourier.verticalTransform(true, transform2, null);
		ColorImg imgInversed3 = Fourier.horizontalInverseTransform(null, verticalInv, ColorImg.channel_r);
		imgInversed3.forEach(px->{
			assertEquals(px.r_asDouble(), imgInversed2.getValueR(px.getX(), px.getY()), 0.00001);
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
