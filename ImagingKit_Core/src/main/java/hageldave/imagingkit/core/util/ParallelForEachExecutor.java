package hageldave.imagingkit.core.util;

import java.util.Spliterator;
import java.util.concurrent.CountedCompleter;
import java.util.function.Consumer;

import hageldave.imagingkit.core.Img;

/**
 * CountedCompleter class for multithreaded execution of a Consumer on a
 * Pixel Spliterator. Used to realise multithreaded forEach loop.
 * @author hageldave
 * @see Img#forEachParallel(Consumer)
 * @since 1.0
 */
public final class ParallelForEachExecutor<T> extends CountedCompleter<Void> {
	private static final long serialVersionUID = 1L;

	final Spliterator<T> spliterator;
	final Consumer<? super T> action;

	public ParallelForEachExecutor(
			ParallelForEachExecutor<T> parent,
			Spliterator<T> spliterator,
			Consumer<? super T> action)
	{
		super(parent);
		this.spliterator = spliterator;
		this.action = action;
	}

	@Override
	public void compute() {
		Spliterator<T> sub;
		while ((sub = spliterator.trySplit()) != null) {
			addToPendingCount(1);
			new ParallelForEachExecutor<T>(this, sub, action).fork();
		}
		spliterator.forEachRemaining(action);
		propagateCompletion();
	}
}