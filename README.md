# IA_2015
Github Repository for all the projects related to the Intelligent Agents class of 2015

In this course, we had to design several types of intelligent agents to tackle the Pickup & Delivery problem.

## Implementing a Reactive Agent

In this exercise, we learned how to use a reactive agent to solve the Pickup and Delivery Problem. For that, we implemented a reinforcement learning algorithm (RLA) to compute an optimal strategy off-line. This strategy is then used by the agent to travel through the network.

We compute the best action to take in every state offline by Value Iteration. 

## Implementing a Deliberative Agent

In this exercise, we learned how to use a deliberative agent to solve the Pickup and Delivery Problem. A deliberative agent does not simply react to percepts coming from the environment. It can build a plan that specifies the sequence of actions to be taken in order to reach a certain goal. A deliberative agent has goals (e.g. to deliver all tasks) and is fully aware of the world it is acting in.


We implemented Breadth-first search algorithm as well as state-based A* search algorithm.

## Implementing a Centralized Agent

The simplest form of coordination is centralized coordination, in which one entity (e.g. the logistics company in our case) instructs the agents how to act. In our problem centralized coordination means that the company builds a plan for delivering all the packages with the available vehicles, and then communicates the respective parts of the plan to each vehicle. The vehicles simply execute the plans that were given to them.

Here we expressed the problem as a Constraint Satisfaction Problem (CSP) and solved using techniques tailored to this class of problems, such as Stochastic Local Search (SLS).

## Implementing a Decentralized Agent

Centralized coordination is guaranteed to produce the optimal plan (globally or locally) for a multi-agent PDP problem, when :
– All tasks are known in advance (they were until now, in our setting)
– The company has complete information about the parameters of its vehicles
– The vehicles blindly follow the orders of the company
Unfortunately, these conditions are not always met in real life applications. Most often vehicles or group of vehicles (i.e. companies) are self-interested and might not be willing
to obey a central planner. This may happen for various  reasons : for example, the central planning might be unfair, or the agent might not wish to reveal the true information
about its state. One intuitive solution is to allow the agents to negotiate and distribute the transportation
tasks among themselves, such that they coordinate their actions in a decentralized fashion. A market is thus created, where the tasks are "sold" to the agent that is most willing to take them. The competition usually leads to an efficient delivery solution.


