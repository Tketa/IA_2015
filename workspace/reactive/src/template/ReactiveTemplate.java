package template;


import java.util.Random;
import java.util.Set;

import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;
import model.ReinforcementLearningModel;
import model.State;


public class ReactiveTemplate implements ReactiveBehavior {

	private Random random;
	private double pPickup;

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class,
				0.95);

		this.random = new Random();
		this.pPickup = discount;
		
		// This agent is a vehicle so it has only one in its list.
		Vehicle vehicle = agent.vehicles().get(0);
		
		ReinforcementLearningModel.offlineProcessing(topology, td, vehicle, discount);
	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		
		Action action;
		
		City currentCity = vehicle.getCurrentCity();
		City potentialDeliveryCity = (availableTask == null) ? null : availableTask.deliveryCity;
		
		State currentState = new State(-1, currentCity, potentialDeliveryCity);
		
		
		City newDestinationCity = ReinforcementLearningModel.getNextMoveForState(currentState);
		
		if(availableTask != null && newDestinationCity.equals(availableTask.deliveryCity)) {
			System.err.println("Picking up task from [" + currentCity + "] to [" + newDestinationCity + "]") ;
			action = new Pickup(availableTask);
		} else {
			System.err.println("Simply move from [" + currentCity + "] to [" + newDestinationCity + "]");
			action = new Move(newDestinationCity);
		}
		
		return action;
		
//		State currentState = new State(stateId, homeCity, deliveryCity)
//
//		if (availableTask == null || random.nextDouble() > pPickup) {
//			City currentCity = vehicle.getCurrentCity();
//			action = new Move(currentCity.randomNeighbor(random));
//		} else {
//			action = new Pickup(availableTask);
//		}
//		return action;
	}
	
	
}
