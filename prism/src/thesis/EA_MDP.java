package thesis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

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
		//uncomment this to generate all markov chains
		//new AllMCGenerator().run();
	}

	private void run() {
		final int POP_SIZE = 10;
		final int GEN_SIZE = 100;
		final boolean MAXIMIZATION = true;
//		final float EPSILON=0.00001f;
		final int RUNS=100;
		
		ArrayList<Long> times = new ArrayList<Long>();
		ArrayList<Float> finalFitnesses = new ArrayList<Float>();
		String fileName = "examples/mdp_h3_c2_t2_tree.prism";
		String fitnessesAndTimesFile = "results2/output_"+fileName.split("[./]")[1]+"_pop"+POP_SIZE+"_gen"+GEN_SIZE+"_runs"+RUNS+".csv";
		try {
			PrintWriter fw = new PrintWriter("results2/output_"+fileName.split("[./]")[1]+"_pop"+POP_SIZE+"_gen"+GEN_SIZE+".txt");
			PrintWriter fw2 = new PrintWriter("results2/output_max_fitnesses_"+fileName.split("[./]")[1]+"_pop"+POP_SIZE+"_gen"+GEN_SIZE+".txt");
			ArrayList<Float> fitnesses = new ArrayList<Float>();
			ArrayList<Float> maxFitnesses = new ArrayList<Float>();
			
			// Create a log for PRISM output (hidden or stdout)
			PrismLog mainLog = new PrismDevNullLog();
			//PrismLog mainLog = new PrismFileLog("stdout");

			// Initialise PRISM engine 
			Prism prism = new Prism(mainLog);
			prism.initialise();
			prism.setEngine(Prism.EXPLICIT);
			// Parse and load a PRISM model (an MDP) from a file
			ModulesFile modulesFile = prism.parseModelFile(new File(fileName));
			prism.loadPRISMModel(modulesFile);
			System.out.println("building model");
			prism.buildModel();
			System.out.println("model built");
			MDP mdp = (MDP) prism.getBuiltModelExplicit();
		
			for(int run = 0; run<RUNS; run++) {
				MarkovChain best = new MarkovChain();
				best.setFitness(MAXIMIZATION ? -1 : 2);
					//System.out.println(mdp);
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
					float maxFitness = MAXIMIZATION ? -1f : 2f;
					Long startTime=System.nanoTime();
					for(int i = 0; i<p.getPopulationSize(); i++) {
						int[] stratArray = UnityDistribution.getStrategyFromMarkovChain(p.getMarkovChainAtIndex(i), nChoices);
						MDStrategy strat = new MDStrategyArray(mdp, stratArray);
						DTMC dtmc = new DTMCFromMDPAndMDStrategy(mdp, strat);
						DTMCModelChecker mc = new DTMCModelChecker(prism);
						
						PropertiesFile pf = prism.parsePropertiesString("Pmax=?[F \"goal1\"]");
						//Long mcStart = System.nanoTime();
						Result r;
						try {
							r = mc.check(dtmc, pf.getProperty(0));
						} catch (Exception e) {
							r = new Result(0d);
						}
						//System.out.println((System.nanoTime()-mcStart)/1000000);
						p.getMarkovChainAtIndex(i).setFitness((float)((double)r.getResult()));
						avgFitness+=p.getMarkovChainAtIndex(i).getfitness();
						if(MAXIMIZATION) {
							if(p.getMarkovChainAtIndex(i).getfitness()==1) {
								break;
							}
							if(p.getMarkovChainAtIndex(i).getfitness()>maxFitness) {
								maxFitness = p.getMarkovChainAtIndex(i).getfitness();
							}
						}else {
							if(p.getMarkovChainAtIndex(i).getfitness()==0) {
								break;
							}
							if(p.getMarkovChainAtIndex(i).getfitness()<maxFitness) {
								maxFitness = p.getMarkovChainAtIndex(i).getfitness();
							}
						}
						/*
						System.out.println("DTMC at index " + i + " has fitness of " + r.getResult()+" with choice array: ");
						for(int j=0;j<dtmc.getStatesList().size();j++) {
							System.out.print(stratArray[j]+ " ");
						}
						System.out.println();
						*/
					}
					 
					fitnesses.add(avgFitness);
					maxFitnesses.add(maxFitness);				
					/*
					System.out.println("INITIAL POPULATION");
					for(int i = 0; i<p.getPopulationSize();i++) {
						System.out.println(p.getMarkovChainAtIndex(i));
					}
					System.out.println("//////////////////////////////////");
					*/
					
					Object[] params = new Object[3];
					params[0] = (int)p.getMarkovChainAtIndex(0).getSize()/10;
					params[1] = (int)(9*p.getMarkovChainAtIndex(0).getSize()/10);
					int gen = 0, z = 0;
					boolean end = false;
					if(MAXIMIZATION) {
						if(maxFitnesses.get(0)==1f) {
							end = true;
						}
					} else {
						if(maxFitnesses.get(0)==0f) {
							end=true;
						}
					}
					maxFitness=0;
					float lastFitness=0;
					for(gen = 0; gen<GEN_SIZE && end == false; gen++) {
						avgFitness /= POP_SIZE;
						params[2] = avgFitness;
						//System.out.println("avg fitness " + avgFitness +" gen " +gen);
						fitnesses.add(avgFitness);
						//EA TYPE
						//p = Selection.rouletteWheel(p, "cx", "adaptive", params);
						//p = Selection.elitism(p, "cx", "adaptive", params);						
						p = Selection.doSelection("steadystate", "cx", "adaptive", p, params, MAXIMIZATION);
						avgFitness = 0;
						maxFitness = MAXIMIZATION ? -1f : 2f;
						for(z = 0; z<p.getPopulationSize(); z++) {
							//System.out.println(p.getMarkovChainAtIndex(z));
							int[] stratArray = UnityDistribution.getStrategyFromMarkovChain(p.getMarkovChainAtIndex(z), nChoices);
							MDStrategy strat = new MDStrategyArray(mdp, stratArray);
							DTMC dtmc = new DTMCFromMDPAndMDStrategy(mdp, strat);
							
							DTMCModelChecker mc = new DTMCModelChecker(prism);
							
							PropertiesFile pf = prism.parsePropertiesString("Pmax=?[F \"goal1\"]");
							Result r;
							try {
								r = mc.check(dtmc, pf.getProperty(0));
							}catch (Exception e) {
								//export mc
								r = new Result(0d);
							}
							p.getMarkovChainAtIndex(z).setFitness((float)((double) r.getResult()));
							MarkovChain currMC = p.getMarkovChainAtIndex(z);
							avgFitness+= currMC.getfitness();
							if(MAXIMIZATION) {
								if(best.getfitness()<currMC.getfitness()) {
									best = currMC;
								}
								if(maxFitness<currMC.getfitness()) {
									maxFitness=currMC.getfitness();
								}
								if(currMC.getfitness() == 1f) end = true;
							}else {
								if(best.getfitness()>currMC.getfitness()) {
									best = currMC;
								}
								if(maxFitness>currMC.getfitness()) {
									maxFitness=currMC.getfitness();
								}
								if(currMC.getfitness() == 0f) end = true;
							}
						}
						maxFitnesses.add(maxFitness);
						//System.out.println(lastFitness +" " + avgFitness);
						//undivided so technically not avg yet
						/*
						if(Math.abs(lastFitness-avgFitness) < EPSILON && gen!=0) {
							break;
						}
						*/
						//System.out.println("max fitness: " + maxFitness);
						lastFitness = avgFitness;
					}
					times.add((System.nanoTime()-startTime)/1000000);
					System.out.println("Execution time: "+((System.nanoTime()-startTime)/1000000)+"ms");
					/*
					if(gen<GEN_SIZE) {
						if(Math.abs(lastFitness-maxFitness) < EPSILON) {
							System.out.println("CONVERGED AFTER "+gen+ " GENS");
						} else {
							System.out.println("STRATEGY FOUND AT INDEX " + z + " AFTER " + gen + " GENS");
		
						}
					}
					*/
					
					/*
					for(int i = 0; i<p.getPopulationSize();i++) {
						System.out.println("DTMC at index " + i + " has fitness of " + p.getMarkovChainAtIndex(i).getfitness()+" with choice array: ");
						System.out.println(p.getMarkovChainAtIndex(i));
					}
					*/
					
					if(MAXIMIZATION) {
						System.out.println("DTMC with max fitness is DTMC: "+best);
						System.out.println("Fitness "+best.getfitness());
						finalFitnesses.add((float)p.getMaxFitness()[1]);
					} else {
						System.out.println("DTMC with min fitness is DTMC: "+best);
						System.out.println("Fitness "+best.getfitness());
						finalFitnesses.add((float)p.getMinFitness()[1]);
					}
					
					//final fitness
					fitnesses.add(p.getAvgFitness());
					
					//OUTPUT FILE WRITING
					
					for(Float f : fitnesses) { 
						fw.println(f);
					}
					for(Float f : maxFitnesses) {
						fw2.println(f);
					}
					fw.close();
					fw2.close();
					
		
					prism.closeDown();
				
			}
		} catch (PrismException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			PrintWriter pw = new PrintWriter(fitnessesAndTimesFile);
			float avgTime =0, avgFitness=0;
			for(int i =0; i<times.size();i++) {
				if(i!=0) {
					avgTime+=times.get(i);
					avgFitness+=finalFitnesses.get(i);
					pw.println(finalFitnesses.get(i)+","+times.get(i));
				}
			}
			avgFitness/=finalFitnesses.size()-1;
			avgTime/=times.size()-1;
			pw.println(avgFitness+","+avgTime);
			System.out.println(avgFitness+","+avgTime);
			pw.close();
		}catch(Exception e) {e.printStackTrace();}
	}

}
