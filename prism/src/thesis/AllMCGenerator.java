package thesis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import explicit.DTMC;
import explicit.DTMCFromMDPAndMDStrategy;
import explicit.DTMCModelChecker;
import explicit.MDP;
import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import prism.Prism;
import prism.PrismDevNullLog;
import prism.PrismException;
import prism.PrismLog;
import prism.Result;
import strat.MDStrategy;
import strat.MDStrategyArray;

public class AllMCGenerator {
	public void run() { 
		String fileName = "examples/mdp_s20_c3_t3_random.prism";
		try {
			System.out.println("Computing Markov Chains");
			// Create a log for PRISM output (hidden or stdout)
			PrismLog mainLog = new PrismDevNullLog();
			ArrayList<int[]> chains = new ArrayList<int[]>();
			//PrismLog mainLog = new PrismFileLog("stdout");

			// Initialise PRISM engine 
			Prism prism = new Prism(mainLog);
			prism.initialise();
			prism.setEngine(Prism.EXPLICIT);
			// Parse and load a PRISM model (an MDP) from a file
			ModulesFile modulesFile = prism.parseModelFile(new File(fileName));
			prism.loadPRISMModel(modulesFile);
			System.out.println("building model");
			prism.buildModel();
			System.out.println("model built");
			MDP mdp = (MDP) prism.getBuiltModelExplicit();
			int statesN = mdp.getStatesList().size();
			int[] nChoices = new int[statesN];
			int[] chain = new int[statesN];
			for(int i = 0; i<statesN; i++) {
				nChoices[i] = mdp.getNumChoices(i);
				chain[i]=0;
			}
			{
			int[] tba = new int[statesN];
			for(int i =0; i<statesN;i++) {
				tba[i] = chain[i];
			}
			chains.add(tba);
			}
			boolean end = false;
			int index = statesN-1;
			while (!end) {
				chain[index]++;
				int tempIndex=index;
				while(chain[tempIndex]>=nChoices[tempIndex] && !end) {
					if(tempIndex != 0) {
						chain[tempIndex] = 0;
						tempIndex--;
						chain[tempIndex]++;
					} else {
						end=true;
					}
				}
				if(!end) {
					int[] tba = new int[statesN];
					for(int i =0; i<statesN;i++) {
						tba[i] = chain[i];
					}
					chains.add(tba);
				}
			}
			/*
			for (int[] c : chains) {
				for(int i = 0; i<statesN;i++) {
					System.out.print(c[i]);
				}
				System.out.println();
			}
			*/
			int resultZero=0, resultOne=0;
			for(int[] c : chains ) {
				MDStrategy strat = new MDStrategyArray(mdp, c);
				DTMC dtmc = new DTMCFromMDPAndMDStrategy(mdp, strat);
				DTMCModelChecker mc = new DTMCModelChecker(prism);
				
				PropertiesFile pf = prism.parsePropertiesString("Pmax=?[F \"goal1\"]");
				//Long mcStart = System.nanoTime();
				Result r = mc.check(dtmc, pf.getProperty(0));
				if((double)r.getResult()==0) {
					resultZero++;
				} else {
					resultOne++;
				}
			}
			System.out.println("Zero: " +resultZero +" One: "+resultOne);
		} catch (PrismException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
