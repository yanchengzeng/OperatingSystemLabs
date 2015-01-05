package Elevator;

public class Rider implements Runnable{
	private int riderID;
	private int from;
	private int to;
	private boolean upOr;
	private Elevator elevator;
	
	public static Building building;
	
	public Rider(int ID, int from, int to, Building building){
		this.riderID = ID;
		this.from = from;
		this.to = to;
		this.upOr = to > from;
		this.building = building;
	}

	@Override
	public void run() {
		if (upOr){
			do{
				elevator = (Elevator) building.CallUp(from);
			} while(!elevator.Enter());
			elevator.RequestFloor(to);
			elevator.Exit();
		}
		else {
			do{
				elevator = (Elevator) building.CallDown(from);
			} while(!elevator.Enter());
			elevator.RequestFloor(to);
			elevator.Exit();
		}
	}	
}
