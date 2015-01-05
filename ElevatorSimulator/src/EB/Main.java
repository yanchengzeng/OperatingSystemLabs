package EB;

import Elevator.Rider;



public class Main {

	public static void main(String[] args) {
		Thread[] threads = new Thread[5];
		threads[0] = new Thread(new MyRunnable(true));
		threads[1] = new Thread(new MyRunnable(true));
		threads[2] = new Thread(new MyRunnable(true));
		threads[3] = new Thread(new MyRunnable(true));
		threads[4] = new Thread(new MyRunnable(false));
		
		
		
		for (int i = 0; i < threads.length; i++) {
			
		}
		
		
		
		for (int i = 0; i < 5; i++) {
			threads[i].start();
		}
	}

}
