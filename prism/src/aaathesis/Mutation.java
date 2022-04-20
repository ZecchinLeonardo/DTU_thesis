package aaathesis;

import java.util.Random;

public class Mutation {
	
	private static final float HQ_MUTATION_RATE = 0.1f;
	private static final float LQ_MUTATION_RATE = 0.75f;
	
	private Mutation() {
		
	}
	
	private static float getShrinkingFactor() {
		Random r = new Random();
		return r.nextFloat(1f);
	}
	
	public static void oneChromosomeMutation(MarkovChain mc) {
		Random r = new Random();
		int index = r.nextInt(mc.getSize());
		//System.out.println("mutating chromosome at index "+ index);
		mc.setChromosomeAtIndex(index, (mc.getChromosomeAtIndex(index)+getShrinkingFactor())%1f);
	}
	
	public static void allChromosomesMutation(MarkovChain mc) {
		for (int i = 0; i<mc.getSize();i++) {
			mc.setChromosomeAtIndex(i, (mc.getChromosomeAtIndex(i)+getShrinkingFactor())%1f);
		}
	}
	
	public static void allGenesMutationWithProbability(MarkovChain mc, float probability) {
		Random r = new Random();
		for (int i = 0; i<mc.getSize();i++) {
			if (r.nextFloat()<probability)
				mc.setChromosomeAtIndex(i, (mc.getChromosomeAtIndex(i)+getShrinkingFactor())%1f);
		}
	}
	
	public static void adaptiveMutation(MarkovChain mc, float avgFitness) {
		if(mc.getfitness()>=avgFitness) { 
			allGenesMutationWithProbability(mc, HQ_MUTATION_RATE);
		} else {
			allGenesMutationWithProbability(mc, LQ_MUTATION_RATE);
		}
	}
}
