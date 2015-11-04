package template;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Solution {

	/* takes vehicle id as index */
	private ExtendedTask[] vehicleTasks;
	
	/* takes a task id as index */
	private ExtendedTask[] nextTasks;
	
	private HashMap<Integer, List<ExtendedTask>> solution;

	public Solution(int nbVehicles) {
		super();
		for(int i = 0; i < nbVehicles; i++) {
			solution.put(i, new LinkedList<ExtendedTask>());
		}
	}
	
	public void addTask(int vehicleId, ExtendedTask t) {
		solution.get(vehicleId).add(t);
	}
	
	public boolean isValid() {
		return true;
	}
	
	
}
