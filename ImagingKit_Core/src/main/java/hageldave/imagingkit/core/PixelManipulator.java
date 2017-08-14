package hageldave.imagingkit.core;

import java.util.function.Consumer;

import hageldave.imagingkit.core.PixelConvertingSpliterator.PixelConverter;

public interface PixelManipulator<T> {

	public PixelConverter<PixelBase, T> getConverter();

	public Consumer<T> getAction();

	public static <T> PixelManipulator<T> fromConverterAndConsumer(PixelConverter<PixelBase, T> converter, Consumer<T> action){
		return new PixelManipulator<T>() {
			@Override
			public PixelConverter<PixelBase, T> getConverter() {
				return converter;
			}

			@Override
			public Consumer<T> getAction() {
				return action;
			}
		};
	}


}
