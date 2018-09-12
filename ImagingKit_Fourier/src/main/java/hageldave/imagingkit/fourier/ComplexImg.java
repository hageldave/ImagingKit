package hageldave.imagingkit.fourier;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Objects;

import hageldave.imagingkit.core.ImgBase;
import hageldave.imagingkit.core.operations.ColorSpaceTransformation;
import hageldave.imagingkit.core.scientific.ColorImg;

/**
 * The ComplexImg class represents an image of complex values.
 * It thus consists of two channels, the real part channel and the
 * imaginary part channel. A third channel can be leveraged to store
 * the power spectrum of the image which consists of the squared lengths
 * of the complex values {@code (complex=a+bi, power=a*a+b*b)}, see
 * {@link #enableSynchronizePowerSpectrum(boolean)} and {@link #recomputePowerChannel()}.
 * <p>
 * The main purpose of the ComplexImg is to store the result of a Fourier transformation
 * of another image, which can be obtained using the {@link Fourier} class.
 * Therefore ComplexImg offers specific methods tailored to Fourier use cases such as<br>
 * <ul>
 * <li>{@link #getDCreal()} for obtaining the DC component, </li>
 * <li>{@link #shiftCornerToCenter()} for DC centering </li>
 * <li>{@link #shift(int, int)}, {@link #resetShift()}, {@link #getCurrentXshift()}, {@link #getCurrentYshift()}
 * for any kinds of shifting stuff</li>
 * <li>{@link #computePower(int, int)},{@link #computePhase(int, int)} to obtain power and phase</li>
 * <li>{@link #getPowerSpectrumImg()}, {@link #getPhaseSpectrumImg()}, {@link #getPowerPhaseSpectrumImg()}
 * for displaying and examining interesting quantities of the transform</li>
 * </ul>
 *
 * @author hageldave
 *
 */
public class ComplexImg implements ImgBase<ComplexPixel> {

	/** real part channel index - equal to red channel */
	public static final int CHANNEL_REAL = ColorImg.channel_r;
	/** imaginary part channel index - equal to green channel */
	public static final int CHANNEL_IMAG = ColorImg.channel_g;
	/** power channel index - equal to blue channel */
	public static final int CHANNEL_POWER = ColorImg.channel_b;

	private final int width;
	private final int height;

	private final double[] real;
	private final double[] imag;
	/* power spectrum: real*real+imag*imag */
	private final double[] power;

	/* delegate image for operations like interpolate that are already implemented */
	private final ColorImg delegate;

	private int currentXshift = 0;
	private int currentYshift = 0;

	private boolean synchronizePowerSpectrum = false;

	/**
	 * Creates a new ComplexImg of specified dimension
	 * @param dims desired dimensions
	 */
	public ComplexImg(Dimension dims){
		this(dims.width, dims.height);
	}

	/**
	 * Creates a new ComplexImg of specified dimension
	 * @param width of the image
	 * @param height of the image
	 */
	public ComplexImg(int width, int height) {
		this(width, height, new double[width*height],new double[width*height],new double[width*height]);
	}

	/**
	 * Creates a new ComplexImg of specified dimension, using the provided arrays as channels.
	 * Only the real channel is mandatory, the others may be null and will then be initialized by the constructor.
	 * @param width of the image
	 * @param height of the image
	 * @param real the real part array to be used by this image (has to have length {@code width*height})
	 * @param imag (optional, may be null) the imaginary part array to be used by this image (has to have length {@code width*height})
	 * @param power (optional, may be null) the power channel array to be used by ths image (has to have length {@code width*height})
	 *
	 * @throws NullPointerException when real array is null
	 * @throws IllegalArgumentException when provided arrays are not of length width*height
	 */
	public ComplexImg(int width, int height, double[] real, double[] imag, double[] power){
		// sanity check 1:
		this.real = Objects.requireNonNull(real);
		if(width*height != real.length){
			throw new IllegalArgumentException(String.format(
					"Provided Dimension (width=%d, height=$d) does not match number of provided Pixels %d", 
					width, height, real.length));
		}

		this.width = width;
		this.height = height;
		this.imag = imag !=null ?  imag:new double[width*height];
		this.power= power!=null ? power:new double[width*height];

		// sanity check 2:
		if(this.real.length != this.imag.length || this.imag.length != this.power.length){
			throw new IllegalArgumentException(String.format(
					"Provided data arrays are not of same size. real[%d] imag[%d] power[%d]", 
					this.real.length, this.imag.length, this.power.length));
		}

		this.delegate = new ColorImg(this.width, this.height, this.real, this.imag, this.power, null);
	}


	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public int getHeight() {
		return this.height;
	}


