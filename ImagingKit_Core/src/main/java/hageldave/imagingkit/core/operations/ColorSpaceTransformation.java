/*
 * Copyright 2017 David Haegele
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package hageldave.imagingkit.core.operations;

import java.util.function.Consumer;

import hageldave.imagingkit.core.Pixel;
import hageldave.imagingkit.core.PixelBase;

/**
 * Enum providing multiple color space transformations.
 * Each transformation implements the {@link Consumer} interface
 * that accepts {@link PixelBase}. 
 * <p>
 * To tranform an image to the desired Colorspace the following could be used:<br>
 * {@code myImg.forEach(ColorSpaceTransformation.RGB_2_HSV)}
 * @author hageldave
 * @since 1.2 (relocated from core package)
 */
public enum ColorSpaceTransformation implements Consumer<PixelBase> {

	/**
	 * Transforms colors from the RGB domain to the CIE L*a*b* domain.
	 * <p>
	 * The L component ranges from 0 to 255, the A and B component from 0 to 254.
	 * Zero chromaticity is located at A=127 B=127 on the AB plane.
	 * <p>
	 * The B component is mapped to the least significant 8 bits of the integer
	 * value (0..7 equivalent to blue in RGB), <br>
	 * the A component to the next 8 bits (8..15 equivalent to green in RGB) and <br>
	 * the L component to the following 8 bits (16..23 equivalent to red in RGB). <br>
	 * The alpha channel (most significant 8 bits 24..31) will be preserved.
	 * <p>
	 * This is a two step transformation as RGB needs to be transformed to CIE XYZ
	 * and then to CIE L*a*b*. For the transformation to XYZ it is assumed that
	 * colors are in sRGB with D65 illuminant.
	 * @see #LAB_2_RGB
	 * @since 1.2
	 */
	RGB_2_LAB(
			ColorSpaceTransformation::rgb2lab_continuous
	),

	/**
	 * Transforms colors from the CIE L*a*b* domain to the RGB domain. <br>
	 * This is the inverse transformation of {@link #RGB_2_LAB}.
	 * <p>
	 * The CIE L*a*b* color space comprises more colors than RGB. Colors that
	 * are outside the RGB gamut will be clamped to the RGB boundaries.
	 * <p>
	 * This is a two step transformation because CIE L*a*b* has to be transformed
	 * to CIE XYZ first and then to RGB. For the conversion from XYZ to RGB
	 * it is assumed that RGB is sRGB with D65 illuminant.
	 * @see #LAB_2_RGB
	 * @since 1.2
	 */
	LAB_2_RGB(
			ColorSpaceTransformation::lab2rgb_continuous
	),

	/**
	 * Transforms colors from the RGB domain to the HSV domain (hue, saturation, value).
	 * <p>
	 * All of the HSV components range from 0 to 255 and therefore use 8bits of the
	 * integer value each. <br>
	 * The V component uses the least significant 8 bits (0..7 equivalent to blue in RGB), <br>
	 * the S component uses the next 8 bits (8..15 equivalent to green in RGB) and <br>
	 * the H component the following 8 bits (16..23 equivalent to red in RGB). <br>
	 * The alpha channel (most significant 8 bits 24..31) will be preserved.
	 * <p>
	 * Notice that the H (hue) component is cyclic (describes an angle) and can therefore
	 * be kept in range using the modulo operator {@code (h%256)} or
	 * 8bit truncation {@code (h&0xff)} which is implemented by all of the
	 * Pixel.argb or rgb methods that do not explicitly state differently
	 * (e.g. rgb_fast or rgb_bounded do not use truncation).
	 * @see #HSV_2_RGB
	 * @since 1.2
	 */
	RGB_2_HSV(
			ColorSpaceTransformation::rgb2hsv_continuous
	),

	/**
	 * Transforms colors from the HSV domain (hue, saturation, value) to the RGB domain.
	 * <br>
	 * This is the inverse transformation of {@link #RGB_2_HSV}.
	 * @see #RGB_2_HSV
	 * @since 1.2
	 */
	HSV_2_RGB(
			ColorSpaceTransformation::hsv2rgb_continuous
	),


