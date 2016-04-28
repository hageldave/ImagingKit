package hageldave.imagingkit.core;

import java.util.function.Consumer;

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
	 */
	RGB_2_LAB(px->
	{
		float x,y,z; x=y=z=0;
		{// first convert to CIEXYZ (assuming sRGB color space with D65 white)
			float r = px.r_normalized(); float g = px.g_normalized(); float b = px.b_normalized();
			x = r*0.4124564f + g*0.3575761f + b*0.1804375f;
			y = r*0.2126729f + g*0.7151522f + b*0.0721750f;
			z = r*0.0193339f + g*0.1191920f + b*0.9503041f;
		}
		float L,a,b; L=a=b=0;
		{// now convert to Lab 
			float temp = LAB.func(y/LAB.Yn);
//			// with ranges L[0,100] ab[-100,100]
//			L = 116*temp-16;
//			a = 500*(LAB.func(x/LAB.Xn) - temp);
//			b = 200*(temp - LAB.func(z/LAB.Zn));
			// with ranges L[0,255] ab[-127,127];
			L = (116*temp-16)*(255.0f/100);
			a = 500*(127.0f/100)*(LAB.func(x/LAB.Xn) - temp);
			b = 200*(127.0f/100)*(temp - LAB.func(z/LAB.Zn));
		}
		
		px.setRGB_preserveAlpha(
				(int)L,
				(int)(a+127),
				(int)(b+127));
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
	 */
	LAB_2_RGB(px->{
		float L = (px.r_normalized()     )*100;
		float A = ((px.g()-127)/254.0f)*200;
		float B = ((px.b()-127)/254.0f)*200;
		
		// LAB to XYZ
		float temp = (L+16)/116;
		float x =  LAB.Xn*LAB.funcInv(temp + (A/500));
		float y =  LAB.Yn*LAB.funcInv(temp);
		float z =  LAB.Zn*LAB.funcInv(temp - (B/200));
		
		// XYZ to RGB
//		float r =  3.2404542f*x -1.5371385f*y  -0.4985314f*z;
//		float g = -0.9692660f*x +1.8760108f*y  +0.0415560f*z;
//		float b =  0.0556434f*x -0.2040259f*y  +1.0572252f*z;
//		px.setARGB(px.a(), 
//				clamp0xff((int)(r*0xff)), 
//				clamp0xff((int)(g*0xff)), 
//				clamp0xff((int)(b*0xff)));
		px.setRGB_preserveAlpha(
				//                            X             Y             Z
				clamp0xff( (int)(( 3.2404542f*x -1.5371385f*y -0.4985314f*z)*0xff) ),  // R
				clamp0xff( (int)((-0.9692660f*x +1.8760108f*y +0.0415560f*z)*0xff) ),  // G
				clamp0xff( (int)(( 0.0556434f*x -0.2040259f*y +1.0572252f*z)*0xff) )); // B
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
	 */
	RGB_2_HSV(px->
	{
		float r = px.r_normalized();
		float g = px.g_normalized();
		float b = px.b_normalized();
		
		float max,p,q,o; max=p=q=o=0;
		if(r > max){ max=r; p=g; q=b; o=0; }
		if(g > max){ max=g; p=b; q=r; o=2; }
		if(b > max){ max=b; p=r; q=g; o=4; }
		
		float min = Math.min(Math.min(r,g),b);
		if(max==min){
			px.setARGB(px.a(),0,0,(int)(max*255));
		} else {
			r = 256+(256.0f/6) * (o + (p-q)/(max-min));
			g = 255*((max-min)/max);
			b = 255*max;
			px.setRGB_preserveAlpha((int)r,(int)g,(int)b);
		}
	}),
	
	/**
	 * Transforms colors from the HSV domain (hue, saturation, value) to the RGB domain.
	 * <br>
	 * This is the inverse transformation of {@link #RGB_2_HSV}.
	 * @see #RGB_2_HSV
	 */
	HSV_2_RGB(px->
	{
		float h = px.r() * (360.0f/256);
		float s = px.g_normalized();
		float v = px.b_normalized();
		float hi = h/60;
		float f = hi - (hi=(int)hi);
		float p = v*(1-s);
		float q = v*(1-s*f);
		float t = v*(1-s*(1-f));
		switch((int)hi){
		case 1: px.setRGB_fromNormalized_preserveAlpha(q,v,p); break;
		case 2: px.setRGB_fromNormalized_preserveAlpha(p,v,t); break;
		case 3: px.setRGB_fromNormalized_preserveAlpha(p,q,v); break;
		case 4: px.setRGB_fromNormalized_preserveAlpha(t,p,v); break;
		case 5: px.setRGB_fromNormalized_preserveAlpha(v,p,q); break;
		default:px.setRGB_fromNormalized_preserveAlpha(v,t,p); break;
		}
	})
	;	
	
	////// ATTRIBUTES / METHODS //////
	/** 
	 * The Pixel Consumer that transforms a pixel's value. <br>
	 * Pass this to {@link Img#forEach(Consumer)} or similar methods.
	 * @see #get()
	 */
	public final Consumer<Pixel> transformation;
	
	private ColorSpaceTransformation(Consumer<Pixel> transformation) {
		this.transformation = transformation;
	}
	
	/**
	 * This is syntactic sugar for referencing the {@link #transformation}
	 * attribute. 
	 * @return the Pixel Consumer that transforms a pixel's value. <br>
	 * Pass this to {@link Img#forEach(Consumer)} or similar methods.
	 * @see #transformation
	 */
	public final Consumer<Pixel> get(){return transformation;}
	
	
	
	////// STATIC //////
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
		}

		static float funcInv(float q){
			return q > lab6_29 ? q*q*q : 3*lab6_29*lab6_29*(q-(4.0f/29.0f));
		}
	}
}
