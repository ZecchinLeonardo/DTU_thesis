package thesis;

import java.util.Random;

import javax.print.attribute.standard.PrinterMakeAndModel;

public class Selection {

	private Selection() {
		
	}
	public static Population steadyState(Population pop, String crossoverType, String mutationType, Object[] params, boolean maximization) {
		float avgFitness = pop.getAvgFitness();
		Random r = new Random();
		MarkovChain parent1,parent2, toBeAdded;
		Population newPop = new Population();
		Population elites = new Population();
		for(int i = 0; i<pop.getPopulationSize();i++) {
			if(maximization) {
				if (pop.getMarkovChainAtIndex(i).getfitness()>=avgFitness) {
					newPop.addMarkovChain(pop.getMarkovChainAtIndex(i));
					elites.addMarkovChain(pop.getMarkovChainAtIndex(i));
				}
			} else {
				if (pop.getMarkovChainAtIndex(i).getfitness()<=avgFitness) {
					newPop.addMarkovChain(pop.getMarkovChainAtIndex(i));
					elites.addMarkovChain(pop.getMarkovChainAtIndex(i));
				}
			}
		}
		while (newPop.getPopulationSize() < pop.getPopulationSize()) {
			if(elites.getPopulationSize()!=0) {
				parent1 = elites.getMarkovChainAtIndex(r.nextInt(elites.getPopulationSize()));
				parent2 = elites.getMarkovChainAtIndex(r.nextInt(elites.getPopulationSize()));	
			} else {
				parent1 = pop.getMarkovChainAtIndex(r.nextInt(pop.getPopulationSize()));
				parent2 = pop.getMarkovChainAtIndex(r.nextInt(pop.getPopulationSize()));	
			}
			toBeAdded = doCrossover(crossoverType, parent1, parent2, params);
			if(r.nextFloat()<0.5) {
				toBeAdded.setFitness(parent1.getfitness());
			} else {
				toBeAdded.setFitness(parent2.getfitness());
			}
			try {
				if (params[2] != null) {
					doMutate(mutationType, toBeAdded, (Float)params[2], maximization);
				}
			} catch(IndexOutOfBoundsException oobEx) {
			}
			newPop.addMarkovChain(toBeAdded);
		}
		return newPop;
	}
	
	public static Population rouletteWheel(Population pop, String crossoverType, String mutationType, Object[] params, boolean maximization) {
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
			//System.out.println("Roulette " + parent1.getfitness() + " " + parent2.getfitness());
			toBeAdded = doCrossover(crossoverType, parent1, parent2, params);
			try {
				if (params[2] != null) {
					doMutate(mutationType, toBeAdded, (Float)params[2], maximization);
				}
			} catch(IndexOutOfBoundsException oobEx) {
			}
			
			newPop.addMarkovChain(toBeAdded);
		}
		return newPop;
	}
	
	public static Population doSelection(String selectionType, String crossoverType, String mutationType, Population pop, Object[] params, boolean maximization) {
		Population p = null;
		switch (selectionType) {
		case "roulette": {
			p = rouletteWheel(pop, crossoverType, mutationType, params, maximization);
			break;
		}
		case "steadystate": {
			p = steadyState(pop, crossoverType, mutationType, params, maximization);
			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + selectionType);
		}
		return p;
	}
	
	private static MarkovChain doCrossover(String crossoverType, MarkovChain parent1, MarkovChain parent2, Object[] params) {
		MarkovChain mc = new MarkovChain();
		switch (crossoverType.toLowerCase()) {
		case "ox": {
			mc = Crossover.ox(parent1,parent2, params);
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
	
	private static void doMutate(String mutationType, MarkovChain toMutate, float avgFitness, boolean maximization)  {
		switch (mutationType.toLowerCase()) {
		case "one": {
			Mutation.oneChromosomeMutation(toMutate);
			break;
		}
		case "all": {
			Mutation.allChromosomesMutation(toMutate);
			break;
		}
		case "adaptive": {
			Mutation.adaptiveMutation(toMutate, avgFitness,maximization);
			break;
		}
		default:
			
		}
	}
}
