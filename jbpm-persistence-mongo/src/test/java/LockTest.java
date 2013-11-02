import static org.junit.Assert.*;

import java.util.concurrent.locks.ReentrantLock;
import java.util.*;

import org.junit.Test;

public class LockTest {

	public static class Sync1 {
		public synchronized void synchronizedMethod(String caller,
				int entryCount) {
			System.out.println("caller" + caller + ":entryCount" + entryCount);
			if (entryCount > 1) {
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
				}
				synchronizedMethod(caller, entryCount - 1);
			} else {
				new Sync2().synchronizedLastCall(caller);
			}
		}

		public static synchronized void synchronizedStaticMethod(String caller,
				int entryCount) {
			System.out.println("caller" + caller + ":entryCount" + entryCount);
			if (entryCount > 1) {
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
				}
				synchronizedStaticMethod(caller, entryCount - 1);
			} else {
				new Sync2().synchronizedLastCall(caller);
			}
		}

	}

	public static class Sync2 {
		public synchronized void synchronizedLastCall(String caller) {
			System.out.println("last call, caller" + caller);
		}
	}

	@Test
	public void testSynchronized() {
		new Sync1().synchronizedMethod("Test", 2);
	}

	@Test
	public void testThread() {
		Thread thread1 = new Thread() {
			public void run() {
				new Sync1().synchronizedMethod("Test", 2);
			}
		};
		thread1.start();
		try {
			thread1.join();
		} catch (Exception e) {
		}
		System.out.println("test done");
	}

	@Test
	public void testMultiThread() {
		Thread thread1 = new Thread() {
			public void run() {
				new Sync1().synchronizedMethod("Thread1", 2);
			}
		};
		Thread thread2 = new Thread() {
			public void run() {
				new Sync2().synchronizedLastCall("Thread2");
			}
		};
		thread1.start();
		thread2.start();
		try {
			thread1.join();
		} catch (Exception e) {
		}
		try {
			thread2.join();
		} catch (Exception e) {
		}
		System.out.println("test done");
	}

	@Test
	public void testMultiThread2() {
		Thread thread1 = new Thread() {
			public void run() {
				new Sync1().synchronizedMethod("Thread1", 2);
			}
		};
		Thread thread2 = new Thread() {
			public void run() {
				new Sync1().synchronizedMethod("Thread2", 2);
			}
		};
		thread1.start();
		thread2.start();
		try {
			thread1.join();
		} catch (Exception e) {
		}
		try {
			thread2.join();
		} catch (Exception e) {
		}
		System.out.println("test done");
	}

	@Test
	public void testMultiThread3() {
		Thread thread1 = new Thread() {
			public void run() {
				Sync1.synchronizedStaticMethod("Thread1", 2);
			}
		};
		Thread thread2 = new Thread() {
			public void run() {
				Sync1.synchronizedStaticMethod("Thread2", 2);
			}
		};
		thread1.start();
		thread2.start();
		try {
			thread1.join();
		} catch (Exception e) {
		}
		try {
			thread2.join();
		} catch (Exception e) {
		}
		System.out.println("test done");
	}

	private final ReentrantLock lock = new ReentrantLock();

	@Test
	public void testLock() {
		lock.lock(); // block until condition holds
		try {
			System.out.println("locked");
			assertTrue(lock.isLocked());
			assertTrue(lock.getHoldCount() == 1);
			assertTrue(lock.isHeldByCurrentThread());
			System.out.println("locked twice");
			lock.lock();
			assertTrue(lock.getHoldCount() == 2);
			System.out.println("locked three times");
			lock.lock();
			assertTrue(lock.getHoldCount() == 3);
			assertTrue(lock.getQueueLength() == 0);
		} finally {
			lock.unlock();
		}
	}

	public void lockMethod() {
		lock.lock(); // block until condition holds
		try {
			String threadName= "" + Thread.currentThread();
			System.out.println("locked by this thread:" + threadName);
			Thread.sleep(5000);
			System.out.println("hold count in this thread:" + threadName + "," + lock.getHoldCount());
			System.out.println("hold queue length in this thread:" + threadName + "," + lock.getQueueLength());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}

	@Test
	public void testMultiThreadLock() {
		
		Thread thread1 = new Thread() {
			public void run() {
				lockMethod();
			}
		};
		Thread thread2 = new Thread() {
			public void run() {
				lockMethod();
			}
		};
		thread1.start();
		thread2.start();
		try {
			thread1.join();
		} catch (Exception e) {
		}
		try {
			thread2.join();
		} catch (Exception e) {
		}
		System.out.println("test done");

		List<?> list1 = new ArrayList<Object>();
		List<Object> list2 = new ArrayList<Object>();
		List<? extends Number super Integer> list3 = new ArrayList<String>();
		list1.add("A");
		list2.add("A");
		list3.add("A");
		List<Integer> intList = new ArrayList<Integer>();
		List<? extends Number>  numList = intList;  // OK. List<? extends Integer> is a subtype of List<? extends Number>

		// The following statement should fail since addAll expects
		// Collection<? extends String>
		list.addAll(new ArrayList<? extends String>());	}

}
