package hageldave.imagingkit.core;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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
	 * @param os {@link OutputStream} to write image to.
	 * @param imgFileFormat image file format. Consult {@link #getSaveableImageFileFormats()}
	 * to get the supported img file formats of your system. 
	 * @throws ImageSaverException if an IOException occured during 
	 * the process or no appropriate writer could be found for specified format.
	 * @since 1.1
	 */
	public static void saveImage(Image image, OutputStream os, String imgFileFormat){
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
			boolean success = ImageIO.write(bImg, imgFileFormat, os);
			if(!success){
				throw new ImageSaverException("Could not save Image! No appropriate writer was found.");
			}
		} catch (IOException e) {
			throw new ImageSaverException(e);
		}
	}
	
	/**
	 * Saves image using {@link #saveImage(Image, File, String)}.
	 * @param image to be saved
	 * @param filename path to file
	 * @param imgFileFormat image file format. Consult {@link #getSaveableImageFileFormats()}
	 * @throws ImageSaverException if an IOException occured during 
	 * the process, the provided filename path does refer to a directory or no 
	 * appropriate writer could be found for specified format. 
	 */
	public static void saveImage(Image image, String filename, String imgFileFormat){
		File f = new File(filename);
		if(!f.isDirectory()){
			saveImage(image, f, imgFileFormat);
		} else {
			throw new ImageSaverException(new IOException(String.format("provided file name denotes a directory. %s", filename)));
		}
	}
	
	/**
	 * Saves image using {@link #saveImage(Image, OutputStream, String)}.
	 * @param image to be saved
	 * @param file to save image to
	 * @param imgFileFormat image file format. Consult {@link #getSaveableImageFileFormats()}
	 * @throws ImageSaverException if an IOException occured during 
	 * the process, no OutputStream could be created due to a 
	 * FileNotFoundException or no appropriate writer could be found for 
	 * specified format.
	 */
	public static void saveImage(Image image, File file, String imgFileFormat){
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(file);
			saveImage(image, fos, imgFileFormat);
		} catch (FileNotFoundException e) {
			throw new ImageSaverException(e);
		}
	}
	
	/**
	 * Saves Image using {@link #saveImage(Image, File, String)}. The file
	 * format is extracted from the files name.
	 * @param image to be saved
	 * @param file to save image to
	 * @throws ImageSaverException if an IOException occured during 
	 * the process, the filename does not contain a dot to get the filetype
	 * or no appropriate writer could be found for specified format.
	 */
	public static void saveImage(Image image, File file){
		// get file ending
		int dotIndex = file.getName().lastIndexOf('.');
		if(dotIndex >= 0){
			String ending = file.getName().substring(dotIndex+1, file.getName().length());
			saveImage(image, file, ending);
		} else {
			throw new ImageSaverException("could not detect file format from file name. Missing dot. " + file.getName());
		}
	}
	
	/**
	 * Saves Image using {@link #saveImage(Image, File)}. The file
	 * format is extracted from the files name.
	 * @param image to be saved
	 * @param filename path to file to save image to
	 * @throws ImageSaverException if an IOException occured during 
	 * the process, the filename does not contain a dot to get the filetype,
	 * the provided filename path does refer to a directory, 
	 * or no appropriate writer could be found for specified format.
	 */
	public static void saveImage(Image image, String filename){
		File f = new File(filename);
		if(!f.isDirectory()){
			saveImage(image, f);
		} else {
			throw new ImageSaverException(new IOException(String.format("provided file name denotes a directory. %s", filename)));
		}
	}
	
	/**
	 * RuntimeException class for Exceptions that occur during image saving.
	 * @author hageldave
	 * @since 1.1
	 */
	public static class ImageSaverException extends RuntimeException {
		private static final long serialVersionUID = -2590926614530103717L;

		public ImageSaverException() {
		}
		
		public ImageSaverException(String message) {
			super(message);
		}
		
		public ImageSaverException(Throwable cause) {
			super(cause);
		}
		
		public ImageSaverException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
