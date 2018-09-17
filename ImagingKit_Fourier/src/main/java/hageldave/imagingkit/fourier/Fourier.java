package hageldave.imagingkit.fourier;

import java.awt.Dimension;

import hageldave.ezfftw.dp.FFT;
import hageldave.ezfftw.dp.FFTW_Guru;
import hageldave.ezfftw.dp.NativeRealArray;
import hageldave.ezfftw.dp.PrecisionDependentUtils;
import hageldave.ezfftw.dp.RowMajorArrayAccessor;
import hageldave.ezfftw.dp.samplers.ComplexValuedSampler;
import hageldave.ezfftw.dp.writers.ComplexValuedWriter;
import hageldave.imagingkit.core.scientific.ColorImg;
import hageldave.imagingkit.core.scientific.ColorPixel;

/**
 * The Fourier class provides methods to execute FFTs on {@link ColorImg}es and {@link ComplexImg}es.
 * @author hageldave
 */
public class Fourier {
	
	private Fourier(){/* not constructable */}

	/**
	 * Fourier transforms the specified channel of the specified {@link ColorImg}.
	 * @param img of which one channel is to be transformed
	 * @param channel the channel which will be transformed
	 * @return the transform as ComplexImg
	 * 
	 * @throws IllegalArgumentException if the specified channel is out of range ([0..3]) or is alpha (3)
	 * but the specified image does not have an alpha channel 
	 */
	public static ComplexImg transform(ColorImg img, int channel) {
		sanityCheckForward(img, channel);
		// transform
		ComplexImg transformed = new ComplexImg(img.getDimension());
		FFT.fft(
				img.getData()[channel], // input
				transformed.getDataReal(), // real out
				transformed.getDataImag(), // imaginary out
				img.getWidth(), img.getHeight()); // dimensions
		return transformed;
	}
	
	/**
	 * Fourier transforms the specified {@link ComplexImg} (inversely if specified).
	 * The result of the transform will be stored in the specified target image (if specified, may be null).
	 * Shifts of the specified images are taken into account, so that the transform is independent of the
	 * shift of the transformed image and the resulting transform will be shifted according to the targets shift.
	 * @param inverse calculates inverse transform if true, otherwise forward transform
	 * @param toTransform ComplexImg to be transformed
	 * @param target (may be null) the target image for the transform.
	 * @return target image or new {@link ComplexImg} if target was null
	 * 
	 * @throws IllegalArgumentException if specified target does not match dimensions of transformed image
	 */
	public static ComplexImg transform(final boolean inverse, ComplexImg toTransform, ComplexImg target){
		if(target == null){
			target = new ComplexImg(toTransform.getDimension());
		} else if(!target.getDimension().equals(toTransform.getDimension())){
			throw new IllegalArgumentException(String.format(
					"specified target is of wrong dimensions. Expected %s but has %s.", 
					toTransform.getDimension(), target.getDimension()));
		}
		final int w = toTransform.getWidth();
		final int h = toTransform.getHeight();
		try(
				NativeRealArray inr = new NativeRealArray(toTransform.numValues());
				NativeRealArray ini = new NativeRealArray(inr.length);
				NativeRealArray outr = new NativeRealArray(inr.length);
				NativeRealArray outi = new NativeRealArray(inr.length);
		){
			if(toTransform.getCurrentXshift() != 0 || toTransform.getCurrentYshift() != 0){
				ComplexValuedSampler sampler = getSamplerForShiftedComplexImg(toTransform);
				PrecisionDependentUtils.fillNativeArrayFromSampler(inr, sampler.getPartSampler(false), w,h);
				PrecisionDependentUtils.fillNativeArrayFromSampler(ini, sampler.getPartSampler(true),  w,h);
			} else {
				inr.set(toTransform.getDataReal());
				ini.set(toTransform.getDataImag());
			}
			if(inverse){
				// swap real and imaginary args
				FFTW_Guru.execute_split_c2c(ini, inr, outi, outr, w,h);
			} else {
				FFTW_Guru.execute_split_c2c(inr, ini, outr, outi, w,h);
			}
			if(target.getCurrentXshift() != 0 || target.getCurrentYshift() != 0){
				ComplexValuedWriter writer = getWriterForShiftedComplexImg(target);
				PrecisionDependentUtils.readNativeArrayToWriter(outr, writer.getPartWriter(false), w,h);
				PrecisionDependentUtils.readNativeArrayToWriter(outi, writer.getPartWriter(true), w,h);
			} else {
				outr.get(0, target.getDataReal());
				outi.get(0, target.getDataImag());
			}
			if(inverse){
				// need to rescale
				double scaling = 1.0/toTransform.numValues();
				ArrayUtils.scaleArray(target.getDataReal(), scaling);
				ArrayUtils.scaleArray(target.getDataImag(), scaling);
			}
		}
		return target;
	}
	
