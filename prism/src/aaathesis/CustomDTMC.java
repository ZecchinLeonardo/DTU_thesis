package aaathesis;

import java.util.Arrays;
import java.util.List;

import parser.State;
import parser.type.Type;
import parser.type.TypeInt;
import prism.ModelGenerator;
import prism.ModelType;
import prism.PrismException;

public class CustomDTMC implements ModelGenerator {
	// Current state being explored
	private State exploreState, initialState;
	private List<State> states;
	private List<String> actions;
	private List<Integer> transitionN;
	private String action,labels;
	private int transitions,tracking;
	private List<List<Double>> probabilities;
	private List<List<State>> destinations;
	
	public CustomDTMC(State initialState, String action, int transitions, List<Double> probabilities) {
		this.initialState = initialState;
		this.action= action;
		this.transitions = transitions;
		states.add(initialState);
		actions.add(action);
		transitionN.add(transitions);
		tracking = 0;
		this.probabilities.add(probabilities);
	}
	
	public CustomDTMC() {
		tracking = 0;
	}
	
	public void addData(State state, List<State> destinations, String action, int transitions, List<Double> probabilities) {
		this.states.add(state);
		this.destinations.add(destinations);
		this.actions.add(action);
		this.transitionN.add(transitions);
		this.probabilities.add(probabilities);
	}

	@Override
	public ModelType getModelType() {
		return ModelType.DTMC;
	}

	@Override
	public List<String> getVarNames() {
		return null;
	}

	@Override
	public List<Type> getVarTypes() {
		return null;
	}

	@Override
	public State getInitialState() throws PrismException {
		return this.initialState;
	}

	@Override
	public void exploreState(State exploreState) throws PrismException {
		this.exploreState = exploreState;
		tracking++;
	}

	@Override
	public int getNumChoices() throws PrismException {
		return 1;
	}

	@Override
	public int getNumTransitions(int i) throws PrismException {
		return transitionN.get(tracking);
	}

	@Override
	public Object getTransitionAction(int i, int offset) throws PrismException {
		return actions.get(tracking);
	}

	@Override
	public double getTransitionProbability(int i, int offset) throws PrismException {
		return this.probabilities.get(tracking).get(offset);
	}

	@Override
	public State computeTransitionTarget(int i, int offset) throws PrismException {
		return this.destinations.get(tracking).get(offset);
	}

}
