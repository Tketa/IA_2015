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

	private final double GOLDEN_RATIO = 2 / (1.0 + Math.sqrt(5)) ;
	
	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;
	private Random random;
	private List<Vehicle> vehicles;
	private List<Task> currentTasks;
	
	private Solution currentSolution;
	private Solution futureSolution;
	
    private long timeout_bid;
    private long timeout_plan;
	
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
		this.timeout_plan = LogistPlatform.getSettings().get(LogistSettings.TimeoutKey.PLAN);
		
		System.out.println(timeout_bid);
		System.out.println(timeout_plan);
		
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
		
		this.futureSolution = CentralizedPlanner.centralizedSolution(vehicles, futureTasks, endTime - 5000);
		double cost = this.futureSolution.computeCost(vArray);
		
		double marginalCost = cost - currentSolution.computeCost(vArray);
		
		System.out.println(endTime - System.currentTimeMillis());
		
		return (long) Math.ceil(marginalCost * GOLDEN_RATIO);
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
		
		long startTime = System.currentTimeMillis();
		long endTime = startTime + timeout_plan;
		
		List<Task> tmpTasks = new ArrayList<Task>();
		for (Task task : tasks) {
			tmpTasks.add(task);
		}
		this.currentSolution = CentralizedPlanner.centralizedSolution(vehicles, tmpTasks, endTime);
		
		List<Plan> plans = new LinkedList<Plan>();
		for(Vehicle v : vehicles) plans.add(this.currentSolution.generatePlan(v));
		
		return plans;
	}
	
	
}
