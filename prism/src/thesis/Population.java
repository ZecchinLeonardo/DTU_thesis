package thesis;

import java.util.ArrayList;

public class Population {
	private ArrayList<MarkovChain> pop;
	
	public Population() {
		pop = new ArrayList<MarkovChain>();
	}
	
	public void addMarkovChain(MarkovChain mc) {
		this.pop.add(mc);
	}
	
	public boolean removeMarkovChain(MarkovChain mc) {
		return pop.remove(mc);
	}
	
	public MarkovChain getMarkovChainAtIndex(int index) {
		return this.pop.get(index);
	}
	
	public ArrayList<MarkovChain> getPopulation() {
		return this.pop;
	}
	
	public int getPopulationSize() {
		return this.pop.size();
	}
	
	public Object[] getMaxFitness() {
		float max = -1f;
		int index = -1;
		for (int i = 0; i<pop.size(); i++) {
			if(pop.get(i).getfitness()>max) {
				max = pop.get(i).getfitness();
				index = i;
			}
		}
		Object[] output = {index, max};
		return output;
	}
	
	public Object[] getMinFitness() {
		float min = 2f;
		int index = -1;
		for (int i = 0; i<pop.size(); i++) {
			if(pop.get(i).getfitness()<min) {
				min = pop.get(i).getfitness();
				index = i;
			}
		}
		Object[] output = {index, min};
		return output;
	}
	
	public float getAvgFitness() {
		float avg = 0;
		for (MarkovChain mc : pop) {
			avg += mc.getfitness();
		}
		return (avg/pop.size());
	}
	
	public String toString() {
		String tbr = "";
		for (MarkovChain mc : pop) {
			tbr = tbr + mc.toString() + "\n";
		}
		return tbr;
	}
}
