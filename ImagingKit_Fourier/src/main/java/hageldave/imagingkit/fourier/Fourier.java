package hageldave.imagingkit.fourier;

import java.awt.Dimension;
import java.util.Arrays;

import hageldave.ezfftw.dp.FFT;
import hageldave.ezfftw.dp.FFTW_Guru;
import hageldave.ezfftw.dp.NativeRealArray;
import hageldave.ezfftw.dp.PrecisionDependentUtils;
import hageldave.ezfftw.dp.RowMajorArrayAccessor;
import hageldave.ezfftw.dp.samplers.ComplexValuedSampler;
import hageldave.ezfftw.dp.writers.ComplexValuedWriter;
import hageldave.imagingkit.core.scientific.ColorImg;
import hageldave.imagingkit.core.scientific.ColorPixel;

public class Fourier {

	public static ComplexImg[] transform(ColorImg img, int ...channels) {
		sanityCheckForward(img, channels);
		// make transforms
		ComplexImg[] fourierPerChannel = new ComplexImg[channels.length];
		for(int i = 0; i < channels.length; i++){
			int ch = channels[i];
			ComplexImg transformed = new ComplexImg(img.getDimension());
			FFT.fft(
					img.getData()[ch], // input
					transformed.getDataReal(), // real out
					transformed.getDataImag(), // imaginary out
					img.getWidth(), img.getHeight()); // dimensions
			fourierPerChannel[i] = transformed;
		}
		return fourierPerChannel;
	}
	
	public static ComplexImg[] transformHorizontal(ColorImg img, int ...channels) {
		// sanity checks
		sanityCheckForward(img, channels);
		// make transforms
		ComplexImg[] fourierPerChannel = new ComplexImg[channels.length];
		for(int i = 0; i < channels.length; i++){
			int ch = channels[i];
			ComplexImg transformed = new ComplexImg(img.getDimension());
			try(
				NativeRealArray row = new NativeRealArray(img.getWidth());
				NativeRealArray fft_r = new NativeRealArray(row.length);
				NativeRealArray fft_i = new NativeRealArray(row.length);
			) {
				for(int y = 0; y < img.getHeight(); y++){
					row.set(0, img.getWidth(), y*img.getWidth(), img.getData()[ch]);
					FFTW_Guru.execute_split_r2c(row, fft_r, fft_i, img.getWidth());
					fft_r.get(0, img.getWidth(), y*img.getWidth(), transformed.getDataReal());
					fft_i.get(0, img.getWidth(), y*img.getWidth(), transformed.getDataImag());
				}
			}
			fourierPerChannel[i] = transformed;
		}
		return fourierPerChannel;
	}
	
