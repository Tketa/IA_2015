package template;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import logist.plan.Action;
import logist.plan.Action.Delivery;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

public class State {
	
	private City currentCity;
	
	boolean isFinal;
	
	private TaskSet availableTasks;
	private TaskSet carriedTasks;
	
	private ArrayList<Action> actionList;
		
	private int currentWeight;
	private int freeWeight;
	
	

	public State(City currentCity, TaskSet availableTasks,
			TaskSet carriedTasks, ArrayList<Action> action, 
			int currentWeight, int freeWeight) {
		super();
		this.currentCity = currentCity;
		this.availableTasks = availableTasks;
		this.carriedTasks = carriedTasks;
		this.actionList = action;
		this.currentWeight = currentWeight;
		this.freeWeight = freeWeight;
		
		this.isFinal = this.availableTasks.isEmpty() && this.carriedTasks.isEmpty(); 
	}
	
	public Set<State> getNextStates(){
		
		Set<State> states = new HashSet<State>();
		
		TaskSet tmpCarriedTask = carriedTasks.clone();
		TaskSet tmpAvailableTask = availableTasks.clone();
		
		
		// Find the annoying case where we deliver a task in a city where we can get a new one.
		for (Task carried : carriedTasks) {
			for (Task available : availableTasks) {
				
				// Only take the available task if we have enough free weight AFTER DROPPING THE CARRIED ONE hehe.
				if(available.weight <= (freeWeight + carried.weight) && carried.deliveryCity.equals(available.pickupCity)) {
					
					TaskSet newCarried = carriedTasks.clone();
					newCarried.remove(carried);
					newCarried.add(available);
					
					TaskSet newAvailable = availableTasks.clone();
					newAvailable.remove(available);
					
					ArrayList<Action> tmpActionList = new ArrayList<Action>(actionList);
					tmpActionList.add(new Move(carried.deliveryCity));
					tmpActionList.add(new Delivery(carried));
					tmpActionList.add(new Pickup(available));
					
					int newWeight = currentWeight + available.weight - carried.weight;
					int newFreeWeight = freeWeight + carried.weight - available.weight;
					
					states.add(new State(carried.deliveryCity, newAvailable, newCarried, tmpActionList, newWeight, newFreeWeight));
					
					tmpCarriedTask.remove(carried);
					tmpAvailableTask.remove(available);
				}
				
			}
		}
		
		// Handle the case where we PICKUP tasks from cities
		for(Task available : tmpAvailableTask) {
			
			if(available.weight > freeWeight)
				// Passe ton chemin.
				continue;
			
			
			TaskSet newAvailable = availableTasks.clone();
			newAvailable.remove(available);
			TaskSet newCarried = carriedTasks.clone();
			newCarried.add(available);
			
			ArrayList<Action> tmpActionList = new ArrayList<Action>(actionList);
			tmpActionList.add(new Move(available.deliveryCity));
			tmpActionList.add(new Pickup(available));
			
			int newWeight = currentWeight + available.weight;
			int newFreeWeight = freeWeight - available.weight;
			
			states.add(new State(available.pickupCity, newAvailable, newCarried, tmpActionList, newWeight, newFreeWeight));
		}
		
		// Handle the cases where we DELIVER tasks to cities
		for (Task carried : tmpCarriedTask) {
			TaskSet newCarried = carriedTasks.clone();
			newCarried.remove(carried);
			
			int newWeight = currentWeight - carried.weight;
			int newFreeWeight = freeWeight + carried.weight;
			
			ArrayList<Action> tmpActionList = new ArrayList<Action>(actionList);
			tmpActionList.add(new Move(carried.deliveryCity));
			tmpActionList.add(new Delivery(carried));
			
			states.add(new State(carried.deliveryCity, availableTasks, newCarried, tmpActionList, newWeight, newFreeWeight));
		}
		return states;
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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
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
		result = prime * result + (isFinal ? 1231 : 1237);
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
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
		if (isFinal != other.isFinal)
			return false;
		return true;
	}
	
	
	
	
	
	

}
