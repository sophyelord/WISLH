package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import miniGD.Distribuible;
import miniGD.RemoteRunnable;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.converters.ConverterUtils.DataSink;
import weka.core.converters.ConverterUtils.DataSource;

public class GreedyWrapper implements Runnable, Distribuible{
	
	private AbstractClassifier classifier;
	private Instances insts;
	private int folds;
	private Random randSeed;
	private Instances wrpInst;
	
	//for distrib
	boolean[] selection;
	double maxScore = 0;
	
	int atAt[];
	Queue<Integer> atributes = new LinkedList<>();
	Queue<Integer> partialQueue = new LinkedList<>();
	
	boolean finished = false;
	
	private int maxThreads = 8;
	
	private class EvalRunnable implements RemoteRunnable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1061053369951329460L;
		
		private Instances nInst;
		private Evaluation eval;
		private boolean[] sMask;
		private Classifier ac;
		int addBias;
		
		Random rr;
		
		public EvalRunnable( int nAt) {
			this.nInst = new Instances(insts);
			this.eval = null;
			this.sMask = selection;
			this.addBias = nAt;
			rr = new Random(randSeed.nextLong());
			try {
				this.ac = AbstractClassifier.makeCopy(classifier);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			
			//Remove attributes
			for (int j = sMask.length - 1 ; j >= 0 ; j--) {
				
				if (!sMask[j] && j != addBias) {
					nInst.deleteAttributeAt(j);
				}
					
			}			
			
			nInst.setClassIndex(nInst.numAttributes()-1);
			
			try {
				this.eval = new Evaluation(nInst);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				eval.crossValidateModel(ac,nInst, folds, rr);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		public Evaluation getEval() {
			return eval;
		}


		@Override
		public Serializable getResult() {
			
			return getEval();
		}
		
	}
	
	
	public GreedyWrapper( AbstractClassifier classifier, Instances inst, int folds, Random randSeed) {
	
		this.classifier = classifier;
		this.insts = inst;
		this.folds = folds;
		this.randSeed = randSeed;
		
		selection = new boolean[inst.numAttributes()-1];
		
		for (int i = 0 ; i < selection.length ; i++) {
			atributes.add(i);
		}
		
		
	}
	
	public void run() {
		
		
		
		Thread[] ths = new Thread[insts.numAttributes() - 1];
		Arrays.fill(selection, false);
		
		EvalRunnable[] evr;
		evr = new EvalRunnable[insts.numAttributes() - 1];
		
		
		while (!atributes.isEmpty()) {
		
			int atributeCount = atributes.size();
			
			for (int i = 0 ; i < atributeCount ; i++) {
				int nAt = atributes.poll();
				partialQueue.add(nAt);
				
				
				evr[i] = new EvalRunnable( nAt);
				ths[i] = new Thread(evr[i]);
							
				
			}
			
			ThreadManager thm = new ThreadManager(ths, maxThreads,atributeCount);
			thm.run();
			
			
			double localMax = 0;
			int best = 0;
			for (int i = 0 ;  i < atributeCount ; i++) {
									
				double score = evr[i].getEval().pctCorrect();
				
				if (score > localMax) {
					localMax = score;
					best = i;
				}
					
				
			}
			
			System.out.println("localMax : " + localMax);
			if (localMax <= maxScore)
				break;
			else {
				maxScore = localMax;
				int addAt = evr[best].addBias;
				selection[addAt] = true;
				
				while (!partialQueue.isEmpty()) {
					int pl = partialQueue.poll();
					
					if (addAt != pl) {
						atributes.add(pl);
					}
				}
			}
		
			
		}
		
		wrpInst = new Instances(insts);
		
		//Remove attributes
		for (int j = selection.length - 1 ; j >= 0 ; j--) {
			
			if (!selection[j]) {
				wrpInst.deleteAttributeAt(j);
			}
				
		}
		
		for (int i = 0 ; i < selection.length ; i++) {
			if (selection[i]) {
				System.out.print(i + " ");
			}
			
		}
		System.out.println();
		
	}
	
	
	public Instances getWrappedInstances() {
		return wrpInst;
	}
	
	
	
	@Override
	public RemoteRunnable[] getTasks() {
		int atributeCount = atributes.size();
		EvalRunnable[] evr;
		evr = new EvalRunnable[atributeCount];
		atAt = new int[atributeCount];
		for (int i = 0 ; i < atributeCount ; i++) {
			int nAt = atributes.poll();
			partialQueue.add(nAt);
			atAt[i] = nAt;
			
			evr[i] = new EvalRunnable( nAt);					
			
		}
		
		return evr;
	}

	@Override
	public void receiveResults(Serializable[] rr) {
		
		Evaluation[] evs = new Evaluation[rr.length];

		for (int i = 0 ; i < rr.length ; i++) {
			evs[i] = (Evaluation) rr[i];
		}
		
		double localMax = 0;
		int best = 0;
		for (int i = 0 ;  i < evs.length ; i++) {
								
			double score = evs[i].pctCorrect();
			
			if (score > localMax) {
				localMax = score;
				best = i;
			}
				
			
		}
		
		if (localMax <= maxScore) {
			wrpInst = new Instances(insts);
			
			//Remove attributes
			for (int j = selection.length - 1 ; j >= 0 ; j--) {
				
				if (!selection[j]) {
					wrpInst.deleteAttributeAt(j);
				}
					
			}
			
			finished = true;
			
		}
		else {
			maxScore = localMax;
			int addAt = atAt[best];
			selection[addAt] = true;
			
			while (!partialQueue.isEmpty()) {
				int pl = partialQueue.poll();
				
				if (addAt != pl) {
					atributes.add(pl);
				}
			}
			
			if (evs.length == 1) {
				wrpInst = new Instances(insts);
				
				//Remove attributes
				for (int j = selection.length - 1 ; j >= 0 ; j--) {
					
					if (!selection[j]) {
						wrpInst.deleteAttributeAt(j);
					}
						
				}
				
				finished = true;
			}
		}
		
	}

	@Override
	public boolean tasksLeft() {
		
		return !finished;
	}
	
	
	
	public static void main(String[] args) {
	
		if (args.length < 2) {
			System.out.println("Wrong arg count, expected at least 2 arguments got " + args.length);
			System.out.println("Usage: MultiFilterApplier arff_dir source_arff [image_dir]");
			System.exit(1);
		}
		
		
		String arffDir = args[0];
		String sourceArffDir = args[1];
		
		
		IBk ibk = new IBk(1);
		ibk.setDistanceWeighting(new SelectedTag(IBk.WEIGHT_INVERSE, IBk.TAGS_WEIGHTING));
		
		AbstractClassifier ac = ibk;
		
		Random r = new Random(3);
		
		for (int i = 2 ; i < args.length ; i++) {
			
			System.out.println(args[i]);
			File sourceArff = new File(sourceArffDir,args[i]+".arff");
			
			//System.out.println(sourceArff.getAbsolutePath());
			FileInputStream fr = null;
			
			try {
				fr = new FileInputStream(sourceArff);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Instances inst = null;
			try {
				inst = DataSource.read(fr);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			GreedyWrapper gw = new GreedyWrapper(ac, inst, 3, r);
			gw.run();
			Instances wrpInst = gw.getWrappedInstances();
			try {
				DataSink.write((new File(arffDir, "wrp" + "IBkDst" + args[i] + ".arff")).getAbsolutePath(), wrpInst);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		
	}

	
	

	
} 

	