	@Override
	public ComplexPixel getPixel() {
		return new ComplexPixel(this, 0);
	}


	@Override
	public ComplexPixel getPixel(int x, int y) {
		return new ComplexPixel(this, 0).setPosition(x, y);
	}


	@Override
	public BufferedImage toBufferedImage(BufferedImage bimg) {
		return delegate.toBufferedImage(bimg);
	}


	@Override
	public ComplexImg copy() {
		ComplexImg copy = new ComplexImg(
				getWidth(),
				getHeight(),
				Arrays.copyOf(real, real.length),
				Arrays.copyOf(imag, imag.length),
				Arrays.copyOf(power, power.length));
		copy.currentXshift = currentXshift;
		copy.currentYshift = currentYshift;
		return copy;
	}

	/**
	 * Returns the value of this image at the specified position for the specified channel.
	 * No bounds checks will be performed, positions outside of this
	 * image's dimension can either result in a value for a different position
	 * or an ArrayIndexOutOfBoundsException.
	 * <p>
	 * Valid channels are {@link #CHANNEL_REAL}, {@link #CHANNEL_IMAG} and {@link #CHANNEL_POWER}.
	 * @param x coordinate
	 * @param y coordinate
	 * @return value for specified position and channel
	 * @throws ArrayIndexOutOfBoundsException if resulting index from x and y
	 * is not within the data arrays bounds or if the specified channel is not in [0,2]
	 */
	public double getValue(int channel, int x, int y) {
		return delegate.getValue(channel, x, y);
	}

	/**
	 * Returns the real value (a of a+bi) of this image at the specified position.
	 * No bounds checks will be performed, positions outside of this
	 * image's dimension can either result in a value for a different position
	 * or an ArrayIndexOutOfBoundsException.
	 * @param x coordinate
	 * @param y coordinate
	 * @return real value for specified position
	 * @throws ArrayIndexOutOfBoundsException if resulting index from x and y
	 * is not within the data arrays bounds
	 */
	public double getValueR(int x, int y) {
		return delegate.getValueR(x, y);
	}

	/**
	 * Returns the imaginary value (b of a+bi) of this image at the specified position.
	 * No bounds checks will be performed, positions outside of this
	 * image's dimension can either result in a value for a different position
	 * or an ArrayIndexOutOfBoundsException.
	 * @param x coordinate
	 * @param y coordinate
	 * @return imaginary value for specified position
	 * @throws ArrayIndexOutOfBoundsException if resulting index from x and y
	 * is not within the data arrays bounds
	 */
	public double getValueI(int x, int y) {
		return delegate.getValueG(x, y);
	}

	/**
	 * Returns the power value ({@code a*a+b*b} of {@code a+bi}) of this image at the specified position. <br>
	 * <b>Please Note:</b> there is no guarantee that the power channel is up to date, except when {@link #recomputePowerChannel()}
	 * has been called before. You can also use {@link #synchronizePowerSpectrum} to enable power spectrum synchronization
	 * on changes via the set and pixel methods. Alternatively to {@link #getValueP(int, int)} you can use
	 * {@link #computePower(int, int)}.
	 * <p>
	 * No bounds checks will be performed, positions outside of this
	 * image's dimension can either result in a value for a different position
	 * or an ArrayIndexOutOfBoundsException.
	 * @param x coordinate
	 * @param y coordinate
	 * @return imaginary value for specified position
	 * @throws ArrayIndexOutOfBoundsException if resulting index from x and y
	 * is not within the data arrays bounds
	 */
	public double getValueP(int x, int y) {
		return delegate.getValueB(x, y);
	}

