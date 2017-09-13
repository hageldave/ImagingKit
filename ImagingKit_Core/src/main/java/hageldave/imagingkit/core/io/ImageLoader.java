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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;

import hageldave.imagingkit.core.Img;
import hageldave.imagingkit.core.util.BufferedImageFactory;

/**
 * Class providing convenience methods for loading Images with {@link ImageIO}.
 * @author hageldave
 * @since 1.0
 */
public class ImageLoader {

	
	private ImageLoader(){}
	
	/**
	 * @return {@link ImageIO#getReaderFileSuffixes()}
	 * @since 1.0
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
	 * @since 1.0
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
	 * @since 1.0
	 */
	public static BufferedImage loadImage(File file){
		try {
			BufferedImage bimg = ImageIO.read(file);
			if(bimg == null){
				throw new ImageLoaderException("Could not load Image! ImageIO.read() returned null.");
			}
			return bimg;
		} catch (IOException e) {
			throw new ImageLoaderException(e);
		}
	}
	
	/**
	 * Tries to load image from specified {@link InputStream}.
	 * The InputStream is not closed, this is the responsibility of the caller.
	 * @param is InputStream of the image
	 * @return loaded Image.
	 * @throws ImageLoaderException if no image could be loaded from the 
	 * InputStream.
	 * @since 1.1
	 */
	public static BufferedImage loadImage(InputStream is){
		try {
			BufferedImage bimg = ImageIO.read(is);
			if(bimg == null){
				throw new ImageLoaderException("Could not load Image! ImageIO.read() returned null.");
			}
			return bimg;
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
	 * @since 1.0
	 */
	public static BufferedImage loadImage(File file, int imageType){
		BufferedImage img = loadImage(file);
		if(img.getType() != imageType){
			img = BufferedImageFactory.get(img, imageType);
		}
		return img;
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
	 * @since 1.0
	 */
	public static BufferedImage loadImage(String fileName, int imageType){
		BufferedImage img = loadImage(fileName);
		if(img.getType() != imageType){
			img = BufferedImageFactory.get(img, imageType);
		}
		return img;
	}
	
	/**
	 * Tries to load Image from file and converts it to the
	 * desired image type if needed. <br>
	 * See {@link BufferedImage#BufferedImage(int, int, int)} for details on
	 * the available image types.
	 * The InputStream is not closed, this is the responsibility of the caller.
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
		if(img.getType() != imageType){
			img = BufferedImageFactory.get(img, imageType);
		}
		return img;
	}
	
	/**
	 * Tries to load an Img from the specified {@link InputStream}.
	 * The image color model will be ARGB.
	 * The InputStream is not closed after the image was read,
	 * this is the responsibility of the caller.
	 * @param is {@link InputStream} of the image file
	 * @return loaded Img
	 * @throws ImageLoaderException if no image could be loaded from the 
	 * InputStream.
	 * @since 1.3
	 */
	public static Img loadImg(InputStream is){
		return Img.createRemoteImg(loadImage(is, BufferedImage.TYPE_INT_ARGB));
	}
	
	/**
	 * Tries to load an Img from the specified url 
	 * (e.g. "file:///home/user1/myimage.png" or "http://mywebsite.org/myimage.png").
	 * The image color model will be ARGB.
	 * @param urlspec the String to parse as a {@link URL}.
	 * @return loaded Img.
	 * @throws ImageLoaderException if no image could be loaded from the 
	 * specified url.
	 * @since 1.3
	 */
	public static Img loadImgFromURL(String urlspec) {
		try{
			URL url = new URL(urlspec);
			try(InputStream is = url.openStream()){
				return loadImg(is);
			}
		} catch (IOException e){
			throw new ImageLoaderException(e);
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
