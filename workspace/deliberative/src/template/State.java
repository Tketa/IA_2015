package template;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import logist.plan.Action;
import logist.plan.Action.Delivery;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

public class State implements Comparable<State>{
	
	private Vehicle vehicle;
	private City currentCity;
	
	boolean isFinal;
	
	private TaskSet availableTasks;
	private TaskSet carriedTasks;
	
	private ArrayList<Action> actionList;
	
	private double hCost;
	private double gCost;
	private double fCost;
		
	private int currentWeight;
	private int freeWeight;
	
	public int stateDepth;

	public State(Vehicle vehicle, City currentCity, TaskSet availableTasks,
			TaskSet carriedTasks, ArrayList<Action> action, 
			double gCost, int currentWeight, int freeWeight, int stateDepth) {
		super();
		this.vehicle = vehicle;
		this.currentCity = currentCity;
		
		this.availableTasks = availableTasks;
		this.carriedTasks = carriedTasks;
		this.actionList = action;
		this.gCost = gCost;
		this.currentWeight = currentWeight;
		this.freeWeight = freeWeight;
		
		this.stateDepth = stateDepth;
		
		this.isFinal = this.availableTasks.isEmpty() && this.carriedTasks.isEmpty();
		
		double heuristicTmp = 0;
		for (Task ta : availableTasks) {
			double tmpCost = (currentCity.distanceTo(ta.pickupCity) + ta.pathLength())*vehicle.costPerKm();
			if(heuristicTmp < tmpCost) heuristicTmp = tmpCost;
		}
		for(Task tc: carriedTasks){
			double tmpCost = currentCity.distanceTo(tc.deliveryCity)*vehicle.costPerKm();
			if(heuristicTmp < tmpCost) heuristicTmp = tmpCost;
		}
		this.hCost = heuristicTmp;
		//hCost=0;
		this.fCost = hCost + gCost;
	}
	
	public List<State> getNextStates(){
		
		LinkedList<State> states = new LinkedList<State>();
			
		// Find the annoying case where we deliver a task in a city where we can get a new one.
		/*for (Task carried : carriedTasks) {
			for (Task available : availableTasks) {
				
				// Only take the available task if we have enough free weight AFTER DROPPING THE CARRIED ONE hehe.
				if(carried.deliveryCity.equals(available.pickupCity) && available.weight <= (freeWeight + carried.weight)) {
					TaskSet newCarried = carriedTasks.clone();
					newCarried.remove(carried);
					newCarried.add(available);
					
					TaskSet newAvailable = availableTasks.clone();
					newAvailable.remove(available);
					
					ArrayList<Action> tmpActionList = new ArrayList<Action>(actionList);
					for (City city : currentCity.pathTo(carried.deliveryCity)) {
						tmpActionList.add(new Move(city));
					}
					tmpActionList.add(new Delivery(carried));
					tmpActionList.add(new Pickup(available));
					
					double newCost = gCost + currentCity.distanceTo(carried.deliveryCity)*vehicle.costPerKm();
					
					int newWeight = currentWeight + available.weight - carried.weight;
					int newFreeWeight = freeWeight + carried.weight - available.weight;
					
					states.add(new State(vehicle, carried.deliveryCity, newAvailable, newCarried, tmpActionList, newCost, newWeight, newFreeWeight, stateDepth + 1));
				}
			}
		}*/
		
		// Handle the case where we PICKUP tasks from cities
		for(Task available : availableTasks) {
			
			if(available.weight > freeWeight)
				// Passe ton chemin.
				continue;
			
			TaskSet newAvailable = availableTasks.clone();
			newAvailable.remove(available);
			TaskSet newCarried = carriedTasks.clone();
			newCarried.add(available);
			
			ArrayList<Action> tmpActionList = new ArrayList<Action>(actionList);
			for (City city : currentCity.pathTo(available.pickupCity)) {
				tmpActionList.add(new Move(city));
			}
			tmpActionList.add(new Pickup(available));
			
			double newCost = gCost + currentCity.distanceTo(available.pickupCity)*vehicle.costPerKm();
			
			int newWeight = currentWeight + available.weight;
			int newFreeWeight = freeWeight - available.weight;
			
			states.add(new State(vehicle, available.pickupCity, newAvailable, newCarried, tmpActionList, newCost, newWeight, newFreeWeight, stateDepth + 1));
		}
		
		// Handle the cases where we DELIVER tasks to cities
		for (Task carried : carriedTasks) {
			TaskSet newCarried = carriedTasks.clone();
			newCarried.remove(carried);
			
			ArrayList<Action> tmpActionList = new ArrayList<Action>(actionList);
			for (City city : currentCity.pathTo(carried.deliveryCity)) {
				tmpActionList.add(new Move(city));
			}
			tmpActionList.add(new Delivery(carried));
			
			double newCost = gCost + currentCity.distanceTo(carried.deliveryCity)*vehicle.costPerKm();
			
			int newWeight = currentWeight - carried.weight;
			int newFreeWeight = freeWeight + carried.weight;
			
			states.add(new State(vehicle, carried.deliveryCity, availableTasks, newCarried, tmpActionList, newCost, newWeight, newFreeWeight, stateDepth + 1));
		}
		return states;
	}

