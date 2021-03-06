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

	//private final double GOLDEN_RATIO = 2 / (1.0 + Math.sqrt(5)) ;
	
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
		
		double timerRatio = Math.min(1, Math.log(1+(futureTasks.size()/10)));
		long timer = (long) timerRatio*(endTime - 1000);
		
		
		this.futureSolution = CentralizedPlanner.centralizedSolution(vehicles, futureTasks, timer);
		double cost = this.futureSolution.computeCost(vArray);
		
		double marginalCost = Math.abs(cost - currentSolution.computeCost(vArray));
		
		// ----- STRATEGY --------------
		
		Random r = new Random();
		
		double randomRatio = 1 + 0.5*r.nextDouble();
		
		if(marginalCost > 1000 && r.nextDouble() > 0.9) marginalCost = 7000;
		if(marginalCost == 0) marginalCost = 50;
		
		return (long) Math.ceil(randomRatio*marginalCost);
	}

	@Override
	public void auctionResult(Task lastTask, int lastWinner, Long[] lastOffers) {
		
		if (lastWinner == this.agent.id()) {
			System.out.println("You won the auction!");
			this.currentSolution = this.futureSolution.clone();
			this.currentTasks.add(lastTask);
		}
	}

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		
		List<Plan> plans = new LinkedList<Plan>();
		
		if(!tasks.isEmpty()){
			long startTime = System.currentTimeMillis();
			long endTime = startTime + timeout_plan;

			List<Task> tmpTasks = new ArrayList<Task>();
			
			for (Task task : tasks) {
				tmpTasks.add(task);
			}
			this.currentSolution = CentralizedPlanner.centralizedSolution(vehicles, tmpTasks, endTime- 2000);
			for(Vehicle v : vehicles) plans.add(this.currentSolution.generatePlan(v));
		}
		return plans;
	}
}
