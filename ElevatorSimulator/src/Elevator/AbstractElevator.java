package Elevator;

public abstract class AbstractElevator {

	protected int numFloors; 
	protected int elevatorId;
	protected int maxOccupancyThreshold;

	public AbstractElevator(int numFloors, int elevatorId, int maxOccupancyThreshold) {
		this.numFloors = numFloors;
		this.elevatorId = elevatorId;
		this.maxOccupancyThreshold = maxOccupancyThreshold;
	}

	public abstract void OpenDoors(); 	

	public abstract void ClosedDoors();

	public abstract void VisitFloor(int floor);

	public abstract boolean Enter();
	
	public abstract void Exit();

 	public abstract void RequestFloor(int floor);	
}
