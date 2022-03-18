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

	
}
