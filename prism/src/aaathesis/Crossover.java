package aaathesis;

import java.util.ArrayList;

public class Crossover {
	private Crossover() {
		
	}
	
	public static MarkovChain ox(MarkovChain parent1, MarkovChain parent2, Object[] params) {
		try {
			int index1 =((int) params[0]);
			int index2 =((int) params[1]);
			if (index2 < index1) {
				throw new Exception();
			}
			MarkovChain crossover = new MarkovChain();
			ArrayList<Float> al1 = new ArrayList<Float>();
			ArrayList<Float> al2 = new ArrayList<Float>();
			for (int i = index1; i <= index2; i++) {
				al1.add(parent1.getChromosomeAtIndex(i));
			}
			for(Float chromosome : parent2) {
				if(!al1.contains(chromosome)) {
					al2.add(chromosome);
				}
			}
			int currentSize = 0, j=0,k=0;
			while (currentSize<parent1.getSize()) {
				if(currentSize>=index1 && currentSize<=index2) {
					crossover.addChromosome(al1.get(j));
					j++;
				} else {
					crossover.addChromosome(al2.get(k));
					k++;
				}
				currentSize++;
			}
			return crossover;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static MarkovChain pmx(MarkovChain parent1, MarkovChain parent2, Object[] params) {
		return null;
	}
	
	public static MarkovChain cx(MarkovChain parent1, MarkovChain parent2, Object[] params) {
		return null;
	}
}
