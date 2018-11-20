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
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.Bagging;
import weka.classifiers.meta.Stacking;
import weka.classifiers.meta.Vote;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.REPTree;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.converters.ConverterUtils.DataSource;

public class MassClassifierEvaluator {

	
	public static void main(String[] args) {
		
		if (args.length < 3) {
			System.out.println("Wrong arg count, expected at least 3 arguments got " + args.length);
			System.out.println("Usage: MassClassifierEvaluator result_file fold_count fold_random_seed [arff_dir , class_index]");
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
		
		Classifier[] clas = new Classifier[25];
		
		Bagging bg = new Bagging();
		bg.setClassifier(new REPTree());
		clas[0] = bg;
		
		bg = new Bagging();
		bg.setClassifier(new J48());
		clas[1] = bg;
		
		AdaBoostM1 ab = new AdaBoostM1();
		ab.setClassifier(new REPTree());
		clas[2] = ab;
		
		ab = new AdaBoostM1();
		ab.setClassifier(new NaiveBayes());
		clas[3] = ab;
		
		Classifier[] v1clas = new Classifier[] {new IBk(5) , new J48() , new NaiveBayes()};
		Classifier[] v2clas = new Classifier[] {new IBk(1) , new IBk(3) , new IBk(5), new IBk(9) , new IBk(13)};
		Classifier[] v3clas = new Classifier[] {new BayesNet(), new J48() , new SMO(), new RandomForest()};
		
		IBk[] ibks1 = new IBk[] {new IBk(1) , new IBk(3) , new IBk(5), new IBk(9) , new IBk(13)};
		IBk[] ibks2 = new IBk[] {new IBk(1) , new IBk(3) , new IBk(5), new IBk(9) , new IBk(13)};
		Classifier[] allIBK = new Classifier[15];
		
		for (int i = 0 ; i < v2clas.length ; i++) {
			allIBK[i] = v2clas[i];
		}
		for (int i = 0 ; i < ibks1.length ; i++) {
			allIBK[i + 5] = ibks1[i];
		}
		for (int i = 0 ; i < ibks2.length ; i++) {
			allIBK[i + 10] = ibks2[i];
		}
		
		for (IBk ib : ibks1) {
			ib.setDistanceWeighting(new SelectedTag(IBk.WEIGHT_INVERSE, IBk.TAGS_WEIGHTING));
		}
		
		for (IBk ib : ibks2) {
			ib.setDistanceWeighting(new SelectedTag(IBk.WEIGHT_SIMILARITY, IBk.TAGS_WEIGHTING));
		}
		
		
		Vote v = new Vote();
		v.setClassifiers(v1clas);
		clas[4] = v;
		
		v = new Vote();
		v.setClassifiers(v2clas);
		clas[5] = v;
		
		Stacking stck = new Stacking();
		stck.setClassifiers(v1clas);
		stck.setMetaClassifier(new RandomForest());
		clas[6] = stck;
		
		stck = new Stacking();
		stck.setClassifiers(v1clas);
		stck.setMetaClassifier(new J48());
		clas[7] = stck;
		
		stck = new Stacking();
		stck.setClassifiers(v1clas);
		stck.setMetaClassifier(new NaiveBayes());
		clas[8] = stck;
		
		stck = new Stacking();
		stck.setClassifiers(v2clas);
		stck.setMetaClassifier(new RandomForest());
		clas[9] = stck;
		
		stck = new Stacking();
		stck.setClassifiers(v2clas);
		stck.setMetaClassifier(new J48());
		clas[10] = stck;
		
		stck = new Stacking();
		stck.setClassifiers(v2clas);
		stck.setMetaClassifier(new NaiveBayes());
		clas[11] = stck;
		
		v = new Vote();
		v.setClassifiers(ibks1);
		clas[12] = v;
		
		v = new Vote();
		v.setClassifiers(ibks2);
		clas[13] = v;
		
		stck = new Stacking();
		stck.setClassifiers(ibks1);
		stck.setMetaClassifier(new RandomForest());
		clas[14] = stck;
		
		stck = new Stacking();
		stck.setClassifiers(ibks1);
		stck.setMetaClassifier(new J48());
		clas[15] = stck;
		
		stck = new Stacking();
		stck.setClassifiers(ibks1);
		stck.setMetaClassifier(new NaiveBayes());
		clas[16] = stck;
		
		stck = new Stacking();
		stck.setClassifiers(ibks2);
		stck.setMetaClassifier(new RandomForest());
		clas[17] = stck;
		
		stck = new Stacking();
		stck.setClassifiers(ibks2);
		stck.setMetaClassifier(new J48());
		clas[18] = stck;
		
		stck = new Stacking();
		stck.setClassifiers(ibks2);
		stck.setMetaClassifier(new NaiveBayes());
		clas[19] = stck;
		
		v = new Vote();
		v.setClassifiers(v3clas);
		clas[20] = v;
		
		stck = new Stacking();
		stck.setClassifiers(v3clas);
		stck.setMetaClassifier(new RandomForest());
		clas[21] = stck;
		
		stck = new Stacking();
		stck.setClassifiers(v3clas);
		stck.setMetaClassifier(new J48());
		clas[22] = stck;
		
		stck = new Stacking();
		stck.setClassifiers(v3clas);
		stck.setMetaClassifier(new NaiveBayes());
		clas[23] = stck;
		
		
		
		v = new Vote();
		v.setClassifiers(allIBK);
		clas[24] = v;
		
		
		
		ClassifierApplier[] ca = new ClassifierApplier[(args.length-3)/2];
		Thread[] ths = new Thread[(args.length-3)/2];
		
		
		
		for (int i = 0 ; i < ca.length ; i++) {

			FileInputStream fr = null;
			
			try {
				fr = new FileInputStream(args[i*2+3]);
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
			
			inst.setClassIndex(Integer.parseInt(args[i*2+4]));
			
			
			
			
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
					
					String[] splitend = args[i*2+3].split("/");
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
