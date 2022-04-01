package aaathesis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class MarkovChain implements Iterable<Float> {
	private ArrayList<Float> chromosomes;
	private float fitness;
	public MarkovChain(ArrayList<Float> element) {
		this.chromosomes = element;
	}
	
	public MarkovChain() {
		this.chromosomes = new ArrayList<Float>();
	}
	
	public void addChromosome(float chromosome) {
		this.chromosomes.add(chromosome);
	}

	@Override
	public Iterator<Float> iterator() {
		return this.chromosomes.iterator();
	}
	
	@Override
	public String toString() {
		return (this.chromosomes.toString() + ", fitness: " + String.valueOf(fitness));
	}
	
	public static float getRandomChromosome() {
		Random r = new Random();
		return r.nextFloat();
	}
	
	public float getChromosomeAtIndex(int index) {
		return this.chromosomes.get(index);
	}
	
	public void setChromosomeAtIndex(int index, Float value) {
		this.chromosomes.set(index, value);
	}
	
	public void setFitness(float fitness) {
		this.fitness=fitness;
	}
	
	public float getfitness() {
		return this.fitness;
	}
	
	public int getSize() {
		return this.chromosomes.size();
	}
}
