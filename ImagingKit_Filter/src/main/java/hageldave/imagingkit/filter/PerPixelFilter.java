package hageldave.imagingkit.filter;

import java.util.function.Consumer;

import hageldave.imagingkit.core.Img;
import hageldave.imagingkit.core.Pixel;
import hageldave.imagingkit.filter.settings.FilterSettings;

public abstract class PerPixelFilter extends Filter {
	
	private Consumer<Pixel> perPixelAction;
	
	public PerPixelFilter(Consumer<Pixel> perPixelAction) {
		this(perPixelAction, null);
	}
	
	public PerPixelFilter(Consumer<Pixel> perPixelAction, FilterSettings settings) {
		super(settings);
		this.perPixelAction = perPixelAction;
		if(perPixelAction == null){
			this.perPixelAction = initiallyGetPerPixelAction();
		}
	}
	
	protected Consumer<Pixel> initiallyGetPerPixelAction(){
		return perPixelAction;
	}

	@Override
	protected void doApply(final Img img) {
			img.forEach(perPixelAction);
	}
}
