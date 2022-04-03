package aaathesis;

import java.util.Random;

public class UnityDistribution {
	private UnityDistribution() {
		
	}
	
	public static int getTransitionFromChromosomeValue(float chromosomeValue, int nTransitions) {
		float intervalLength = 1f/nTransitions;
		int count=0;
		if(chromosomeValue==1f) {
			count--;
		}
		while(chromosomeValue>=intervalLength) {
			chromosomeValue-=intervalLength;
			count++;
		}
		return count;
	}
	
	public static int[] getStrategyFromMarkovChain(MarkovChain mc, int[] nChoices) {
		int[] strat = new int[mc.getSize()];
		int i = 0;
		for(Float chromosome : mc) {
			strat[i] = getTransitionFromChromosomeValue(chromosome, nChoices[i]);
			i++;
		}
		return strat;
	}
	
}
