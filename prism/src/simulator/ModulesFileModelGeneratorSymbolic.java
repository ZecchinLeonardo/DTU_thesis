package simulator;

import java.util.ArrayList;
import java.util.List;

import param.Function;
import param.FunctionFactory;
import param.ModelBuilder;
import param.SymbolicEngine;
import parser.State;
import parser.Values;
import parser.VarList;
import parser.ast.ConstantList;
import parser.ast.DeclarationType;
import parser.ast.Expression;
import parser.ast.LabelList;
import parser.ast.ModulesFile;
import parser.ast.RewardStruct;
import parser.type.Type;
import prism.ModelGeneratorSymbolic;
import prism.ModelType;
import prism.PrismComponent;
import prism.PrismException;
import prism.PrismLangException;
import prism.RewardGenerator;

/**
 * A variant of ModulesFileGenerator that is suitable for model generation
 * at a symbolic level, i.e., where numeric values are kept as expressions
 * instead of being evaluated.
 * <br>
 * Used by the parametric / exact engine to build models.
 * <br>
 * Uses exact arithmetic to evaluate the various expressions in a model description.
 */
public class ModulesFileModelGeneratorSymbolic implements ModelGeneratorSymbolic, RewardGenerator
{
	// Parent PrismComponent (logs, settings etc.)
	protected PrismComponent parent;
	
	// PRISM model info
	/** The original modules file (might have unresolved constants) */
	private ModulesFile originalModulesFile;
	/** The modules file used for generating (has no unresolved constants after {@code initialise}) */
	private ModulesFile modulesFile;
	private ModelType modelType;
	private Values mfConstants;
	private VarList varList;
	private LabelList labelList;
	private List<String> labelNames;
	
	// Model exploration info
	
	// State currently being explored
	private State exploreState;
	// Updater object for model
	//protected Updater updater;
	protected SymbolicEngine engine;
	// List of currently available transitions
	protected param.TransitionList transitionList;
	// Has the transition list been built? 
	protected boolean transitionListBuilt;
	
	// Symbolic stuff
	boolean symbolic = false;
	protected ModelBuilder modelBuilder;
	protected FunctionFactory functionFactory;
	
	/**
	 * Build a ModulesFileModelGenerator for a particular PRISM model, represented by a ModuleFile instance.
	 * Throw an explanatory exception if this is not possible.
	 * @param modulesFile The PRISM model
	 */
	public ModulesFileModelGeneratorSymbolic(ModulesFile modulesFile) throws PrismException
	{
		this(modulesFile, null);
	}
	
	/**
	 * Build a ModulesFileModelGenerator for a particular PRISM model, represented by a ModuleFile instance.
	 * Throw an explanatory exception if this is not possible.
	 * @param modulesFile The PRISM model
	 */
	public ModulesFileModelGeneratorSymbolic(ModulesFile modulesFile, PrismComponent parent) throws PrismException
	{
		this.parent = parent;
		
		// No support for PTAs yet
		if (modulesFile.getModelType() == ModelType.PTA) {
			throw new PrismException("PTAs are not currently supported");
		}
		// No support for system...endsystem yet
		if (modulesFile.getSystemDefn() != null) {
			throw new PrismException("The system...endsystem construct is not currently supported");
		}
		
		// Store basic model info
		this.modulesFile = modulesFile;
		this.originalModulesFile = modulesFile;
		modelType = modulesFile.getModelType();
		
		// If there are no constants to define, go ahead and initialise;
		// Otherwise, setSomeUndefinedConstants needs to be called when the values are available  
		mfConstants = modulesFile.getConstantValues();
		if (mfConstants != null) {
			initialise();
		}
	}
	
