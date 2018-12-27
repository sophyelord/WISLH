package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;

import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.CorrelationAttributeEval;
import weka.attributeSelection.GainRatioAttributeEval;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSink;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;

public class MultiFilterApplier {

	private static class ASSapplier implements Runnable{

		private Instances inst;
		private Instances nInst;
		private AttributeSelection as;
		
		
		public ASSapplier(AttributeSelection as, Instances inst) {
			this.inst = inst;
			this.as = as;
		}
		
		@Override
		public void run() {
			
			try {
				as.setInputFormat(inst);
				nInst = Filter.useFilter(inst, as);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		public Instances getNewInstances() {
			return nInst;
		}
		
		
		public Instances getInstances() {
			return inst;
		}
	}
	
	public static void main(String[] args) {
		
		if (args.length < 2) {
			System.out.println("Wrong arg count, expected at least 2 arguments got " + args.length);
			System.out.println("Usage: MultiFilterApplier arff_dir source_arff [image_dir]");
			System.exit(1);
		}
		
		
		String arffDir = args[0];
		String sourceArffDir = args[1];
		
		Thread[] threads1 = new Thread[args.length-2];
		Thread[] threads2 = new Thread[threads1.length*9];
		ASSapplier[] appliers1 = new ASSapplier[args.length-2];
		ASSapplier[] appliers2 = new ASSapplier[threads1.length*9];
		
		Class<ASEvaluation>[] aseA = (Class<ASEvaluation>[]) new Class[]{InfoGainAttributeEval.class,GainRatioAttributeEval.class,CorrelationAttributeEval.class};
		String[] asEstr = {"InfGain","GainRt","Correl"};
		int[] varSizes = new int[3];
		
		for (int i = 2 ; i < args.length ; i++) {
			
			
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
			BestFirst bf = new BestFirst();
			CfsSubsetEval cse = new CfsSubsetEval();
			AttributeSelection as = new AttributeSelection();
			as.setSearch(bf);
			as.setEvaluator(cse);
			ASSapplier asap = new ASSapplier(as, inst);
			appliers1[i-2] = asap;
			threads1[i-2] = new Thread(asap);
			threads1[i-2].start();
		}
		
		for (int i = 0 ; i < threads1.length ; i++)
			try {
				threads1[i].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		for (int i = 0 ; i < threads1.length ; i++) {
			try {
				
				Instances nInst = appliers1[i].getNewInstances();
				Instances inst = appliers1[i].getInstances();
				System.out.println("nInst: "+nInst.numAttributes());
				System.out.println("inst: "+inst.numAttributes());
				DataSink.write((new File(arffDir,"BF" + args[i+2] + ".arff")).getAbsolutePath(), nInst);
				
				
				int n = nInst.numAttributes();
				int nHalf = n/2;
				int nNHalf = n + nHalf;
				
				if (nNHalf > inst.numAttributes()) {
					nNHalf = inst.numAttributes();
				}
				
				varSizes[0] = n;
				varSizes[1] = nHalf;
				varSizes[2] = nNHalf;
				
				for (int j = 0 ; j < varSizes.length ; j++) {
					
					for (int k = 0 ; k < aseA.length ; k++) {
						Ranker r = new Ranker();
						r.setNumToSelect(varSizes[j]);
						AttributeSelection as = new AttributeSelection();
						as.setSearch(r);
						Constructor<ASEvaluation> c = aseA[k].getDeclaredConstructor();
						ASEvaluation asss = c.newInstance();
						as.setEvaluator(asss);
						ASSapplier ass = new ASSapplier(as, inst);
						appliers2[i*varSizes.length*aseA.length + j*aseA.length + k] = ass;
						threads2[i*varSizes.length*aseA.length + j*aseA.length + k] = new Thread(ass);
						threads2[i*varSizes.length*aseA.length + j*aseA.length + k].start();
					}
					
				}
				
				
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		for (int i = 0 ; i < threads1.length ; i++) {
			for (int j = 0 ; j < varSizes.length ; j++) {
				for (int k = 0 ; k <  aseA.length ; k++){
						try {
							threads2[i*varSizes.length*aseA.length + j*aseA.length + k].join();
							Instances nInst = appliers2[i*varSizes.length*aseA.length + j*aseA.length + k].getNewInstances();
							DataSink.write((new File(arffDir, asEstr[k] + varSizes[j] + args[i+2] + ".arff")).getAbsolutePath(), nInst);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				}
			}
		}
		
		
	}
	
}
