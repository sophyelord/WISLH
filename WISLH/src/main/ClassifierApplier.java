package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.rules.DecisionTable;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.HoeffdingTree;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.LMT;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class ClassifierApplier implements Runnable{

	public final static String[] clasSpec = {"1-NN","3-NN","5-NN","9-NN",
			"1-NN-dist","3-NN-dist","5-NN-dist","9-NN-dist",
			"J48","NaiveBayes","ZeroR","HoeffdingTree","DecisionTable"} ;
	Instances inst;
	Evaluation[] eval;
	Classifier[] clas;
	private int folds;
	private Random randSeed;
	
	class EvalRunnable implements Runnable{

		int cIndex;
		
		public EvalRunnable(int classifierIndex) {
			
			this.cIndex = classifierIndex;
		}
		
		public void run() {

			try {
				eval[cIndex].crossValidateModel(clas[cIndex], inst, folds, randSeed);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}		
	}
	
	public ClassifierApplier(Instances inst, int folds, Random randSeed) {
		
		this.folds = folds;
		this.randSeed = randSeed;
		this.inst = inst;
		
		this.clas = new Classifier[13];
		
		IBk ibk = new IBk();
		try {
			ibk.setOptions(new String[] {"-K" , "1"});
			this.clas[0] = ibk;
			ibk = new IBk();
			ibk.setOptions(new String[] {"-K" , "3"});
			this.clas[1] = ibk;
			ibk = new IBk();
			ibk.setOptions(new String[] {"-K" , "5"});
			this.clas[2] = ibk;
			ibk = new IBk();
			ibk.setOptions(new String[] {"-K" , "9"});
			this.clas[3] = ibk;
			ibk.setOptions(new String[] {"-K" , "1" , "-I"});
			this.clas[4] = ibk;
			ibk = new IBk();
			ibk.setOptions(new String[] {"-K" , "3" , "-I"});
			this.clas[5] = ibk;
			ibk = new IBk();
			ibk.setOptions(new String[] {"-K" , "5" , "-I"});
			this.clas[6] = ibk;
			ibk = new IBk();
			ibk.setOptions(new String[] {"-K" , "9" , "-I"});
			this.clas[7] = ibk;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		this.clas[8] = new J48();
		this.clas[9] = new NaiveBayes();
		this.clas[10] = new ZeroR();
		this.clas[11] = new HoeffdingTree();
		this.clas[12] = new DecisionTable();
		
		this.eval = new Evaluation[clas.length];
		
		for (int i = 0 ; i < eval.length ;i++) {
			//System.out.println(i);
			try {
				this.eval[i] = new Evaluation(inst);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public Evaluation[] getEvaluations(){
		return eval;
	}

	@Override
	public void run() {
		
		Thread[] th = new Thread[clas.length];
		
		for (int i = 0 ; i < clas.length ; i++) {
			
			th[i] = new Thread(new EvalRunnable(i));
			th[i].start();
		}
		
		
		for (Thread ht : th) {
			try {
				ht.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		
		if (args.length != 1) {
			System.out.println("Wrong arg count, expected 1 argument got " + args.length);
			System.out.println("Usage: ClassifierApplier arff_file");
			System.exit(1);
		}
		String sourceArffStr = args[0];

		File sourceArff = new File(sourceArffStr);
		
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
		inst.setClassIndex(756);
		ClassifierApplier ca = new ClassifierApplier(inst, 10, new Random(1));
		ca.run();
		
		Evaluation[] evals = ca.getEvaluations();
		
		for (Evaluation ev: evals) {
			System.out.println(ev.toSummaryString());
		}
	}
}
