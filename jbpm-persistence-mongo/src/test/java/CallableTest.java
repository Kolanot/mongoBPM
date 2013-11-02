import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;

public class CallableTest {
	public static class WordLengthCallable implements Callable<Integer> {
		private String word;

		public WordLengthCallable(String word) {
			this.word = word;
		}

		public Integer call() {
			return Integer.valueOf(word.length());
		}
	}

	@Test
	public void testCallable() {
		String[] args = new String[] { "ABC", "Simon" };
		ExecutorService pool = Executors.newFixedThreadPool(3);
		Set<Future<Integer>> set = new HashSet<Future<Integer>>();
		for (String word : args) {
			Callable<Integer> callable = new WordLengthCallable(word);
			Future<Integer> future = pool.submit(callable);
			set.add(future);
		}
		int sum = 0;
		for (Future<Integer> future : set) {
			try {
				sum += future.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
				fail(e.toString());
			} catch (ExecutionException e) {
				e.printStackTrace();
				fail(e.toString());
			}
		}
		System.out.printf("The sum of lengths is %s%n", sum);
		assertEquals(sum, 8);
	}

}
