package hageldave.imagingkit.core;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Consumer;

public class PerformanceNonlinear {
	public static void main(String[] args) {

		long time;
		int[][] problemsizes = new int[][]{{128,128},{1280,720},{1920,1080},{5568,3712}};
		int numLoops = 8;

		int contrastLuminance = 128;
		double contrastLum = contrastLuminance/255.0;
		float contrastIntensity = 0.21f;

		Consumer<PixelBase> action = px -> {
			double r = px.r_asDouble();
			double g = px.g_asDouble();
			double b = px.b_asDouble();
			double luminance = r*0.2126 + g*0.7152 + b*0.0722;
			double lumDif = luminance-contrastLum;
			r += lumDif*contrastIntensity;
			g += lumDif*contrastIntensity;
			b += lumDif*contrastIntensity;
			px.setRGB_fromDouble_preserveAlpha(r, g, b);
		};

		for(int[] probsize:problemsizes){

			long[] times = new long[7];
			String[] methods = new String[times.length];

			Img img = new Img(probsize[0],probsize[1]);
			{ // init img;
				Random rand = new Random();
				for(int i = 0; i < img.getData().length; i++){
					img.getData()[i] = Pixel.argb(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
				}
			}
			Img imgBackup = img.copy();

			img.setSpliteratorMinimumSplitSize(Math.max(img.numValues()/512, 1024));
			System.out.println("spliterator min size :" + img.getSpliteratorMinimumSplitSize());

			for(int i = 0; i < numLoops; i++){

				methods[0] = "serial for";
//				System.out.println(methods[0]);
				time = System.currentTimeMillis();
				{
					for(int k = 0; k < img.numValues(); k++){
						int color = img.getData()[k];
						double r = Pixel.r_normalized(color);
						double g = Pixel.g_normalized(color);
						double b = Pixel.b_normalized(color);
						double luminance = r*0.2126 + g*0.7152 + b*0.0722;
						double lumDif = luminance-contrastLum;
						r += lumDif*contrastIntensity;
						g += lumDif*contrastIntensity;
						b += lumDif*contrastIntensity;
						img.getData()[k] = Pixel.argb_fromNormalized(Pixel.a_normalized(color), r, g, b);
					}
				}
				times[0] += System.currentTimeMillis()-time;
				imgBackup.copyArea(0, 0, img.getWidth(), img.getHeight(), img, 0, 0);


				methods[1] = "serial forEach";
//				System.out.println(methods[1]);
				time = System.currentTimeMillis();
				{
					img.forEach(action);
				}
				times[1] += System.currentTimeMillis()-time;
				imgBackup.copyArea(0, 0, img.getWidth(), img.getHeight(), img, 0, 0);


				methods[2] = "serial forEach default";
//				System.out.println(methods[2]);
				time = System.currentTimeMillis();
				{
					img.forEach_defaultimpl(action);
				}
				times[2] += System.currentTimeMillis()-time;
				imgBackup.copyArea(0, 0, img.getWidth(), img.getHeight(), img, 0, 0);


				methods[3] = "serial Stream";
//				System.out.println(methods[3]);
				time = System.currentTimeMillis();
				{
					img.stream().forEach(action);
				}
				times[3] += System.currentTimeMillis()-time;
				imgBackup.copyArea(0, 0, img.getWidth(), img.getHeight(), img, 0, 0);


				methods[4] = "parallel forEach";
//				System.out.println(methods[4]);
				time = System.currentTimeMillis();
				{
					img.forEach(true,action);
				}
				times[4] += System.currentTimeMillis()-time;
				imgBackup.copyArea(0, 0, img.getWidth(), img.getHeight(), img, 0, 0);


				methods[5] = "parallel Stream";
//				System.out.println(methods[5]);
				time = System.currentTimeMillis();
				{
					img.stream(true).forEach(action);
				}
				times[5] += System.currentTimeMillis()-time;
				imgBackup.copyArea(0, 0, img.getWidth(), img.getHeight(), img, 0, 0);


				methods[6] = "parallel converted";
//				System.out.println(methods[6]);
				time = System.currentTimeMillis();
				{
					img.forEach(PixelConvertingSpliterator.getDoubleArrayConverter(), true, arr-> {
						double luminance = arr[0]*0.2126 + arr[1]*0.7152 + arr[2]*0.0722;
						double lumDif = luminance-contrastLum;
						arr[0] += lumDif*contrastIntensity;
						arr[1] += lumDif*contrastIntensity;
						arr[2] += lumDif*contrastIntensity;
					});
				}
				times[6] += System.currentTimeMillis()-time;
				imgBackup.copyArea(0, 0, img.getWidth(), img.getHeight(), img, 0, 0);


			}

			System.out.println("-----Results--"+Arrays.toString(probsize)+"-----");
			// results
			for(int i = 0; i < times.length; i++){
				System.out.format("%s:%s%n", methods[i], times[i]/numLoops);
			}
		}

	}


}
