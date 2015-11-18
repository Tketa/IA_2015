package template;

//the list of imports
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import logist.agent.Agent;
import logist.behavior.AuctionBehavior;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

/**
 * A very simple auction agent that assigns all tasks to its first vehicle and
 * handles them sequentially.
 * 
 */


@SuppressWarnings("unused")
public class AuctionTemplate implements AuctionBehavior {

	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;
	private Random random;
	private Vehicle vehicle;
	private City currentCity;
	
	private Solution currentSolution;

	@Override
	public void setup(Topology topology, TaskDistribution distribution,
			Agent agent) {

		this.topology = topology;
		this.distribution = distribution;
		this.agent = agent;
		this.vehicle = agent.vehicles().get(0);
		this.currentCity = vehicle.homeCity();

		long seed = -9019554669489983951L * currentCity.hashCode() * agent.id();
		this.random = new Random(seed);
		
		this.currentSolution = new Solution(agent.vehicles().size());
	}

	@Override
	public void auctionResult(Task previous, int winner, Long[] bids) {
		
		Vehicle[] vArray = new Vehicle[agent.vehicles().size()];
		vArray = agent.vehicles().toArray(vArray);
		
		if (winner == agent.id()) {
			System.out.println("You won the auction!");
			currentSolution = currentSolution.getOptimalSolution(previous, vArray);
			currentSolution.print();
		}
	}
	
	@Override
	public Long askPrice(Task task) {
		System.out.println("Ask price");

		if (vehicle.capacity() < task.weight)
			return null;

		Vehicle[] vArray = new Vehicle[agent.vehicles().size()];
		vArray = agent.vehicles().toArray(vArray);
		
		double currentCost = currentSolution.computeCost(vArray);
		Solution newSol = currentSolution.getOptimalSolution(task, vArray);
		double newCost = newSol.computeCost(vArray);

		double marginalCost = newCost - currentCost;

		//double ratio = 1.0 + (random.nextDouble() * 0.05 * task.id);
		//double bid = ratio * marginalCost;
		
		
		
		return (long) Math.ceil(marginalCost);
	}

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		
//		System.out.println("Agent " + agent.id() + " has tasks " + tasks);
//		System.out.println("Generate plan");
//		Vehicle[] vArray = new Vehicle[vehicles.size()];
//		vArray = vehicles.toArray(vArray);
//		
//		Plan planVehicle1 = naivePlan(vehicle, tasks);
//
//		List<Plan> plans = new ArrayList<Plan>();
//		plans.add(planVehicle1);
//		while (plans.size() < vehicles.size())
//			plans.add(Plan.EMPTY);
//
//		return plans;
		List<Plan> plans = new ArrayList<Plan>();
		for(Vehicle v : vehicles) {
			Plan p = currentSolution.generatePlan(v);
			System.out.println(p);
			plans.add(p);
		}
		
		return plans;
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
}
