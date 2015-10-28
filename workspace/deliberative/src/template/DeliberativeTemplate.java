package template;

/* import table */
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;

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

	enum Algorithm { BFS, ASTAR, NAIVE}
	
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
		case NAIVE:
			// ...
			plan = naivePlan(vehicle, tasks);
			break;
		case ASTAR:
			// ...
			long startTime = System.currentTimeMillis();
			
			plan = astarPlan(vehicle, tasks);
			
			long stopTime = System.currentTimeMillis();
			long elapsedTime = stopTime - startTime;
			System.out.println(1.0*elapsedTime/1000+"s");
			break;
		case BFS:
			// ...
			startTime = System.currentTimeMillis();
			
			plan = bfsPlan(vehicle, tasks);
			
			stopTime = System.currentTimeMillis();
			elapsedTime = stopTime - startTime;
			System.out.println(1.0*elapsedTime/1000+"s");
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
		
		int weight = 0;
		for (Task t : vehicle.getCurrentTasks()) {
			weight += t.weight;
		}
		
		int freeWeight = vehicle.capacity() - weight;
		
		State currentState = new State(vehicle, current, tasks, vehicle.getCurrentTasks(), new ArrayList<Action>(), 0, weight, freeWeight);
		
		Plan plan = null;
		LinkedList<State> Q = new LinkedList<State>();
		//ArrayDeque<State> Q = new ArrayDeque<State>();
		ArrayList<State> C = new ArrayList<State>();
		
		boolean reachFinal = false;
		State finalState = null;
		
		Q.add(currentState);
		
		while(!reachFinal){
			if(Q.isEmpty()){
				reachFinal = true;
			}
			else{
				State tmpState = Q.poll();
				if(!C.contains(tmpState)){
					if(tmpState.isFinal == true){
					finalState = tmpState;
					reachFinal = true;
					}
					else{
						C.add(tmpState);
						Q.addAll(tmpState.getNextStates());
					}
				}
			}
			if(finalState != null){
				plan = new Plan(currentState.getCurrentCity(), finalState.getActionList());
			}
		}	
		return plan;
	}
	
	private Plan astarPlan(Vehicle vehicle, TaskSet tasks){
		
		System.out.println("Using A* plan..");
		
		City current = vehicle.getCurrentCity();
		
		int weight = 0;
		for (Task t : vehicle.getCurrentTasks()) {
			weight += t.weight;
		}
		
		int freeWeight = vehicle.capacity() - weight;
		
		State currentState = new State(vehicle, current, tasks, vehicle.getCurrentTasks(), new ArrayList<Action>(), 0, weight, freeWeight);
		City initialCity = currentState.getCurrentCity();
		
		Plan plan = null;
		LinkedList<State> Q = new LinkedList<State>();	
		ArrayList<State> C = new ArrayList<State>();
		
		boolean reachFinal = false;
		State finalState = null;
		
		Q.add(currentState);
		
		while(!reachFinal){
			if(Q.isEmpty()){
				reachFinal = true;
			}
			else{
				State tmpState = Q.poll();
				if(!C.contains(tmpState) || tmpState.getHeuristicValue() < C.get(C.indexOf(tmpState)).getHeuristicValue()){
					if(tmpState.isFinal == true){
						finalState = tmpState;
						reachFinal = true;
					} else{
						C.add(tmpState);
						Q.addAll(tmpState.getNextStates());
						Collections.sort(Q);
					}
				}
				currentState = tmpState;
			}
			if(finalState != null){
				plan = new Plan(initialCity, finalState.getActionList());
			}
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
