package thesis;

import java.util.ArrayList;
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
	private ArrayList<State> states;
	private ArrayList<String> actions;
	private ArrayList<Integer> transitionN;
	private ArrayList<String> labelList;
	private String action,labels;
	private int transitions,tracking;
	private ArrayList<ArrayList<Double>> probabilities;
	private ArrayList<ArrayList<State>> destinations;
	
	public CustomDTMC(State initialState, String action, int transitions, ArrayList<Double> probabilities) {
		this.initialState = initialState;
		this.action= action;
		this.transitions = transitions;
		this.states = new ArrayList<State>();
		this.actions = new ArrayList<String>();
		this.transitionN = new ArrayList<Integer>();
		this.probabilities = new ArrayList<ArrayList<Double>>();
		this.destinations = new ArrayList<ArrayList<State>>();
		this.labelList = new ArrayList<String>();
		states.add(initialState);
		actions.add(action);
		transitionN.add(transitions);
		tracking = 0;
		this.probabilities.add(probabilities);
	}
	
	public CustomDTMC() {
		this.states = new ArrayList<State>();
		this.actions = new ArrayList<String>();
		this.transitionN = new ArrayList<Integer>();
		this.probabilities = new ArrayList<ArrayList<Double>>();
		this.destinations = new ArrayList<ArrayList<State>>();
		this.labelList = new ArrayList<String>();
		tracking = 0;
	}
	
	public CustomDTMC(State initialState) {
		this.states = new ArrayList<State>();
		this.actions = new ArrayList<String>();
		this.transitionN = new ArrayList<Integer>();
		this.probabilities = new ArrayList<ArrayList<Double>>();
		this.destinations = new ArrayList<ArrayList<State>>();
		this.initialState=initialState;
		this.labelList = new ArrayList<String>();
		tracking = 0;
	}
	
	public void addData(State state, ArrayList<State> destinations, String action, int transitions, ArrayList<Double> probabilities) {
		this.states.add(state);
		this.destinations.add(destinations);
		this.actions.add(action);
		this.transitionN.add(transitions);
		this.probabilities.add(probabilities);
	}
	
	public void addData(State state, ArrayList<State> destinations, int transitions, ArrayList<Double> probabilities) {
		this.states.add(state);
		this.destinations.add(destinations);
		this.transitionN.add(transitions);
		this.probabilities.add(probabilities);
	}
	
	public void addChoice(String choice) {
		actions.add(choice);
	}
	
	public void goNext() {
		tracking++;
	}

	@Override
	public ModelType getModelType() {
		return ModelType.DTMC;
	}

	@Override
	public List<String> getVarNames() {
		return labelList;
	}

	@Override
	public List<Type> getVarTypes() {
		return Arrays.asList(TypeInt.getInstance());
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
