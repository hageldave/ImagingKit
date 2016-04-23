package hageldave.imagingkit.core;

import java.util.function.Consumer;

public enum ColorSpaceTransformation {
	/** TODO: javadocs for each */
	RGB_2_XYZ(px->
	{
		px.setARGB(px.a(), 
				//             R      G     B
				px.getGrey(   49,    31,   20),  // X
				px.getGrey(17697, 81240, 1063),  // Y
				px.getGrey(    0,     1,   99)); // Z
	}),
	XYZ_2_LAB(px->
	{
		float temp = labFunc(px.g()/255.0f);
		px.setARGB(px.a(), 
				(int) (255*temp),                               // L
				(int) (127*(labFunc(px.r()/255.0f)-temp)+128),  // a
				(int) (127*(temp-labFunc(px.b()/255.0f))+128)); // b
	}),
	LAB_2_XYZ(px->
	{
		float temp = px.r()/255.0f; // =L/255
		px.setValue(Pixel.argb_bounded(px.a(), 
				(int) (255*labFuncInv(temp + (px.g()-127)/128.0f)),   // X
				(int) (255*labFuncInv(temp)),                         // Y
				(int) (255*labFuncInv(temp - (px.b()-127)/128.0f)))); // Z
	}),
	XYZ_2_RGB(px->
	{
		px.setValue(Pixel.argb_bounded(px.a(),
				//             X       Y        Z
				px.getGrey(23646,  -8966,   -4680),   // R 
				px.getGrey(-5151,  14264,     887),   // G
				px.getGrey(   52,   -144,   10092))); // B
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
	HSV_2_RGB(px->{
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
	
	
	//// STATIC ////
	private static final float lab6_29 = 6.0f/29.0f;
	private static final float lab6_29_3 = lab6_29*lab6_29*lab6_29;
	private static final float lab1_3_29_6_2 = (1.0f/3.0f) * (29.0f/6.0f) * (29.0f/6.0f);
	
	private static float labFunc(float q){
//		return q > lab6_29_3 ? (float)Math.cbrt(q):lab1_3_29_6_2*q + (4.0f/29.0f);
		return (float) Math.cbrt(q);
	}
	
	private static float labFuncInv(float q){
//		return q > lab6_29 ? q*q*q : 3*lab6_29*lab6_29*(q-(4.0f/29.0f));
		return q*q*q;
	}
	
}
