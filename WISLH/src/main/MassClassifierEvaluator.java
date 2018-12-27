package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Random;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.functions.SMO;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.Stacking;
import weka.classifiers.meta.Vote;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.converters.ConverterUtils.DataSource;

public class MassClassifierEvaluator {

	
	public static void main(String[] args) {
		
		if (args.length < 3) {
			System.out.println("Wrong arg count, expected at least 3 arguments got " + args.length);
			System.out.println("Usage: MassClassifierEvaluator result_file fold_count fold_random_seed arff_dir ");
			System.exit(1);
		}
		
		
		File resultFile = new File(args[0]);
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(resultFile);
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		int folds = Integer.parseInt(args[1]);
		Random rand = new Random(Long.parseLong(args[2]));
		
		/* Sailkatzaileak*/
		
		Classifier[] clas = new Classifier[3];
		IBk ibk = new IBk(1);
		ibk.setDistanceWeighting(new SelectedTag(IBk.WEIGHT_INVERSE, IBk.TAGS_WEIGHTING));
		
		clas[0] = ibk;
		
		Vote v = new Vote();
		Classifier[] cIbk = {new IBk(1),new IBk(3),new IBk(5),new IBk(9),new IBk(13)};
		v.setClassifiers(cIbk);
		clas[1] = v;
		
		Stacking stck = new Stacking();
		Classifier[] stcnd = new Classifier[] {new BayesNet(), new J48() , new SMO(), new RandomForest()};
		stck.setClassifiers(stcnd);
		stck.setMetaClassifier(new RandomForest());
		clas[2] = stck;
		
		
		ClassifierApplier[] ca = new ClassifierApplier[(args.length-3)];
		Thread[] ths = new Thread[(args.length-3)];
		
		
		
		for (int i = 0 ; i < ca.length ; i++) {

			FileInputStream fr = null;
			
			try {
				fr = new FileInputStream(args[i+3]);
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
			
			int NA = inst.numAttributes();
			inst.setClassIndex(NA-1);
			
			
			
			
			ca[i] = new ClassifierApplier(inst, folds, rand,clas);
			ths[i] = new Thread(ca[i]);
			ths[i].start();
			
			
		}
		
		pw.println("Dataset Classifier Statistic");
		for (int i = 0 ; i < ths.length ; i++) {
			try {
				ths[i].join();
				Evaluation[] evals = ca[i].getEvaluations();
				
				for (int j = 0 ; j < evals.length ; j++) {
					
					String[] splitend = args[i+3].split("/");
					String splited = splitend[splitend.length-1].split("\\.")[0];
					
					pw.print("\"" + splited + "\"");
					pw.print(" \"");
					pw.print(classifierDesc(clas[j],j));
					pw.print("\" ");
					pw.print(evals[j].pctCorrect());
					pw.print("\n");
				}
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		pw.close();
		
		
	}

	private static String classifierDesc(Classifier classifier,int j) {
	
		AbstractClassifier ac = (AbstractClassifier) classifier;
		String[] opts = ac.getOptions();
		
		StringBuilder sb = new StringBuilder();
		sb.append(j);
		sb.append(ac.getClass().getSimpleName());
		
		for (String str : opts) {
			sb.append(":" + str);
		}
		
		return sb.toString();
		
	}
}
