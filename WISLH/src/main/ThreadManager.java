package main;

public class ThreadManager implements Runnable {

	private Thread[] threads;
	private int maxActiveThreads;
	private int threadC;
	
	public ThreadManager(Thread[] threads, int maxActiveThreads, int threadC) {
		
		this.threads = threads;
		this.maxActiveThreads = maxActiveThreads;
		this.threadC = threadC;
	}

	@Override
	public void run() {
		
		int next = 0;
		int before = 0;
		while (next != threadC) {
			
			if ( next - before == maxActiveThreads) {
				try {
					threads[before].join();
					before++;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			threads[next].start();
			next++;
			
			
		}
		
		for (int i = before ; i < next ; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
}
