package hageldave.imagingkit.core;

public interface PixelBase {

	public double a_normalized();

	public double r_normalized();

	public double g_normalized();

	public double b_normalized();

	public void setARGB_fromNormalized(double a, double r, double g, double b);

	public void setRGB_fromNormalized(double r, double g, double b);

	public void setRGB_fromNormalized_preserveAlpha(double r, double g, double b);

	public int getX();

	public int getY();

	public double getXnormalized();

	public double getYnormalized();

}
