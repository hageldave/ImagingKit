package hageldave.imagingkit.core;

import java.util.function.Consumer;

/**
 * Enum providing multiple color space transformations.
 * To transform a single color value use the {@link #transform(int)} method, 
 * to transform a whole {@link Img} use the {@link #get()} method to obtain a 
 * Pixel Consumer for the conversion.
 * @author hageldave
 * @since 1.2
 */
public enum ColorSpaceTransformation {
	
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
	RGB_2_LAB(val->
	{
		float x,y,z; x=y=z=0;
		{// first convert to CIEXYZ (assuming sRGB color space with D65 white)
			float r = Pixel.r_normalized(val); float g = Pixel.g_normalized(val); float b = Pixel.b_normalized(val);
			x = r*0.4124564f + g*0.3575761f + b*0.1804375f;
			y = r*0.2126729f + g*0.7151522f + b*0.0721750f;
			z = r*0.0193339f + g*0.1191920f + b*0.9503041f;
		}
		float L,a,b; L=a=b=0;
		{// now convert to Lab 
			float temp = LAB.func(y/LAB.Yn);
			// with ranges L[0,100] ab[-100,100]
			// L = 116*temp-16;
			// a = 500*(LAB.func(x/LAB.Xn) - temp);
			// b = 200*(temp - LAB.func(z/LAB.Zn));
			
			// with ranges L[0,255] ab[-127,127];
			L = (116*temp-16)*(255.0f/100);
			a = 500*(127.0f/100)*(LAB.func(x/LAB.Xn) - temp);
			b = 200*(127.0f/100)*(temp - LAB.func(z/LAB.Zn));
		}
		
		return Pixel.rgb_bounded(
				Math.round(L),
				Math.round(a+127),
				Math.round(b+127));
	}),
	
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
	LAB_2_RGB(val->{
		float L = (Pixel.r_normalized(val)  )*100;
		float A = ((Pixel.g(val)-127)/254.0f)*200;
		float B = ((Pixel.b(val)-127)/254.0f)*200;
		
		// LAB to XYZ
		float temp = (L+16)/116;
		float x =  LAB.Xn*LAB.funcInv(temp + (A/500));
		float y =  LAB.Yn*LAB.funcInv(temp);
		float z =  LAB.Zn*LAB.funcInv(temp - (B/200));
		
		return Pixel.rgb_bounded(
				//                            X             Y             Z
				Math.round(( 3.2404542f*x -1.5371385f*y -0.4985314f*z)*0xff ),  // R
				Math.round((-0.9692660f*x +1.8760108f*y +0.0415560f*z)*0xff ),  // G
				Math.round(( 0.0556434f*x -0.2040259f*y +1.0572252f*z)*0xff )); // B
	}),
	
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
	 * be kept in range using the modulo operator <code>(h%256)</code> or 
	 * 8bit truncation <code>(h&0xff)</code> which is implemented by all of the 
	 * Pixel.argb or rgb methods that do not explicitly state differently 
	 * (e.g. rgb_fast or rgb_bounded do not use truncation).
	 * @see #HSV_2_RGB
	 * @since 1.2
	 */
	RGB_2_HSV(val->
	{
		float r = Pixel.r_normalized(val);
		float g = Pixel.g_normalized(val);
		float b = Pixel.b_normalized(val);
		
		float max,p,q,o; max=p=q=o=0;
		if(r > max){ max=r; p=g; q=b; o=0; }
		if(g > max){ max=g; p=b; q=r; o=2; }
		if(b > max){ max=b; p=r; q=g; o=4; }
		
		float min = Math.min(Math.min(r,g),b);
		if(max==min){
			return Pixel.rgb(0,0,(int)(max*255));
		} else {
			r = 256+(256.0f/6) * (o + (p-q)/(max-min));
			g = 255*((max-min)/max);
			b = 255*max;
			return Pixel.rgb(Math.round(r),Math.round(g),Math.round(b));
		}
	}),
	
	/**
	 * Transforms colors from the HSV domain (hue, saturation, value) to the RGB domain.
	 * <br>
	 * This is the inverse transformation of {@link #RGB_2_HSV}.
	 * @see #RGB_2_HSV
	 * @since 1.2
	 */
	HSV_2_RGB(val->
	{
		float h = Pixel.r(val) * (360.0f/256);
		float s = Pixel.g_normalized(val);
		float v = Pixel.b_normalized(val);
		float hi = h/60;
		float f = hi - (hi=(int)hi);
		float p = v*(1-s);
		float q = v*(1-s*f);
		float t = v*(1-s*(1-f));
		switch((int)hi){
		case 1:  return Pixel.rgb_fromNormalized(q,v,p);
		case 2:  return Pixel.rgb_fromNormalized(p,v,t);
		case 3:  return Pixel.rgb_fromNormalized(p,q,v);
		case 4:  return Pixel.rgb_fromNormalized(t,p,v);
		case 5:  return Pixel.rgb_fromNormalized(v,p,q);
		default: return Pixel.rgb_fromNormalized(v,t,p);
		}
	})
	;	
	
	////// ATTRIBUTES / METHODS //////
	private final Transformation transformation;
	
	private ColorSpaceTransformation(Transformation transformation) {
		this.transformation = transformation;
	}
	
	/**
	 * Returns the Pixel Consumer that transforms a pixel's value. <br>
	 * Pass this to {@link Img#forEach(Consumer)} or similar methods.
	 * @return the Pixel Consumer corresponding to this transformation.
	 * @since 1.2
	 */
	public final Consumer<Pixel> get(){
		return px -> px.setValue(transform(px.getValue()));
	}
	
	/**
	 * Transforms the specified value according to this color space transformation.
	 * It is assumed that all information is stored in the first 24 bits of the value,
	 * the last 8 bits are preserved and can be used for alpha like ARGB does.
	 * @param color to be transformed.
	 * @return transformed color.
	 * @since 1.2
	 */
	public final int transform(int color){
		return (color & 0xff000000) | (transformation.transform(color) & 0x00ffffff);
	}
	
	
	////// STATIC //////
	
	/** Interface for a transformation function from int to int */
	private static interface Transformation {
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
	
	// clamps value to range 0 .. 255 (0xff)
	static int clamp0xff(int i){
		return Math.min(0xff, Math.max(0, i));
	}
	
	// CIE L*a*b* helper class
	private static final class LAB {
		static final float Xn = 0.95047f;
		static final float Yn = 1.00000f;
		static final float Zn = 1.08883f;
		static final float lab6_29 = 6.0f/29.0f;
		static final float lab6_29_3 = lab6_29*lab6_29*lab6_29;
		static final float lab1_3_29_6_2 = (1.0f/3.0f) * (29.0f/6.0f) * (29.0f/6.0f);

		static float func(float q){
			return q > lab6_29_3 ? (float)Math.cbrt(q):lab1_3_29_6_2*q + (4.0f/29.0f);
//			return (float)Math.cbrt(q);
		}

		static float funcInv(float q){
			return q > lab6_29 ? q*q*q : 3*lab6_29*lab6_29*(q-(4.0f/29.0f));
//			return q*q*q;
		}
	}
}