	/**
	 * (Re-)Initialise the class ready for model exploration
	 * (can only be done once any constants needed have been provided)
	 */
	private void initialise() throws PrismException
	{
		// Evaluate constants on (a copy) of the modules file, insert constant values
		// Note that we don't optimise expressions since this can create some round-off issues
		modulesFile = (ModulesFile) modulesFile.deepCopy().replaceConstants(mfConstants);

		// Get info
		varList = modulesFile.createVarList();
		labelList = modulesFile.getLabelList();
		labelNames = labelList.getLabelNames();
		
		// Create data structures for exploring model
		//updater = new Updater(modulesFile, varList, parent);
		//transitionList = new TransitionList();
		engine = new SymbolicEngine(modulesFile, modelBuilder, functionFactory);
		transitionListBuilt = false;
	}
	
	@Override
	public void setSymbolic(ModelBuilder modelBuilder, FunctionFactory functionFactory)
	{
		symbolic = true;
		this.modelBuilder = modelBuilder;
		this.functionFactory = functionFactory;
		//updater.setSymbolic(modelBuilder, functionFactory);
		// TODO: created twice
		engine = new SymbolicEngine(modulesFile, modelBuilder, functionFactory);
	}
	
	// Methods for ModelInfo interface
	
	@Override
	public ModelType getModelType()
	{
		return modelType;
	}

	@Override
	public void setSomeUndefinedConstants(Values someValues) throws PrismException
	{
		setSomeUndefinedConstants(someValues, false);
	}

	@Override
	public void setSomeUndefinedConstants(Values someValues, boolean exact) throws PrismException
	{
		// We start again with a copy of the original modules file
		// and set the constants in the copy.
		// As {@code initialise()} can replace references to constants
		// with the concrete values in modulesFile, this ensures that we
		// start again at a place where references to constants have not
		// yet been replaced.
		modulesFile = (ModulesFile) originalModulesFile.deepCopy();
		modulesFile.setSomeUndefinedConstants(someValues, exact);
		mfConstants = modulesFile.getConstantValues();
		initialise();
	}
	
	@Override
	public Values getConstantValues()
	{
		return mfConstants;
	}
	
	@Override
	public boolean containsUnboundedVariables()
	{
		return modulesFile.containsUnboundedVariables();
	}
	
	@Override
	public int getNumVars()
	{
		return modulesFile.getNumVars();
	}
	
	@Override
	public List<String> getVarNames()
	{
		return modulesFile.getVarNames();
	}

	@Override
	public List<Type> getVarTypes()
	{
		return modulesFile.getVarTypes();
	}

	@Override
	public DeclarationType getVarDeclarationType(int i) throws PrismException
	{
		return modulesFile.getVarDeclarationType(i);
	}
	
	@Override
	public int getVarModuleIndex(int i)
	{
		return modulesFile.getVarModuleIndex(i);
	}
	
	@Override
	public String getModuleName(int i)
	{
		return modulesFile.getModuleName(i);
	}
	
	@Override
	public VarList createVarList() throws PrismException
	{
		return varList;
	}
	
	@Override
	public int getNumLabels()
	{
		return labelList.size();	
	}

	@Override
	public String getActionStringDescription()
	{
		return "Module/[action]";
	}
	
	@Override
	public List<String> getLabelNames()
	{
		return labelNames;
	}
	
	@Override
	public String getLabelName(int i) throws PrismException
	{
		return labelList.getLabelName(i);
	}
	
	@Override
	public int getLabelIndex(String label)
	{
		return labelList.getLabelIndex(label);
	}
	
	// Methods for ModelGenerator interface
	
	@Override
	public boolean hasSingleInitialState() throws PrismException
	{
		return modulesFile.getInitialStates() == null;
	}
	
	@Override
	public State getInitialState() throws PrismException
	{
		if (modulesFile.getInitialStates() == null) {
			// get initial state, using exact evaluation
			return modulesFile.getDefaultInitialState(true);
		} else {
			// Inefficient but probably won't be called
			return getInitialStates().get(0);
		}
	}
	
	@Override
	public List<State> getInitialStates() throws PrismException
	{
		List<State> initStates = new ArrayList<State>();
		// Easy (normal) case: just one initial state
		if (modulesFile.getInitialStates() == null) {
			// get initial state, using exact evaluation
			State state = modulesFile.getDefaultInitialState(true);
			initStates.add(state);
		}
		// Otherwise, there may be multiple initial states
		// For now, we handle this is in a very inefficient way
		else {
			Expression init = modulesFile.getInitialStates();
			List<State> allPossStates = varList.getAllStates();
			for (State possState : allPossStates) {
				if (init.evaluateExact(modulesFile.getConstantValues(), possState).toBoolean()) {
					initStates.add(possState);
				}
			}
		}
		return initStates;
	}

