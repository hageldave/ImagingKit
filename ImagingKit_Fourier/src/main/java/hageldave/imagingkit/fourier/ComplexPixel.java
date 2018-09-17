package hageldave.imagingkit.fourier;

import hageldave.imagingkit.core.PixelBase;

/**
 * The {@link PixelBase} implementation corresponding to {@link ComplexImg}.<p>
 * A {@link ComplexPixel} represents a complex number consisting of real and imaginary part.
 * The {@link ComplexImg} class is mainly used to store Fourier transforms, thus a ComplexPixel
 * corresponds to a specific x and y frequency in such a transform.
 * This class provides some methods that are specific to complex numbers and Fourier transforms.
 * 
 * @author hageldave
 */
public class ComplexPixel implements PixelBase {
	
	final ComplexImg source;
	int index;

	/**
	 * Creates a new {@link ComplexPixel} referencing the specified {@link ComplexImg}.
	 * @param source image to reference 
	 * @param index of the pixel (i.e. the location in row major order indexing)
	 */
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

	/**
	 * Does not have an effect, {@link ComplexImg} does not have alpha
	 */
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

	/**
	 * Does not have an effect, b corresponds to power and is computed from real and imaginary
	 */
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

	/**
	 * Computes the power of this ComplexPixel which is the squared length of the imaginary
	 * point in the complex plane.<br>
	 * {@code power = real*real + imaginary*imaginary }.<br>
	 * This value will be stored in the power channel ({@link ComplexImg#CHANNEL_POWER})
	 * and can be read via {@link #power()} or {@link #b_asDouble()}.
	 * @return this
	 */
	public ComplexPixel computePower(){
		getSource().computePower(index);
		return this;
	}
	
	/**
	 * @return Real value of this ComplexPixel
	 */
	public double real(){
		return r_asDouble();
	}
	
	/**
	 * @return Imaginary value of this ComplexPixel
	 */
	public double imag(){
		return g_asDouble();
	}
	
	/**
	 * @return Power value of this ComplexPixel. <br>
	 * {@code power = real*real + imaginary*imaginary }<br>
	 * To make sure this value is up to date, use {@link #computePower()}
	 * or {@link ComplexImg#recomputePowerChannel()}.
	 * {@link ComplexImg#enableSynchronizePowerSpectrum(boolean)} can also be used.
	 */
	public double power(){
		return b_asDouble();
	}
	
	/**
	 * Sets the real part of this pixel
	 * @param r real value
	 * @return this
	 */
	public ComplexPixel setReal(double r){
		return setR_fromDouble(r);
	}
	
	/**
	 * Sets the imaginary part of this pixel
	 * @param i imaginary value
	 * @return this
	 */
	public ComplexPixel setImag(double i){
		return setG_fromDouble(i);
	}
	
	/**
	 * Sets both, real and imaginary part of this pixel
	 * @param r real part
	 * @param i imaginary part
	 * @return this
	 */
	public ComplexPixel setComplex(double r, double i){
		return setRGB_fromDouble(r, i, 0);
	}

	/**
	 * Adds a complex number to this pixel
	 * @param r real part to add
	 * @param i imaginary to add
	 * @return this
	 */
	public ComplexPixel add(double r, double i){
		return setComplex(real()+r, imag()+i);
	}
	
	/**
	 * Subtracts a complex number from this pixel
	 * @param r real part to subtract
	 * @param i imaginary to subtract
	 * @return this
	 */
	public ComplexPixel subtract(double r, double i){
		return add(-r,-i);
	}
	
	/**
	 * Conjugates this complex pixel (negates imaginary part).
	 * @return
	 */
	public ComplexPixel conjugate(){
		return setImag(-imag());
	}
	
	/**
	 * Multiplies a complex number to this pixel. This performs a complex multiplication
	 * and stores the result in this pixel.
	 * @param r real part to multiply
	 * @param i imaginary part to multiply
	 * @return
	 */
	public ComplexPixel mult(double r, double i){
		double thisr = real(), thisi = imag();
		return setComplex(thisr*r-thisi*i, thisr*i+thisi*r);
	}
	
	/**
	 * @return the Fourier frequency in x direction this pixel corresponds to
	 */
	public int getXFrequency() {
		int w = getSource().getWidth();
		int center = getSource().getCurrentXshift();
		int freq = ((getX()+w-center+(w-1)/2)%w)-(w-1)/2;
		return freq;
	}
	
	/**
	 * @return the Fourier frequency in y direction this pixel corresponds to
	 */
	public int getYFrequency() {
		int h = getSource().getHeight();
		int center = getSource().getCurrentYshift();
		int freq = ((getY()+h-center+(h-1)/2)%h)-(h-1)/2;
		return freq;
	}
	

}
