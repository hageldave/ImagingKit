package hageldave.imagingkit.core;

import java.util.Random;
import java.util.function.Consumer;

public class Performance {

	public static void main(String[] args) {
		
		long time;
		long[] times = new long[6];
		String[] methods = new String[times.length];
		int numLoops = 15;
		
		int contrastLuminance = 128;
		float contrastIntensity = 0.21f;
		
		Consumer<Pixel> action = px -> {
			int lumDif = px.getLuminance()-contrastLuminance;
			int r = (int) (px.r()+lumDif*contrastIntensity);
			int g = (int) (px.g()+lumDif*contrastIntensity);
			int b = (int) (px.b()+lumDif*contrastIntensity);
			px.setValue(Pixel.argb_bounded(px.a(), r, g, b));
		};
		
		Img img = new Img(6000, 4000);
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
			System.out.println(methods[0]);
			time = System.currentTimeMillis();
			{
				for(int k = 0; k < img.numValues(); k++){
					int lumDif = Pixel.getLuminance(img.getData()[k])-contrastLuminance;
					int r = (int) (Pixel.r(img.getData()[k])+lumDif*contrastIntensity);
					int g = (int) (Pixel.g(img.getData()[k])+lumDif*contrastIntensity);
					int b = (int) (Pixel.b(img.getData()[k])+lumDif*contrastIntensity);
					img.getData()[k] = Pixel.argb(Pixel.a(img.getData()[k]), r, g, b);
				}
			}
			times[0] += System.currentTimeMillis()-time;
			imgBackup.copyArea(0, 0, img.getWidth(), img.getHeight(), img, 0, 0);
			
			
			methods[1] = "serial forEach";
			System.out.println(methods[1]);
			time = System.currentTimeMillis();
			{
				img.forEach(action);
			}
			times[1] += System.currentTimeMillis()-time;
			imgBackup.copyArea(0, 0, img.getWidth(), img.getHeight(), img, 0, 0);
			
			
			methods[2] = "serial forEach default";
			System.out.println(methods[2]);
			time = System.currentTimeMillis();
			{
				img.forEach_defaultimpl(action);
			}
			times[2] += System.currentTimeMillis()-time;
			imgBackup.copyArea(0, 0, img.getWidth(), img.getHeight(), img, 0, 0);
			
			
			methods[3] = "serial Stream";
			System.out.println(methods[3]);
			time = System.currentTimeMillis();
			{
				img.stream().forEach(action);
			}
			times[3] += System.currentTimeMillis()-time;
			imgBackup.copyArea(0, 0, img.getWidth(), img.getHeight(), img, 0, 0);
			
			
			methods[4] = "parallel forEach";
			System.out.println(methods[4]);
			time = System.currentTimeMillis();
			{
				img.forEach(true,action);
			}
			times[4] += System.currentTimeMillis()-time;
			imgBackup.copyArea(0, 0, img.getWidth(), img.getHeight(), img, 0, 0);
			
			
			methods[5] = "parallel Stream";
			System.out.println(methods[5]);
			time = System.currentTimeMillis();
			{
				img.stream(true).forEach(action);
			}
			times[5] += System.currentTimeMillis()-time;
			imgBackup.copyArea(0, 0, img.getWidth(), img.getHeight(), img, 0, 0);
		
		
		}
		
		System.out.println("-----Results-----");
		// results
		for(int i = 0; i < times.length; i++){
			System.out.format("%s:%s%n", methods[i], times[i]/numLoops);
		}
		
	}
	
	
	static abstract class IndexedRunnable implements Runnable {
		int startIndex;
		int endIndex;
		public IndexedRunnable(int startIndex, int endIndex) {
			this.startIndex = startIndex;
			this.endIndex = endIndex;
		}
	}
	
	
}
