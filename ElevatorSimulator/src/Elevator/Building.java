package Elevator;

import java.util.ArrayList;

public class Building extends AbstractBuilding {
	
	private int maxOccupancyThreshold;
	public ArrayList<Elevator> elevatorList;
	
	
	
	public Building(int numFloors, int numElevators) {
		super(numFloors, numElevators);

	}
	
	public Building(int numFloors, int numElevators, int maxOccupancyThreshold){
		super(numFloors, numElevators);
		this.maxOccupancyThreshold = maxOccupancyThreshold;
		elevatorList = new ArrayList<Elevator>();
		for (int i = 0; i < numElevators; i++) {
			elevatorList.add(new Elevator(numFloors,i,maxOccupancyThreshold));
		}
		
	}
	
	public int EstimateDistance(Elevator elevator, int fromFloor, boolean direction){
		int distance = 0;
		if (direction){
				if (elevator.getcurrentFloor() <= fromFloor){
					if(elevator.direc){
						distance+=10;
						distance+= (5-(fromFloor - elevator.getcurrentFloor()))*10;
						for (int i = elevator.getcurrentFloor(); i < fromFloor; i++){
							distance-= elevator.getOff.get(i-1)*5;
						}
					} else {
						distance+= (fromFloor-elevator.getcurrentFloor())*5;
					}
				}else{
					if (!elevator.direc)
					distance+=5;
				}
			
		} else {
			if (elevator.getcurrentFloor() >= fromFloor){
				if (!elevator.direc){
					distance+=10;
					distance+= (5-(elevator.getcurrentFloor()-fromFloor))*10;
					for (int j = fromFloor; j < elevator.getcurrentFloor(); j++){
						distance-= elevator.getOff.get(j-1)*5;
					}
				} else {
					distance += (elevator.getcurrentFloor() - fromFloor)*5;
				}
			} else {
				if (elevator.direc)
				distance+=5;
			}
		}
		return distance;
	}
	
	public int EstimatePeople(Elevator elevator, int fromFloor, boolean direction){
		int people = 0;
		if (direction){
			if(elevator.direc){
				if (elevator.getcurrentFloor() <= fromFloor){
						people += (maxOccupancyThreshold - elevator.gettotalMen())*10;
					for (int i = elevator.getcurrentFloor(); i < fromFloor; i++){
						people-=elevator.goUp.get(i-1)*50;
						people-=elevator.gettotalMen()*100;
						people+=elevator.getOff.get(i-1)*50;
					}
				}
			}
		} else {
			if(!elevator.direc){
				if (elevator.getcurrentFloor() >= fromFloor){
						people += (maxOccupancyThreshold - elevator.gettotalMen())*10;
					for (int i = elevator.getcurrentFloor(); i < fromFloor; i++){
						people-=elevator.goDown.get(i-1)*50;
						people-=elevator.gettotalMen()*100;
						people+=elevator.getOff.get(i-1)*50;
					}
				}
			}
			
		}
		return people;
	}


	@Override
	public AbstractElevator CallUp(int fromFloor) {
		synchronized(this){
			int pickup = 0;
			int highScore = 0;
			int[] scores = new int[numElevators];
			for (int i = 0; i < numElevators; i++){
				scores[i] = EstimateDistance(elevatorList.get(i), fromFloor, true) + EstimatePeople(elevatorList.get(i), fromFloor, true);
				if (scores[i] > highScore){
					pickup = i;
					highScore = scores[i];
				}
			}
			elevatorList.get(pickup).lock1(fromFloor);
			return elevatorList.get(pickup);
		}
	}

	@Override
	public AbstractElevator CallDown(int fromFloor) {
		synchronized(this){
				int pickup = 0;
				int highScore = 0;
				int[] scores = new int[numElevators];
				for (int i = 0; i < numElevators; i++){
					scores[i] = EstimateDistance(elevatorList.get(i), fromFloor, false) + EstimatePeople(elevatorList.get(i), fromFloor, false);
					if (scores[i] > highScore){
						pickup = i;
						highScore = scores[i];
					}
				}
				elevatorList.get(pickup).lock2(fromFloor);
				return elevatorList.get(pickup);
			}
	}

}
