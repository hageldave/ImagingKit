package hageldave.imagingkit.fourier;

import java.util.function.Supplier;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.fftw3;
import org.bytedeco.javacpp.fftw3.fftw_iodim;
import org.bytedeco.javacpp.fftw3.fftw_iodim_do_not_use_me;
import org.bytedeco.javacpp.fftw3.fftw_plan;

import hageldave.imagingkit.core.Img;
import hageldave.imagingkit.core.scientific.ColorImg;
import hageldave.imagingkit.core.util.ImageFrame;

public class Testing {

	public static void main(String[] args) {


//		ColorImg img = new ColorImg(5, 10, false);
//		img.forEach(px->px.setR_fromDouble(px.getIndex()));
//
//		for(int x = 0; x<img.getWidth()*2; x++){
//			for(int y =0; y < img.getHeight()*2; y++){
//				double[] array = img.copy().getDataR();
//				shift2D_(array, img.getWidth(), img.getHeight(), x, y);
//				for(int i = 0; i < img.numValues(); i++){
//					if(img.getDataR()[i] != array[shiftedIndex(i, img.getWidth(), img.getHeight(), x, y)]){
//						System.out.println("bad index "+ i+ " at shift " + x + " "+ y);
//					}
//				}
//			}
//		}


		System.out.println(Loader.load(fftw3.class));

		ColorImg img = new ColorImg(256,256, false);
		ColorImg dft = new ColorImg(img.getDimension(), false);

//		img.forEach(px->px.setR_fromDouble(Math.sin(px.getXnormalized()*px.getYnormalized()*Math.PI*2)*Math.tan(px.getXnormalized())));
		final int radius = 5;
		img.forEach(img.getWidth()/2-radius,img.getHeight()/2-radius, radius*2+1,radius*2+1, px->{
			double x = px.getX()-img.getWidth()/2; x/=radius;
			double y = px.getY()-img.getHeight()/2; y/=radius;
			px.setR_fromDouble(1-(x*x+y*y)*.5);
		});
//		shift2D(img.getDataR(), img.getWidth(), img.getHeight(), 10, 12);
//		img.forEach(img.getWidth()/2-4,img.getHeight()/2-4, 9,9, px->{
//			double x = px.getX()-img.getWidth()/2; x/=4;
//			double y = px.getY()-img.getHeight()/2; y/=4;
//			px.setR_fromDouble(1-(x*x+y*y)*.5);
//		});
//		shift2D(img.getDataR(), img.getWidth(), img.getHeight(), -40, -20);
//		img.forEach(img.getWidth()/2-4,img.getHeight()/2-4, 9,9, px->{
//			double x = px.getX()-img.getWidth()/2; x/=4;
//			double y = px.getY()-img.getHeight()/2; y/=4;
//			px.setR_fromDouble(1-(x*x+y*y)*.5);
//		});
		ImageFrame.display(img.getRemoteBufferedImage()).setTitle("source image");;

		execSplit2D(img.getWidth(),img.getHeight(), img.getDataR(), dft.getDataR(), dft.getDataG());
		shift2D(dft.getDataR(), dft.getWidth(), dft.getHeight(), dft.getWidth()/2, dft.getHeight()/2);
		shift2D(dft.getDataG(), dft.getWidth(), dft.getHeight(), dft.getWidth()/2, dft.getHeight()/2);
		dft.forEach(px->{
			double v = Math.sqrt(px.r_asDouble()*px.r_asDouble()+px.g_asDouble()+px.g_asDouble());
			px.setB_fromDouble(v);
		});
		ColorImg dftReal = dft.getChannelImage(ColorImg.channel_r);
		ColorImg dftImag = dft.getChannelImage(ColorImg.channel_g);
		ColorImg dftMagn = dft.getChannelImage(ColorImg.channel_b);

		dftMagn.forEach(px->{
			double v = Math.log(px.b_asDouble());
			px.setRGB_fromDouble(v, v, v);
		});
		double[] mm = {0,0};
		mm[0] = dftMagn.getMinValue(ColorImg.channel_r);
		mm[1] = dftMagn.getMaxValue(ColorImg.channel_r);
		dftMagn.forEach(px->px.convertRange(mm[0],mm[1], 0,1));

		mm[0] = dftReal.getMinValue(ColorImg.channel_r);
		mm[1] = dftReal.getMaxValue(ColorImg.channel_r);
		dftReal.forEach(px->px.convertRange(mm[0],mm[1], 0,1));

		mm[0] = dftImag.getMinValue(ColorImg.channel_r);
		mm[1] = dftImag.getMaxValue(ColorImg.channel_r);
		dftImag.forEach(px->px.convertRange(mm[0],mm[1], 0,1));

		ImageFrame.display(dftMagn.getRemoteBufferedImage()).setTitle("ft-magnitude");
		ImageFrame.display(dftReal.getRemoteBufferedImage()).setTitle("ft-real");
		ImageFrame.display(dftImag.getRemoteBufferedImage()).setTitle("ft-imag");
	}


