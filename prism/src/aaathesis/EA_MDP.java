package aaathesis;

import java.beans.Expression;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import parser.ast.ExpressionProp;
import parser.ast.ModulesFile;
import prism.Prism;
import prism.PrismDevNullLog;
import prism.PrismException;
import prism.PrismLog;
import simulator.SimulatorEngine;
import strat.Strategy;

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
				int toBePicked=UnityDistribution.getTransitionFromChromosomeValue(chromosome, sim.getNumTransitions());
				//System.out.println(toBePicked);
				sim.manualTransition(toBePicked);
			}
			System.out.println(sim.getPath());	
			String check = "Pmax=?[F \"goal1\"]";
			ExpressionProp e = new ExpressionProp(check);
			sim.checkPropertyForSimulation(e);
			
			
			prism.closeDown();
		} catch (PrismException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		
	}
}
