package hageldave.imagingkit.core;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

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
	 * @return loaded Image.
	 * @throws ImageLoaderException if the file does not exist or cannot be 
	 * loaded.
	 */
	public static BufferedImage loadImage(String fileName){
		File f = new File(fileName);
		if(f.exists()){
			return loadImage(f);
		}
		throw new ImageLoaderException(new FileNotFoundException(fileName));
	}
	
	/**
	 * Tries to load image from specified file.
	 * @param file the image file
	 * @return loaded Image.
	 * @throws ImageLoaderException if no image could be loaded from the file.
	 */
	public static BufferedImage loadImage(File file){
		try {
			return ImageIO.read(file);
		} catch (IOException e) {
			throw new ImageLoaderException(e);
		}
	}
	
	/**
	 * Tries to load image from specified file.
	 * @param is {@link InputStream} of the image file
	 * @return loaded Image.
	 * @throws ImageLoaderException if no image could be loaded from the 
	 * InputStream.
	 * @since 1.1
	 */
	public static BufferedImage loadImage(InputStream is){
		try {
			return ImageIO.read(is);
		} catch (IOException e) {
			throw new ImageLoaderException(e);
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
	 * @return loaded Image.
	 * @throws ImageLoaderException if no image could be loaded from the file.
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
	 * @return loaded Image.
	 * @throws ImageLoaderException if no image could be loaded from the file.
	 */
	public static BufferedImage loadImage(String fileName, int imageType){
		BufferedImage img = loadImage(fileName);
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
	 * @param is {@link InputStream} of the image file
	 * @param imageType of the resulting BufferedImage
	 * @return loaded Image.
	 * @throws ImageLoaderException if no image could be loaded from the 
	 * InputStream.
	 * @since 1.1
	 */
	public static BufferedImage loadImage(InputStream is, int imageType){
		BufferedImage img = loadImage(is);
		if(img != null && img.getType() != imageType){
			return BufferedImageFactory.get(img, imageType);
		} else {
			return img;
		}
	}
	
	/**
	 * RuntimeException class for Exceptions that occur during image loading.
	 * @author hageldave
	 * @since 1.1
	 */
	public static class ImageLoaderException extends RuntimeException {
		private static final long serialVersionUID = -3787003755333923935L;

		public ImageLoaderException() {
		}
		
		public ImageLoaderException(String message) {
			super(message);
		}
		
		public ImageLoaderException(Throwable cause) {
			super(cause);
		}
		
		public ImageLoaderException(String message, Throwable cause) {
			super(message, cause);
		}
	}

}
