package Elevator;

public abstract class AbstractBuilding {

	protected int numFloors;
	protected int numElevators;

	public AbstractBuilding(int numFloors, int numElevators) {
		this.numFloors = numFloors;
		this.numElevators = numElevators;
	}

	public abstract AbstractElevator CallUp(int fromFloor);

	public abstract AbstractElevator CallDown(int fromFloor); 
    
}
