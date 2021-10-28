package helper;

import java.util.concurrent.locks.Lock;

public class AutoClosableLock implements AutoCloseable {
	private final Lock lock;

	public AutoClosableLock(final Lock lock) {
		this.lock = lock;

		this.lock.lock();
	}

	@Override
	public void close() {
		try {
			lock.unlock();
		} catch (final Exception e) {
			throw new Error(e);
		}
	}
}