	/**
	 * Executes the inverse Fourier transforms on the specified {@link ComplexImg} that corresponds
	 * to a specific channel of a {@link ColorImg} defined by the channel argument.
	 * The resulting transform will be stored in the specified channel of the specified target.
	 * If target is null a new ColorImg will be created and returned.
	 * <p>
	 * If the alpha channel was specified the specified target has to contain an alpha channel 
	 * ({@link ColorImg#hasAlpha()}).
	 * 
	 * @param target image where the transform is stored to
	 * @param fourier the ComplexImg that will be transformed and corresponds to the specified channel
	 * @param channel the specified ComplexImg correspond to
	 * @return the target img or a new ColorImg if target was null
	 * 
	 * @throws IllegalArgumentException <br>
	 * if images are not of the same dimensions <br>
	 * if alpha is specified as channel but specified target (if not null) is does not have an alpha channel
	 */
	public static ColorImg inverseTransform(ColorImg target, ComplexImg fourier, int channel) {
		Dimension dim = fourier.getDimension();
		// if no target was specified create a new one
		if(target == null) {
			target = new ColorImg(dim, channel==ColorImg.channel_a);
		}
		// continue sanity checks
		sanityCheckInverse_target(target, dim, channel);
		// now do the transforms
		if(fourier.getCurrentXshift() != 0 || fourier.getCurrentYshift() != 0){
			ComplexValuedSampler complexIn = getSamplerForShiftedComplexImg(fourier);
			RowMajorArrayAccessor realOut = new RowMajorArrayAccessor(target.getData()[channel], target.getWidth(), target.getHeight());
			FFT.ifft(complexIn, realOut, realOut.getDimensions());
		} else {
			FFT.ifft(	fourier.getDataReal(), 
						fourier.getDataImag(), 
						target.getData()[channel], 
						target.getWidth(), target.getHeight());
		}
		double scaling = 1.0/target.numValues();
		ArrayUtils.scaleArray(target.getData()[channel], scaling);
		return target;
	}

	/**
	 * Executes row wise Fourier transforms of the specified channel of the specified {@link ColorImg}.
	 * A 1-dimensional Fourier transform is done for each row of the image's channel.
	 * @param img of which one channel is to be transformed
	 * @param channel the channel which will be transformed
	 * @return transform as ComplexImg
	 * 
	 * @throws IllegalArgumentException if the specified channel is out of range ([0..3]) or is alpha (3)
	 * but the specified image does not have an alpha channel 
	 */
	public static ComplexImg horizontalTransform(ColorImg img, int channel) {
		// sanity checks
		sanityCheckForward(img, channel);
		// make transforms
		ComplexImg transformed = new ComplexImg(img.getDimension());
		try(
				NativeRealArray row = new NativeRealArray(img.getWidth());
				NativeRealArray fft_r = new NativeRealArray(row.length);
				NativeRealArray fft_i = new NativeRealArray(row.length);
		) {
			for(int y = 0; y < img.getHeight(); y++){
				row.set(0, img.getWidth(), y*img.getWidth(), img.getData()[channel]);
				FFTW_Guru.execute_split_r2c(row, fft_r, fft_i, img.getWidth());
				fft_r.get(0, img.getWidth(), y*img.getWidth(), transformed.getDataReal());
				fft_i.get(0, img.getWidth(), y*img.getWidth(), transformed.getDataImag());
			}
		}
		return transformed;
	}
	
