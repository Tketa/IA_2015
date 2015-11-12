package template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.topology.Topology.City;

public class Solution implements Cloneable{
	
	/* maps a vehicule id to a list of task ordered by order of action */
	private HashMap<Integer, LinkedList<ExtendedTask>> solution;
	private int nbVehicles;

	public Solution(int nbVehicles) {
		this.nbVehicles = nbVehicles;
		solution = new HashMap<Integer, LinkedList<ExtendedTask>>();
		for(int i = 0; i < nbVehicles; i++) {
			solution.put(i, new LinkedList<ExtendedTask>());
		}
	}
	
	/**
	 * Adds the given task to the vehicule.
	 * 
	 * It actually adds 'Pickup(t)' and 'Delivery(t)' as the two LAST actions for this vehicule
	 * 
	 * @param vehicleId
	 * @param t
	 */
	public void addTask(int vehicleId, Task t) {
		if(t == null) 
			return;
		
		ExtendedTask pickup = new ExtendedTask(t, true);
		ExtendedTask delivery = new ExtendedTask(t, false);
		
		this.solution.get(vehicleId).addLast(pickup);
		this.solution.get(vehicleId).addLast(delivery);

	}
	
	public void removTask(int vehicleId, Task t) {
		Iterator<ExtendedTask> it = this.solution.get(vehicleId).iterator();
		if(t == null) 
			return;
		
		int taskId = t.id;
		
		while (it.hasNext()) {
		    ExtendedTask iTask = it.next();
		    if (iTask.getT().id == taskId) {
		        it.remove();
		    }
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
	
	public Solution swapVehicles(int v1, int v2) {
		
		ExtendedTask tv1 = getVehicleFirstTask(v1);
		ExtendedTask tv2 = getVehicleFirstTask(v2);
		
		if(tv1 != null) {
			this.removTask(v1, tv1.getT());
			this.addTask(v2, tv1.getT());
		}
		
		if(tv2 != null) {
			this.removTask(v2, tv2.getT());
			this.addTask(v1, tv2.getT());
		}
		
		return this;
	}
	
	public Solution swapTasks(int v, int t1, int t2) {
		Solution newS = this.clone();
					
		LinkedList<ExtendedTask> currentTasks = this.solution.get(v);
		LinkedList<ExtendedTask> newTasks = new LinkedList<ExtendedTask>();
			
		for(int j = 0; j < currentTasks.size(); j++) {
				if(j == t1) {
					newTasks.add(currentTasks.get(t2));
				} else if (j == t2) {
					newTasks.add(currentTasks.get(t1));
				} else {
					newTasks.add(currentTasks.get(j));
				}
		}
			
		newS.solution.put(v, newTasks);
		
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
		
		this.solution.clear();
		
		for(Map.Entry<Integer, List<ExtendedTask>> entry : solution.entrySet()) {
			LinkedList<ExtendedTask> tasks = new LinkedList<ExtendedTask>();
			
			for (ExtendedTask extendedTask : entry.getValue()) {
				tasks.add(extendedTask);
			}
			
			this.solution.put(entry.getKey(), tasks);
		}
	}
	
}
