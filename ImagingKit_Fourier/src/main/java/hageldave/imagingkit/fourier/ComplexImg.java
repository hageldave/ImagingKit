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

	/* delegate image for operations like interpolate that already are implemented */
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
			throw new IllegalArgumentException(String.format("Provided Dimension (width=%d, height=$d) does not match number of provided Pixels %d", width, height, real.length));
		}

		this.width = width;
		this.height = height;
		this.imag = imag !=null ?  imag:new double[width*height];
		this.power= power!=null ? power:new double[width*height];

		// sanity check 2:
		if(this.real.length != this.imag.length || this.imag.length != this.power.length){
			throw new IllegalArgumentException(String.format("Provided data arrays are not of same size. real[%d] imag[%d] power[%d]", this.real.length, this.imag.length, this.power.length));
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

	public double getValue(int channel, int x, int y, int boundaryMode) {
		return delegate.getValue(channel, x, y, boundaryMode);
	}

	public double getValueR(int x, int y, int boundaryMode) {
		return delegate.getValueR(x, y, boundaryMode);
	}

	public double getValueI(int x, int y, int boundaryMode) {
		return delegate.getValueG(x, y, boundaryMode);
	}

	public double getValueP(int x, int y, int boundaryMode) {
		return delegate.getValueB(x, y, boundaryMode);
	}

	public int getIndexOfMaxValue(int channel) {
		return delegate.getIndexOfMaxValue(channel);
	}

	public double getMaxValue(int channel) {
		return delegate.getMaxValue(channel);
	}

	public int getIndexOfMinValue(int channel) {
		return delegate.getIndexOfMinValue(channel);
	}

	public double getMinValue(int channel) {
		return delegate.getMinValue(channel);
	}

	public double interpolate(int channel, double xNormalized, double yNormalized) {
		return delegate.interpolate(channel, xNormalized, yNormalized);
	}

	public double interpolateR(double xNormalized, double yNormalized) {
		return delegate.interpolateR(xNormalized, yNormalized);
	}

	public double interpolateI(double xNormalized, double yNormalized) {
		return delegate.interpolateG(xNormalized, yNormalized);
	}

	public double interpolateP(double xNormalized, double yNormalized) {
		return delegate.interpolateB(xNormalized, yNormalized);
	}

	public double getValueR_atIndex(int index){
		return real[index];
	}

	public double getValueI_atIndex(int index){
		return imag[index];
	}

	public double getValueP_atIndex(int index){
		return power[index];
	}

	public void setValueR_atIndex(int index, double value){
		real[index] = value;
		if(synchronizePowerSpectrum){
			computePower(index);
		}
	}

	public void setValueI_atIndex(int index, double value){
		imag[index] = value;
		if(synchronizePowerSpectrum){
			computePower(index);
		}
	}

	public void setComplex_atIndex(int index, double real, double imag){
		this.real[index] = real;
		this.imag[index] = imag;
		if(synchronizePowerSpectrum){
			computePower(index);
		}
	}

	public void setValueR(int x, int y, double value) {
		int idx=y*width+x;
		setValueR_atIndex(idx, value);
	}

	public void setValueI(int x, int y, double value) {
		int idx=y*width+x;
		setValueI_atIndex(idx, value);
	}

	public void setComplex(int x, int y, double real, double imag){
		int idx=y*width+x;
		setComplex_atIndex(idx, real, imag);
	}

	public double computePower(int idx){
		double r = real[idx];
		double i = imag[idx];
		power[idx] = r*r+i*i;
		return power[idx];
	}

	public ComplexImg recomputePowerChannel(){
		for(int i=0; i<real.length; i++){
			computePower(i);
		}
		return this;
	}

	public double computePower(int x, int y){
		return computePower(y*width+x);
	}

	public double computePhase(int idx){
		double r = real[idx];
		double i = imag[idx];
		return atan2(r, i);
	}

	public double computePhase(int x, int y){
		return computePhase(y*width+x);
	}

	public ComplexImg fill(int channel, double value) {
		delegate.fill(channel, value);
		return this;
	}

	public BufferedImage getRemoteBufferedImage() {
		return delegate.getRemoteBufferedImage();
	}

	public boolean supportsRemoteBufferedImage() {
		return delegate.supportsRemoteBufferedImage();
	}

	public ColorImg copyArea(int x, int y, int w, int h, ComplexImg dest, int destX, int destY) {
		return delegate.copyArea(x, y, w, h, dest.delegate, destX, destY);
	}

	public ColorImg getDelegate(){
		return delegate;
	}

	public double[] getDataReal() {
		return real;
	}

	public double[] getDataImag() {
		return imag;
	}

	public double[] getDataPower() {
		return power;
	}

	public boolean isSynchronizePowerSpectrum() {
		return synchronizePowerSpectrum;
	}

	public ComplexImg enableSynchronizePowerSpectrum(boolean synchronizePowerSpectrum) {
		this.synchronizePowerSpectrum = synchronizePowerSpectrum;
		if(synchronizePowerSpectrum){
			recomputePowerChannel();
		}
		return this;
	}

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

	public ComplexImg shiftCornerToCenter(){
		return shift(width/2, height/2);
	}

	public ComplexImg resetShift(){
		return shift(width-currentXshift, height-currentYshift);
	}

	protected void setCurrentShift(int xshift, int yshift){
		this.currentXshift = xshift%width;
		this.currentYshift = yshift%height;
	}

	public double getDCreal(){
		return getValueR(currentXshift, currentYshift);
	}

	public int getCurrentXshift() {
		return currentXshift;
	}

	public int getCurrentYshift() {
		return currentYshift;
	}

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
	public static double atan2(double x, double y){
		return (TWOPI+Math.atan2(y,x))%TWOPI;
	}
}
