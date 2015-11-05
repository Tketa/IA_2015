package template;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.topology.Topology.City;

public class Solution {
	
	private HashMap<Integer, List<ExtendedTask>> solution;

	public Solution(int nbVehicles) {
		super();
		solution = new HashMap<>();
		for(int i = 0; i < nbVehicles; i++) {
			solution.put(i, new LinkedList<ExtendedTask>());
		}
	}
	
	public void addTask(int vehicleId, ExtendedTask t) {
		solution.get(vehicleId).add(t);
	}
	
	public boolean isValid() {
		return true;
	}
	
	public Plan generatePlan(Vehicle vehicle) {
		
		int vehicleId = vehicle.id();
		City current = vehicle.getCurrentCity();
		
		Plan p = new Plan(current);
		
		List<ExtendedTask> tasks = solution.get(vehicleId);
		
		City intermediateCity = current;
		for (ExtendedTask t : tasks) {
			City nextDestination = t.isPickup() ? t.getT().pickupCity : t.getT().deliveryCity;
			
			for (City c :  intermediateCity.pathTo(nextDestination)) {
				p.appendMove(c);
			}
			
			if(t.isPickup())
				p.appendPickup(t.getT());
			else
				p.appendDelivery(t.getT());
			
			intermediateCity = nextDestination;
		}
		
		return p;
	}
	
	
}