	/**
	 * See {@link ColorImg#getValue(int, int, int, int)}.
	 * <p>
	 * Please Notice that when using {@link #CHANNEL_POWER}, 
	 * this will not calculate the power in case it is not up to date.
	 * Use {@link #recomputePowerChannel()} to make sure this method yields reasonable results.
	 * @param channel one of {@link #CHANNEL_REAL},{@link #CHANNEL_IMAG},{@link #CHANNEL_POWER}(0,1,2)
	 * @param x coordinate
	 * @param y coordinate
	 * @param boundaryMode one of the boundary modes e.g. {@link ColorImg#boundary_mode_repeat_image}
	 * @return value at specified position or a value depending on the
	 * boundary mode for out of bounds positions.
	 */
	public double getValue(int channel, int x, int y, int boundaryMode) {
		return delegate.getValue(channel, x, y, boundaryMode);
	}

	/**
	 * Equivalent to {@link #getValue(int, int, int, int)} with <br>
	 * {@code getValue(CHANNEL_REAL, x, y, boundaryMode)}.
	 * @param x coordinate
	 * @param y coordinate
	 * @param boundaryMode one of the boundary modes e.g. {@link ColorImg#boundary_mode_repeat_image}
	 * @return real part at specified position or a value depending on the
	 * boundary mode for out of bounds positions.
	 */
	public double getValueR(int x, int y, int boundaryMode) {
		return delegate.getValueR(x, y, boundaryMode);
	}

	/**
	 * Equivalent to {@link #getValue(int, int, int, int)} with <br>
	 * {@code getValue(CHANNEL_IMAG, x, y, boundaryMode)}.
	 * @param x coordinate
	 * @param y coordinate
	 * @param boundaryMode one of the boundary modes e.g. {@link ColorImg#boundary_mode_repeat_image}
	 * @return imaginary part at specified position or a value depending on the
	 * boundary mode for out of bounds positions.
	 */
	public double getValueI(int x, int y, int boundaryMode) {
		return delegate.getValueG(x, y, boundaryMode);
	}

	/**
	 * Equivalent to {@link #getValue(int, int, int, int)} with <br>
	 * {@code getValue(CHANNEL_POWER, x, y, boundaryMode)}.
	 * <p>
	 * Please Notice that this will not calculate the power in case it is not up to date.
	 * Use {@link #recomputePowerChannel()} to make sure this method yields reasonable results.
	 * @param x coordinate
	 * @param y coordinate
	 * @param boundaryMode one of the boundary modes e.g. {@link ColorImg#boundary_mode_repeat_image}
	 * @return power value (real*real+imag*imag) at specified position or a value depending on the
	 * boundary mode for out of bounds positions.
	 */
	public double getValueP(int x, int y, int boundaryMode) {
		return delegate.getValueB(x, y, boundaryMode);
	}

	/**
	 * Returns the index of the pixel with highest value for specified channel
	 * @param channel one of {@link #CHANNEL_REAL},{@link #CHANNEL_IMAG},{@link #CHANNEL_POWER}(0,1,2)
	 * @return index of pixel with max channel value
	 */
	public int getIndexOfMaxValue(int channel) {
		return delegate.getIndexOfMaxValue(channel);
	}

	/**
	 * Returns the highest value for specified channel
	 * @param channel one of {@link #CHANNEL_REAL},{@link #CHANNEL_IMAG},{@link #CHANNEL_POWER}(0,1,2)
	 * @return max channel value
	 */
	public double getMaxValue(int channel) {
		return delegate.getMaxValue(channel);
	}

