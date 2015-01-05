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
			System.out.println("Call Up from passenger "+riderID+" from floor "+from);
			
			elevator = (Elevator) building.CallUp(from);
			System.out.println("Passenger "+ riderID + " is going to enter the elevator "+elevator.elevatorId+" from floor "+from);
			}while(!elevator.Enter());
			//{
			//	System.out.println("Thread "+Thread.currentThread().getId()+" Keeps Trying.");
			//}
			//elevator.Enter();
			System.out.println("Passenger 1" + riderID + " successfully enters the elevator "+elevator.elevatorId);
			elevator.RequestFloor(to);
			System.out.println("Passenger 1" + riderID + " needs to go to floor "+ to);
			elevator.Exit();
			System.out.println("Passenger 1" + riderID + " successfully arrives");
		}
		else {
			do{
				System.out.println("Call Down from passenger "+riderID+" from floor "+from);
				
				elevator = (Elevator) building.CallDown(from);
				System.out.println("Passenger "+ riderID + " is going to enter the elevator "+elevator.elevatorId+" from floor "+from);
				}while(!elevator.Enter());
			
			System.out.println("Passenger 1" + riderID + " successfully enters the elevator");
			elevator.RequestFloor(to);
			System.out.println("Passenger 1" + riderID + "  needs to go to floor "+ to);
			elevator.Exit();
			System.out.println("Passenger 1" + riderID + " successfully arrives on floor" + to);
		}
	}	
}
