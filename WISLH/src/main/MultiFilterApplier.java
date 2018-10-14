package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSink;
import weka.core.converters.ConverterUtils.DataSource;

public class MultiFilterApplier {

	public static void main(String[] args) {
		
		String datasetDir = args[0];
		String arffDir = args[1];
		String sourceArffDir = args[2];
		
		Thread[] threads = new Thread[args.length-3];
		FilterApplier[] fas = new FilterApplier[args.length-3];
		
		for (int i = 3 ; i < args.length ; i++) {
			
			File imageDir = new File(datasetDir + args[i]);
			File sourceArff = new File(sourceArffDir);
			
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
		
			FilterApplier fa = new FilterApplier(inst,imageDir);
			fas[i-3] = fa;
			threads[i-3] = new Thread(fa);
			threads[i-3].start();
		}
		
		for (int i = 0 ; i < threads.length ; i++) {
			try {
				threads[i].join();
				Instances[] filtered = fas[i].getFiltered();
				
				
				DataSink.write((new File(arffDir,"bppf" + args[i+3] + ".arff")).getAbsolutePath(), filtered[0]);
				DataSink.write((new File(arffDir,"jcf" + args[i+3] + ".arff")).getAbsolutePath(), filtered[1]);
				DataSink.write((new File(arffDir,"schf" + args[i+3] + ".arff")).getAbsolutePath(), filtered[2]);
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
	
}
