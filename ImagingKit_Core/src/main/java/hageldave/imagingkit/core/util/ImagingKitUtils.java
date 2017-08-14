package hageldave.imagingkit.core.util;

public final class ImagingKitUtils {
	static{new ImagingKitUtils();}
	private ImagingKitUtils() {}


	public static final int clamp_0_255(int val){
		return Math.max(0, Math.min(val, 255));
	}

	public static double clamp_0_1(double val){
		return Math.max(0.0, Math.min(val, 1.0));
	}

}