	RGB_2_YCbCr(
			ColorSpaceTransformation::rgb2ycbcr_continuous
	),


	YCbCr_2_RGB(
			ColorSpaceTransformation::ycbcr2rgb_continuous
	)
	;

	static {
		pairTransforms(RGB_2_HSV,     HSV_2_RGB);
		pairTransforms(RGB_2_LAB,     LAB_2_RGB);
		pairTransforms(RGB_2_YCbCr, YCbCr_2_RGB);
	}

	private static final void pairTransforms(ColorSpaceTransformation t1, ColorSpaceTransformation t2){
		t1.inverse = t2;
		t2.inverse = t1;
	}

	////// ATTRIBUTES / METHODS //////
	private final Consumer<PixelBase> continousTransform;
	private ColorSpaceTransformation inverse;

	private ColorSpaceTransformation(Consumer<PixelBase> continousTransform) {
		this.continousTransform = continousTransform;
	}


	/**
	 * Applies this transformation to the specified pixel
	 * @param px pixel to be transformed
	 * @since 1.4
	 */
	@Override
	public void accept(PixelBase px) {
		continousTransform.accept(px);
	}

	/**
	 * Applies this transformation to the specified pixel
	 * @param px pixel to be transformed
	 * @return the pixel for chaining
	 * @since 2.0
	 */
	public <P extends PixelBase> P transform(P px){
		accept(px);
		return px;
	}

	/**
	 * Returns the inverse transformation of this.
	 * @return the transform that reverses this one.
	 * @since 2.0
	 */
	public ColorSpaceTransformation inverse() {
		return inverse;
	}


	////// STATIC //////

	
	// CIE L*a*b* helper class
	private static final class LAB {
		private LAB(){};
		
		static final double Xn = 0.95047f;
		static final double Yn = 1.00000f;
		static final double Zn = 1.08883f;
		static final double lab6_29 = 6.0/29.0;
		static final double lab6_29_3 = lab6_29*lab6_29*lab6_29;
		static final double lab1_3_29_6_2 = (1.0/3.0) * (29.0/6.0) * (29.0/6.0);

		static double func(double q){
			return q > lab6_29_3 ? (double)Math.cbrt(q):lab1_3_29_6_2*q + (4.0/29.0);
//			return (double)Math.cbrt(q);
		}

		static double funcInv(double q){
			return q > lab6_29 ? q*q*q : 3*lab6_29*lab6_29*(q-(4.0/29.0));
//			return q*q*q;
		}
	}


	////// TRANSFORMS //////
	private static void rgb2lab_continuous(PixelBase px)
	{

		double x,y,z; x=y=z=0;
		{// first convert to CIEXYZ (assuming sRGB color space with D65 white)
			double r = px.r_asDouble(); double g = px.g_asDouble(); double b = px.b_asDouble();
			x = r*0.4124564f + g*0.3575761f + b*0.1804375f;
			y = r*0.2126729f + g*0.7151522f + b*0.0721750f;
			z = r*0.0193339f + g*0.1191920f + b*0.9503041f;
		}
		double L,a,b; L=a=b=0;
		{// now convert to Lab
			double temp = LAB.func(y/LAB.Yn);
//			 with ranges L[0,100] ab[-100,100]
//			 L = 116*temp-16;
//			 a = 500*(LAB.func(x/LAB.Xn) - temp);
//			 b = 200*(temp - LAB.func(z/LAB.Zn));

//			 // with ranges L[0,1] ab[-0.5,0.5];
//			 L = (116*temp-16)*(0.01);
//			 a = 2.5*(LAB.func(x/LAB.Xn) - temp);
//			 b = 1.0*(temp - LAB.func(z/LAB.Zn));

			if(px instanceof Pixel){
				/* this is special:
				 * Pixels value range per channel is [0,255] (discrete).
				 * We want to map an interval [-1.0, 1.0] to [0,255] for the a and b channels.
				 * It is mandatory that the range [-1.0, 0.0[ and ]0.0, 1.0] are mapped to the same amount of
				 * discrete values and that 0 is directly mapped to a value as it corresponds to zero
				 * chromaticity (i.e. grey level values).
				 * The problem with this requirement is that the number of values in [0,255] is even (256)
				 * and therefore having a zero mapping and two ranges of equal size left and right to it cannot
				 * use the full range.
				 * We will thus map negative values to [0,126] 0 to [127] and positive values to [128,254],
				 * leaving out the last value (255) so actually [-1.0, 1.0] is mapped to [0,254]
				 */

				// with ranges L[0,255] ab[-127,127];
				L = (116*temp-16)*(255.0/100);
				a = 500*(127.0/100)*(LAB.func(x/LAB.Xn) - temp);
				b = 200*(127.0/100)*(temp - LAB.func(z/LAB.Zn));

				L /= 255;
				a = (a+127)/255;
				b = (b+127)/255;
			} else {
				 // with ranges L[0,1] ab[-0.5,0.5];
				 L = (116*temp-16)*(0.01);
				 a = 2.5*(LAB.func(x/LAB.Xn) - temp);
				 b = 1.0*(temp - LAB.func(z/LAB.Zn));

				 a = a+0.5;
				 b = b+0.5;
			}

		}
		px.setRGB_fromDouble_preserveAlpha(L, a, b);
	}

