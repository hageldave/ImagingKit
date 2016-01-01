package hageldave.imagingkit.core;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Class providing convenience methods for saving Images to file.
 * @author hageldave
 */
public class ImageSaver {
	
	/**
	 * @return {@link ImageIO#getWriterFileSuffixes()}
	 */
	public static String[] getSaveableImageFileFormats(){
		return ImageIO.getWriterFileSuffixes();
	}
	
	/**
	 * returns if specified image format supports rgb values only. This means
	 * argb values probably need conversion beforehand.
	 * @param imgFormat
	 * @return true if format only supports rgb values
	 */
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
	
	/**
	 * Saves specified Image to file of specified img file format.
	 * <p>
	 * Some image file formats may require image type conversion before
	 * saving. This method converts to type INT_RGB for jpg, jpeg and bmp, 
	 * and to type BYTE_BINARY for wbmp.
	 * @param image to be saved
	 * @param file to save image to
	 * @param imgFileFormat image file format. Consult {@link #getSaveableImageFileFormats()}
	 * to get the supported img file formats of your system. 
	 * @return true if image was saved, false if an IOException occured during 
	 * the process or no appropriate writer could be found for specified format.
	 */
	public static boolean saveImage(Image image, File file, String imgFileFormat){
		BufferedImage bImg = null;
		if(image instanceof BufferedImage){
			bImg = (BufferedImage) image;
		} else {
			bImg = BufferedImageFactory.getINT_ARGB(image);
		}
		
		if(isFormatRGBOnly(imgFileFormat) && bImg.getType() != BufferedImage.TYPE_INT_RGB){
			image = BufferedImageFactory.get(image, BufferedImage.TYPE_INT_RGB);
		} else if(imgFileFormat.toLowerCase().equals("wbmp") && bImg.getType() != BufferedImage.TYPE_BYTE_BINARY){
			image = BufferedImageFactory.get(image, BufferedImage.TYPE_BYTE_BINARY);
		}

		try {
			return ImageIO.write(bImg, imgFileFormat, file);
		} catch (IOException e) {
			return false;
		}
	}
	
	/**
	 * Saves image using {@link #saveImage(Image, File, String)}.
	 * @param image to be saved
	 * @param filename path to file
	 * @param imgFileFormat image file format. Consult {@link #getSaveableImageFileFormats()}
	 * @return true if image was saved, false if an IOException occured during 
	 * the process, the provided filename path does refer to a directory or no 
	 * appropriate writer could be found for specified format.
	 */
	public static boolean saveImage(Image image, String filename, String imgFileFormat){
		File f = new File(filename);
		if(!f.isDirectory()){
			return saveImage(image, f, imgFileFormat);
		}
		return false;
	}
	
	/**
	 * Saves Image using {@link #saveImage(Image, File, String)}. The file
	 * format is extracted from the files name.
	 * @param image to be saved
	 * @param file to save image to
	 * @return true if image was saved, false if an IOException occured during 
	 * the process, the filename does not contain a dot to get the filetype
	 * or no appropriate writer could be found for specified format.
	 */
	public static boolean saveImage(Image image, File file){
		// get file ending
		int dotIndex = file.getName().lastIndexOf('.');
		if(dotIndex >= 0){
			String ending = file.getName().substring(dotIndex+1, file.getName().length());
			return saveImage(image, file, ending);
		}
		return false;
	}
	
	/**
	 * Saves Image using {@link #saveImage(Image, File)}. The file
	 * format is extracted from the files name.
	 * @param image to be saved
	 * @param filename path to file to save image to
	 * @return true if image was saved, false if an IOException occured during 
	 * the process, the filename does not contain a dot to get the filetype,
	 * the provided filename path does refer to a directory, 
	 * or no appropriate writer could be found for specified format.
	 */
	public static boolean saveImage(Image image, String filename){
		File f = new File(filename);
		if(!f.isDirectory()){
			return saveImage(image, f);
		}
		return false;
	}
}
