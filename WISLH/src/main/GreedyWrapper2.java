package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;


import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.converters.ConverterUtils.DataSink;
import weka.core.converters.ConverterUtils.DataSource;

public class GreedyWrapper2 implements Runnable {
	
	private AbstractClassifier classifier;
	private Instances insts;
	private int folds;
	private Random randSeed;
	
	//for distrib
	boolean[] selection;
	double maxScore = 0;
	
	int atAt[];
	Queue<Integer> atributes = new LinkedList<>();
	Queue<Integer> partialQueue = new LinkedList<>();
	
	boolean finished = false;
	
	private int maxThreads = 8;
	
	private class EvalRunnable implements Runnable{
		
		int addBias;
		double pct;
		Random rr;
		
		public EvalRunnable( int nAt) {
			this.addBias = nAt;
			rr = new Random(randSeed.nextLong());
		}

		@Override
		public void run() {
			Instances nInst = new Instances(insts);
			nInst.deleteAttributeAt(addBias);
					
			nInst.setClassIndex(nInst.numAttributes()-1);
			Evaluation eval = null;
			try {
				eval = new Evaluation(nInst);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				eval.crossValidateModel(AbstractClassifier.makeCopy(classifier),nInst, folds, rr);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			pct = eval.pctCorrect();
			
		}
		
		public double getEval() {
			return pct;
		}



	}
	
	
	public GreedyWrapper2( AbstractClassifier classifier, Instances inst, int folds, Random randSeed) {
	
		this.classifier = classifier;
		this.insts = inst;
		this.insts.setClassIndex(insts.numAttributes()-1);
		this.folds = folds;
		this.randSeed = randSeed;
		
		selection = new boolean[inst.numAttributes()-1];
		
		for (int i = 0 ; i < selection.length ; i++) {
			atributes.add(i);
		}
	
		this.atAt = new int[selection.length];
		
		try {
			Evaluation eval = new Evaluation(this.insts);
			eval.crossValidateModel(AbstractClassifier.makeCopy(this.classifier),this.insts, this.folds, this.randSeed);
			maxScore = eval.pctCorrect();
			System.out.println("initial score: " + maxScore);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void run() {
		
		
		
		Thread[] ths = new Thread[insts.numAttributes() - 1];
		Arrays.fill(selection, true);
		
		EvalRunnable[] evr;
		evr = new EvalRunnable[insts.numAttributes() - 1];
		
		
		while (!atributes.isEmpty()) {
		
			int atributeCount = atributes.size();
			
			for (int i = 0 ; i < atributeCount ; i++) {
				int nAt = atributes.poll();
				partialQueue.add(nAt);
				atAt[i] = nAt;
				
				evr[i] = new EvalRunnable(i);
				ths[i] = new Thread(evr[i]);
							
				
			}
			
			ThreadManager thm = new ThreadManager(ths, maxThreads,atributeCount);
			thm.run();
			
			
			double localMax = 0;
			int best = 0;
			for (int i = 0 ;  i < atributeCount ; i++) {
									
				double score = evr[i].getEval();
				
				if (score > localMax) {
					localMax = score;
					best = i;
				}
					
				
			}
			
			System.out.println("localMax : " + localMax);
			if (localMax < maxScore)
				break;
			else {
				maxScore = localMax;
				int aAt = evr[best].addBias;
				int addAt = atAt[aAt];
				selection[addAt] = false;
				insts.deleteAttributeAt(evr[best].addBias);
				while (!partialQueue.isEmpty()) {
					int pl = partialQueue.poll();
					
					if (addAt != pl) {
						atributes.add(pl);
					}
				}
			}
		
			
		}
		
		
		for (int i = 0 ; i < selection.length ; i++) {
			if (!selection[i]) {
				System.out.print(i + " ");
			}
			
		}
		System.out.println();
		
	}
	
	
	public Instances getWrappedInstances() {
		return insts;
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
		
		Random r = new Random(1);
		
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
			
			GreedyWrapper2 gw = new GreedyWrapper2(ac, inst, 10, r);
			gw.run();
			Instances wrpInst = gw.getWrappedInstances();
			try {
				DataSink.write((new File(arffDir, "wrp2" + "IBkDst" + args[i] + ".arff")).getAbsolutePath(), wrpInst);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		
	}

	
	

	
} 

	