	private static void lab2rgb_continuous(PixelBase px)
	{
		double L,A,B;
		if(px instanceof Pixel){
			/* this is special:
			 * Pixels value range per channel is [0,255] (discrete).
			 * But we mapped an interval [-1.0, 1.0] to [0,254] for the a and b channels.
			 * This was done because the range [-1.0, 0.0[ and ]0.0, 1.0] has to be mapped to the same amount of
			 * discrete values and 0 has to be directly mapped to a value as it corresponds to zero
			 * chromaticity (i.e. grey level values).
			 * The problem with this requirement was that the number of values in [0,255] is even (256)
			 * and therefore having a zero mapping and two ranges of equal size left and right to it cannot
			 * use the full range of [0,255].
			 * We thus mapped negative values of a and b to [0,126] 0 to [127] and positive values to [128,254],
			 * leaving out the last value (255).
			 */
			L = (px.r_asDouble()        )*100.0;
			A = (px.g_asDouble()*255-127)*(200.0/254);
			B = (px.b_asDouble()*255-127)*(200.0/254);
		} else {
			L = (px.r_asDouble()    )*100.0;
			A = (px.g_asDouble()-0.5)*200.0;
			B = (px.b_asDouble()-0.5)*200.0;
		}

		// L in range [0,100] a,b in range [-100,100]


		// LAB to XYZ
		double temp = (L+16)/116;
		double x =  LAB.Xn*LAB.funcInv(temp + (A/500));
		double y =  LAB.Yn*LAB.funcInv(temp);
		double z =  LAB.Zn*LAB.funcInv(temp - (B/200));

		px.setRGB_fromDouble_preserveAlpha(
				//                           X             Y             Z
				( 3.2404542f*x -1.5371385f*y -0.4985314f*z),  // R
				(-0.9692660f*x +1.8760108f*y +0.0415560f*z),  // G
				( 0.0556434f*x -0.2040259f*y +1.0572252f*z)); // B
	}

	private static void rgb2hsv_continuous(PixelBase px)
	{
		double r = px.r_asDouble();
		double g = px.g_asDouble();
		double b = px.b_asDouble();

		double max,p,q,o; max=p=q=o=0;
		if(r > max){ max=r; p=g; q=b; o=0; }
		if(g > max){ max=g; p=b; q=r; o=2; }
		if(b > max){ max=b; p=r; q=g; o=4; }

		double min = Math.min(Math.min(r,g),b);
		if(max==min){
			px.setRGB_fromDouble_preserveAlpha(0, 0, max);
		} else {
			double h,s,v;
			h = (1.0/6.0) * (o + (p-q)/(max-min));
			h -= Math.floor(h);
			s = (max-min)/max;
			v = max;
			px.setRGB_fromDouble_preserveAlpha(h, s, v);
		}
	}

