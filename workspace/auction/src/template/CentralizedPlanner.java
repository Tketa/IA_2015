package template;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

public class CentralizedPlanner {

	public static Solution centralizedSolution(List<Vehicle> vehicles, List<Task> tasks, long endTime) {

		//long time_start = System.currentTimeMillis();

		final double PROBABILITY = 0.8;

		int nbTasks = tasks.size();

		Vehicle[] vArray = new Vehicle[vehicles.size()];
		vArray = vehicles.toArray(vArray);

		Random r = new Random();
		Solution initialSolution = initialSolutionClassic(vArray, tasks);

		Solution intermediateSolution = initialSolution;
		Solution bestSolution = intermediateSolution;

		long startTime = System.currentTimeMillis();
		long elapsedTime = 0;
		
		
		while(startTime + elapsedTime < endTime){
			
			//System.out.println(elapsedTime);
			//System.out.println(endTime - System.currentTimeMillis());
			long roundSpeed = System.currentTimeMillis();
			//System.out.println("Iteration " + (nbIterations + 1));
			int vi = -1;
			do {
				vi = r.nextInt(vehicles.size());
			} while(intermediateSolution.getVehicleFirstTask(vi) == null);    	

			List<Solution> possibleNeighbours = new ArrayList<Solution>();
			List<Solution> validNeighbours = new LinkedList<Solution>();

			possibleNeighbours.addAll(changeVehicleOperator2(intermediateSolution, vArray, vi));
			possibleNeighbours.addAll(changeTaskOrderOperation(intermediateSolution, vi));


			Solution ultimateSolution = null;
			ArrayList<Solution> bestSolutions = new ArrayList<Solution>();

			double currentCost = intermediateSolution.computeCost(vArray);
			double maxImprovement = Double.MAX_VALUE;

			boolean strictMin = false;


			for(Solution s : possibleNeighbours) {
				if(s.isValid(vArray, nbTasks)) {
					double cost = s.computeCost(vArray);
					if(cost != currentCost){
						if(cost < maxImprovement) {
							ultimateSolution = s;
							maxImprovement = cost;

							strictMin = true;
							bestSolutions.clear();
						} else if(cost == maxImprovement) {

							if(strictMin)
								bestSolutions.add(ultimateSolution);

							bestSolutions.add(s);
							strictMin = false;
						}
					}
					validNeighbours.add(s);
				}
			}

			double randomNumber = r.nextDouble();

			if(randomNumber < PROBABILITY) {
				if(strictMin) {
					intermediateSolution = ultimateSolution;
				} else {
					if(!bestSolutions.isEmpty()){
						int rndIndex = r.nextInt(bestSolutions.size());
						intermediateSolution = bestSolutions.get(rndIndex);
					}
				}
			}
			else{
				int rndIndex = (int) Math.random() * validNeighbours.size();
    			intermediateSolution = validNeighbours.get(rndIndex);
			}

			currentCost = intermediateSolution.computeCost(vArray);


			//System.out.println("Cost [" + currentCost + "]");

			if(intermediateSolution.computeCost(vArray) < bestSolution.computeCost(vArray)){
				bestSolution = intermediateSolution;
			}
			
			//System.out.println("Round Speed = "+(System.currentTimeMillis()-roundSpeed));
			elapsedTime = System.currentTimeMillis() - startTime; 
		}


		/*
		// Print the final solution.
		System.out.println("Best Cost [" + bestSolution.computeCost(vArray) + "]");
		bestSolution.print();

		long time_end = System.currentTimeMillis();
		long duration = time_end - time_start;
		System.out.println("The plan was generated in " + duration + " milliseconds.");*/
		
		return bestSolution;
	}

	private static Solution selectInitialSolution(Vehicle[] vehicles, TaskSet tasks) {

		int maxCapacity = 0;
		int maxCapacityIdx = -1;

		for(int i = 0; i < vehicles.length; i++) {
			if(vehicles[i].capacity() > maxCapacity) {
				maxCapacity = vehicles[i].capacity();
				maxCapacityIdx = i;
			}
		}

		Solution s = new Solution(vehicles.length);

		int i = 0;
		for(Task t : tasks) {

			s.addTask(i % vehicles.length, t);
			i++;
		}
		return s;

	}

