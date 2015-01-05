package EB;

public class EventBarrier extends AbstractEventBarrier {
	
	private boolean raise;
	private int waiters;
	public EventBarrier() {
		raise = true;
		waiters = 0;
	}

	@Override
	public void arrive() {
		synchronized(this){
			waiters++;
			System.out.println("There is one travler coming");
			System.out.println("The number of waiters in arrive is " + waiters);
			while (raise){
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("Finish arrive");
			}
		}

	}

	@Override
	public void raise() {
		synchronized(this){
			if(raise){
				raise = false;
				notifyAll();
				while (waiters != 0){
					try {
						wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				raise = true;
				System.out.println("Finish Raise");
			}
		}

	}

	@Override
	public void complete() {
		synchronized(this){
			waiters--;
			System.out.println("There is one traveler finishing");
			System.out.println("The number of waiters in complete is "+ waiters);
			if(waiters == 0)
				notifyAll();
		}

	}

	@Override
	public int waiters() {
		return waiters;
	}

}
