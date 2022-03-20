package aaathesis;

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
	
	public String toString() {
		String tbr = "";
		for (MarkovChain mc : pop) {
			tbr = tbr + mc.toString() + "\n";
		}
		return tbr;
	}
}
