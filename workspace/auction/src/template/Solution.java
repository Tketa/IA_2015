package template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.topology.Topology.City;

public class Solution implements Cloneable{
	
	/* maps a vehicle id to a list of task ordered by order of action */
	private HashMap<Integer, ArrayList<ExtendedTask>> solution;
	private int nbVehicles;

	public Solution(int nbVehicles) {
		this.nbVehicles = nbVehicles;
		solution = new HashMap<Integer, ArrayList<ExtendedTask>>();
		for(int i = 0; i < nbVehicles; i++) {
			solution.put(i, new ArrayList<ExtendedTask>());
		}
	}
	
	public boolean isValid(Vehicle[] vehicles, int nbTasks) {
		
		List<Integer> taskIdPickup = new ArrayList<Integer>();
		List<Integer> taskIdDelivery = new ArrayList<Integer>();
		
		int nbExtended = 0;
		
		for(int i = 0; i < nbVehicles; i++) {
			int weight = 0;
			Set<Integer> vehicleTasks = new HashSet<Integer>();
			List<ExtendedTask> tasks = solution.get(i);
			
			nbExtended += tasks.size();
			
			for (ExtendedTask t : tasks) {
				if(t.isPickup()) {
					
					if(taskIdPickup.contains(t.getT().id)){
						//System.err.println("Already encountered PICKUP taks with id " + t.getT().id);
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
						//System.err.println("Already encountered DELIVERY or NOPICKUP for task with id " + t.getT().id);
						return false;
					}
					
					taskIdDelivery.add(t.getT().id);
					
					weight -= t.getT().weight;
				}
			}
		}
		
		if(nbExtended / 2 != nbTasks) {
			System.err.println("Not enough tasks");
			return false;
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
	    s.setSolution(new HashMap<Integer, ArrayList<ExtendedTask>>(this.solution));
	    return s;
	}
	
	public Solution getOptimalSolution(Task t, Vehicle[] vehicles) {
		
		ExtendedTask p = new ExtendedTask(t, true);
		ExtendedTask d = new ExtendedTask(t, false);
		
		double minCost = Double.MAX_VALUE;
		Solution optimalSolution = null;
		
		for(int i = 0; i < vehicles.length; i++) {
			Solution s = new Solution(vehicles.length);
			ArrayList<ExtendedTask> tasks = new ArrayList<ExtendedTask>(solution.get(i));

			for(int j = 0; j <= tasks.size(); j++) {
				for(int k = j + 1; k <= tasks.size() + 1; k++) {
					HashMap<Integer, ArrayList<ExtendedTask>> tmpSol = new HashMap(solution);
					ArrayList<ExtendedTask> tmpTasks = new ArrayList<ExtendedTask>(tasks); 
					tmpTasks.add(j, p);
					tmpTasks.add(k , d);
					tmpSol.put(i, tmpTasks);
					s.setSolution(tmpSol);
					
					double cost = s.computeCost(vehicles);

					if(cost < minCost) {
						minCost = cost;
						optimalSolution = s;
					}
				}
			}
			
		}
		
		return optimalSolution;
	}
	
	public double addTaskOptimally(Task t, Vehicle[] vehicles) {
		
		ExtendedTask p = new ExtendedTask(t, true);
		ExtendedTask d = new ExtendedTask(t, false);
		
		List<Double> newCosts = new LinkedList<Double>();
		
		for(int i = 0; i < vehicles.length; i++) {
			Solution s = new Solution(vehicles.length);
			ArrayList<ExtendedTask> tasks = new ArrayList<ExtendedTask>(solution.get(i));

			for(int j = 0; j < tasks.size() - 1; j++) {
				for(int k = j + 1; k < tasks.size(); k++) {
					HashMap<Integer, ArrayList<ExtendedTask>> tmpSol = new HashMap(solution);
					ArrayList<ExtendedTask> tmpTasks = new ArrayList<ExtendedTask>(tasks); 
					tmpTasks.add(j, p);
					tmpTasks.add(k , d);
					tmpSol.put(i, tmpTasks);
					s.setSolution(tmpSol);
					
					newCosts.add(s.computeCost(vehicles));
				}
			}
			
		}
		
		return Collections.max(newCosts);
	}

	public void setSolution(HashMap<Integer, ArrayList<ExtendedTask>> tmpSol) {
		
		this.solution.clear();
		
		for(Map.Entry<Integer, ArrayList<ExtendedTask>> entry : tmpSol.entrySet()) {
			ArrayList<ExtendedTask> tasks = new ArrayList<ExtendedTask>();
			
			for (ExtendedTask extendedTask : entry.getValue()) {
				tasks.add(extendedTask);
			}
			
			this.solution.put(entry.getKey(), tasks);
		}
	}
	
}