	/**
	 * Executes row wise Fourier transforms of the specified {@link ComplexImg} (inversely if specified).
	 * The result of the transform will be stored in the specified target image (if specified, may be null).
	 * Shifts of the specified images are taken into account, so that the transform is independent of the
	 * shift of the transformed image and the resulting transform will be shifted according to the targets shift.
	 * @param inverse calculates inverse transforms if true, otherwise forward transform
	 * @param toTransform ComplexImg to be transformed
	 * @param target (may be null) the target image for the transform.
	 * @return target image or new {@link ComplexImg} if target was null
	 * 
	 * @throws IllegalArgumentException if specified target does not match dimensions of transformed image
	 */
	public static ComplexImg horizontalTransform(final boolean inverse, ComplexImg toTransform, ComplexImg target){
		if(target == null){
			target = new ComplexImg(toTransform.getDimension());
		} else if(!target.getDimension().equals(toTransform.getDimension())){
			throw new IllegalArgumentException(String.format(
					"specified target is of wrong dimensions. Expected %s but has %s.", 
					toTransform.getDimension(), target.getDimension()));
		}
		final int w = toTransform.getWidth();
		final int h = toTransform.getHeight();
		try(
				NativeRealArray inr = new NativeRealArray(w);
				NativeRealArray ini = new NativeRealArray(w);
				NativeRealArray outr = new NativeRealArray(w);
				NativeRealArray outi = new NativeRealArray(w);
		){
			ComplexValuedSampler complexIn = getSamplerForShiftedComplexImg(toTransform);
			ComplexValuedWriter  complexOut = getWriterForShiftedComplexImg(target);
			for(int y = 0; y < h; y++){
				for(int x = 0; x < w; x++){
					inr.set(x, complexIn.getValueAt(false, x,y));
					ini.set(x, complexIn.getValueAt(true,  x,y));
				}
				if(inverse){
					FFTW_Guru.execute_split_c2c(ini,inr, outi,outr, w);
				} else {
					FFTW_Guru.execute_split_c2c(ini,inr, outi,outr, w);
				}
				for(int x = 0; x < w; x++){
					complexOut.setValueAt(outr.get(x), false, x,y);
					complexOut.setValueAt(outi.get(x), true,  x,y);
				}
			}
		}
		if(inverse){
			// need to rescale
			double scaling = 1.0/w;
			ArrayUtils.scaleArray(target.getDataReal(), scaling);
			ArrayUtils.scaleArray(target.getDataImag(), scaling);
		}
		return target;
	}

	/**
	 * Executes row wise inverse Fourier transforms of the specified {@link ComplexImg}.
	 * A 1-dimensional Fourier transform is done for each row of the ComplexImg.
	 * The resulting transforms will be stored in the specified channel of the specified target {@link ColorImg}.
	 * If target is null, a new ColorImg is created.
	 * @param target image  of which one channel is to be transformed
	 * @param fourier the image that will be transformed
	 * @param channel the channel to which the results are stored
	 * @return the specified target or a new {@link ColorImg} if target was null
	 * 
	 * @throws IllegalArgumentException if the specified channel is out of range ([0..3]) or is alpha (3)
	 * but the specified image does not have an alpha channel 
	 */
	public static ColorImg horizontalInverseTransform(ColorImg target, ComplexImg fourier, int channel) {
		Dimension dim = fourier.getDimension();
		// if no target was specified create a new one
		if(target == null) {
			target = new ColorImg(dim, channel==ColorImg.channel_a);
		}
		// continue sanity checks
		sanityCheckInverse_target(target, dim, channel);
		// now do the transforms
		try(
				NativeRealArray row = new NativeRealArray(target.getWidth());
				NativeRealArray fft_r = new NativeRealArray(row.length);
				NativeRealArray fft_i = new NativeRealArray(row.length);
		){
			if(fourier.getCurrentXshift() != 0 || fourier.getCurrentYshift() != 0){
				ComplexValuedSampler complexIn = getSamplerForShiftedComplexImg(fourier);
				for(int y = 0; y < target.getHeight(); y++){
					for(int x = 0; x < target.getWidth(); x++){
						fft_r.set(x, complexIn.getValueAt(false, x,y));
						fft_i.set(x, complexIn.getValueAt(true,  x,y));
					}
					FFTW_Guru.execute_split_c2r(fft_r, fft_i, row, target.getWidth());
					row.get(0, target.getWidth(), y*target.getWidth(), target.getData()[channel]);
				}
			} else {
				for(int y = 0; y < target.getHeight(); y++){
					fft_r.set(0, target.getWidth(), y*target.getWidth(), fourier.getDataReal());
					fft_i.set(0, target.getWidth(), y*target.getWidth(), fourier.getDataImag());
					FFTW_Guru.execute_split_c2r(fft_r, fft_i, row, target.getWidth());
					row.get(0, target.getWidth(), y*target.getWidth(), target.getData()[channel]);
				}
			}
		}
		double scaling = 1.0/target.getWidth();
		ArrayUtils.scaleArray(target.getData()[channel], scaling);
		return target;
	}

