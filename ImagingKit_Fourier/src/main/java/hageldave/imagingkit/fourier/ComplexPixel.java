package hageldave.imagingkit.fourier;

import hageldave.imagingkit.core.PixelBase;

public class ComplexPixel implements PixelBase {
	
	final ComplexImg source;
	int index;

	public ComplexPixel(ComplexImg source, int index) {
		this.source = source;
		this.index = index;
	}
	
	@Override
	public double a_asDouble() {
		return 1.0;
	}

	@Override
	public double r_asDouble() {
		return getSource().getDataReal()[index];
	}

	@Override
	public double g_asDouble() {
		return getSource().getDataImag()[index];
	}

	@Override
	public double b_asDouble() {
		return getSource().getDataPower()[index];
	}

	@Override
	public ComplexPixel setA_fromDouble(double a) {
		// ignore, has no alpha channel
		return this;
	}

	@Override
	public ComplexPixel setR_fromDouble(double r) {
		getSource().getDataReal()[index] = r;
		getSource().computePower(index);
		return this;
	}

	@Override
	public ComplexPixel setG_fromDouble(double g) {
		getSource().getDataImag()[index] = g;
		getSource().computePower(index);
		return this;
	}

	@Override
	public ComplexPixel setB_fromDouble(double b) {
		// ignore, this channel is auto calculated
		return this;
	}
	
	@Override
	public ComplexPixel setRGB_fromDouble(double r, double g, double b) {
		getSource().getDataReal()[index] = r;
		getSource().getDataImag()[index] = g;
		getSource().computePower(index);
		return this;
	}
	
	@Override
	public ComplexPixel setRGB_fromDouble_preserveAlpha(double r, double g, double b) {
		return this.setRGB_fromDouble(r, g, b);
	}
	
	@Override
	public ComplexPixel setARGB_fromDouble(double a, double r, double g, double b) {
		return this.setRGB_fromDouble(r, g, b);
	}

	@Override
	public int getX() {
		return index % getSource().getWidth();
	}

	@Override
	public int getY() {
		return index / getSource().getWidth();
	}

	@Override
	public int getIndex() {
		return this.index;
	}

	@Override
	public ComplexPixel setIndex(int index) {
		this.index = index;
		return this;
	}

	@Override
	public ComplexPixel setPosition(int x, int y) {
		this.index = y * getSource().getWidth() + x;
		return this;
	}

	@Override
	public ComplexImg getSource() {
		return this.source;
	}



}