	/**
	 * Returns the index of the pixel with lowest value for specified channel
	 * @param channel one of {@link #CHANNEL_REAL},{@link #CHANNEL_IMAG},{@link #CHANNEL_POWER}(0,1,2)
	 * @return index of pixel with min channel value
	 */
	public int getIndexOfMinValue(int channel) {
		return delegate.getIndexOfMinValue(channel);
	}

	/**
	 * Returns lowest value for specified channel
	 * @param channel one of {@link #CHANNEL_REAL},{@link #CHANNEL_IMAG},{@link #CHANNEL_POWER}(0,1,2)
	 * @return min channel value
	 */
	public double getMinValue(int channel) {
		return delegate.getMinValue(channel);
	}

	/**
	 * Bilinearly interpolates the value for the specified channel at the specified coordinates.
	 * See {@link ColorImg#interpolate(int, double, double)} for details.
	 * @param channel one of {@link #CHANNEL_REAL},{@link #CHANNEL_IMAG},{@link #CHANNEL_POWER}(0,1,2)
	 * @param xNormalized coordinate within [0,1]
	 * @param yNormalized coordinate within [0,1]
	 * @return bilinearly interpolated value
	 */
	public double interpolate(int channel, double xNormalized, double yNormalized) {
		return delegate.interpolate(channel, xNormalized, yNormalized);
	}

	/**
	 * Bilinearly interpolates the real part at the specified coordinates.
	 * See {@link ColorImg#interpolate(int, double, double)} for details.
	 * @param xNormalized coordinate within [0,1]
	 * @param yNormalized coordinate within [0,1]
	 * @return bilinearly interpolated value
	 */
	public double interpolateR(double xNormalized, double yNormalized) {
		return delegate.interpolateR(xNormalized, yNormalized);
	}

	/**
	 * Bilinearly interpolates the imaginary part at the specified coordinates.
	 * See {@link ColorImg#interpolate(int, double, double)} for details.
	 * @param xNormalized coordinate within [0,1]
	 * @param yNormalized coordinate within [0,1]
	 * @return bilinearly interpolated value
	 */
	public double interpolateI(double xNormalized, double yNormalized) {
		return delegate.interpolateG(xNormalized, yNormalized);
	}

	/**
	 * Bilinearly interpolates the power (real*real+imag*imag) at the specified coordinates.
	 * See {@link ColorImg#interpolate(int, double, double)} for details.
	 * <p>
	 * Please Notice that this will not calculate the power in case it is not up to date.
	 * Use {@link #recomputePowerChannel()} to make sure this method yields reasonable results.
	 * @param xNormalized coordinate within [0,1]
	 * @param yNormalized coordinate within [0,1]
	 * @return bilinearly interpolated value
	 */
	public double interpolateP(double xNormalized, double yNormalized) {
		return delegate.interpolateB(xNormalized, yNormalized);
	}

	/**
	 * Equivalent to {@link #getDataReal()}[index].
	 * @param index of the desired value
	 * @return value at index
	 */
	public double getValueR_atIndex(int index){
		return real[index];
	}

	/**
	 * Equivalent to {@link #getDataImag()}[index].
	 * @param index of the desired value
	 * @return value at index
	 */
	public double getValueI_atIndex(int index){
		return imag[index];
	}

	/**
	 * Equivalent to {@link #getDataPower()}[index].
	 * <p>
	 * Please Notice that this will not calculate the power in case it is not up to date.
	 * Use {@link #recomputePowerChannel()} before to make sure this method yields reasonable results.
	 * Alternatively you can use {@link #computePower(int)}.
	 * @param index of the desired value
	 * @return value at index
	 */
	public double getValueP_atIndex(int index){
		return power[index];
	}

	/**
	 * Equivalent to {@link #getDataReal()}[index] = value.
	 * <br>
	 * If {@link #isSynchronizePowerSpectrum()} is true, then this will also
	 * update the corresponding power value.
	 * @param index for the value to be set
	 * @param value to be set at index
	 * 
	 * @see #setValueI_atIndex(int, double)
	 * @see #setComplex_atIndex(int, double, double)
	 */
	public void setValueR_atIndex(int index, double value){
		real[index] = value;
		if(synchronizePowerSpectrum){
			computePower(index);
		}
	}

