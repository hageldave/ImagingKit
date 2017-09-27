package hageldave.imagingkit.filter;

public interface ProgressListener {

	public static final ProgressListener NOOP_LISTENER = new ProgressListener() {
		@Override
		public void pushPendingFilter(ImgFilter filter, long progressCount) {}
		@Override
		public void notifyFilterProgress(ImgFilter filter, long progressCount, long progress) {}
		@Override
		public void popFinishedFilter(ImgFilter filter) {}
	};
	
	
	public void pushPendingFilter(ImgFilter filter, long progressCount);
	
	public void notifyFilterProgress(ImgFilter filter, long progressCount, long progress);
	
	public void popFinishedFilter(ImgFilter filter);
	
}
