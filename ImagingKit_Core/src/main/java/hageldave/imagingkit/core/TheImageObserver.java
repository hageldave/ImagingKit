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

package hageldave.imagingkit.core;

import java.awt.Image;
import java.awt.image.ImageObserver;

/**
 * {@link ImageObserver} implementation evaluating the infoflags.
 * @author hageldave
 * @since 1.0
 */
public final class TheImageObserver implements ImageObserver {
	
	private final int testbits;
	
	/**
	 * Creates an ImageObserver that returns false when all specified testbits 
	 * are present in the infoflags of {@link #imageUpdate(Image, int, int, int, int, int)}
	 * @param testbits to be tested against infoflags
	 * @since 1.0
	 */
	public TheImageObserver(int testbits) {
		this.testbits = testbits;
	}

	@Override
	public boolean imageUpdate(Image img, int infoflags, int x, int y,
			int width, int height) {
		return (infoflags & testbits) != testbits;
	}
	
	/** Observer that tests infoflags against {@link ImageObserver#ALLBITS} 
	 * @since 1.0 
	 */
	public static final TheImageObserver OBS_ALLBITS = new TheImageObserver(ImageObserver.ALLBITS);
	
	/** Observer that tests infoflags against bitwise OR of 
	 * {@link ImageObserver#WIDTH} and  {@link ImageObserver#HEIGHT}
	 * @since 1.0 
	 */
	public static final TheImageObserver OBS_WIDTHHEIGHT = new TheImageObserver(ImageObserver.WIDTH | ImageObserver.HEIGHT);
}
