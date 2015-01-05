package EB;

public class MyRunnable implements Runnable {

	private boolean cOrp;
	private static EventBarrier barrier;
	
	public MyRunnable(boolean yesOrno){ 	
		barrier = new EventBarrier();
		cOrp = yesOrno;
	}
	@Override
	public void run() {
		if(cOrp){
			barrier.arrive();
			barrier.complete();
		} else {
			barrier.raise();
		}
	}
}
