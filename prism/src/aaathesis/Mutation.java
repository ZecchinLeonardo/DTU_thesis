package aaathesis;

import java.util.Random;

public class Mutation {
	private Mutation() {
		
	}
	
	private void uniformShrink(MarkovChain mc) {
		Random r = new Random();
		for(float chromosome : mc) {
			float shrinkFactor = r.nextFloat();
			chromosome= (chromosome+shrinkFactor)%1f;
		}
	}
	
	public static void oneChromosomeMutation(MarkovChain mc) {
		
	}
	
	public static void allChromosomesMutation(MarkovChain mc) {
		
	}
	
	public static void adaptiveMutation(MarkovChain mc) {
		
	}
}
