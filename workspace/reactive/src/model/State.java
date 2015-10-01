package model;

import java.util.Set;

import logist.plan.Action;
import logist.topology.Topology.City;

public class State {

	
	private City currentCity;
	private City deliveryCity; // Or null if no delivery city
	
	private Set<Action> possibleActions;
	
	public State(City homeCity, Set<Action> possibleActions) {
		
		this.currentCity = homeCity;
		
		// Initially no package to deliver
		this.deliveryCity = null;
		
		this.possibleActions = possibleActions;
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
	 * @return the deliveryCity
	 */
	public City getDeliveryCity() {
		return deliveryCity;
	}

	/**
	 * @param deliveryCity the deliveryCity to set
	 */
	public void setDeliveryCity(City deliveryCity) {
		this.deliveryCity = deliveryCity;
	}

	/**
	 * @return the possibleActions
	 */
	public Set<Action> getPossibleActions() {
		return possibleActions;
	}

	/**
	 * @param possibleActions the possibleActions to set
	 */
	public void setPossibleActions(Set<Action> possibleActions) {
		this.possibleActions = possibleActions;
	}
	
	
	
	
}
