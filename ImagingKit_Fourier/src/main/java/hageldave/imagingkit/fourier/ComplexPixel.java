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
		return getSource().getValueR_atIndex(index);
	}

	@Override
	public double g_asDouble() {
		return getSource().getValueI_atIndex(index);
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
		getSource().setValueR_atIndex(index, r);
		return this;
	}

	@Override
	public ComplexPixel setG_fromDouble(double g) {
		getSource().setValueI_atIndex(index, g);
		return this;
	}

	@Override
	public ComplexPixel setB_fromDouble(double b) {
		// ignore, this channel is not setable (can only be computed from other 2 channels)
		return this;
	}
	
	@Override
	public ComplexPixel setRGB_fromDouble(double r, double g, double b) {
		getSource().setComplex_atIndex(index, r, g);
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

	public ComplexPixel computePower(){
		getSource().computePower(index);
		return this;
	}
	
	public double real(){
		return r_asDouble();
	}
	
	public double imag(){
		return g_asDouble();
	}
	
	public double power(){
		return b_asDouble();
	}
	
	public ComplexPixel setReal(double r){
		return setR_fromDouble(r);
	}
	
	public ComplexPixel setImag(double i){
		return setG_fromDouble(i);
	}
	
	public ComplexPixel setComplex(double r, double i){
		return setRGB_fromDouble(r, i, 0);
	}

	public ComplexPixel add(double r, double i){
		return setComplex(real()+r, imag()+i);
	}
	
	public ComplexPixel subtract(double r, double i){
		return add(-r,-i);
	}
	
	public ComplexPixel conjugate(){
		return setImag(-imag());
	}
	
	public ComplexPixel mult(double r, double i){
		return setComplex(real()*r-imag()*i, real()*i+r*imag());
	}
	

}
