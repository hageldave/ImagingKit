/*
 * Copyright 2017 David Haegele
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to 
 * deal in the Software without restriction, including without limitation the 
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or 
 * sell copies of the Software, and to permit persons to whom the Software is 
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * IN THE SOFTWARE.
 */

package hageldave.imagingkit.core.io;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import hageldave.imagingkit.core.util.BufferedImageFactory;

/**
 * Class providing convenience methods for saving Images to file.
 * @author hageldave
 * @since 1.0
 */
public class ImageSaver {
	
	private ImageSaver(){}
	
	/**
	 * @return {@link ImageIO#getWriterFileSuffixes()}
	 * @since 1.0
	 */
	public static String[] getSaveableImageFileFormats(){
		return ImageIO.getWriterFileSuffixes();
	}
	
	/**
	 * returns if specified image format supports rgb values only. This means
	 * argb values probably need conversion beforehand.
	 * @param imgFormat the file format e.g. jpg or png
	 * @return true if format only supports rgb values
	 * @since 1.0
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
	 * <p>
	 * The provided {@link OutputStream} will not be closed, this is the
	 * responsibility of the caller.
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
	 * @since 1.0 
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
	 * @since 1.0
	 */
	public static void saveImage(Image image, File file, String imgFileFormat){
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			saveImage(image, fos, imgFileFormat);
		} catch (FileNotFoundException e) {
			throw new ImageSaverException(e);
		} finally {
			closeQuietly(fos);
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
	 * @since 1.0
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
	 * @since 1.0
	 */
	public static void saveImage(Image image, String filename){
		File f = new File(filename);
		if(!f.isDirectory()){
			saveImage(image, f);
		} else {
			throw new ImageSaverException(new IOException(String.format("provided file name denotes a directory. %s", filename)));
		}
	}
	
	private static void closeQuietly(Closeable toClose){
		if(toClose != null){
			try {
				toClose.close();
			} catch (IOException e) {/* ignore */}
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