	private static void hsv2rgb_continuous(PixelBase px)
	{
		double h = px.r_asDouble();
		h -= Math.floor(h);
		h *= 360;
		double s = px.g_asDouble();
		double v = px.b_asDouble();
		double hi = h/60;
		double f = hi - (hi=(int)hi);
		double p = v*(1-s);
		double q = v*(1-s*f);
		double t = v*(1-s*(1-f));
		switch((int)hi){
		case 1:  px.setRGB_fromDouble_preserveAlpha(q,v,p);break;
		case 2:  px.setRGB_fromDouble_preserveAlpha(p,v,t);break;
		case 3:  px.setRGB_fromDouble_preserveAlpha(p,q,v);break;
		case 4:  px.setRGB_fromDouble_preserveAlpha(t,p,v);break;
		case 5:  px.setRGB_fromDouble_preserveAlpha(v,p,q);break;
		default: px.setRGB_fromDouble_preserveAlpha(v,t,p);break;
		}
	}
	
	private static void hsl2rgb_continuous(PixelBase px)
	{
		double h = px.r_asDouble();
		h -= Math.floor(h);
		h *= 360;
		double s = px.g_asDouble();
		double l = px.b_asDouble();
		double hi = h/60;
		double c = (1-Math.abs(2*l-1))*s;
		double x = c*(1-Math.abs((hi%2)-1));
		double m = l-0.5*c;
		switch ((int)hi) {
		case 1:  px.setRGB_fromDouble_preserveAlpha(x+m,c+m,0+m);break;
		case 2:  px.setRGB_fromDouble_preserveAlpha(0+m,c+m,x+m);break;
		case 3:  px.setRGB_fromDouble_preserveAlpha(0+m,x+m,c+m);break;
		case 4:  px.setRGB_fromDouble_preserveAlpha(x+m,0+m,c+m);break;
		case 5:  px.setRGB_fromDouble_preserveAlpha(c+m,0+m,x+m);break;
		default: px.setRGB_fromDouble_preserveAlpha(c+m,x+m,0+m);break;
		}
	}
	
	private static void hcy2rgb_continuous(PixelBase  px)
	{
		double h = px.r_asDouble();
		h -= Math.floor(h);
		h *= 360;
		double c = px.g_asDouble();
		double y = px.b_asDouble();
		double hi = h/60;
		double x = c*(1-Math.abs((hi%2)-1));
		double r,g,b;
		switch ((int)hi) {
		case 1:  r=x;g=c;b=0;break;
		case 2:  r=0;g=c;b=x;break;
		case 3:  r=0;g=x;b=c;break;
		case 4:  r=x;g=0;b=c;break;
		case 5:  r=c;g=0;b=x;break;
		default: r=c;g=x;b=0;break;
		}
		double m=y-(0.30*r+0.59*g+0.11*b);
		px.setRGB_fromDouble_preserveAlpha(r+m, g+m, b+m);
	}

	private static void rgb2ycbcr_continuous(PixelBase px)
	{
		double r = px.r_asDouble(), g = px.g_asDouble(), b = px.b_asDouble();
		px.setRGB_fromDouble_preserveAlpha(
				(0.2990f*r +0.5870f*g +0.1140f*b),
				(-0.1687f*r -0.3313f*g +0.5000f*b +0.5),
				( 0.5000f*r -0.4187f*g +0.0813f*b +0.5));
	}

	private static void ycbcr2rgb_continuous(PixelBase px)
	{
		double y = px.r_asDouble(), cb = px.g_asDouble()-0.5, cr = px.b_asDouble()-0.5;
		px.setRGB_fromDouble_preserveAlpha(
				(0.7720f*y -0.4030f*cb +1.4020f*cr),
				(1.1161f*y -0.1384f*cb -0.7141f*cr),
				(1.0000f*y +1.7720f*cb -0.0001f*cr));
	}

}
