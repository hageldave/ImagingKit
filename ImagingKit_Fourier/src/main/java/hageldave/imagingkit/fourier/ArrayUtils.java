/*
 * ImagingKit-Fourier - Copyright 2018 David Haegele
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package hageldave.imagingkit.fourier;

import java.util.Collections;
import java.util.function.Supplier;

/**
 * Utility class for doing things with double arrays
 * @author hageldave
 */
public class ArrayUtils {
	
	private ArrayUtils(){/* not constructable */}

	/**
	 * Applies a 2D torus shift to the specified row major order array of specified dimensions.
	 * The array is interpreted as an image of given width and height (with elements in row major order)
	 * and is shifted by x to the right and by y to the bottom.
	 * @param a array
	 * @param w width
	 * @param h height
	 * @param x shift in x direction
	 * @param y shift in y direction
	 */
	public static void shift2D(double[] a, int w, int h, int x, int y){
		assertPositive(w, ()->"specified width is not positive. w="+w);
		assertPositive(h, ()->"specified height is not positive. h="+h);
		while(x < 0) x = w+x;
		while(y < 0) y = h+y;
		x %=w;
		y %=h;
		if(x==0 && y==0){
			return;
		}
		for(int row = 0; row < h; row++){
			int offset = row*w;
			rotateArray(a, w, offset, x);
		}
		rotateArray(a,w*h, 0, y*w);
	}

	/**
	 * This is a copy of {@link Collections#rotate1(java.util.List, int)} for double[]
	 * with capability of rotating only a specific part of the array.
	 * @param a array to rotate (within)
	 * @param size of the part to rotate
	 * @param offset into the array to the starting index of the part that is rotated
	 * @param distance to rotate (how many elements to the right)
	 */
	public static void rotateArray(double[] a, final int size, final int offset, int distance){
		// this is a copy of Collections.rotate1(...) for double[]
		if (size == 0)
			return;
		distance = distance % size;
		if (distance < 0)
			distance += size;
		if (distance == 0)
			return;

		for (int cycleStart = 0, nMoved = 0; nMoved != size; cycleStart++) {
			double displaced = a[cycleStart+offset];
			int i = cycleStart;
			do {
				i += distance;
				if (i >= size)
					i -= size;
				double t=a[i+offset];
				a[i+offset]=displaced;
				displaced=t;
				nMoved ++;
			} while (i != cycleStart);
		}
	}

	/**
	 * Throws an {@link IllegalArgumentException} if specified integer argument is not positive.
	 * @param x to check
	 * @param errmsg generates message to put into exception
	 */
	public static void assertPositive(int x, Supplier<String> errmsg){
		if(x < 1){
			throw new IllegalArgumentException(errmsg.get());
		}
	}
	
	/**
	 * Throws an {@link IllegalArgumentException} is the specified array is not of specified size
	 * @param size for array to have
	 * @param array to check
	 * @param errmsg generates message to put into exception
	 */
	public static void assertArraySize(int size, double[] array, Supplier<String> errmsg){
		if(array.length != size){
			throw new IllegalArgumentException(errmsg.get());
		}
	}

	/**
	 * Multiplies elements of array by specified factor
	 * @param array of elements to scale
	 * @param factor to scale by
	 */
	public static void scaleArray(final double[] array, final double factor){
		for(int i = 0; i < array.length; i++){
			array[i] *= factor;
		}
	}

}
