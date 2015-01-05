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
		System.out.println("Door opens of elevator "+ elevatorId+" on floor "+currentFloor);
		doorOpen.set(currentFloor-1, 1);
	}

	@Override
	public void ClosedDoors() {
		System.out.println("Elevator "+elevatorId+" ClosedDoors");
		doorOpen.set(currentFloor-1, 0);

	}

	@Override
	public void VisitFloor(int floor) {
		synchronized(this){
			System.out.println("Elevator "+ elevatorId+" takes lock in VisitFloor on floor "+currentFloor+" with "+totalMen+" people in it.");
			currentFloor = floor;
			//System.out.println("Now the elevator " + elevatorId+ " is at floor "+floor+" and there are "+totalMen+" people in it.");
			if (direc){
				System.out.println("The goUp list of elevator "+ elevatorId+" in VisitFloor on floor "+ floor + " is: "+goUp.get(currentFloor-1).toString());
				
				System.out.print("The getOff list of elevator "+ elevatorId+" in VisitFloor is ");
				for (int i=0;i<numFloors;i++){
					System.out.print(getOff.get(i).toString()+" ");
					
				}
				System.out.println();
				
				while (getOff.get(currentFloor-1) != 0 || goUp.get(currentFloor-1) != 0){
					OpenDoors();
					//System.out.println("goUp of elevator "+elevatorId+" is "+goUp.get(currentFloor-1).toString()+" with getOff being "+getOff.get(currentFloor-1).toString());
					notifyAll();
					try {
						//System.out.print("Elevator " + elevatorId+ " before waits in VisitFloor, the getOff for F "+floor+" is "+getOff.get(floor-1).toString()+" ");
						//System.out.println("And the goUp of elevator "+ elevatorId+" in VisitFloor is "+ goUp.get(floor-1).toString()+" and is going to wait in Up");
						wait();
						System.out.println("Elevator "+ elevatorId+" wakes up in Up");
						
					} catch (InterruptedException e) {
					e.printStackTrace();
					}
				}
				//System.out.print("The doorOpen list of elevator "+ elevatorId+" in VisitFloor is ");
				//for (int i=0;i<numFloors;i++){
				//	System.out.print(doorOpen.get(i).toString()+" ");
			//	}
			//	System.out.println();
				if(doorOpen.get(currentFloor-1) == 1)
				ClosedDoors();
			if(idle()){
				System.out.println("Elevator "+elevatorId+" hits idle.");
					try {
						notifyAll();
						System.out.println("Before goes to sleep, the number of people in elevator "+elevatorId+" is "+totalMen);
						wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else {
				while (getOff.get(currentFloor-1) != 0 || goDown.get(currentFloor-1) != 0){
					OpenDoors();
					notifyAll();
					try {
						//System.out.println("Just about to keep the door open for down");
						System.out.println("Elevator "+elevatorId+ " Before wait in VisitFloor, the getOff for F "+floor+" is "+getOff.get(floor-1).toString()+" ");
						System.out.println("And the goDown of elevator "+elevatorId+" in VisitFloor is "+ goUp.get(floor-1).toString()+" and is going to wait in Down");
						wait();
						System.out.println("Elevator "+ elevatorId+" wakes up in Down");
					} catch (InterruptedException e) {
					e.printStackTrace();
					}
				}
				//System.out.print("The doorOpen list of elevator "+ elevatorId+" in VisitFloor is ");
				//for (int i=0;i<numFloors;i++){
				//	System.out.print(doorOpen.get(i).toString()+" ");
				//}
				//System.out.println();
				if(doorOpen.get(currentFloor-1) == 1)
				ClosedDoors();
				if(idle() && totalMen == 0){
					System.out.println("Elevator "+elevatorId+" Ever hits idle.");
					try {
						notifyAll();
						System.out.println("Before goes to sleep, the number of people in elevator "+elevatorId+" is "+totalMen);
						wait();
							} catch (InterruptedException e) {
				
						e.printStackTrace();
					}
				}
			}
			System.out.println("Elevator "+ elevatorId+" releases lock in VisitFloor");
		}
	}
	


	@Override
	public boolean Enter() {
		synchronized(this){
			boolean inFact = true;
			System.out.println("Passenger "+Thread.currentThread().getId()+" Trys to Enter from floor "+currentFloor+" from elevator "+elevatorId+" and takes the lock");
				if (direc){
					goUp.set(currentFloor-1, (goUp.get(currentFloor-1)-1));
					
					System.out.print("The goUp list of elevator "+ elevatorId+" in Enter after operation is ");
					for (int i=0;i<numFloors;i++){
						System.out.print(goUp.get(i).toString()+" ");
					}
					System.out.println();
					if (totalMen == maxOccupancyThreshold){
						inFact = false;
						System.out.println("Passenger " + Thread.currentThread().getId()+" Enter  of elevator "+elevatorId+" fails. Needs to re-try.");
					} else {
						System.out.println("Passenger "+Thread.currentThread().getId()+" is in at the first attempt of elevator "+elevatorId+" ");
						totalMen+=1;
						System.out.println("After increment, the totalMen is "+totalMen);
					}
				} else {
					goDown.set(currentFloor-1, (goDown.get(currentFloor-1)-1));
					
					System.out.print("The goDown list  of elevator "+elevatorId+" in Enter after operation is ");
					for (int i=0;i<numFloors;i++){
						System.out.print(goDown.get(i).toString()+" ");
					}
					System.out.println();
					if (totalMen == maxOccupancyThreshold){
						inFact = false;
						System.out.println("Passenger "+Thread.currentThread().getId()+" Enter fails of elevator "+elevatorId+" Needs to re-try.");
					}else{
						System.out.println("Passenger "+Thread.currentThread().getId()+" is in at the first attempt. of elevator "+elevatorId+" ");
						totalMen+=1;
						System.out.println("After increment, the totalMen of elevator "+elevatorId+" is "+totalMen);
					}
				}
				
			notifyAll();
			/*try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			
			System.out.println("Passenger "+ Thread.currentThread().getId()+" releases lock in Enter of elevator "+elevatorId+" with "+inFact);
		  return inFact;
		}
	}

	@Override
	public void Exit() {
		synchronized(this){
				System.out.println("Passenger "+ Thread.currentThread().getId()+" takes lock in Exit of elevator "+elevatorId);
				System.out.println("Passenger "+Thread.currentThread().getId()+" Exits from floor "+currentFloor+ "of elevator "+elevatorId);
				getOff.set(currentFloor-1, (getOff.get(currentFloor-1)-1));
				totalMen-=1;
				System.out.print("The getOff in Exit is ");
				for (int i=0;i<numFloors;i++){
					System.out.print(getOff.get(i).toString()+" ");
				}
				System.out.println();
				
				notifyAll();
				System.out.println("Passenger "+ Thread.currentThread().getId()+" releases lock in Exit of elevator "+elevatorId);
			}
			
		}


	@Override
	public void RequestFloor(int floor) {
		synchronized(this){
			System.out.println("Passenger "+ Thread.currentThread().getId()+" Requests floor "+floor+" of elevator "+elevatorId+" and takes the lock");
			getOff.set(floor-1, (getOff.get(floor-1)+1));
			System.out.print("The getOff in RequestFloor is ");
			for (int i=0;i<numFloors;i++){
				System.out.print(getOff.get(i).toString()+" ");
			}
			System.out.println();
			while(doorOpen.get(currentFloor-1) != 1 || currentFloor != floor ){
				try {
					notifyAll();
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			System.out.println("Passenger "+ Thread.currentThread().getId()+" releases the lock  of elevator "+elevatorId+" in RequestFloor");
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
			System.out.println("Passenger "+Thread.currentThread().getId()+" Takes lock in lock1 of elevator "+elevatorId+" ");
			goUp.set(fromFloor-1, (goUp.get(fromFloor-1)+1));
			System.out.print("Passenger "+Thread.currentThread().getId()+" The goUp list of elevator "+elevatorId+" in lock1 on floor "+ fromFloor + " is: ");
			for (int i=0;i<numFloors;i++){
				System.out.print(goUp.get(i).toString()+" ");
				
			}
			System.out.println();
		while (!direc || currentFloor !=fromFloor || doorOpen.get(fromFloor-1) == 0){
			try {
				System.out.println("Passenger "+Thread.currentThread().getId()+" is going to wait  of elevator "+elevatorId+" in lock1 from floor "+ fromFloor+" and the current floor is "+currentFloor);
				notifyAll();
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
			System.out.println("Passenger "+Thread.currentThread().getId()+" Releases lock in lock1 of elevator "+elevatorId+" ");
		}
	}
	
	public void lock2(int fromFloor){
		synchronized(this){
			System.out.println("Passenger "+Thread.currentThread().getId()+" Takes lock in lock2 of elevator "+elevatorId+" ");
			goDown.set(fromFloor-1, (goDown.get(fromFloor-1)+1));
			System.out.print("Passenger "+Thread.currentThread().getId()+" The goDown list  of elevator "+elevatorId+" in lock1 on floor "+ fromFloor + " is: ");
			for (int i=0;i<numFloors;i++){
				System.out.print(goDown.get(i).toString()+" ");
				
			}
			System.out.println();
			while (direc || currentFloor !=fromFloor || doorOpen.get(fromFloor-1) == 0){
				try {
					System.out.println("Passenger "+Thread.currentThread().getId()+" is going to wait  of elevator "+elevatorId+" in lock2 from floor "+ fromFloor+" and the current floor is "+currentFloor);
					notifyAll();
					wait();
					System.out.println("Passenger "+Thread.currentThread().getId()+" wakes up in lock2 of elevator "+elevatorId+" ");
				} catch (InterruptedException e) {
				e.printStackTrace();
				}
			}
			System.out.println("Passenger "+Thread.currentThread().getId()+" Releases lock in lock2 of elevator "+elevatorId+" ");
		}
	}

	public boolean idle(){
		int check = 0;

		for (int i = 0; i < numFloors; i++) {
			check += goUp.get(i) + goDown.get(i) + getOff.get(i);
		}
		
		//check+=totalMen;
		
		System.out.println("The result of idle is of elevator "+elevatorId+" is "+check+" and the number of people is "+totalMen);
		
		return (check  == 0);
	}
	@Override
	public void run() {
		synchronized(this){
			System.out.println("Elevator " + Thread.currentThread().getId()+" starts running");
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
