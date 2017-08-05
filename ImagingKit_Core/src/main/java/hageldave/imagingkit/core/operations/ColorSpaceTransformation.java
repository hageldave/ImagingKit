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

import hageldave.imagingkit.core.Img;
import hageldave.imagingkit.core.Pixel;
import hageldave.imagingkit.core.PixelBase;

/**
 * Enum providing multiple color space transformations.
 * To transform a single color value use the {@link #discreteTransform(int)} method,
 * to transform a whole {@link Img} use the {@link #get()} method to obtain a
 * Pixel Consumer for the conversion.
 * @author hageldave
 * @since 1.2
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
			ColorSpaceTransformation::rgb2lab_discrete,
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
			ColorSpaceTransformation::lab2rgb_discrete,
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
			ColorSpaceTransformation::rgb2hsv_discrete,
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
			ColorSpaceTransformation::hsv2rgb_discrete,
			ColorSpaceTransformation::hsv2rgb_continuous
	),


	RGB_2_YCbCr(
			ColorSpaceTransformation::rgb2ycbcr_discrete,
			ColorSpaceTransformation::rgb2ycbcr_continuous
	),


	YCbCr_2_RGB(
			ColorSpaceTransformation::ycbcr2rgb_discrete,
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
	private final DiscreteTransformation discreteTransform;
	private final Consumer<PixelBase> continousTransform;
	private ColorSpaceTransformation inverse;

	private ColorSpaceTransformation(DiscreteTransformation discreteTransform, Consumer<PixelBase> continousTransform) {
		this.discreteTransform = discreteTransform;
		this.continousTransform = continousTransform;
	}

	/**
	 * Transforms the specified value according to this color space transformation.
	 * It is assumed that all information is stored in the first 24 bits of the value,
	 * the last 8 bits are preserved and can be used for alpha like ARGB does.
	 * @param color to be transformed.
	 * @return transformed color.
	 * @since 1.2
	 */
	public final int discreteTransform(int color){
		return (color & 0xff000000) | (discreteTransform.transform(color) & 0x00ffffff);
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

	public ColorSpaceTransformation inverse() {
		return inverse;
	}


	////// STATIC //////


	/** Interface for a transformation function from int to int */
	private static interface DiscreteTransformation {
		/**
		 * Transforms the value.
		 * It is assumed that the value stores information in 24 bits which will
		 * be transformed to another value also storing 24 bits of information.
		 * The last 8 bits of the integer are reserved for information about alpha.
		 * E.g. RGB stores 8 bits for each color channel.
		 * @param value (24 bits of information).
		 * @return transformed value (24 bits of information).
		 */
		public int transform(int value);
	}

	// CIE L*a*b* helper class
	private static final class LAB {
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

	static double clamp_0_1(double val){
		return Math.max(0.0, Math.min(val, 1.0));
	}


	////// TRANSFORMS //////

	private static int rgb2lab_discrete(int val)
	{
		double x,y,z; x=y=z=0;
		{// first convert to CIEXYZ (assuming sRGB color space with D65 white)
			double r = Pixel.r_normalized(val); double g = Pixel.g_normalized(val); double b = Pixel.b_normalized(val);
			x = r*0.4124564f + g*0.3575761f + b*0.1804375f;
			y = r*0.2126729f + g*0.7151522f + b*0.0721750f;
			z = r*0.0193339f + g*0.1191920f + b*0.9503041f;
		}
		double L,a,b; L=a=b=0;
		{// now convert to Lab
			double temp = LAB.func(y/LAB.Yn);
			// with ranges L[0,100] ab[-100,100]
			// L = 116*temp-16;
			// a = 500*(LAB.func(x/LAB.Xn) - temp);
			// b = 200*(temp - LAB.func(z/LAB.Zn));

			 // with ranges L[0,255] ab[-127,127];
			 L = (116*temp-16)*(255.0/100);
			 a = 500*(127.0/100)*(LAB.func(x/LAB.Xn) - temp);
			 b = 200*(127.0/100)*(temp - LAB.func(z/LAB.Zn));
		}
		return Pixel.rgb_bounded(
				(int)Math.round(L),
				(int)Math.round(a+127),
				(int)Math.round(b+127));
	}

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
			// with ranges L[0,100] ab[-100,100]
			// L = 116*temp-16;
			// a = 500*(LAB.func(x/LAB.Xn) - temp);
			// b = 200*(temp - LAB.func(z/LAB.Zn));

			 // with ranges L[0,1] ab[-0.5,0.5];
			 L = (116*temp-16)*(0.01);
			 a = 2.5*(LAB.func(x/LAB.Xn) - temp);
			 b = 1.0*(temp - LAB.func(z/LAB.Zn));
		}
		px.setRGB_fromDouble_preserveAlpha(L, a+0.5, b+0.5);
	}

	private static int lab2rgb_discrete(int val)
	{
		double L = (Pixel.r_normalized(val)  )*100;
		double A = ((Pixel.g(val)-127)/254.0)*200;
		double B = ((Pixel.b(val)-127)/254.0)*200;

		// LAB to XYZ
		double temp = (L+16)/116;
		double x =  LAB.Xn*LAB.funcInv(temp + (A/500));
		double y =  LAB.Yn*LAB.funcInv(temp);
		double z =  LAB.Zn*LAB.funcInv(temp - (B/200));

		return Pixel.rgb_bounded(
				//                           X             Y             Z
				(int)Math.round(( 3.2404542f*x -1.5371385f*y -0.4985314f*z)*0xff ),  // R
				(int)Math.round((-0.9692660f*x +1.8760108f*y +0.0415560f*z)*0xff ),  // G
				(int)Math.round(( 0.0556434f*x -0.2040259f*y +1.0572252f*z)*0xff )); // B
	}

	private static void lab2rgb_continuous(PixelBase px)
	{
		double L = (px.r_asDouble()  )*100;
		double A = (px.g_asDouble()-0.5)*200;
		double B = (px.b_asDouble()-0.5)*200;

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


	private static int rgb2hsv_discrete(int val)
	{
		double r = Pixel.r_normalized(val);
		double g = Pixel.g_normalized(val);
		double b = Pixel.b_normalized(val);

		double max,p,q,o; max=p=q=o=0;
		if(r > max){ max=r; p=g; q=b; o=0; }
		if(g > max){ max=g; p=b; q=r; o=2; }
		if(b > max){ max=b; p=r; q=g; o=4; }

		double min = Math.min(Math.min(r,g),b);
		if(max==min){
			return Pixel.rgb(0,0,(int)(max*255));
		} else {
			r = 256+(256.0/6) * (o + (p-q)/(max-min));
			g = 255*((max-min)/max);
			b = 255*max;
			return Pixel.rgb((int)Math.round(r),(int)Math.round(g),(int)Math.round(b));
		}
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

	private static int hsv2rgb_discrete(int val)
	{
		double h = Pixel.r(val) * (360.0/256);
		double s = Pixel.g_normalized(val);
		double v = Pixel.b_normalized(val);
		double hi = h/60;
		double f = hi - (hi=(int)hi);
		double p = v*(1-s);
		double q = v*(1-s*f);
		double t = v*(1-s*(1-f));
		switch((int)hi){
		case 1:  return Pixel.rgb_fromNormalized(q,v,p);
		case 2:  return Pixel.rgb_fromNormalized(p,v,t);
		case 3:  return Pixel.rgb_fromNormalized(p,q,v);
		case 4:  return Pixel.rgb_fromNormalized(t,p,v);
		case 5:  return Pixel.rgb_fromNormalized(v,p,q);
		default: return Pixel.rgb_fromNormalized(v,t,p);
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

	private static int rgb2ycbcr_discrete(int val)
	{
		double r = Pixel.r(val), g = Pixel.g(val), b = Pixel.b(val);
		return Pixel.rgb_bounded(
				(int)Math.round( 0.2990f*r +0.5870f*g +0.1140f*b),
				(int)Math.round(-0.1687f*r -0.3313f*g +0.5000f*b +128f),
				(int)Math.round( 0.5000f*r -0.4187f*g +0.0813f*b +128f));
	}

	private static void rgb2ycbcr_continuous(PixelBase px)
	{
		double r = px.r_asDouble(), g = px.g_asDouble(), b = px.b_asDouble();
		px.setRGB_fromDouble_preserveAlpha(
				(0.2990f*r +0.5870f*g +0.1140f*b),
				(-0.1687f*r -0.3313f*g +0.5000f*b +0.5),
				( 0.5000f*r -0.4187f*g +0.0813f*b +0.5));
	}

	private static int ycbcr2rgb_discrete(int val)
	{
		double y = Pixel.r(val), cb = Pixel.g(val)-128, cr = Pixel.b(val)-128;
		return Pixel.rgb_bounded(
				(int)Math.round(0.7720f*y -0.4030f*cb +1.4020f*cr),
				(int)Math.round(1.1161f*y -0.1384f*cb -0.7141f*cr),
				(int)Math.round(1.0000f*y +1.7720f*cb -0.0001f*cr));
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
