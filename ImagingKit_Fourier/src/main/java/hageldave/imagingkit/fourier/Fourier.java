package hageldave.imagingkit.fourier;

import java.util.Arrays;
import java.util.function.Supplier;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.fftw3;
import org.bytedeco.javacpp.fftw3.fftw_iodim;
import org.bytedeco.javacpp.fftw3.fftw_iodim_do_not_use_me;
import org.bytedeco.javacpp.fftw3.fftw_plan;

import hageldave.imagingkit.core.Img;
import hageldave.imagingkit.core.PixelBase;
import hageldave.imagingkit.core.io.ImageLoader;
import hageldave.imagingkit.core.scientific.ColorImg;
import hageldave.imagingkit.core.util.ImageFrame;

import static hageldave.imagingkit.fourier.ArrayUtils.*;

public class Fourier {

	private static boolean setupDone = false;

	private static void setup(){
		if(!setupDone){
			synchronized (Fourier.class) {
				if(!setupDone){
					String loadedlib = Loader.load(fftw3.class);
					Fourier.setupDone = true;
					System.out.format("Loaded FFTW library [%s]%n",loadedlib);
				}
			}
		}
	}

	public static void execSplit2D_r2c(int width, int height, double[] inR, double[] outR, double[] outI)
	throws IllegalArgumentException
	{
		setup();
		/* parameter sanity checks */
		assertPositive(width, ()->"Specified width is not positive: " + width);
		assertPositive(height, ()->"Specified height is not positive: " + height);
		assertArraySize(width*height, inR,
				()->"Specified input (real) array does not macth specified size. Array length:"
						+inR.length + " Size:" + width*height + "=("+width+"*"+height+")");
		assertArraySize(width*height, outR,
				()->"Specified output (real) array does not macth specified size. Array length:"
						+outR.length + " Size:" + width*height + "=("+width+"*"+height+")");
		assertArraySize(width*height, outI,
				()->"Specified output (imaginary) array does not macth specified size. Array length:"
						+outI.length + " Size:" + width*height + "=("+width+"*"+height+")");

		/* executing fftw */
		try(
				fftw_iodim_do_not_use_me array = new fftw_iodim_do_not_use_me(3);
				fftw_iodim dims = new fftw_iodim(array);
				fftw_iodim_do_not_use_me d0 = new fftw_iodim_do_not_use_me();
				fftw_iodim_do_not_use_me d1 = new fftw_iodim_do_not_use_me();
				fftw_iodim_do_not_use_me d2 = new fftw_iodim_do_not_use_me();
				DoublePointer iR = new DoublePointer(width*height);
				DoublePointer oR = new DoublePointer(width*height);
				DoublePointer oI = new DoublePointer(width*height);
		){
			d0.n(width).is(1).os(1);
			d1.n(height).is(width).os(width);
			d2.n(1).is(width*height).os(width*height);
			array.position(0).put(d0).position(1).put(d1).position(2).put(d2);

			fftw_plan plan = fftw3.fftw_plan_guru_split_dft_r2c(
					3, dims,
					0, null,
					iR, oR, oI,
					(int)fftw3.FFTW_ESTIMATE);
			iR.put(inR);
			fftw3.fftw_execute_split_dft_r2c(plan, iR, oR, oI);
			oR.get(outR);
			oI.get(outI);
			fftw3.fftw_destroy_plan(plan);
		}
		/* rescale result */
		double scaling = 1.0/(width*height);
		for(int i = 0; i < width*height; i++){
			outR[i]*=scaling;
			outI[i]*=scaling;
		}
	}

