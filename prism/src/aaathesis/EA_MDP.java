package aaathesis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import explicit.DTMC;
import explicit.DTMCFromMDPAndMDStrategy;
import explicit.DTMCModelChecker;
import explicit.MDP;
import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import prism.Prism;
import prism.PrismDevNullLog;
import prism.PrismException;
import prism.PrismLog;
import prism.Result;
import strat.MDStrategy;
import strat.MDStrategyArray;

public class EA_MDP {
	public static void main(String [] args) {
		new EA_MDP().run();
	}

	private void run() {
		final int POP_SIZE = 50;
		final int GEN_SIZE = 100;
		
		try {
			PrintWriter fw = new PrintWriter("output.txt");
			ArrayList<Float> fitnesses = new ArrayList<Float>();
			//MarkovChain mc = new MarkovChain();
			// Create a log for PRISM output (hidden or stdout)
			PrismLog mainLog = new PrismDevNullLog();
			//PrismLog mainLog = new PrismFileLog("stdout");

			// Initialise PRISM engine 
			Prism prism = new Prism(mainLog);
			
			prism.initialise();
			prism.setEngine(Prism.EXPLICIT);
			// Parse and load a PRISM model (an MDP) from a file
			ModulesFile modulesFile = prism.parseModelFile(new File("examples/mdp_s50_c7_t5.prism"));
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
			float avgFitness = 0;
			for(int i = 0; i<p.getPopulationSize(); i++) {
				int[] stratArray = UnityDistribution.getStrategyFromMarkovChain(p.getMarkovChainAtIndex(i), nChoices);
				MDStrategy strat = new MDStrategyArray(mdp, stratArray);
				DTMC dtmc = new DTMCFromMDPAndMDStrategy(mdp, strat);
				
				DTMCModelChecker mc = new DTMCModelChecker(prism);
				
				PropertiesFile pf = prism.parsePropertiesString("P=?[F \"goal1\"]");
				Result r = mc.check(dtmc, pf.getProperty(0));
				p.getMarkovChainAtIndex(i).setFitness((float)((double) r.getResult()));
				avgFitness+=p.getMarkovChainAtIndex(i).getfitness();
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
			
			Object[] params = new Object[3];
			params[0] = 1;
			params[1] = 3;
			int gen = 0, z = 0;
			boolean end = false;
			for(gen = 0; gen<GEN_SIZE && end == false; gen++) {
				avgFitness /= POP_SIZE;
				params[2] = avgFitness;
				System.out.println("avg fitness " + avgFitness);
				fitnesses.add(avgFitness);
				//EA TYPE
				//p = Selection.rouletteWheel(p, "cx", "adaptive", params);
				//p = Selection.elitism(p, "cx", "adaptive", params);
				p = Selection.doSelection("elitism", "cx", "adaptive", p, params);
				avgFitness = 0;
				for(z = 0; z<p.getPopulationSize(); z++) {
					int[] stratArray = UnityDistribution.getStrategyFromMarkovChain(p.getMarkovChainAtIndex(z), nChoices);
					MDStrategy strat = new MDStrategyArray(mdp, stratArray);
					DTMC dtmc = new DTMCFromMDPAndMDStrategy(mdp, strat);
					
					DTMCModelChecker mc = new DTMCModelChecker(prism);
					
					PropertiesFile pf = prism.parsePropertiesString("P=?[F \"goal1\"]");
					Result r = mc.check(dtmc, pf.getProperty(0));
					p.getMarkovChainAtIndex(z).setFitness((float)((double) r.getResult()));
					avgFitness+= p.getMarkovChainAtIndex(z).getfitness();
					if(p.getMarkovChainAtIndex(z).getfitness() == 1f) end = true;
				}
			}
			
			if(gen<GEN_SIZE) {
				System.out.println("STRATEGY FOUND AT INDEX " + z + " AFTER " + gen + " GENS");
			}
			
			for(int i = 0; i<p.getPopulationSize();i++) {
				System.out.println("DTMC at index " + i + " has fitness of " + p.getMarkovChainAtIndex(i).getfitness()+" with choice array: ");
				System.out.println(p.getMarkovChainAtIndex(i));
			}
			
			Object[] best = p.getMaxFitness();
			System.out.println("DTMC with max fitness is DTMC: "+p.getMarkovChainAtIndex((int)best[0]) +" at index " + best[0]);
			
			//final fitness
			fitnesses.add(p.getAvgFitness());
			
			//OUTPUT FILE WRITING
			/*
			for(Float f : fitnesses) { 
				fw.println(f);
			}
			fw.close();
			*/

			prism.closeDown();
		} catch (PrismException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		
	}

}
