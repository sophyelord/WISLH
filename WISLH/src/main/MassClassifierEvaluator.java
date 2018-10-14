package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Random;

import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class MassClassifierEvaluator {

	public static void main(String[] args) {
		
		
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
			ca[i] = new ClassifierApplier(inst, folds, rand);
			ths[i] = new Thread(ca[i]);
			ths[i].start();
			
			
		}
		
		for (int i = 0 ; i < ths.length ; i++) {
			try {
				ths[i].join();
				Evaluation[] evals = ca[i].getEvaluations();
				
				pw.println("DATASET: " + args[i*2+3]);
				
				for (int j = 0 ; j < evals.length ; j++) {
					pw.println();
					pw.println("Classifier: " + ca[i].clasSpec[j].toString());
					pw.println(evals[j].toSummaryString());
				}
				
				pw.println();
				pw.println();
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		pw.close();
		
		
	}
}