	static void shift2D(double[] a, int w, int h, int xShift, int yShift){
		assertPositive(w, ()->"specified width is not positive. w="+w);
		assertPositive(h, ()->"specified height is not positive. h="+h);
		while(xShift<0) xShift = w+xShift;
		while(yShift<0) yShift = h+yShift;
		xShift %= w;
		yShift %= h;
		if(yShift != 0){
			// shift in y direction
			rotateArray(a, w*h, yShift*w);
		}
		if(xShift != 0){
			double[] row = new double[w];
			for(int i = 0; i < h; i++){
				System.arraycopy(a, i*w, row, 0, w);
				rotateArray(row, w, xShift);
				System.arraycopy(row, 0, a, i*w, w);
			}
		}
	}

	static void shift2D_(double[] a, int w, int h, int x, int y){
		assertPositive(w, ()->"specified width is not positive. w="+w);
		assertPositive(h, ()->"specified height is not positive. h="+h);
		while(x < 0) x = w+x;
		while(y < 0) y = h+y;
		x %=w;
		y %=h;
		if(x==0 && y==0){
			return;
		}
		final int size = w;
		for(int row = 0; row < h; row++){
			final int offset = row*w;
			for (int cycleStart = 0, nMoved = 0; nMoved != size; cycleStart++) {
				double displaced = a[cycleStart+offset];
				int i = cycleStart;
				do {
					i += x;
					if (i >= size)
						i -= size;
					double t=a[i+offset];
					a[i+offset]=displaced;
					displaced=t;
					nMoved ++;
				} while (i != cycleStart);
			}
		}
		rotateArray(a,w*h, y*w);
	}


	static void rotateArray(double[] a, final int size, int distance){
		// this is a copy of Collections.rotate1(...) for double[]
		if (size == 0)
			return;
		distance = distance % size;
		if (distance < 0)
			distance += size;
		if (distance == 0)
			return;

		for (int cycleStart = 0, nMoved = 0; nMoved != size; cycleStart++) {
			double displaced = a[cycleStart];
			int i = cycleStart;
			do {
				i += distance;
				if (i >= size)
					i -= size;
				double t=a[i];
				a[i]=displaced;
				displaced=t;
				nMoved ++;
			} while (i != cycleStart);
		}
	}

	static void execSplit2D(int width, int height, double[] inR, double[] outR, double[] outI){
		assertArraySize(width*height, inR,
				()->"Specified input (real) array does not macth specified size. Array length:"
						+inR.length + " Size:" + width*height + "=("+width+"*"+height+")");
		assertArraySize(width*height, outR,
				()->"Specified output (real) array does not macth specified size. Array length:"
						+outR.length + " Size:" + width*height + "=("+width+"*"+height+")");
		assertArraySize(width*height, outI,
				()->"Specified output (imaginary) array does not macth specified size. Array length:"
						+outI.length + " Size:" + width*height + "=("+width+"*"+height+")");
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
	}

	static int shiftedIndex(int i, int w, int h, int xs, int ys){
		int x = ((i%w)+xs)%w;
		int y = ((i/w)+ys)%h;
		return y*w+x;
	}

	static void assertPositive(int x, Supplier<String> errmsg){
		if(x < 1){
			throw new IllegalArgumentException(errmsg.get());
		}
	}

	static void assertArraySize(int size, double[] array, Supplier<String> errmsg){
		if(array.length != size){
			throw new IllegalArgumentException(errmsg.get());
		}
	}



}
