package model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import logist.simulation.Vehicle;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

/**
 * Class with static interfaces that models the reinforcement learning model of our reactive agent.
 * @author gottelan
 *
 */
public class ReinforcementLearningModel {

	/** maps a state id to a State object **/
	private static Map<Integer, State> states = new HashMap<>();
	
	/** maps a stateId to another map: for each action, the ward for action the action from state with stateId **/
	private static Map<Integer, Map<City, Double>> rewards = new HashMap<>();
	
	/** maps a stateId to a map of action to stateId. it represents the probability to end up
	 * in state s' while taking action a in state s
	 */
	private static Map<Integer, Map<City, Integer>> stateTransitions = new HashMap<>();
	
	private static Map<Integer, Map<City, Double>> Q = new HashMap<>();
	
	private static Map<Integer, Double> V = new HashMap<>();
	
	private static Map<Integer, City> best = new HashMap<>();
	
	
	/** discount factor **/
	private static Double gamma = 0.95;
	
	
	
	/** This methods handles all preprocessing
	 * 
	 * @param t
	 * @param td
	 * @param v
	 * @param discount
	 */
	public static void offlineProcessing(Topology t, TaskDistribution td, Vehicle v, double discount) {
		
		generateAllStates(t);
		generateActionsForStates();
				
		double difference = Double.MAX_VALUE;
		final double EPSILON = 1E-10;
		
		
		while(difference > EPSILON) {
			System.out.println(difference);
			double newDifference = 0;
			
			for (Entry<Integer, State> state : states.entrySet()) {
				City maxAction = null;
				double maxActionValue = Double.NEGATIVE_INFINITY;
				
				for(City nextCity : state.getValue().getPossibleMoves()) {
					
					City currentCity = state.getValue().getCurrentCity();
					City deliveryCity = state.getValue().getDeliveryCity();
					
					// Negative reward for the distance and cost
					double rsa = - 1.0 * currentCity.distanceTo(nextCity) * v.costPerKm();
					
					if(nextCity.equals(deliveryCity)) {
						rsa += td.reward(currentCity, deliveryCity);
					}
					// --------------------------------------------------------------------
					
					double sum = 0.0;
					double probabilitySum = 0.0;
					
					
					// Create all the states for which there is a task in nextCity
					State tmp = new State(-1, nextCity, null);
					for (City sprime : t) {
						tmp.setDeliveryCity(sprime);
						
						int sprimeId = getIdForState(tmp);
						
						if(sprimeId > -1) {
							sum += td.probability(nextCity, sprime) * V.get(sprimeId);
							
							probabilitySum += td.probability(nextCity, sprime);
						}
					}
					
					// Add other case probability (no task in nextCity)
					tmp.setDeliveryCity(null);
					int noPickupId = getIdForState(tmp);
					sum += V.get(noPickupId) *  (1 - probabilitySum); // td.probability(nextCity, null);
					
					
					sum = discount * sum;
					//sum = gamma * sum;
					
					double qValue = sum + rsa;
					
					if(qValue > maxActionValue) {
						maxActionValue = qValue;
						maxAction = nextCity;
					}
					
					// At the end
					Q.get(state.getKey()).put(nextCity, qValue);
				}
				
				double oldVsValue = V.get(state.getKey()); 
				newDifference += (maxActionValue - oldVsValue);
				
				V.put(state.getKey(), maxActionValue);
				best.put(state.getKey(), maxAction);
			}
			difference = newDifference;
		}
		
	}
	
	private static void generateAllStates(Topology topology) {
		
		if(!states.isEmpty()) {
			//System.err.println("States were already generated! Aborting.");
			return;
		} else {
			
			int i = 0;
			List<City> allCities = topology.cities();
			
			for(City city : allCities) {
				for(City otherCity : allCities) {
										
					// Only create state if otherCity is different 
					if(!city.equals(otherCity)) {
						createState(i, city, otherCity);
						i++;
					}
					
					createState(i, city, null);
					i++;
				}
			}
			
		}
	}
	
	private static void createState(int id, City currentCity, City destinationCity) {
		
		if(states.containsKey(id)) {
			System.err.println("[ERROR] State ID " + id + " appears twice");
		}
		State s = new State(id, currentCity, destinationCity);
		//s.setPossiblesMoves(possibleMoves);
		
		states.put(id, s);
		V.put(id, 0.0);
		Q.put(id, new HashMap<City, Double>());
	}
	
	private static void generateActionsForStates() {
		
		for (Entry<Integer, State> state : states.entrySet()) {
			Set<City> moves = new HashSet<>();
			
			// Iterate over all neighbours to add to moves
			for (City neighbour : state.getValue().getCurrentCity().neighbors()) {
				moves.add(neighbour);
			}
			
			// Also add the potential delivery city to the list of moves.
			City deliveryCity = state.getValue().getDeliveryCity();
			if(state.getValue().getDeliveryCity() != null) {
				moves.add(deliveryCity);
			}
			
			state.getValue().setPossiblesMoves(moves);
		}
	}
	
	public static City getNextMoveForState(State fromState) {
		
		// Find state ID
		int fromStateID = getIdForState(fromState);
		
		City nextMove = best.get(fromStateID);
		//System.out.println("Next move from State with ID " + fromStateID + " is to city " + nextMove);
		return nextMove;
	}
	
	private static int getIdForState(State fromState) {
		// Find state ID
		int fromStateID = -1;
		
		for (Entry<Integer, State> state : states.entrySet()) {
			
			State s = state.getValue();
			if(fromState.getCurrentCity().equals(s.getCurrentCity())) {
				
				City fromDelivery = fromState.getDeliveryCity();
				City sDelivery = s.getDeliveryCity();
				
				boolean condition1 = (fromDelivery != null && fromDelivery.equals(sDelivery));
				boolean condition2 = (fromDelivery == null && sDelivery == null);
				
				
				if(condition1 || condition2) {
					fromStateID = state.getKey();
					return fromStateID;
				}
			}
			
		}
		
		return -1;
	}
	
}