	public static void execSplit2D_c2c(int width, int height, double[] inR, double[] inI, double[] outR, double[] outI)
	throws IllegalArgumentException
	{
		setup();
		/* parameter sanity checks */
		assertPositive(width, ()->"Specified width is not positive: " + width);
		assertPositive(height, ()->"Specified height is not positive: " + height);
		assertArraySize(width*height, inR,
				()->"Specified input (real) array does not macth specified size. Array length:"
						+inR.length + " Size:" + width*height + "=("+width+"*"+height+")");
		assertArraySize(width*height, inI,
				()->"Specified input (imaginary) array does not macth specified size. Array length:"
						+inR.length + " Size:" + width*height + "=("+width+"*"+height+")");
		assertArraySize(width*height, outR,
				()->"Specified output (real) array does not macth specified size. Array length:"
						+outR.length + " Size:" + width*height + "=("+width+"*"+height+")");
		assertArraySize(width*height, outI,
				()->"Specified output (imaginary) array does not macth specified size. Array length:"
						+outI.length + " Size:" + width*height + "=("+width+"*"+height+")");

		/* executing fftw */
		try(
				fftw_iodim_do_not_use_me array = new fftw_iodim_do_not_use_me(3);
				fftw_iodim dims = new fftw_iodim(array);
				fftw_iodim_do_not_use_me d0 = new fftw_iodim_do_not_use_me();
				fftw_iodim_do_not_use_me d1 = new fftw_iodim_do_not_use_me();
				fftw_iodim_do_not_use_me d2 = new fftw_iodim_do_not_use_me();
				DoublePointer iR = new DoublePointer(width*height);
				DoublePointer iI = new DoublePointer(width*height);
				DoublePointer oR = new DoublePointer(width*height);
				DoublePointer oI = new DoublePointer(width*height);
		){
			d0.n(width).is(1).os(1);
			d1.n(height).is(width).os(width);
			d2.n(1).is(width*height).os(width*height);
			array.position(0).put(d0).position(1).put(d1).position(2).put(d2);

			fftw_plan plan = fftw3.fftw_plan_guru_split_dft(
					3, dims,
					0, null,
					iR, iI, oR, oI,
					(int)fftw3.FFTW_ESTIMATE);
			iR.put(inR);
			iI.put(inI);
			fftw3.fftw_execute_split_dft(plan, iR, iI, oR, oI);
			oR.get(outR);
			oI.get(outI);
			fftw3.fftw_destroy_plan(plan);
		}
		/* rescale result */
		double scaling = 1.0/(width*height);
		for(int i = 0; i < width*height; i++){
			outR[i]*=scaling;
			outI[i]*=scaling;
		}
	}

	public static double[][] fft2D_real2complex(double[] realIn, int width, int height){
		/* parameter sanity checks */
		assertPositive(width, ()->"Specified width is not positive: " + width);
		assertPositive(height, ()->"Specified height is not positive: " + height);
		/* execute fftw */
		double[] realOut = new double[width*height];
		double[] imagOut = new double[width*height];
		execSplit2D_r2c(width, height, realIn, realOut, imagOut);
		return new double[][]{realOut,imagOut};
	}

	public static ComplexImg fft2D_real2complexImg(double[] realIn, int width, int height){
		double[][] fft = fft2D_real2complex(realIn, width, height);
		return new ComplexImg(width, height, fft[0], fft[1], null);
	}

	public static ComplexImg fft2D_real2complexImg(double[] realIn, ComplexImg fftImg){
		int w=fftImg.getWidth(), h=fftImg.getHeight();
		execSplit2D_r2c(w, h, realIn, fftImg.getDataReal(), fftImg.getDataImag());
		fftImg.setCurrentShift(0, 0);
		if(fftImg.isSynchronizePowerSpectrum()){
			fftImg.recomputePowerChannel();
		}
		return fftImg;
	}

	public static ComplexImg fft2D_complexImg2complexImg(ComplexImg input, ComplexImg output){
		/* sanity checks */
		if(!input.getDimension().equals(output.getDimension())){
			throw new IllegalArgumentException(
					"input output dimension mismatch! input:"+input.getDimension()+" output:"+output.getDimension());
		}
		/* execute fft */
		execSplit2D_c2c(
				input.getWidth(), input.getHeight(),
				input.getDataReal(), input.getDataImag(),
				output.getDataReal(), output.getDataImag());
		/* reset shift information */
		output.setCurrentShift(0, 0);
		/* calc power spectrum if desired */
		if(output.isSynchronizePowerSpectrum()){
			output.recomputePowerChannel();
		}

		return output;
	}

	public static ComplexImg ifft2D_complexImg2complexImg(ComplexImg input, ComplexImg output){
		/* sanity checks */
		if(!input.getDimension().equals(output.getDimension())){
			throw new IllegalArgumentException(
					"input output dimension mismatch! input:"+input.getDimension()+" output:"+output.getDimension());
		}
		/* execute fft */
		execSplit2D_c2c(
				input.getWidth(), input.getHeight(),
				/* swapped real with imaginary for inverse */
				input.getDataImag(), input.getDataReal(),
				output.getDataImag(), output.getDataReal());
		/* reset shift information */
		output.setCurrentShift(0, 0);
		/* calc power spectrum if desired */
		if(output.isSynchronizePowerSpectrum()){
			output.recomputePowerChannel();
		}

		return output;
	}

	public static void main(String[] args) {
		Img gauss = ImageLoader.loadImgFromURL("https://theiszm.files.wordpress.com/2010/06/gaussian.jpg");
		ColorImg img = new ColorImg(gauss, false);
		ImageFrame.display(img.getRemoteBufferedImage());
		ComplexImg fft = fft2D_real2complexImg(img.getDataR(), img.getWidth(),img.getHeight());
		ifft2D_complexImg2complexImg(fft, fft);
		ImageFrame.display(fft.getDelegate().getChannelImage(0).scaleChannelToUnitRange(0).toBufferedImage());
	}

}
