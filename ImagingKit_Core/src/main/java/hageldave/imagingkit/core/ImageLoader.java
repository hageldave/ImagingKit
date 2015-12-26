package hageldave.imagingkit.core;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageLoader {
	
	public static String[] getLoadableImageFormats(){
		return ImageIO.getReaderFileSuffixes();
	}
	
	public static BufferedImage loadImage(String fileName){
		File f = new File(fileName);
		if(f.exists()){
			return loadImage(f);
		}
		return null;
	}
	
	public static BufferedImage loadImage(File file){
		try {
			return ImageIO.read(file);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static BufferedImage loadImage(File file, int imageType){
		BufferedImage img = loadImage(file);
		if(img != null && img.getType() != imageType){
			return BufferedImageFactory.get(img, imageType);
		} else {
			return img;
		}
	}
	
	public static BufferedImage loadImage(String fileName, int imageType){
		BufferedImage img = loadImage(fileName);
		if(img != null && img.getType() != imageType){
			return BufferedImageFactory.get(img, imageType);
		} else {
			return img;
		}
	}

}
