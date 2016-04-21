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
				(int) (255*labFuncInv(temp + (px.g()-128)/127.0f)),   // X
				(int) (255*labFuncInv(temp)),                         // Y
				(int) (255*labFuncInv(temp - (px.b()-128)/127.0f)))); // Z
	}),
	XYZ_2_RGB(px->
	{
		px.setValue(Pixel.argb_bounded(px.a(),
				//             X       Y        Z
				px.getGrey(23646,  -8966,   -4680),   // R 
				px.getGrey(-5151,  14264,     887),   // G
				px.getGrey(   52,   -144,   10092))); // B
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
		return q > lab6_29_3 ? (float)Math.cbrt(q):lab1_3_29_6_2*q + (4.0f/29.0f);
	}
	
	private static float labFuncInv(float q){
		return q > lab6_29 ? q*q*q : 3*lab6_29*lab6_29*(q-(4.0f/29.0f));
	}
	
}