	/**
	 * Executes column wise Fourier transforms of the specified channel of the specified {@link ColorImg}.
	 * A 1-dimensional Fourier transform is done for each column of the image's channel.
	 * @param img of which one channel is to be transformed
	 * @param channel the channel which will be transformed
	 * @return transform as ComplexImg
	 * 
	 * @throws IllegalArgumentException if the specified channel is out of range ([0..3]) or is alpha (3)
	 * but the specified image does not have an alpha channel 
	 */
	public static ComplexImg verticalTransform(ColorImg img, int channel) {
		sanityCheckForward(img, channel);
		// make transforms
		ComplexImg transformed = new ComplexImg(img.getDimension());
		try(
				NativeRealArray col = new NativeRealArray(img.getHeight());
				NativeRealArray fft_r = new NativeRealArray(col.length);
				NativeRealArray fft_i = new NativeRealArray(col.length);
		) {
			ColorPixel px = img.getPixel();
			ComplexPixel tpx = transformed.getPixel();
			for(int x = 0; x < img.getWidth(); x++){
				for(int y = 0; y < img.getHeight(); y++){
					px.setPosition(x, y);
					col.set(y, px.getValue(channel));
				}
				FFTW_Guru.execute_split_r2c(col, fft_r, fft_i, img.getHeight());
				for(int y = 0; y < img.getHeight(); y++){
					tpx.setPosition(x, y);
					tpx.setComplex(fft_r.get(y), fft_i.get(y));
				}
			}
		}
		return transformed;
	}
	
	/**
	 * Executes column wise Fourier transforms of the specified {@link ComplexImg} (inversely if specified).
	 * The result of the transform will be stored in the specified target image (if specified, may be null).
	 * Shifts of the specified images are taken into account, so that the transform is independent of the
	 * shift of the transformed image and the resulting transform will be shifted according to the targets shift.
	 * @param inverse calculates inverse transforms if true, otherwise forward transform
	 * @param toTransform ComplexImg to be transformed
	 * @param target (may be null) the target image for the transform.
	 * @return target image or new {@link ComplexImg} if target was null
	 * 
	 * @throws IllegalArgumentException if specified target does not match dimensions of transformed image
	 */
	public static ComplexImg verticalTransform(final boolean inverse, ComplexImg toTransform, ComplexImg target){
		if(target == null){
			target = new ComplexImg(toTransform.getDimension());
		} else if(!target.getDimension().equals(toTransform.getDimension())){
			throw new IllegalArgumentException(String.format(
					"specified target is of wrong dimensions. Expected %s but has %s.", 
					toTransform.getDimension(), target.getDimension()));
		}
		final int w = toTransform.getWidth();
		final int h = toTransform.getHeight();
		try(
				NativeRealArray inr = new NativeRealArray(h);
				NativeRealArray ini = new NativeRealArray(h);
				NativeRealArray outr = new NativeRealArray(h);
				NativeRealArray outi = new NativeRealArray(h);
		){
			ComplexValuedSampler complexIn = getSamplerForShiftedComplexImg(toTransform);
			ComplexValuedWriter  complexOut = getWriterForShiftedComplexImg(target);
			for(int x = 0; x < w; x++){
				for(int y = 0; y < h; y++){
					inr.set(y, complexIn.getValueAt(false, x,y));
					ini.set(y, complexIn.getValueAt(true,  x,y));
				}
				if(inverse){
					FFTW_Guru.execute_split_c2c(ini,inr, outi,outr, h);
				} else {
					FFTW_Guru.execute_split_c2c(ini,inr, outi,outr, h);
				}
				for(int y = 0; y < h; y++){
					complexOut.setValueAt(outr.get(y), false, x,y);
					complexOut.setValueAt(outi.get(y), true,  x,y);
				}
			}
		}
		if(inverse){
			// need to rescale
			double scaling = 1.0/h;
			ArrayUtils.scaleArray(target.getDataReal(), scaling);
			ArrayUtils.scaleArray(target.getDataImag(), scaling);
		}
		return target;
	}

