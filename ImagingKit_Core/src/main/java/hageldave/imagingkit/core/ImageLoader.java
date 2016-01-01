package hageldave.imagingkit.core;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Class providing convenience methods for loading Images with {@link ImageIO}.
 * @author hageldave
 */
public class ImageLoader {
	
	/**
	 * @return {@link ImageIO#getReaderFileSuffixes()}
	 */
	public static String[] getLoadableImageFileFormats(){
		return ImageIO.getReaderFileSuffixes();
	}
	
	/**
	 * Tries to load Image from specified filename.
	 * @param fileName path of the image file
	 * @return loaded Image or null if the file does not exist or cannot
	 * be loaded.
	 */
	public static BufferedImage loadImage(String fileName){
		File f = new File(fileName);
		if(f.exists()){
			return loadImage(f);
		}
		return null;
	}
	
	/**
	 * Tries to load image from specified file.
	 * @param file the image file
	 * @return loaded Image or null if no image could be loaded from the file.
	 */
	public static BufferedImage loadImage(File file){
		try {
			return ImageIO.read(file);
		} catch (IOException e) {
			return null;
		}
	}
	
	/**
	 * Tries to load Image from file and converts it to the
	 * desired image type if needed. <br>
	 * See {@link BufferedImage#BufferedImage(int, int, int)} for details on
	 * the available image types.
	 * 
	 * @param file the image file
	 * @param imageType of the resulting BufferedImage
	 * @return loaded Image or null if image could not be loaded from the file
	 */
	public static BufferedImage loadImage(File file, int imageType){
		BufferedImage img = loadImage(file);
		if(img != null && img.getType() != imageType){
			return BufferedImageFactory.get(img, imageType);
		} else {
			return img;
		}
	}
	
	/**
	 * Tries to load Image from file and converts it to the
	 * desired image type if needed. <br>
	 * See {@link BufferedImage#BufferedImage(int, int, int)} for details on
	 * the available image types.
	 * 
	 * @param fileName path to the image file
	 * @param imageType of the resulting BufferedImage
	 * @return loaded Image or null if image could not be loaded from the file
	 */
	public static BufferedImage loadImage(String fileName, int imageType){
		BufferedImage img = loadImage(fileName);
		if(img != null && img.getType() != imageType){
			return BufferedImageFactory.get(img, imageType);
		} else {
			return img;
		}
	}

}
