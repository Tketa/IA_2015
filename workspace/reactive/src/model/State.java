package model;

import java.util.HashSet;
import java.util.Set;

import logist.topology.Topology.City;

public class State {

	private int stateId;
	
	private City currentCity;
	private City deliveryCity; // Or null if no delivery city
	
	private Set<City> possibleMoves;
	
	public State(int stateId, City homeCity, City deliveryCity) {
		
		this.stateId = stateId;
		this.currentCity = homeCity;
		this.deliveryCity = deliveryCity;
		
		this.possibleMoves = new HashSet<City>();
	}

	/**
	 * @return the stateId
	 */
	public int getStateId() {
		return stateId;
	}



	/**
	 * @param stateId the stateId to set
	 */
	public void setStateId(int stateId) {
		this.stateId = stateId;
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
	public Set<City> getPossibleMoves() {
		return possibleMoves;
	}

	/**
	 * @param possibleActions the possibleActions to set
	 */
	public void setPossiblesMoves(Set<City> possibleMoves) {
		this.possibleMoves = possibleMoves;
	}
	
	
	
	
}
