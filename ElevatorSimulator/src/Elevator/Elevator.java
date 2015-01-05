package Elevator;

import java.util.ArrayList;

public class Elevator extends AbstractElevator implements Runnable{
	private int currentFloor;
	private int totalMen;
	public boolean direc; //1 is up, 0 is down
	private ArrayList<Integer> doorOpen;//1 is open, 0 is closed
	public ArrayList<Integer> getOff;
	public ArrayList<Integer> goUp;
	public ArrayList<Integer> goDown;
	
	
	public Elevator(int numFloors, int elevatorId, int maxOccupancyThreshold) {
		super(numFloors, elevatorId, maxOccupancyThreshold);
		currentFloor = 1;
		direc = true;
		doorOpen = new ArrayList<Integer>();
		getOff = new ArrayList<Integer>();
		goUp = new ArrayList<Integer>();
		goDown = new ArrayList<Integer>();
		totalMen = 0;
		for (int i = 0; i < numFloors; i++) {
			doorOpen.add(0);
			getOff.add(0);
		}
		for (int i = 0; i < numFloors; i++) {
			goUp.add(0);
			goDown.add(0);
		}
	}

	@Override
	public void OpenDoors() {
		doorOpen.set(currentFloor-1, 1);
	}

	@Override
	public void ClosedDoors() {
		doorOpen.set(currentFloor-1, 0);

	}

	@Override
	public void VisitFloor(int floor) {
		synchronized(this){
			currentFloor = floor;
			//System.out.println("Now the elevator " + elevatorId+ " is at floor "+floor+" and there are "+totalMen+" people in it.");
			if (direc){
				while (getOff.get(currentFloor-1) != 0 || goUp.get(currentFloor-1) != 0){
					OpenDoors();
					notifyAll();
					try {
						wait();
					} catch (InterruptedException e) {
					e.printStackTrace();
					}
				}
				if(doorOpen.get(currentFloor-1) == 1)
				ClosedDoors();
			if(idle()){
					try {
						notifyAll();
						wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} else {
				while (getOff.get(currentFloor-1) != 0 || goDown.get(currentFloor-1) != 0){
					OpenDoors();
					notifyAll();
					try {
						wait();
					} catch (InterruptedException e) {
					e.printStackTrace();
					}
				}
				if(doorOpen.get(currentFloor-1) == 1)
				ClosedDoors();
				if(idle() && totalMen == 0){
					try {
						notifyAll();
						wait();
							} catch (InterruptedException e) {
				
						e.printStackTrace();
					}
				}
			}
		}
	}
	


	@Override
	public boolean Enter() {
		synchronized(this){
			boolean inFact = true;
				if (direc){
					goUp.set(currentFloor-1, (goUp.get(currentFloor-1)-1));
					if (totalMen == maxOccupancyThreshold){
						inFact = false;
					} else {
						totalMen+=1;
					}
				} else {
					goDown.set(currentFloor-1, (goDown.get(currentFloor-1)-1));
					if (totalMen == maxOccupancyThreshold){
						inFact = false;
						totalMen+=1;
					}
				}
				
			notifyAll();
		  return inFact;
		}
	}

	@Override
	public void Exit() {
		synchronized(this){
				getOff.set(currentFloor-1, (getOff.get(currentFloor-1)-1));
				totalMen-=1;
				notifyAll();
			}
			
		}


	@Override
	public void RequestFloor(int floor) {
		synchronized(this){
			getOff.set(floor-1, (getOff.get(floor-1)+1));
			while(doorOpen.get(currentFloor-1) != 1 || currentFloor != floor ){
				try {
					notifyAll();
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public int getID(){
		return elevatorId;
	}
	
	public int getcurrentFloor(){
		return currentFloor;
	}
	
	public int gettotalMen(){
		return totalMen;
	}
	
	public ArrayList<Integer> getgetOff(){
		return getOff;
	}
	
	public ArrayList<Integer> getgoUp(){
		return goUp;
	}
	
	public ArrayList<Integer> getgoDown(){
		return goDown;
	}
	
	public void lock1(int fromFloor){
		synchronized(this){
			goUp.set(fromFloor-1, (goUp.get(fromFloor-1)+1));
			while (!direc || currentFloor !=fromFloor || doorOpen.get(fromFloor-1) == 0){
				try {
					notifyAll();
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void lock2(int fromFloor){
		synchronized(this){
			goDown.set(fromFloor-1, (goDown.get(fromFloor-1)+1));
			while (direc || currentFloor !=fromFloor || doorOpen.get(fromFloor-1) == 0){
				try {
					notifyAll();
					wait();
				} catch (InterruptedException e) {
				e.printStackTrace();
				}
			}
		}
	}

	public boolean idle(){
		int check = 0;

		for (int i = 0; i < numFloors; i++) {
			check += goUp.get(i) + goDown.get(i) + getOff.get(i);
		}
		return (check  == 0);
	}
	
	@Override
	public void run() {
		synchronized(this){
			int counter = 0;
			   while (counter<200){
				   if(currentFloor == 5)
					   direc = false;
				   else if (currentFloor == 1)
					   direc = true;
				   VisitFloor(currentFloor);
				   if (direc){
					   currentFloor++;
				   } else {
					   currentFloor--;
				   }
				   counter++;
			   }
		}
		
	}
}
