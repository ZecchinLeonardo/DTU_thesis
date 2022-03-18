package aaathesis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class MarkovChain implements Iterable<Float> {
	private ArrayList<Float> chromosomes;
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
		return this.chromosomes.toString();
	}
	
	public static float getRandomChromosome() {
		Random r = new Random();
		return r.nextFloat();
	}
}
