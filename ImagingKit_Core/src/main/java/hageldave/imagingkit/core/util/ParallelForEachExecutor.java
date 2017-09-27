package hageldave.imagingkit.core.util;

import java.util.Spliterator;
import java.util.concurrent.CountedCompleter;
import java.util.function.Consumer;

import hageldave.imagingkit.core.ImgBase;

/**
 * CountedCompleter class for multithreaded execution of a Consumer on a
 * Spliterator. Used to realise multithreaded forEach loop in {@link ImgBase#forEach(Consumer)}.
 * 
 * @author hageldave
 * @see ImgBase#forEach(boolean parallel, Consumer action)
 * @since 2.0 (relocated from Img class)
 */
public final class ParallelForEachExecutor<T> extends CountedCompleter<Void> {
	private static final long serialVersionUID = 1L;

	private final Spliterator<T> spliterator;
	private final Consumer<? super T> action;
	
	/**
	 * Creates a new ParallelForEachExecutor that executes the 
	 * specified {@link Consumer} (action) on the elements of the specified {@link Spliterator}.
	 * In parallel.
	 * <p>
	 * Call {@link #invoke()} to trigger execution.
	 * 
	 * @param spliterator that provides the elements on which the action is to be performed
	 * @param action to be performed
	 */
	public ParallelForEachExecutor(
			Spliterator<T> spliterator,
			Consumer<? super T> action)
	{
		this(null, spliterator, action);
	}

	private ParallelForEachExecutor(
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