	@Override
	public void exploreState(State exploreState) throws PrismException
	{
		this.exploreState = exploreState;
		transitionListBuilt = false;
	}
	
	@Override
	public int getNumChoices() throws PrismException
	{
		return getTransitionList().getNumChoices();
	}

	@Override
	public int getNumTransitions() throws PrismException
	{
		return getTransitionList().getNumTransitions();
	}

	@Override
	public int getNumTransitions(int i) throws PrismException
	{
		return getTransitionList().getChoice(i).size();
	}

	@Override
	public int getChoiceIndexOfTransition(int index) throws PrismException
	{
		return getTransitionList().getChoiceIndexOfTransition(index);
	}
	
	@Override
	public int getChoiceOffsetOfTransition(int index) throws PrismException
	{
		return getTransitionList().getChoiceOffsetOfTransition(index);
	}
	
	@Override
	public int getTotalIndexOfTransition(int i, int offset) throws PrismException
	{
		return getTransitionList().getTotalIndexOfTransition(i, offset);
	}
	
	@Override
	public Object getTransitionAction(int i, int offset) throws PrismException
	{
		param.TransitionList transitions = getTransitionList();
		int a = transitions.getTransitionModuleOrActionIndex(transitions.getTotalIndexOfTransition(i, offset));
		return a < 0 ? null : modulesFile.getSynch(a - 1);
	}

	@Override
	public String getTransitionActionString(int i, int offset) throws PrismException
	{
		param.TransitionList transitions = getTransitionList();
		int a = transitions.getTransitionModuleOrActionIndex(transitions.getTotalIndexOfTransition(i, offset));
		return getDescriptionForModuleOrActionIndex(a);
	}
	
	@Override
	public Object getChoiceAction(int index) throws PrismException
	{
		param.TransitionList transitions = getTransitionList();
		int a = transitions.getChoiceModuleOrActionIndex(index);
		return a < 0 ? null : modulesFile.getSynch(a - 1);
	}

	@Override
	public String getChoiceActionString(int index) throws PrismException
	{
		param.TransitionList transitions = getTransitionList();
		int a = transitions.getChoiceModuleOrActionIndex(index);
		return getDescriptionForModuleOrActionIndex(a);
	}

	/**
	 * Utility method to get a description for an action label:
	 * "[a]" for a synchronous action a and "M" for an unlabelled
	 * action belonging to a module M. Takes in an integer index:
	 * -i for independent in ith module, i for synchronous on ith action
	 * (in both cases, modules/actions are 1-indexed) 
	 */ 
	private String getDescriptionForModuleOrActionIndex(int a)
	{
		if (a < 0) {
			return modulesFile.getModuleName(-a - 1);
		} else if (a > 0) {
			return "[" + modulesFile.getSynchs().get(a - 1) + "]";
		} else {
			return "?";
		}
	}
	
	@Override
	public double getTransitionProbability(int i, int offset) throws PrismException
	{
		throw new UnsupportedOperationException();
		/*param.TransitionList transitions = getTransitionList();
		return transitions.getChoice(i).getProbability(offset);*/
	}

	@Override
	public Function getTransitionProbabilityFunction(int i, int offset) throws PrismException
	{
		param.TransitionList transitions = getTransitionList();
		return transitions.getChoice(i).getProbability(offset);
	}

	@Override
	public double getChoiceProbabilitySum(int i) throws PrismException
	{
		throw new UnsupportedOperationException();
		/*param.TransitionList transitions = getTransitionList();
		return transitions.getChoice(i).getProbabilitySum();*/
	}
	
	@Override
	public double getProbabilitySum() throws PrismException
	{
		throw new UnsupportedOperationException();
		/*param.TransitionList transitions = getTransitionList();
		return transitions.getProbabilitySum();*/
	}
	