	/**
	 * Equivalent to {@link #getDataImag()}[index] = value.
	 * <br>
	 * If {@link #isSynchronizePowerSpectrum()} is true, then this will also
	 * update the corresponding power value.
	 * @param index for the value to be set
	 * @param value to be set at index
	 * 
	 * @see #setValueR_atIndex(int, double)
	 * @see #setComplex_atIndex(int, double, double)
	 */
	public void setValueI_atIndex(int index, double value){
		imag[index] = value;
		if(synchronizePowerSpectrum){
			computePower(index);
		}
	}

	/**
	 * Sets real part and imaginary part of the complex number at specified index.
	 * <br>
	 * If {@link #isSynchronizePowerSpectrum()} is true, then this will also
	 * update the corresponding power value.
	 * @param index for the value to be set
	 * @param real part
	 * @param imag part
	 */
	public void setComplex_atIndex(int index, double real, double imag){
		this.real[index] = real;
		this.imag[index] = imag;
		if(synchronizePowerSpectrum){
			computePower(index);
		}
	}

	/**
	 * Sets the real part at the specified position
	 * <br>
	 * If {@link #isSynchronizePowerSpectrum()} is true, then this will also
	 * update the corresponding power value.
	 * @param x coordinate
	 * @param y coordinate
	 * @param value to be set
	 */
	public void setValueR(int x, int y, double value) {
		int idx=y*width+x;
		setValueR_atIndex(idx, value);
	}

	/**
	 * Sets the imaginary part at the specified position
	 * <br>
	 * If {@link #isSynchronizePowerSpectrum()} is true, then this will also
	 * update the corresponding power value.
	 * @param x coordinate
	 * @param y coordinate
	 * @param value to be set
	 */
	public void setValueI(int x, int y, double value) {
		int idx=y*width+x;
		setValueI_atIndex(idx, value);
	}

	/**
	 * Sets the real and imaginary part at the specified position
	 * <br>
	 * If {@link #isSynchronizePowerSpectrum()} is true, then this will also
	 * update the corresponding power value.
	 * @param x coordinate
	 * @param y coordinate
	 * @param value to be set
	 */
	public void setComplex(int x, int y, double real, double imag){
		int idx=y*width+x;
		setComplex_atIndex(idx, real, imag);
	}

	/**
	 * Computes the power for all pixels of this image.
	 * Subsequent calls to e.g. {@link #getValueP(int, int)} will be up to date
	 * until real or imaginary parts are modified.
	 * @return this
	 */
	public ComplexImg recomputePowerChannel(){
		for(int i=0; i<real.length; i++){
			computePower(i);
		}
		return this;
	}

	/**
	 * Calculates, stores and returns the power at the specified index.
	 * The power is the squared magnitude of the complex number ( c=a+bi -> power = a*a+b*b ).
	 * Subsequent calls to e.g. {@link #getValueP_atIndex(int)} will return the stored result.
	 * @param idx index
	 * @return power of pixel at index
	 */
	public double computePower(int idx){
		double r = real[idx];
		double i = imag[idx];
		power[idx] = r*r+i*i;
		return power[idx];
	}
	
	/**
	 * Calculates, stores and returns the power at the specified position.
	 * The power is the squared magnitude of the complex number ( c=a+bi -> power = a*a+b*b ).
	 * Subsequent calls to e.g. {@link #getValueP(int, int)} will return the stored result.
	 * @param x coordinate
	 * @param y coordinate
	 * @return power of pixel at position
	 */
	public double computePower(int x, int y){
		return computePower(y*width+x);
	}

