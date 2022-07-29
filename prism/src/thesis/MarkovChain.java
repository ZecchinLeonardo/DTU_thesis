package thesis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class MarkovChain implements Iterable<Float> {
	private ArrayList<Float> genes;
	private float fitness;
	public MarkovChain(ArrayList<Float> element) {
		this.genes = element;
	}
	
	public MarkovChain() {
		this.genes = new ArrayList<Float>();
	}
	
	public void addChromosome(float gene) {
		this.genes.add(gene);
	}

	@Override
	public Iterator<Float> iterator() {
		return this.genes.iterator();
	}
	
	@Override
	public String toString() {
		return (this.genes.toString() + ", fitness: " + String.valueOf(fitness));
	}
	
	public static float getRandomChromosome() {
		Random r = new Random();
		return r.nextFloat();
	}
	
	public float getGeneAtIndex(int index) {
		return this.genes.get(index);
	}
	
	public void setGeneAtIndex(int index, Float value) {
		this.genes.set(index, value);
	}
	
	public void setFitness(float fitness) {
		this.fitness=fitness;
	}
	
	public float getfitness() {
		return this.fitness;
	}
	
	public int getSize() {
		return this.genes.size();
	}
}
