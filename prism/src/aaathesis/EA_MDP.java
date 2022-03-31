package aaathesis;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import parser.State;
import parser.ast.ModulesFile;
import prism.Prism;
import prism.PrismDevNullLog;
import prism.PrismException;
import prism.PrismLog;
import simulator.SimulatorEngine;

public class EA_MDP {
	public static void main(String [] args) {
		new EA_MDP().run();
	}

	private void run() {
		
		try {
			MarkovChain mc = new MarkovChain();
			// Create a log for PRISM output (hidden or stdout)
			PrismLog mainLog = new PrismDevNullLog();
			//PrismLog mainLog = new PrismFileLog("stdout");

			// Initialise PRISM engine 
			Prism prism = new Prism(mainLog);
			
			prism.initialise();
			
			// Parse and load a PRISM model (an MDP) from a file
			ModulesFile modulesFile = prism.parseModelFile(new File("examples/robot.prism"));
			prism.loadPRISMModel(modulesFile);
			
			// Load the model into the simulator
			prism.loadModelIntoSimulator();
			SimulatorEngine sim = prism.getSimulator();
			for (int i = 0; i < 5; i++) {
				mc.addChromosome(MarkovChain.getRandomChromosome());
			}
			System.out.println(mc);
			sim.createNewPath();
			sim.initialisePath(null);
			for (Float chromosome : mc) {
				int toBePicked=UnityDistribution.getTransitionFromChromosomeValue(chromosome, sim.getNumChoices());
				//System.out.println(toBePicked);
				sim.automaticTransitionWithinChoice(toBePicked);
			}
			System.out.println(sim.getPathFull());
			//String exp = "Pmax=?[F \"goal1\"]";
			//System.out.println(prism.modelCheck(exp));
			
			//test();
			test2();
			//prism.closeDown();
		} catch (PrismException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		
	}
	private void test() {
		Population pop = new Population();
		MarkovChain mc1, mc2,mc3;
		mc1 = new MarkovChain();
		mc2 = new MarkovChain();
		mc3 = new MarkovChain();
		for (int i = 0; i < 5; i++) {
			mc1.addChromosome(MarkovChain.getRandomChromosome());
			mc2.addChromosome(MarkovChain.getRandomChromosome());
			mc3.addChromosome(MarkovChain.getRandomChromosome());
		}
		mc1.setFitness(0.5f);
		mc2.setFitness(0.1f);
		mc3.setFitness(0.2f);
		pop.addMarkovChain(mc1);
		pop.addMarkovChain(mc2);
		pop.addMarkovChain(mc3);
		Object[] params = {2,3};
		Population resultPop = Selection.rouletteWheel(pop, "ox","one", params);
		System.out.println(resultPop);
	}
	
	private void test2() {
		try {
			MarkovChain mc = new MarkovChain();
			// Create a log for PRISM output (hidden or stdout)
			PrismLog mainLog = new PrismDevNullLog();
			//PrismLog mainLog = new PrismFileLog("stdout");

			// Initialise PRISM engine 
			Prism prism = new Prism(mainLog);
			
			prism.initialise();
			
			// Parse and load a PRISM model (an MDP) from a file
			ModulesFile modulesFile = prism.parseModelFile(new File("examples/robot.prism"));
			prism.loadPRISMModel(modulesFile);
			
			// Load the model into the simulator
			prism.loadModelIntoSimulator();
			SimulatorEngine sim = prism.getSimulator();
			for (int i = 0; i < 5; i++) {
				mc.addChromosome(MarkovChain.getRandomChromosome());
			}
			System.out.println(mc);
			sim.createNewPath();
			sim.initialisePath(null);
			CustomDTMC dtmc = new CustomDTMC();
			String actionName;
			for (Float chromosome : mc) {
				ArrayList<State> destinations = new ArrayList<State>(); 
				ArrayList<Double> probabilities = new ArrayList<Double>(); 
				int toBePicked=UnityDistribution.getTransitionFromChromosomeValue(chromosome, sim.getNumChoices());
				for(int i = 0; i<sim.getNumTransitions(toBePicked);i++) {
					destinations.add(sim.computeTransitionTarget(toBePicked, i));
					probabilities.add(sim.getTransitionProbability(toBePicked, i));
				}
				int x = sim.getNumTransitions(toBePicked);
				dtmc.addData(sim.getCurrentState(), destinations, sim.getNumTransitions(toBePicked), probabilities);
				//System.out.println(toBePicked);
				actionName = sim.automaticTransitionWithinChoice(toBePicked);
				dtmc.addChoice(actionName);
				System.out.println("test " +dtmc.getNumTransitions(0));
				dtmc.goNext();
			
			}
			System.out.println(sim.getPathFull());
			Prism exportPrism = new Prism(mainLog);
			exportPrism.initialise();
			exportPrism.loadModelGenerator(dtmc);
			//ModulesFile
			//exportPrism.loadPRISMModel(mf);
			exportPrism.exportPRISMModel(new File("test.prism"));
			//exportPrism.exportTransToFile(true, Prism.EXPORT_DOT_STATES, new File("transTest.dot"));
			exportPrism.closeDown();
			//String exp = "Pmax=?[F \"goal1\"]";
			//System.out.println(prism.modelCheck(exp));
		} catch (PrismException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}
