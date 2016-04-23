package hageldave.imagingkit.core;

import java.util.function.Consumer;


public enum ColorSpaceTransformation {
	/** TODO: javadocs for each */
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
			L = 116*temp-16;
			a = 500*(LAB.func(x/LAB.Xn)-temp);
		}
	}),
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
			px.setARGB(px.a(),(int)r,(int)g,(int)b);
		}
	}),
	HSV_2_RGB(px->
	{
		float h = px.r_normalized()*359;
		float s = px.g_normalized();
		float v = px.b_normalized();
		float hi = h/60;
		float f = hi - (hi=(int)hi);
		float p = v*(1-s);
		float q = v*(1-s*f);
		float t = v*(1-s*(1-f));
		switch((int)hi){
		case 1: px.setARGB_fromNormalized(px.a_normalized(), q,v,p); break;
		case 2: px.setARGB_fromNormalized(px.a_normalized(), p,v,t); break;
		case 3: px.setARGB_fromNormalized(px.a_normalized(), p,q,v); break;
		case 4: px.setARGB_fromNormalized(px.a_normalized(), t,p,v); break;
		case 5: px.setARGB_fromNormalized(px.a_normalized(), v,p,q); break;
		default:px.setARGB_fromNormalized(px.a_normalized(), v,t,p); break;
		}
	})
	;	
	
	//// ATTRIBUTES ////
	public final Consumer<Pixel> transformation;
	
	private ColorSpaceTransformation(Consumer<Pixel> transformation) {
		this.transformation = transformation;
	}
	
	
	////STATIC ////
	private static final class LAB {
		static final float Xn = 95.047f;
		static final float Yn = 100f;
		static final float Zn = 108.883f;
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
