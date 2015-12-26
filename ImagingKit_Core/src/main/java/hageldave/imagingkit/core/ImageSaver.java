package hageldave.imagingkit.core;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageSaver {
	
	public static String[] getSaveableImageFormats(){
		return ImageIO.getWriterFileSuffixes();
	}

	public static boolean saveImage(Image image, String filename, String imgformat){
		File f = new File(filename);
		if(!f.isDirectory()){
			return saveImage(image, f, imgformat);
		}
		return false;
	}
	
	public static boolean isFormatRGBOnly(String imgFormat){
		switch (imgFormat.toLowerCase()) {
		/* fall through */
		case "jpg":
		case "jpeg":
		case "bmp":
			return true;
		default:
			return false;
		}
	}
	
	public static boolean saveImage(Image image, File file, String imgformat){
		BufferedImage bImg = null;
		if(isFormatRGBOnly(imgformat)){
			image = BufferedImageFactory.get(image, BufferedImage.TYPE_INT_RGB);
		} else if(imgformat.toLowerCase().equals("wbmp")){
			image = BufferedImageFactory.get(image, BufferedImage.TYPE_BYTE_BINARY);
		}
		if(image instanceof BufferedImage){
			bImg = (BufferedImage) image;
		} else {
			bImg = BufferedImageFactory.getINT_ARGB(image);
		}
		try {
			return ImageIO.write(bImg, imgformat, file);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean saveImage(Image image, File file){
		// get file ending
		int dotIndex = file.getName().lastIndexOf('.');
		if(dotIndex >= 0){
			String ending = file.getName().substring(dotIndex+1, file.getName().length());
			return saveImage(image, file, ending);
		}
		return false;
	}
	
	public static boolean saveImage(Image image, String filename){
		File f = new File(filename);
		if(!f.isDirectory()){
			return saveImage(image, f);
		}
		return false;
	}
}
