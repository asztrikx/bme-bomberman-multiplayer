package helper;

import java.util.concurrent.locks.Lock;

public class AutoClosableLock implements AutoCloseable {
	Lock lock;

	public AutoClosableLock(Lock lock) {
		this.lock = lock;

		this.lock.lock();
	}

	@Override
	public void close() throws Exception {
		lock.unlock();
	}
}