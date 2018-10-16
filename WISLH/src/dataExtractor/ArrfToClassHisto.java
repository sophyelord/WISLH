package dataExtractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class ArrfToClassHisto {

	public static void main(String[] args) {
		
		if (args.length != 2) {
			System.out.println("Wrong arg count, expected 2 arguments got " + args.length);
			System.out.println("Usage: ArffToClassHisto arff_file output_file");
			System.exit(1);
		}
		
		File arff = new File(args[0]);
		File formatedFile = new File(args[1]);
		
		
		FileInputStream arffStream = null;
		
		try {
			arffStream = new FileInputStream(arff);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Instances inst = null;
		try {
			inst = DataSource.read(arffStream);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		inst.setClassIndex(1);
		int histoCount = inst.numClasses();
		int[] histogram = new int[histoCount];
		
		
		for (Instance ins : inst) {
			
			
			histogram[(int)ins.classValue()]++;
		}
		
		PrintWriter pw = null;
		try {
			 pw = new PrintWriter(formatedFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		pw.println("Class count");
		for (int i = 0 ; i < histoCount ; i++) {
			pw.println("\"" + inst.classAttribute().value(i) + "\" " + histogram[i]);
		}
		
		pw.close();
	}
}
