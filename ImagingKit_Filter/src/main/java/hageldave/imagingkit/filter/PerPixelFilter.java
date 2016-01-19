package hageldave.imagingkit.filter;

import java.util.function.Consumer;

import hageldave.imagingkit.core.Img;
import hageldave.imagingkit.core.Pixel;
import hageldave.imagingkit.filter.settings.FilterSettings;
import hageldave.imagingkit.filter.settings.ReadOnlyFilterSettings;

public abstract class PerPixelFilter extends Filter {
	
	private ConfiguredPerPixelAction perPixelAction;
	
	public PerPixelFilter(PerPixelAction perPixelAction) {
		this(perPixelAction, null);
	}
	
	public PerPixelFilter(PerPixelAction perPixelAction, FilterSettings settings) {
		super(settings);
		this.perPixelAction = new ConfiguredPerPixelAction(perPixelAction);
	}

	@Override
	public void applyTo(final Img img) {
		perPixelAction.configuration = getConfiguration(getSettings());
		if(img.numValues() > 600*600){
			img.forEachParallel(perPixelAction);
		} else {
			img.forEach(perPixelAction);
		}
	}
	
	protected abstract Object[] getConfiguration(ReadOnlyFilterSettings settings);
	

	private static class ConfiguredPerPixelAction implements Consumer<Pixel> {
		private Object[] configuration = null;
		private final PerPixelAction action;
		
		ConfiguredPerPixelAction(final PerPixelAction action) {
			this.action = action;
		}
		
		@Override
		public void accept(Pixel px) {
			action.doAction(px, configuration);
		}
		
	}
	
	public static interface PerPixelAction {
		public void doAction(Pixel px, Object ... configuration);
	}
}