	/**
	 * Calculates the phase of the pixel at the specified index.
	 * The phase is the argument of the complex number, i.e. the angle of the complex vector
	 * in the complex plane. 
	 * @param idx
	 * @return the phase in [0,2pi] of the complex number at index
	 */
	public double computePhase(int idx){
		double r = real[idx];
		double i = imag[idx];
		return atan2(r, i);
	}

	/**
	 * Calculates the phase of the pixel at the specified position.
	 * The phase is the argument of the complex number, i.e. the angle of the complex vector
	 * in the complex plane. 
	 * @param x coordinate
	 * @param y coordinate
	 * @return the phase in [0,2pi] of the complex number at position
	 */
	public double computePhase(int x, int y){
		return computePhase(y*width+x);
	}

	/**
	 * Fills the specified channel with the specified value.
	 * All values of this channel will be same afterwards.
	 * <br>
	 * If {@link #isSynchronizePowerSpectrum()} is true, then this will also
	 * call {@link #recomputePowerChannel()}.
	 * 
	 * @param channel to fill
	 * @param value to fill with
	 * @return this
	 */
	public ComplexImg fill(int channel, double value) {
		delegate.fill(channel, value);
		if(synchronizePowerSpectrum && (channel == CHANNEL_REAL || channel == CHANNEL_IMAG)){
			recomputePowerChannel();
		}
		return this;
	}

	/**
	 * Calls {@link ColorImg#getRemoteBufferedImage()} on delegate ({@link #getDelegate()}).
	 * <br>
	 * For display you probably want to use {@link #getPowerSpectrumImg()} or {@link #getPhaseSpectrumImg()}
	 * 
	 * @return {@code getDelegate().getRemoteBufferedImage() }
	 */
	public BufferedImage getRemoteBufferedImage() {
		return delegate.getRemoteBufferedImage();
	}

	/**
	 * Returns true.
	 */
	public boolean supportsRemoteBufferedImage() {
		return delegate.supportsRemoteBufferedImage();
	}

	/**
	 * See {@link ColorImg#copyArea(int, int, int, int, ColorImg, int, int)}
	 * 
	 * @param x area origin in this image (x-coordinate)
	 * @param y area origin in this image (y-coordinate)
	 * @param w width of area
	 * @param h height of area
	 * @param dest destination image
	 * @param destX area origin in destination image (x-coordinate)
	 * @param destY area origin in destination image (y-coordinate)
	 * @return the destination image, or newly created image if destination was null.
	 * @throws IllegalArgumentException if the specified area is not within
	 * the bounds of this image or if the size of the area is not positive.
	 */
	public ComplexImg copyArea(int x, int y, int w, int h, ComplexImg dest, int destX, int destY) {
		if(dest == null){
			dest = new ComplexImg(w, h);
		}
		delegate.copyArea(x, y, w, h, dest.delegate, destX, destY);
		return dest;
	}

	/**
	 * Returns the delegate {@link ColorImg} backing this {@link ComplexImg}.
	 * The delegate is used to implement functionality like {@link #getRemoteBufferedImage()}
	 * or {@link #interpolate(int, double, double)}.
	 * @return {@link ColorImg} delegate of this complex image.
	 */
	public ColorImg getDelegate(){
		return delegate;
	}

	/**
	 * Returns the real part channel
	 * @return real part channel
	 */
	public double[] getDataReal() {
		return real;
	}

	/**
	 * Returns the imaginary part channel
	 * @return imaginary part channel
	 */
	public double[] getDataImag() {
		return imag;
	}

	/**
	 * Returns the power channel
	 * @return power spectrum channel
	 */
	public double[] getDataPower() {
		return power;
	}

	/**
	 * Returns wether {@link #enableSynchronizePowerSpectrum(boolean)} has been set to true or false
	 * @return true when synchronization is enabled, else false
	 */
	public boolean isSynchronizePowerSpectrum() {
		return synchronizePowerSpectrum;
	}

