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
					pw.print(ca[i].clasSpec[j].toString());
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
}