	private static Solution initialSolutionClassic(Vehicle[] vehicles, List<Task> tasks) {

		Solution s = new Solution(vehicles.length);

		for(Task t : tasks) {
			s.addTask(vehicles[0].id(), t);
		}

		return s;
	}

	private  static Solution naiveSolution(Vehicle vehicle, List<Task> tasks) {
		Solution s = new Solution(1);
		int vehicleId = vehicle.id();

		for(Task t : tasks) {
			s.addTask(vehicleId, t);
			//s.addTask(vehicleId, delivery);
		}

		return s;
	}

	private static Plan naivePlan(Vehicle vehicle, List<Task> tasks) {
		City current = vehicle.getCurrentCity();
		Plan plan = new Plan(current);

		for (Task task : tasks) {
			// move: current city => pickup location
			for (City city : current.pathTo(task.pickupCity)) {
				plan.appendMove(city);
			}

			plan.appendPickup(task);

			// move: pickup location => delivery location
			for (City city : task.path()) {
				plan.appendMove(city);
			}

			plan.appendDelivery(task);

			// set current city
			current = task.deliveryCity;
		}
		return plan;
	}

	/**
	 * -----------------------------------
	 * SLS OPERATORS 
	 * -----------------------------------
	 */

	private static Set<Solution> changeVehicleOperator(Solution oldSolution, Vehicle[] vehicles, int vi) {

		Random r = new Random();
		Set<Solution> solutions = new HashSet<Solution>();
		int nbVehicles = oldSolution.getNbVehicles();

		for(int vj = 0; vj < nbVehicles; vj++) {
			if(vi != vj) {
				Solution tmpSolution = oldSolution.clone();
				solutions.add(tmpSolution.swapVehicles(vi, vj, 0));    			
			}
		}

		return solutions;
	}

	private static Set<Solution> changeVehicleOperator2(Solution oldSolution, Vehicle[] vehicles, int vi) {

		Random r = new Random();
		Set<Solution> solutions = new HashSet<Solution>();
		int nbVehicles = oldSolution.getNbVehicles();

		int nbTasks = oldSolution.getNbTaskForVehicle(vi);

		for (int i = 0; i < nbTasks; i++) {
			for(int vj = 0; vj < nbVehicles; vj++) {
				if(vi != vj) {
					Solution tmpSolution = oldSolution.clone();
					solutions.add(tmpSolution.swapVehicles(vi, vj, i)); 			
				}
			}
		}
		return solutions;
	}

	/**
	 * This method is another operator that takes all tasks from a vehicule and assigns them to another vehicle.
	 * In the end we did not use it because we thought it was no 'local' enough.
	 */
	private static Set<Solution> takeOthersTask(Solution oldSolution, Vehicle[] vehicles, int vi) {

		Set<Solution> solutions = new HashSet<Solution>();
		int nbVehicles = oldSolution.getNbVehicles();

		for(int vj = 0; vj < nbVehicles; vj++) {
			if(vi != vj) {

				ExtendedTask t = oldSolution.getVehicleFirstTask(vj);
				Solution tmpSolution = oldSolution.clone();

				do {

					if( t != null) {
						tmpSolution.removTask(vj, t.getT());
						tmpSolution.addTask(vi, t.getT());
					}

					t = tmpSolution.getVehicleFirstTask(vj);

				} while(t != null);

				solutions.add(tmpSolution);
			}
		}
		return solutions;
	}

	private static Set<Solution> changeTaskOrderOperation(Solution oldSolution, int vi) {

		Random r = new Random();
		Set<Solution> solutions = new HashSet<Solution>();

		int length = oldSolution.getNbTaskForVehicle(vi);


		if(length >= 2) {
			for(int i = 0; i < length -1; i++) {
				for(int j = i + 1; j < length; j++) {
					Solution toPrint = oldSolution.swapTasks(vi, i, j);

					solutions.add(toPrint);
				}
			}
		}

		return solutions;
	}
}
