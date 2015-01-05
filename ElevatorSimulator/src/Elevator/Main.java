package Elevator;

public class Main {

	public static void main(String[] args) {
		
		Thread[] myThreads = new Thread[7];
		Building building = new Building(5,2,1);
		
		
		
		myThreads[0] = new Thread(new Rider(0, 1,3,building));
		myThreads[1] = new Thread(new Rider(1, 2,4,building));
		myThreads[2] = new Thread(new Rider(2, 4,1,building));
		myThreads[3] = new Thread(new Rider(3, 5,2,building));
		myThreads[4] = new Thread(new Rider(4, 3,5,building));
		myThreads[5] = new Thread(building.elevatorList.get(0));
		myThreads[6] = new Thread(building.elevatorList.get(1));
		
			
		
		
		for (int i = 0; i < 5; i++) {
			myThreads[i].start();
		}
		
		myThreads[5].start();
		myThreads[6].start();
	}

}
