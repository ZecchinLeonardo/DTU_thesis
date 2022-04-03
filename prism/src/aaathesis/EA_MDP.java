package aaathesis;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import explicit.DTMC;
import explicit.DTMCFromMDPAndMDStrategy;
import explicit.DTMCModelChecker;
import explicit.MDP;
import parser.State;
import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import prism.Prism;
import prism.PrismDevNullLog;
import prism.PrismException;
import prism.PrismLog;
import prism.Result;
import simulator.SimulatorEngine;
import strat.MDStrategy;
import strat.MDStrategyArray;
import java.io.ByteArrayInputStream;

public class EA_MDP {
	public static void main(String [] args) {
		new EA_MDP().run();
	}

	private void run() {
		final int POP_SIZE = 10;
		final int GEN_SIZE = 100;
		try {
			//MarkovChain mc = new MarkovChain();
			// Create a log for PRISM output (hidden or stdout)
			PrismLog mainLog = new PrismDevNullLog();
			//PrismLog mainLog = new PrismFileLog("stdout");

			// Initialise PRISM engine 
			Prism prism = new Prism(mainLog);
			
			prism.initialise();
			prism.setEngine(Prism.EXPLICIT);
			// Parse and load a PRISM model (an MDP) from a file
			ModulesFile modulesFile = prism.parseModelFile(new File("examples/robot.prism"));
			prism.loadPRISMModel(modulesFile);
			prism.buildModel();
			MDP mdp = (MDP) prism.getBuiltModelExplicit();
			System.out.println(mdp);
			int[] nChoices = new int[mdp.getStatesList().size()];
			Population p = new Population();
			for (int popCount = 0; popCount<POP_SIZE; popCount++) {
				MarkovChain mc = new MarkovChain();
				for(int i = 0; i < mdp.getStatesList().size(); i++) {
					float chromosome = MarkovChain.getRandomChromosome();
					mc.addChromosome(chromosome);
					//save num choices for each state
					nChoices[i] = mdp.getNumChoices(i);
					//System.out.print(nChoices[i] +" ");
				}
				//System.out.println();
				p.addMarkovChain(mc);
				
			}
			
			//model checking on each dtmc in population
			for(int i = 0; i<p.getPopulationSize(); i++) {
				int[] stratArray = UnityDistribution.getStrategyFromMarkovChain(p.getMarkovChainAtIndex(i), nChoices);
				MDStrategy strat = new MDStrategyArray(mdp, stratArray);
				DTMC dtmc = new DTMCFromMDPAndMDStrategy(mdp, strat);
				
				DTMCModelChecker mc = new DTMCModelChecker(prism);
				
				PropertiesFile pf = prism.parsePropertiesString("P=?[F \"goal1\"]");
				Result r = mc.check(dtmc, pf.getProperty(0));
				p.getMarkovChainAtIndex(i).setFitness((float)((double) r.getResult()));
				/*
				System.out.println("DTMC at index " + i + " has fitness of " + r.getResult()+" with choice array: ");
				for(int j=0;j<dtmc.getStatesList().size();j++) {
					System.out.print(stratArray[j]+ " ");
				}
				System.out.println();
				*/
			}
			System.out.println("INITIAL POPULATION");
			for(int i = 0; i<p.getPopulationSize();i++) {
				System.out.println(p.getMarkovChainAtIndex(i));
			}
			System.out.println("//////////////////////////////////");
			
			Object[] params = {1,3};
			for(int gen = 0; gen<GEN_SIZE; gen++) {
				p = Selection.rouletteWheel(p, "ox", "one", params);
				
				for(int i = 0; i<p.getPopulationSize(); i++) {
					int[] stratArray = UnityDistribution.getStrategyFromMarkovChain(p.getMarkovChainAtIndex(i), nChoices);
					MDStrategy strat = new MDStrategyArray(mdp, stratArray);
					DTMC dtmc = new DTMCFromMDPAndMDStrategy(mdp, strat);
					
					DTMCModelChecker mc = new DTMCModelChecker(prism);
					
					PropertiesFile pf = prism.parsePropertiesString("P=?[F \"goal1\"]");
					Result r = mc.check(dtmc, pf.getProperty(0));
					p.getMarkovChainAtIndex(i).setFitness((float)((double) r.getResult()));
					
				}
			}
			
			for(int i = 0; i<p.getPopulationSize();i++) {
				System.out.println("DTMC at index " + i + " has fitness of " + p.getMarkovChainAtIndex(i).getfitness()+" with choice array: ");
				System.out.println(p.getMarkovChainAtIndex(i));
			}
			
			//MODEL CHECKING ON DTMC
			/*
			MDStrategy strat = new MDStrategyArray(mdp, stratArray);
			DTMC dtmc = new DTMCFromMDPAndMDStrategy(mdp, strat);
			
			DTMCModelChecker mc = new DTMCModelChecker(prism);
			
			PropertiesFile pf = prism.parsePropertiesString("P=?[F \"goal1\"]");
			Result r = mc.check(dtmc, pf.getProperty(0));
			System.out.println(r.getResult());
			*/

			prism.closeDown();
		} catch (PrismException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		
	}

}