	@Override
	public String getTransitionUpdateString(int i, int offset) throws PrismException
	{
		param.TransitionList transitions = getTransitionList();
		return transitions.getTransitionUpdateString(transitions.getTotalIndexOfTransition(i, offset), exploreState);
	}
	
	@Override
	public String getTransitionUpdateStringFull(int i, int offset) throws PrismException
	{
		param.TransitionList transitions = getTransitionList();
		return transitions.getTransitionUpdateStringFull(transitions.getTotalIndexOfTransition(i, offset));
	}
	
	@Override
	public State computeTransitionTarget(int index, int offset) throws PrismException
	{
		return getTransitionList().getChoice(index).computeTarget(offset, exploreState);
	}

	@Override
	public boolean isLabelTrue(int i) throws PrismException
	{
		Expression expr = labelList.getLabel(i);
		return expr.evaluateExact(exploreState).toBoolean();
	}
	
	// Methods for RewardGenerator interface

	@Override
	public List<String> getRewardStructNames()
	{
		return modulesFile.getRewardStructNames();
	}
	
	@Override
	public boolean rewardStructHasStateRewards(int i)
	{
		return modulesFile.rewardStructHasStateRewards(i);
	}
	
	@Override
	public boolean rewardStructHasTransitionRewards(int i)
	{
		return modulesFile.rewardStructHasTransitionRewards(i);
	}
	
	@Override
	public double getStateReward(int r, State state) throws PrismException
	{
		RewardStruct rewStr = modulesFile.getRewardStruct(r);
		int n = rewStr.getNumItems();
		double d = 0;
		for (int i = 0; i < n; i++) {
			if (!rewStr.getRewardStructItem(i).isTransitionReward()) {
				Expression guard = rewStr.getStates(i);
				if (guard.evaluateExact(modulesFile.getConstantValues(), state).toBoolean()) {
					double rew = rewStr.getReward(i).evaluateExact(modulesFile.getConstantValues(), state).doubleValue();
					if (Double.isNaN(rew))
						throw new PrismLangException("Reward structure evaluates to NaN at state " + state, rewStr.getReward(i));
					d += rew;
				}
			}
		}
		return d;
	}

	@Override
	public double getStateActionReward(int r, State state, Object action) throws PrismException
	{
		RewardStruct rewStr = modulesFile.getRewardStruct(r);
		int n = rewStr.getNumItems();
		double d = 0;
		for (int i = 0; i < n; i++) {
			if (rewStr.getRewardStructItem(i).isTransitionReward()) {
				Expression guard = rewStr.getStates(i);
				String cmdAction = rewStr.getSynch(i);
				if (action == null ? (cmdAction.isEmpty()) : action.equals(cmdAction)) {
					if (guard.evaluateExact(modulesFile.getConstantValues(), state).toBoolean()) {
						double rew = rewStr.getReward(i).evaluateExact(modulesFile.getConstantValues(), state).doubleValue();
						if (Double.isNaN(rew))
							throw new PrismLangException("Reward structure evaluates to NaN at state " + state, rewStr.getReward(i));
						d += rew;
					}
				}
			}
		}
		return d;
	}
	
	// ModelGeneratorSymbolic
	
	@Override
	public Expression getUnknownConstantDefinition(String name) throws PrismException
	{
		ConstantList constantList = modulesFile.getConstantList();
		int i = constantList.getConstantIndex(name);
		if (i == -1) {
			throw new PrismException("Unknown constant " + name);
		}
		return constantList.getConstant(i);
	}

	// Local utility methods
	
	/**
	 * Returns the current list of available transitions, generating it first if this has not yet been done.
	 */
	private param.TransitionList getTransitionList() throws PrismException
	{
		// Compute the current transition list, if required
		if (!transitionListBuilt) {
			//updater.calculateTransitions(exploreState, transitionList);
			transitionList = engine.calculateTransitions(exploreState, true);
			transitionListBuilt = true;
		}
		return transitionList;
	}
}
