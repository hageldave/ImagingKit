package hageldave.imagingkit.fourier;

import java.util.function.Supplier;

public class ArrayUtils {


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

	public static void assertPositive(int x, Supplier<String> errmsg){
		if(x < 1){
			throw new IllegalArgumentException(errmsg.get());
		}
	}
	
	public static void assertArraySize(int size, double[] array, Supplier<String> errmsg){
		if(array.length != size){
			throw new IllegalArgumentException(errmsg.get());
		}
	}

	public static void scaleArray(final double[] array, final double factor){
		for(int i = 0; i < array.length; i++){
			array[i] *= factor;
		}
	}

}