	public double getfCost() {
		return fCost;
	}

	public double getHeuristicValue() {
		return hCost;
	}

	public ArrayList<Action> getActionList() {
		return actionList;
	}

	public void setActionList(ArrayList<Action> actionList) {
		this.actionList = actionList;
	}

	/**
	 * @return the currentCity
	 */
	public City getCurrentCity() {
		return currentCity;
	}

	/**
	 * @param currentCity the currentCity to set
	 */
	public void setCurrentCity(City currentCity) {
		this.currentCity = currentCity;
	}

	/**
	 * @return the availableTasks
	 */
	public TaskSet getAvailableTasks() {
		return availableTasks;
	}

	/**
	 * @param availableTasks the availableTasks to set
	 */
	public void setAvailableTasks(TaskSet availableTasks) {
		this.availableTasks = availableTasks;
	}

	/**
	 * @return the carriedTasks
	 */
	public TaskSet getCarriedTasks() {
		return carriedTasks;
	}

	/**
	 * @param carriedTasks the carriedTasks to set
	 */
	public void setCarriedTasks(TaskSet carriedTasks) {
		this.carriedTasks = carriedTasks;
	}

	/**
	 * @return the currentWeight
	 */
	public int getCurrentWeight() {
		return currentWeight;
	}

	/**
	 * @param currentWeight the currentWeight to set
	 */
	public void setCurrentWeight(int currentWeight) {
		this.currentWeight = currentWeight;
	}

	/**
	 * @return the freeWeight
	 */
	public int getFreeWeight() {
		return freeWeight;
	}

	/**
	 * @param freeWeight the freeWeight to set
	 */
	public void setFreeWeight(int freeWeight) {
		this.freeWeight = freeWeight;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((availableTasks == null) ? 0 : availableTasks.hashCode());
		result = prime * result
				+ ((carriedTasks == null) ? 0 : carriedTasks.hashCode());
		result = prime * result
				+ ((currentCity == null) ? 0 : currentCity.hashCode());
		result = prime * result + currentWeight;
		result = prime * result + freeWeight;
		result = prime * result + ((vehicle == null) ? 0 : vehicle.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		State other = (State) obj;
		if (availableTasks == null) {
			if (other.availableTasks != null)
				return false;
		} else if (!availableTasks.equals(other.availableTasks))
			return false;
		if (carriedTasks == null) {
			if (other.carriedTasks != null)
				return false;
		} else if (!carriedTasks.equals(other.carriedTasks))
			return false;
		if (currentCity == null) {
			if (other.currentCity != null)
				return false;
		} else if (!currentCity.equals(other.currentCity))
			return false;
		if (currentWeight != other.currentWeight)
			return false;
		if (freeWeight != other.freeWeight)
			return false;
		return true;
	}

	@Override
	public int compareTo(State s) {
		return Double.compare(this.fCost, s.fCost);
	}
}
