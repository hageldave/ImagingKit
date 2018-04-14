package hageldave.imagingkit.fourier;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Objects;

import hageldave.imagingkit.core.ImgBase;
import hageldave.imagingkit.core.scientific.ColorImg;

public class ComplexImg implements ImgBase<ComplexPixel> {

	public static final int channel_real = ColorImg.channel_r;
	public static final int channel_imag = ColorImg.channel_g;
	public static final int channel_power = ColorImg.channel_b;


	private final int width;
	private final int height;

	private final double[] real;
	private final double[] imag;
	/* power spectrum: real*real+imag*imag */
	private final double[] power;

	private final ColorImg delegate;

	private int currentXshift = 0;
	private int currentYshift = 0;

	private boolean synchronizePowerSpectrum = false;

	public ComplexImg(Dimension dims){
		this(dims.width, dims.height);
	}
	
	public ComplexImg(int width, int height) {
		this(width, height, new double[width*height],new double[width*height],new double[width*height]);
	}

	public ComplexImg(int width, int height, double[] real, double[] imag, double[] power){
		// sanity check 1:
		Objects.requireNonNull(real);
		if(width*height != real.length){
			throw new IllegalArgumentException(String.format("Provided Dimension (width=%d, height=$d) does not match number of provided Pixels %d", width, height, real.length));
		}

		this.width = width;
		this.height = height;
		this.real = real;
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

	public double getValue(int channel, int x, int y) {
		return delegate.getValue(channel, x, y);
	}

	public double getValueR(int x, int y) {
		return delegate.getValueR(x, y);
	}

	public double getValueI(int x, int y) {
		return delegate.getValueG(x, y);
	}

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
}
