package aaathesis;

import java.util.Random;

import javax.print.attribute.standard.PrinterMakeAndModel;

public class Selection {

	private Selection() {
		
	}
	public static void elitism(Population pop) {
		
		
	}
	
	public static Population rouletteWheel(Population pop, String crossoverType, String mutationType, Object[] params) {
		MarkovChain parent1,parent2, toBeAdded;
		float random;
		int popSize = pop.getPopulationSize();
		Population newPop = new Population();
		Random r = new Random();
		float totalFitness = 0;
		for (MarkovChain mc : pop.getPopulation()) {
			totalFitness += mc.getfitness();
		}
		while (newPop.getPopulationSize() < popSize) {
			parent1 = new MarkovChain();
			parent2 = new MarkovChain();
			random = r.nextFloat() * totalFitness;
			int i;
			for(i =0 ; i<popSize && random>0; i++) {
				random -= pop.getMarkovChainAtIndex(i).getfitness();
			}
			int parentIndex = i==0 ? 0 : i-1;
			parent1 = pop.getMarkovChainAtIndex(parentIndex);
			random = r.nextFloat() * totalFitness;
			for(i =0 ; i<popSize && random>0; i++) {
				random -= pop.getMarkovChainAtIndex(i).getfitness();
			}
			parentIndex = i==0 ? 0 : i-1;
			parent2 = pop.getMarkovChainAtIndex(parentIndex);
			toBeAdded = doCrossover(crossoverType, parent1, parent2, params);
			doMutate(mutationType, toBeAdded);
			newPop.addMarkovChain(toBeAdded);
		}
		return newPop;
	}
	
	public static void steadyState(Population pop) {
		
	}
	
	private static MarkovChain doCrossover(String crossoverType, MarkovChain parent1, MarkovChain parent2, Object[] params) {
		MarkovChain mc = new MarkovChain();
		switch (crossoverType.toLowerCase()) {
		case "ox": {
			mc = Crossover.ox(parent1,parent2, params);
			break;
		}
		case "pmx": {
			mc =Crossover.pmx(parent1,parent2,params);
			break;
		}
		case "cx": {
			mc = Crossover.cx(parent1,parent2,params);
			break;
		}
		default:
			return null;
		}
		return mc;
	}
	
	private static void doMutate(String mutationType, MarkovChain toMutate)  {
		switch (mutationType.toLowerCase()) {
		case "one": {
			Mutation.oneChromosomeMutation(toMutate);
			break;
		}
		case "all": {
			Mutation.allChromosomesMutation(toMutate);
			break;
		}
		default:
			
		}
	}
}
