package template;


import java.util.Random;

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
		
		offlineProcessing(topology, td, vehicle, discount);
	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;

		if (availableTask == null || random.nextDouble() > pPickup) {
			City currentCity = vehicle.getCurrentCity();
			action = new Move(currentCity.randomNeighbor(random));
		} else {
			action = new Pickup(availableTask);
		}
		return action;
	}
	
	/** This methods handles all preprocessing
	 * 
	 * @param t
	 * @param td
	 * @param v
	 * @param discount
	 */
	public void offlineProcessing(Topology t, TaskDistribution td, Vehicle v, double discount) {
		
		
		
	}
	
	
	
}
