package template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.topology.Topology.City;

public class Solution implements Cloneable{
	
	/* maps a vehicule id to a list of task ordered by order of action */
	private HashMap<Integer, List<ExtendedTask>> solution;
	private int nbVehicles;

	public Solution(int nbVehicles) {
		this.nbVehicles = nbVehicles;
		solution = new HashMap<Integer, List<ExtendedTask>>();
		for(int i = 0; i < nbVehicles; i++) {
			solution.put(i, new ArrayList<ExtendedTask>());
		}
	}
	
	public void addTask(int vehicleId, ExtendedTask t) {
		solution.get(vehicleId).add(t);
	}
	
	public void removTask(int vehicleId, ExtendedTask t) {
		solution.get(vehicleId).remove(t);
	}
	
	public boolean isValid(Vehicle[] vehicles) {
		
		List<Integer> taskIdPickup = new ArrayList<Integer>();
		List<Integer> taskIdDelivery = new ArrayList<Integer>();
		
		for(int i = 0; i < nbVehicles; i++) {
			int weight = 0;
			Set<Integer> vehicleTasks = new HashSet<Integer>();
			List<ExtendedTask> tasks = solution.get(i);
			
			for (ExtendedTask t : tasks) {
				if(t.isPickup()) {
					
					if(taskIdPickup.contains(t.getT().id)){
						return false;
					}
					
					taskIdPickup.add(t.getT().id);
					vehicleTasks.add(t.getT().id);
					
					weight += t.getT().weight;
					
					if(weight > vehicles[i].capacity()) {
						return false;
					}
				} else {
					
					if(taskIdDelivery.contains(t.getT().id)
							|| !vehicleTasks.contains(t.getT().id)){
						return false;
					}
					
					taskIdDelivery.add(t.getT().id);
					
					weight -= t.getT().weight;
				}
			}
		}
		return true;
	}
	
	public int getNbVehicles() {
		return nbVehicles;
	}
	
	public ExtendedTask getVehicleFirstTask(int vehicleId) {
		if(solution.containsKey(vehicleId)) {
			if(solution.get(vehicleId).isEmpty()) {
				return null;
			} else {
				return solution.get(vehicleId).get(0);
			}
		} else {
			return null;
		}
	}
	
	public int getNbTaskForVehicle(int vehicleId) {
		return solution.get(vehicleId).size();
	}
	
	public Solution swapVehicles(int v1, int v2) {
		
		ExtendedTask tp = getVehicleFirstTask(v1);
		ExtendedTask td = new ExtendedTask(tp.getT(), false);
		
		this.addTask(v2, tp);
		this.addTask(v2, td);
		
		this.removTask(v1, tp);
		this.removTask(v1, td);
		
		return this;
	}
	
	public Solution swapTasks(int v, int t1, int t2) {
		Solution newS = new Solution(nbVehicles);
		

		
		for(int i = 0; i < nbVehicles; i++) {
			
			List<ExtendedTask> currentTasks = this.solution.get(i);
			List<ExtendedTask> newTasks = new ArrayList<ExtendedTask>(currentTasks);
			
			if(i == v) {
				newTasks.add(t1, currentTasks.get(t2));
				newTasks.add(t2, currentTasks.get(t1));
				
				newS.solution.put(i, newTasks);
			} else {
				newS.solution.put(i, newTasks);
			}
		}
		
		return newS;
	}
	
	public double computeCost(Vehicle[] vehicles) {
		
		double totalCost = 0.0;
		
		for(int i = 0; i < vehicles.length; i++) {
			List<ExtendedTask> tasks = solution.get(i);
			
			City tmpCity = vehicles[i].getCurrentCity();
			
			for (ExtendedTask t : tasks) {
				City nextCity = t.isPickup() ? t.getT().pickupCity : t.getT().deliveryCity;
				totalCost += (tmpCity.distanceTo(nextCity) * vehicles[i].costPerKm());
				tmpCity = nextCity;
			}
		}		
		
		return totalCost;
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
	
	public void print() {
		for (Integer name: solution.keySet()){
            String key =name.toString();
            String value = solution.get(name).toString();  
            System.out.println("Vehicle "+key + " " + value);  
		} 
	}

	@Override
	protected Solution clone(){
	    Solution s = new Solution(nbVehicles);
	    s.setSolution(new HashMap<Integer, List<ExtendedTask>>(this.solution));
	    return s;
	}

	public void setSolution(HashMap<Integer, List<ExtendedTask>> solution) {
		this.solution = solution;
	}
	
}