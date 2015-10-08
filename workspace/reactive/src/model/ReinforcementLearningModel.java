package model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import logist.plan.Action;
import logist.plan.Action.Move;
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
	
	
	private static Map<Integer, Double> V = new HashMap<>();
	
	private static Map<Integer, City> best = new HashMap<>();
	
	
	
	/** discount factor **/
	private static Double gamma = 1.0;
	
	
	
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
		
		Map<Integer, Map<City, Double>> Q = new HashMap<>();
		
		for (Entry<Integer, State> state : states.entrySet()) {
			
			City maxAction = null;
			double maxActionValue = 0;
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
				for(City sprime : t) {
					sum += td.probability(nextCity, sprime) * V.get(state.getKey());
				}
				sum = gamma * sum;
				
				double qValue = sum + rsa;
				
				if(qValue > maxActionValue) {
					maxActionValue = qValue;
					maxAction = nextCity;
				}
				// At the end
				Q.get(state.getKey()).put(nextCity, rsa + sum);
			}
			V.put(state.getKey(), maxActionValue);
			best.put(state.getKey(), maxAction);
		}
		
	}
	
	private static void generateAllStates(Topology topology) {
		
		if(!states.isEmpty()) {
			System.err.println("States were already generated! Aborting.");
			return;
		} else {
			
			int i = 0;
			List<City> allCities = topology.cities();
			
			for(City city : allCities) {
				for(City otherCity : allCities) {
					
					Set<Action> possibleActions = new HashSet<Action>();
					if(!city.equals(otherCity)) {
						states.put(i, new State(i, city, otherCity));
						i++;
					}
					
					states.put(i, new State(i, city, null));
					i++;
				}
			}
			
		}
	
	}
	
	private static void generateActionsForStates() {
		
		for (Entry<Integer, State> state : states.entrySet()) {
			Set<City> moves = new HashSet<>();
			for (City neighbour : state.getValue().getCurrentCity().neighbors()) {
				moves.add(neighbour);
			}
			
			City deliveryCity = state.getValue().getDeliveryCity();
			if(state.getValue().getDeliveryCity() != null) {
				moves.add(deliveryCity);
			}
			
			state.getValue().setPossiblesMoves(moves);
		}
		
	}
}
