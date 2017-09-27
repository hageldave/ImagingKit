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

package hageldave.imagingkit.core.util;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.function.Function;

/**
 * Class providing convenience methods for converting Images to BufferedImages.
 * @author hageldave
 * @since 1.0
 */
public class BufferedImageFactory {

	private BufferedImageFactory(){}
	
	/**
	 * shortcut for get(img, BufferedImage.TYPE_INT_ARGB).
	 * @param img to be copied to BufferedImage of type INT_ARGB
	 * @return a BufferedImage copy of the provided Image
	 * @since 1.0
	 */
	public static BufferedImage getINT_ARGB(Image img){
		return get(img, BufferedImage.TYPE_INT_ARGB);
	}
	
	/**
	 * Creates a new BufferedImage of the specified imgType and same size as 
	 * the provided image and draws the provided Image onto the new BufferedImage.
	 * @param img to be copied to BufferedImage
	 * @param imgType of the BufferedImage. See 
	 * {@link BufferedImage#BufferedImage(int, int, int)} for details on the
	 * available imgTypes.
	 * @return a BufferedImage copy of the provided Image
	 * @since 1.0
	 */
	public static BufferedImage get(Image img, int imgType){
		Function<Integer, ImageObserver> obs = flags->{
			return (image, infoflags, x, y, width, height)->(infoflags & flags)!=flags;
		};
		BufferedImage bimg = new BufferedImage(
				img.getWidth(obs.apply(ImageObserver.WIDTH)), 
				img.getHeight(obs.apply(ImageObserver.HEIGHT)), 
				imgType);
		Graphics2D gr2D = bimg.createGraphics();
		gr2D.drawImage(img, 0, 0, obs.apply(ImageObserver.ALLBITS));
				
		gr2D.dispose();
		
		return bimg;
	}
	
	/**
	 * Instancing method for BufferedImage of type {@link BufferedImage#TYPE_INT_ARGB}
	 * @param d dimension of the created BufferedImage
	 * @return a new BufferedImage of specified dimension and type TYPE_INT_ARGB
	 * @since 1.0
	 */
	public static BufferedImage getINT_ARGB(Dimension d){
		return getINT_ARGB(d.width, d.height);
	}
	
	/**
	 * Instancing method for BufferedImage of type {@link BufferedImage#TYPE_INT_ARGB}
	 * @param width of the created BufferedImage
	 * @param height of the created BufferedImage
	 * @return a new BufferedImage of specified dimension and type TYPE_INT_ARGB
	 * @since 1.0
	 */
	public static BufferedImage getINT_ARGB(int width, int height){
		return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}
}
