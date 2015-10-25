package template;

/* import table */
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import logist.agent.Agent;
import logist.behavior.DeliberativeBehavior;
import logist.plan.Action;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

/**
 * An optimal planner for one vehicle.
 */
@SuppressWarnings("unused")
public class DeliberativeTemplate implements DeliberativeBehavior {

	enum Algorithm { BFS, ASTAR }
	
	/* Environment */
	Topology topology;
	TaskDistribution td;
	
	/* the properties of the agent */
	Agent agent;
	int capacity;

	/* the planning class */
	Algorithm algorithm;
	
	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {
		this.topology = topology;
		this.td = td;
		this.agent = agent;
		
		// initialize the planner
		int capacity = agent.vehicles().get(0).capacity();
		String algorithmName = agent.readProperty("algorithm", String.class, "ASTAR");
		
		// Throws IllegalArgumentException if algorithm is unknown
		algorithm = Algorithm.valueOf(algorithmName.toUpperCase());
		
		// ...
	}
	
	@Override
	public Plan plan(Vehicle vehicle, TaskSet tasks) {
		Plan plan;

		// Compute the plan with the selected algorithm.
		switch (algorithm) {
		case ASTAR:
			// ...
			plan = naivePlan(vehicle, tasks);
			break;
		case BFS:
			// ...
			plan = bfsPlan(vehicle, tasks);
			break;
		default:
			throw new AssertionError("Should not happen.");
		}		
		return plan;
	}
	
	private Plan naivePlan(Vehicle vehicle, TaskSet tasks) {
		City current = vehicle.getCurrentCity();
		Plan plan = new Plan(current);

		for (Task task : tasks) {
			// move: current city => pickup location
			for (City city : current.pathTo(task.pickupCity))
				plan.appendMove(city);

			plan.appendPickup(task);

			// move: pickup location => delivery location
			for (City city : task.path())
				plan.appendMove(city);

			plan.appendDelivery(task);

			// set current city
			current = task.deliveryCity;
		}
		return plan;
	}
	
	private Plan bfsPlan(Vehicle vehicle, TaskSet tasks) {
		
		System.out.println("Using BFS plan..");
		
		City current = vehicle.getCurrentCity();
		Plan plan = new Plan(current);
		
		int weight = 0;
		for (Task t : vehicle.getCurrentTasks()) {
			weight += t.weight;
		}
		
		int freeWeight = vehicle.capacity() - weight;
		
		State currentState = new State(current, tasks, vehicle.getCurrentTasks(), new ArrayList<Action>(),  weight, freeWeight);
		
		plan = findFinalFromState(currentState, plan);
		
		return plan;
	}
	
	private Plan findFinalFromState(State fromState, Plan plan) {
		
		LinkedList<State> Q = new LinkedList<State>();
		ArrayList<State> C = new ArrayList<State>();
		
		boolean reachFinal = false;
		State finalState;
		
		Q.add(fromState);
		
		while(!reachFinal){
			if(Q.isEmpty()){
				reachFinal = true;
			}
			else{
				State currentState = Q.poll();
				//TODO Terminer le BFS mais je suis pas convaincu d'un truc, il faudra en parler!
			}
		}
		
		Set<State> nextStates = fromState.getNextStates();
		
		for(State s : nextStates) {
			if(s.isFinal) {
				
				for(City onTheWay : fromState.getCurrentCity().pathTo(s.getCurrentCity())) {
					plan.appendMove(onTheWay);
				}
				
				for(Task t : fromState.getCarriedTasks()) {
					plan.appendDelivery(t);
				}
				
				return plan;
			}
		}
		
		
		for (State state : nextStates) {
			TaskSet newCarried = state.getCarriedTasks().clone();
			TaskSet oldCarried = fromState.getCarriedTasks().clone();

			newCarried.removeAll(fromState.getCarriedTasks());
			oldCarried.removeAll(state.getCarriedTasks());
						
			for(City onTheWay : fromState.getCurrentCity().pathTo(state.getCurrentCity())) {
				plan.appendMove(onTheWay);
			}
			
			for(Task t : oldCarried) {
				plan.appendDelivery(t);
			}
			
			for(Task t : newCarried) {
				plan.appendPickup(t);
			}
			
			plan = findFinalFromState(state, plan);
		}
		
		return plan;
	}

	@Override
	public void planCancelled(TaskSet carriedTasks) {
		
		if (!carriedTasks.isEmpty()) {
			// This cannot happen for this simple agent, but typically
			// you will need to consider the carriedTasks when the next
			// plan is computed.
		}
	}
}
