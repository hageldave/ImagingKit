package hageldave.imagingkit.core.examples;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.net.URL;

import hageldave.imagingkit.core.Img;
import hageldave.imagingkit.core.Pixel;
import hageldave.imagingkit.core.io.ImageLoader;
import hageldave.imagingkit.core.io.ImageSaver;
import hageldave.imagingkit.core.operations.ColorSpaceTransformation;
import hageldave.imagingkit.core.util.ImageFrame;

public class CodeSnippets {

	public static void main(String[] args) {
		
	}
	
	static URL resource(String res){
		return CodeSnippets.class.getResource(res);
	}
	
	static void ex1(){
		Img img = ImageLoader.loadImgFromURL(resource("/lena.512.png").toString());
		///////////////
//		Img img = ImageLoader.loadImgFromURL("file:///home/pictures/rainbow.jpg");
		for(Pixel px: img){
		    int grey = (px.r() + px.g() + px.b()) / 3;
		    px.setRGB(grey, grey, grey);
		}
		///////////////
		ImageFrame.display(img);
	}
	
	static void ex2(){
		Img img = ImageLoader.loadImgFromURL(resource("/lena.512.png").toString());
		////////////
		img.stream().parallel().forEach( px -> {
		    int grey = px.getLuminance();
		    px.setRGB(grey, grey, grey);
		});
		///////////////
		ImageFrame.display(img);
	}
	
	static void ex3(){
		BufferedImage buffimg = ImageLoader.loadImage(resource("/sailboat.512.png").getPath(), BufferedImage.TYPE_INT_ARGB);
		//////////
//		BufferedImage buffimg = ImageLoader.loadImage("myimage_colored.png", BufferedImage.TYPE_INT_ARGB);
		Img img = Img.createRemoteImg(buffimg);
		img.forEach(true, (pixel) -> {
			int gray = (pixel.r() + pixel.g() + pixel.b())/3;
			pixel.setARGB(pixel.a(), gray, gray, gray);
		});
		ImageSaver.saveImage(buffimg,"myimage_grayscale.png");
	}
	
	static void ex4(){
		Img img = new Img(400, 300);
		img.fill(0xff000000);
		img.paint(g2d -> {
			g2d.setColor(Color.white);
			String helloWorld = "Hello World";
			int textWidth = g2d.getFontMetrics().stringWidth(helloWorld);
			g2d.drawString(helloWorld, img.getWidth()/2-textWidth/2, img.getHeight()/2);
			g2d.drawLine(0, img.getHeight()/2+4, img.getWidth(), img.getHeight()/2+4);
		});
		ImageFrame.display(img);
		ImageSaver.saveImage(img.getRemoteBufferedImage(), "hello_world.png");
	}
	
	static void ex5(){
		Img img = new Img(1024, 1024);
		img.forEach(px -> {
			double x = px.getXnormalized()*2-1;
			double y = px.getYnormalized()*2-1;
			double len = Math.max(Math.abs(x),Math.abs(y));
			double angle = (Math.atan2(x,y)+Math.PI)*(180/Math.PI);
			
			double r = Math.max(0,1-Math.abs((angle-120)/120.0));
			double g = Math.max(0, 1-Math.abs((angle-240)/120.0));
			double b = Math.max(0, angle <= 120 ? 
					1-Math.abs((angle)/120.0):1-Math.abs((angle-360)/120.0));
			
			px.setRGB_fromDouble(r*(1-len), g*(1-len), b*(1-len));
		});
		ImageFrame.display(img);
	}
	
	static void ex6(){
		Img img = ImageLoader.loadImgFromURL("http://sipi.usc.edu/database/preview/misc/4.2.03.png");

		img.forEach(ColorSpaceTransformation.RGB_2_HSV);
		double hueShift = (360-90)/360.0;
		img.forEach(pixel -> {
			// R channel corresponds to hue (modulo 1.0 for cyclic behaviour)
			pixel.setR_fromDouble((pixel.r_asDouble()+hueShift) % 1.0);
		});
		img.forEach(ColorSpaceTransformation.HSV_2_RGB);

		ImageFrame.display(img);
	}
	
	
}
