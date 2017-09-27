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

import java.util.function.Consumer;

import hageldave.imagingkit.core.PixelConvertingSpliterator.PixelConverter;

/**
 * The PixelManipulator interface defines an action to be performed on a pixel.
 * The action however is performed on a different representation of the pixel which
 * is given by the manipulators converter.
 * <p>
 * This works the following way:<br>
 * <ol>
 * <li>convert pixel to element using the converter</li>
 * <li>apply the action on the element</li>
 * <li>convert the element back to pixel using the converter</li>
 * </ol>
 * <p>
 * This is used with an image's <tt>forEach</tt> method (see {@link ImgBase#forEach(PixelManipulator)})
 * 
 * @author hageldave
 * @param <P> the pixel type of the converter (PixelBase or an implementation of it)
 * @param <T> the element type of the converter (the type a pixel is converted to before applying the action)
 * @since 2.0
 */
public interface PixelManipulator<P extends PixelBase,T> {

	/**
	 * Returns the converter that converts a pixel to the element 
	 * accepted by this manipulators action (and the element back to the pixel).
	 * @return this manipulator's converter
	 */
	public PixelConverter<P, T> getConverter();

	/**
	 * Returns the action performed by this manipulator
	 * @return this manipulator's action
	 */
	public Consumer<T> getAction();

	/**
	 * Creates a PixelManipulator from a {@link PixelConverter} and corresponding {@link Consumer} (action).
	 * @param converter of the PixelManipulator
	 * @param action of the Manipulator
	 * @return a PixelManipulator consisting of the specified converter and action.
	 */
	public static <P extends PixelBase,T> PixelManipulator<P,T> fromConverterAndConsumer(PixelConverter<P, T> converter, Consumer<T> action){
		return new PixelManipulator<P,T>() {
			@Override
			public PixelConverter<P, T> getConverter() {
				return converter;
			}
			@Override
			public Consumer<T> getAction() {
				return action;
			}
		};
	}


}