	public static ComplexImg[] transformVertical(ColorImg img, int ...channels) {
		sanityCheckForward(img, channels);
		// make transforms
		ComplexImg[] fourierPerChannel = new ComplexImg[channels.length];
		for(int i = 0; i < channels.length; i++){
			int ch = channels[i];
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
						col.set(y, px.getValue(ch));
					}
					FFTW_Guru.execute_split_r2c(col, fft_r, fft_i, img.getHeight());
					for(int y = 0; y < img.getHeight(); y++){
						tpx.setPosition(x, y);
						tpx.setComplex(fft_r.get(y), fft_i.get(y));
					}
				}
			}
			fourierPerChannel[i] = transformed;
		}
		return fourierPerChannel;
	}
	
	public static ColorImg inverseTransformHorizontal(ColorImg target, ComplexImg[] fourierPerChannel, int...channels) {
		sanityCheckInverse_fftsAndChannels(fourierPerChannel, channels);
		Dimension dim = fourierPerChannel[0].getDimension();
		// if no target was specified create a new one
		if(target == null) {
			target = new ColorImg(dim, Arrays.asList(channels).contains(ColorImg.channel_a));
		}
		// continue sanity checks
		sanityCheckInverse_target(target, dim, channels);
		// now do the transforms
		for(int c = 0; c < channels.length; c++){
			int ch = channels[c];
			ComplexImg fourier = fourierPerChannel[c];
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
						row.get(0, target.getWidth(), y*target.getWidth(), target.getData()[ch]);
					}
				} else {
					for(int y = 0; y < target.getHeight(); y++){
						fft_r.set(0, target.getWidth(), y*target.getWidth(), fourier.getDataReal());
						fft_i.set(0, target.getWidth(), y*target.getWidth(), fourier.getDataImag());
						FFTW_Guru.execute_split_c2r(fft_r, fft_i, row, target.getWidth());
						row.get(0, target.getWidth(), y*target.getWidth(), target.getData()[ch]);
					}
				}
			}
			double scaling = 1.0/target.getWidth();
			ArrayUtils.scaleArray(target.getData()[ch], scaling);
		}
		
		return target;
	}
	
	public static ColorImg inverseTransformVertical(ColorImg target, ComplexImg[] fourierPerChannel, int...channels) {
		sanityCheckInverse_fftsAndChannels(fourierPerChannel, channels);
		Dimension dim = fourierPerChannel[0].getDimension();
		// if no target was specified create a new one
		if(target == null) {
			target = new ColorImg(dim, Arrays.asList(channels).contains(ColorImg.channel_a));
		}
		// continue sanity checks
		sanityCheckInverse_target(target, dim, channels);
		// now do the transforms
		for(int c = 0; c < channels.length; c++){
			int ch = channels[c];
			ComplexImg fourier = fourierPerChannel[c];
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
						px.setValue(ch, col.get(y));
					}
				}
			}
			double scaling = 1.0/target.getHeight();
			ArrayUtils.scaleArray(target.getData()[ch], scaling);
		}
		
		return target;
	}
	
	public static ColorImg inverseTransform(ColorImg target, ComplexImg[] fourierPerChannel, int...channels) {
		sanityCheckInverse_fftsAndChannels(fourierPerChannel, channels);
		Dimension dim = fourierPerChannel[0].getDimension();
		// if no target was specified create a new one
		if(target == null) {
			target = new ColorImg(dim, Arrays.asList(channels).contains(ColorImg.channel_a));
		}
		// continue sanity checks
		sanityCheckInverse_target(target, dim, channels);
		// now do the transforms
		for(int c = 0; c < channels.length; c++){
			int ch = channels[c];
			ComplexImg fourier = fourierPerChannel[c];
			if(fourier.getCurrentXshift() != 0 || fourier.getCurrentYshift() != 0){
				ComplexValuedSampler complexIn = getSamplerForShiftedComplexImg(fourier);
				RowMajorArrayAccessor realOut = new RowMajorArrayAccessor(target.getData()[ch], target.getWidth(), target.getHeight());
				FFT.ifft(complexIn, realOut, realOut.getDimensions());
			} else {
				FFT.ifft(	fourier.getDataReal(), 
							fourier.getDataImag(), 
							target.getData()[ch], 
							target.getWidth(), target.getHeight());
			}
			double scaling = 1.0/target.numValues();
			ArrayUtils.scaleArray(target.getData()[ch], scaling);
		}
		
		return target;
	}
	
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
	
	private static void sanityCheckForward(ColorImg img, int ...channels) throws IllegalArgumentException {
		if(channels.length < 1 || channels.length > 4){
			throw new IllegalArgumentException(
					"Need to at least specify 1 channel, can at most specify 4 (ARGB). Got " + channels.length);
		}
		for(int ch: channels){
			if( ch < 0 || ch > 3 || (ch > 2 && !img.hasAlpha()) ){
				throw new IllegalArgumentException(String.format(
						"Channels can be 0,1,2 (also 3 if image has alpha). But one channel is %d and image %s alpha",
						ch, img.hasAlpha() ? "has":"does not have"));
			}
		}
	}
	
	private static void sanityCheckInverse_fftsAndChannels(ComplexImg[] fourierPerChannel, int ...channels) throws IllegalArgumentException {
		if(channels.length < 1 || channels.length > 4){
			throw new IllegalArgumentException(
					"Need to at least specify 1 channel, can at most specify 4 (ARGB). Got " + channels.length);
		}
		for(int ch: channels){
			if( ch < 0 || ch > 3 ){
				throw new IllegalArgumentException(String.format("Channels can be 0,1,2,3. But one channel is %d", ch));
			}
		}
		if(fourierPerChannel.length != channels.length) {
			throw new IllegalArgumentException(String.format(
					"Number of provided fourier images does not match number of specified channels. %d images, %d channels"
					,fourierPerChannel.length, channels.length));
		}
		Dimension dim = fourierPerChannel[0].getDimension();
		for(ComplexImg f: fourierPerChannel){
			if(!f.getDimension().equals(dim)){
				throw new IllegalArgumentException("Dimensions of fourier images, are inconsistent. They have to be equal.");
			}
		}
	}
	
	private static void sanityCheckInverse_target(ColorImg target, Dimension dim, int...channels) throws IllegalArgumentException {
		if(!target.getDimension().equals(dim)){
			throw new IllegalArgumentException(String.format(
					"The specified target image has wrong dimensions (%s). Fourier images have %s.", 
					target.getDimension(), dim));
		}
		if(Arrays.asList(channels).contains(ColorImg.channel_a) && !target.hasAlpha()){
			throw new IllegalArgumentException(
					"One of the specified channels is alpha, but the specified target image has no alpha.");
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
