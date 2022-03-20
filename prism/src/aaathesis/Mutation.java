package aaathesis;

import java.util.Random;

public class Mutation {
	private Mutation() {
		
	}
	
	private static float getShrinkingFactor() {
		Random r = new Random();
		return r.nextFloat(1f);
	}
	
	public static void oneChromosomeMutation(MarkovChain mc) {
		Random r = new Random();
		int index = r.nextInt(mc.getSize());
		System.out.println("mutating chromosome at index "+ index);
		mc.setChromosomeAtIndex(index, (mc.getChromosomeAtIndex(index)+getShrinkingFactor())%1f);
	}
	
	public static void allChromosomesMutation(MarkovChain mc) {
		
	}
	
	public static void adaptiveMutation(MarkovChain mc) {
		
	}
}
