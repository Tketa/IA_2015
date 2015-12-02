package template;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import logist.LogistPlatform;
import logist.LogistSettings;
import logist.agent.Agent;
import logist.behavior.AuctionBehavior;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;


public class AuctionTemplate implements AuctionBehavior {

	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;
	private Random random;
	private List<Vehicle> vehicles;
	private List<Task> currentTasks;
	
	private Solution currentSolution;
	private Solution futureSolution;
	
    private long timeout_bid;
	
	@Override
	public void setup(Topology topology, TaskDistribution distribution,
			Agent agent) {
		
		this.topology = topology;
		this.distribution = distribution;
		this.agent = agent;
		this.vehicles = agent.vehicles();
		this.currentTasks = new ArrayList<Task>();

		this.currentSolution = new Solution(this.vehicles.size());
		
		this.timeout_bid = LogistPlatform.getSettings().get(LogistSettings.TimeoutKey.BID);
		
	}

	@Override
	public Long askPrice(Task task) {
		
		System.out.println("Ask price");

		long startTime = System.currentTimeMillis();
		long endTime = startTime + timeout_bid;
		
		Vehicle[] vArray = new Vehicle[agent.vehicles().size()];
		vArray = agent.vehicles().toArray(vArray);
		
		List<Task> futureTasks = new ArrayList<Task>(currentTasks);
		futureTasks.add(task);
		
		double minCost = Double.POSITIVE_INFINITY;
		
		//while(System.currentTimeMillis() < endTime) {
			//Solution possibleSolution = computeCentralized(vehicles, futureTasks, 0.5);
			Solution possibleSolution = CentralizedPlanner.centralizedSolution(vehicles, futureTasks);
			double possibleCost = possibleSolution.computeCost(vArray);
			if(possibleCost < minCost) {
				minCost = possibleCost;
				this.futureSolution = possibleSolution;
			}
		//}
		
		double marginalCost = minCost - currentSolution.computeCost(vArray);
		
		return (long) Math.ceil(marginalCost);
	}

	@Override
	public void auctionResult(Task lastTask, int lastWinner, Long[] lastOffers) {
		
		/**
		 * Do shit here to predict what the opponents do
		 */
		
		if (lastWinner == this.agent.id()) {
			System.out.println("You won the auction!");
			this.currentSolution = this.futureSolution.clone();
			this.currentTasks.add(lastTask);
		}
		
	}

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		
		List<Task> tmpTasks = new ArrayList<Task>();
		for (Task task : tasks) {
			tmpTasks.add(task);
		}
		this.currentSolution = CentralizedPlanner.centralizedSolution(vehicles, tmpTasks);
		
		List<Plan> plans = new LinkedList<Plan>();
		for(Vehicle v : vehicles) plans.add(this.currentSolution.generatePlan(v));
		
		return plans;
	}

	
	
	
}