	/**
	 * Enables or disables power channel synchronization.
	 * When enabled this will update the power value (squared magnitude of the complex number) when changes to
	 * the real or imaginary values are made.
	 * Only changes made using methods like {@link #setValueR(int, int, double)} or {@link ComplexPixel#setImag(double)}
	 * will result in the power being updated, indirect modifications via {@link #getDataReal()} or {@link #getDelegate()}
	 * are not noticed.
	 * When enabling, this will also call {@link #recomputePowerChannel()} in order to have the power synchronized
	 * @param synchronizePowerSpectrum true when enabling, false when disabling
	 * @return this
	 */
	public ComplexImg enableSynchronizePowerSpectrum(boolean synchronizePowerSpectrum) {
		this.synchronizePowerSpectrum = synchronizePowerSpectrum;
		if(synchronizePowerSpectrum){
			recomputePowerChannel();
		}
		return this;
	}

	/**
	 * Shifts this image by the specified translation.
	 * The shift is torus like, so when shifting pixels to the right over the image border, 
	 * they will reappear on the left side of the image.
	 * This is useful when the DC value should be centered instead of in the top left corner
	 * (you may use {@link #shiftCornerToCenter()} for this special task though).
	 * To reset use {@link #resetShift()}, 
	 * use {@link #getCurrentXshift()} and {@link #getCurrentYshift()} to get the current shift
	 * of this image.
	 * 
	 * @param x shift (positive shifts right)
	 * @param y shift (positive shift down)
	 * @return this
	 */
	public ComplexImg shift(int x, int y){
		while(x < 0) x += getWidth();
		while(y < 0) y += getHeight();
		x %= getWidth();
		y %= getHeight();
		ArrayUtils.shift2D(real, width, height, x, y);
		ArrayUtils.shift2D(imag, width, height, x, y);
		if(synchronizePowerSpectrum)
			ArrayUtils.shift2D(power, width, height, x, y);
		setCurrentShift(this.currentXshift+x, this.currentYshift+y);
		return this;
	}

	/**
	 * Shifts the image so that the current top left pixel (0,0) will be at the center 
	 * (width/2, height/2).
	 * Useful when you want to display the spectrum where DC is in the center.
	 * @return this
	 */
	public ComplexImg shiftCornerToCenter(){
		return shift(width/2, height/2);
	}

	/**
	 * Resets the current shift, so that DC is in top left corner (0,0).
	 * @return this
	 */
	public ComplexImg resetShift(){
		return shift(width-currentXshift, height-currentYshift);
	}

	/**
	 * Sets the current shift values
	 * @param xshift shift in x direction
	 * @param yshift shift in y direction
	 */
	protected void setCurrentShift(int xshift, int yshift){
		this.currentXshift = xshift%width;
		this.currentYshift = yshift%height;
	}

	/**
	 * Returns the real part of the DC value, i.e. the zero frequency component.
	 * @return DC value
	 */
	public double getDCreal(){
		return getValueR(currentXshift, currentYshift);
	}

	/**
	 * Returns the current shift in x direction
	 * @return x direction shift
	 */
	public int getCurrentXshift() {
		return currentXshift;
	}

	/**
	 * Returns the current shift in y direction
	 * @return y direction shift
	 */
	public int getCurrentYshift() {
		return currentYshift;
	}

	/**
	 * Returns a {@link ColorImg} consisting of this ComplexImg's power channel
	 * log-scaled for display.
	 * Values are displayed in grayscale.
	 * @return image of power spectrum for display
	 * 
	 * @see #getPhaseSpectrumImg()
	 * @see #getPowerPhaseSpectrumImg()
	 */
	public ColorImg getPowerSpectrumImg(){
		this.recomputePowerChannel();
		// get copy of power channel
		ColorImg powerSpectrum = this.getDelegate().getChannelImage(ComplexImg.CHANNEL_POWER).copy();
		// logarithmize values
		powerSpectrum.forEach(px->px.setValue(0, Math.log(1+px.getValue(0))));
		// normalize values
		powerSpectrum.scaleChannelToUnitRange(0);
		// copy 1st channel to others to retain grayscale
		System.arraycopy(powerSpectrum.getData()[0], 0, powerSpectrum.getData()[1], 0, powerSpectrum.numValues());
		System.arraycopy(powerSpectrum.getData()[0], 0, powerSpectrum.getData()[2], 0, powerSpectrum.numValues());
		return powerSpectrum;
	}