	/**
	 * Executes column wise inverse Fourier transforms of the specified {@link ComplexImg}.
	 * A 1-dimensional Fourier transform is done for each column of the ComplexImg.
	 * The resulting transforms will be stored in the specified channel of the specified target {@link ColorImg}.
	 * If target is null, a new ColorImg is created.
	 * @param target image  of which one channel is to be transformed
	 * @param fourier the image that will be transformed
	 * @param channel the channel to which the results are stored
	 * @return the specified target or a new {@link ColorImg} if target was null
	 * 
	 * @throws IllegalArgumentException if the specified channel is out of range ([0..3]) or is alpha (3)
	 * but the specified image does not have an alpha channel 
	 */
	public static ColorImg verticalInverseTransform(ColorImg target, ComplexImg fourier, int channel) {
		Dimension dim = fourier.getDimension();
		// if no target was specified create a new one
		if(target == null) {
			target = new ColorImg(dim, channel==ColorImg.channel_a);
		}
		// continue sanity checks
		sanityCheckInverse_target(target, dim, channel);
		// now do the transforms
		try(
				NativeRealArray col = new NativeRealArray(target.getHeight());
				NativeRealArray fft_r = new NativeRealArray(col.length);
				NativeRealArray fft_i = new NativeRealArray(col.length);
		){
			ComplexValuedSampler complexIn = getSamplerForShiftedComplexImg(fourier);
			ColorPixel px = target.getPixel();
			for(int x = 0; x < target.getWidth(); x++){
				for(int y = 0; y < target.getHeight(); y++){
					fft_r.set(y, complexIn.getValueAt(false, x,y));
					fft_i.set(y, complexIn.getValueAt(true,  x,y));
				}
				FFTW_Guru.execute_split_c2r(fft_r, fft_i, col, target.getHeight());
				for(int y = 0; y < target.getHeight(); y++){
					px.setPosition(x, y);
					px.setValue(channel, col.get(y));
				}
			}
		}
		double scaling = 1.0/target.getHeight();
		ArrayUtils.scaleArray(target.getData()[channel], scaling);
		return target;
	}
	
	
	
	private static void sanityCheckForward(ColorImg img, int channel) throws IllegalArgumentException {
		if( channel < 0 || channel > 3 || (channel > 2 && !img.hasAlpha()) ){
			throw new IllegalArgumentException(String.format(
					"Channels can be 0,1,2 (also 3 if image has alpha). But channel is %d and image %s alpha",
					channel, img.hasAlpha() ? "has":"does not have"));
		}
	}
	
	private static void sanityCheckInverse_target(ColorImg target, Dimension dim, int channel) throws IllegalArgumentException {
		if(!target.getDimension().equals(dim)){
			throw new IllegalArgumentException(String.format(
					"The specified target image has wrong dimensions (%s). Fourier image has %s.", 
					target.getDimension(), dim));
		}
		if(channel==ColorImg.channel_a && !target.hasAlpha()){
			throw new IllegalArgumentException(
					"Specified channel is alpha, but the specified target image has no alpha.");
		}
	}
	
	private static ComplexValuedSampler getSamplerForShiftedComplexImg(ComplexImg img){
		int x = img.getCurrentXshift();
		int y = img.getCurrentYshift();
		int w = img.getWidth();
		int h = img.getHeight();
		
		RowMajorArrayAccessor real = new RowMajorArrayAccessor(img.getDataReal(), w,h);
		RowMajorArrayAccessor imag = new RowMajorArrayAccessor(img.getDataImag(), w,h);
		ComplexValuedSampler complex = real.combineToComplexSampler(imag);
		return (imaginary, coords) -> complex.getValueAt(imaginary, (coords[0]+w-x)%w, (coords[1]+h-y)%h);
	}
	
	private static ComplexValuedWriter getWriterForShiftedComplexImg(ComplexImg img){
		int x = img.getCurrentXshift();
		int y = img.getCurrentYshift();
		int w = img.getWidth();
		int h = img.getHeight();
		
		RowMajorArrayAccessor real = new RowMajorArrayAccessor(img.getDataReal(), w,h);
		RowMajorArrayAccessor imag = new RowMajorArrayAccessor(img.getDataImag(), w,h);
		ComplexValuedWriter complex = real.combineToComplexWriter(imag);
		return (val, imaginary, coords) -> complex.setValueAt(val, imaginary, (coords[0]+w-x)%w, (coords[1]+h-y)%h);
	}
	
}
