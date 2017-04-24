package runback;

import java.util.concurrent.*;

public class ThreadPool {
	private static ExecutorService cachedPool = Executors.newCachedThreadPool();
	
	public static void execute(Runnable run){
		cachedPool.execute(run);
	}
}