	/**
	 * Returns a {@link ColorImg} showing the phase of this ComplexImg intended for display.
	 * The phase angle values are mapped to the a*b* plane of CIE L*a*b* so that same color 
	 * indicates same angle. 
	 * CIE L*a*b* is chosen to ensure a perceived constant luminance of the colors. 
	 * 
	 * @return phase spectrum image for display
	 * 
	 * @see #getPowerSpectrumImg()
	 * @see #getPowerPhaseSpectrumImg()
	 */
	public ColorImg getPhaseSpectrumImg(){
		ColorImg phaseImg = new ColorImg(this.getDimension(), false);
		// compute phase colors
		phaseImg.forEach(px->{
			double phase = this.computePhase(px.getIndex());
			// get phase position on unit circle in a*b* plane
			double x = Math.cos(phase);
			double y = Math.sin(phase);
			// set color (0.2 is ~max radius for which colors are still in RGB gammut at L*=0.74 )
			px.setValue(1, 0.5+0.2*x);
			px.setValue(2, 0.5+0.2*y);
		});
		// set luminance to 0.74 (greatest RGB range around center here)
		phaseImg.fill(0, 0.74);
		// convert from LAB 2 RGB
		phaseImg.forEach(ColorSpaceTransformation.LAB_2_RGB);
		return phaseImg;
	}

	/**
	 * Returns a combination of power and phase spectrum image for display.
	 * This maps power (squared magnitude) to luminance and phase angle to color.
	 * As in {@link #getPhaseSpectrumImg()} color is mapped to the a*b* plane of 
	 * CIE L*a*b* to ensure constant perceived luminance for constant power.
	 * 
	 * @return power phase spectrum image (combination of both spectra) for display
	 * 
	 * @see #getPowerSpectrumImg()
	 * @see #getPhaseSpectrumImg()
	 */
	public ColorImg getPowerPhaseSpectrumImg(){
		// calculate power spectrum
		this.recomputePowerChannel();
		// get upper bound (used for normalization)
		final double maxLogPow = Math.log(1+this.getMaxValue(CHANNEL_POWER));
		ColorImg powerphase = new ColorImg(this.getDimension(), false);
		// create phase image in L*a*b* color space
		powerphase.forEach(px->{
			// calculate phase (in [0..2pi])
			double phase = this.computePhase(px.getIndex());
			// get power
			double logPower = Math.log(1+this.getDataPower()[px.getIndex()]);
			// normalize power
			double normLogPow = logPower/maxLogPow;
			// get phase position on unit circle in a*b* plane
			double x = Math.cos(phase);
			double y = Math.sin(phase);
			// calc radius in a*b* plane (0.2 is ~max radius for which colors are still in RGB gammut at L*=0.74)
			double radius = 0.2*normLogPow;
			// set L* according to power (L*=0.74 has a*b* plane with maximum RGB gammut around center)
			px.setValue(0, 0.74*normLogPow);
			// set color according to phase (also scale by radius because RGB gammut gets smaller with decreasing L*)
			px.setValue(1, 0.5+radius*x);
			px.setValue(2, 0.5+radius*y);
		});
		powerphase.forEach(ColorSpaceTransformation.LAB_2_RGB);
		return powerphase;
	}

	private final static double TWOPI = Math.PI*2;
	/**
	 * Returns angle of point (x,y) in polar coordinates.
	 * Other than {@link Math#atan2(double, double)} this
	 * returns a value in [0, 2pi] instead of [-pi, pi].
	 * @param x coord of point
	 * @param y coord of point
	 * @return angle of point in polar coords
	 */
	public static double atan2(double x, double y){
		return (TWOPI+Math.atan2(y,x))%TWOPI;
	}
}
