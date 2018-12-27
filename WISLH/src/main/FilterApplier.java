package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;

import weka.attributeSelection.BestFirst;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSink;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.imagefilter.BinaryPatternsPyramidFilter;
import weka.filters.unsupervised.instance.imagefilter.JpegCoefficientFilter;
import weka.filters.unsupervised.instance.imagefilter.SimpleColorHistogramFilter;

public class FilterApplier implements Runnable{

	Instances bppfIns = null;
	Instances jcfIns = null;
	Instances schfIns = null;
	
	Instances inst = null;
	private Thread bppfT;
	private Thread jcfT;
	private Thread schfT;
	
	public FilterApplier(Instances inst, File imageDir) {
		
		
		BestFirst bf = new BestFirst();
		JpegCoefficientFilter jcf = new JpegCoefficientFilter();
		SimpleColorHistogramFilter schf = new SimpleColorHistogramFilter();
		
		

		
		
		bppfT = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					bppfIns = Filter.useFilter(inst, bppf);
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
		
		jcfT = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					jcfIns = Filter.useFilter(inst, jcf);
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
		
		schfT = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					schfIns = Filter.useFilter(inst, schf);
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
	}
	
	public void run() {
		bppfT.start();
		jcfT.start();
		schfT.start();
		
		try {
			jcfT.join();
			bppfT.join();
			schfT.join();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		bppfIns.deleteAttributeAt(0);
		schfIns.deleteAttributeAt(0);
		jcfIns.deleteAttributeAt(0);
		
	

	}
	
	public Instances[] getFiltered() {
		
		return new Instances[] {bppfIns,  jcfIns , schfIns};
	} 
	
	public static void main(String[] args) {
		
		if (args.length != 2) {
			System.out.println("Wrong arg count, expected 2 argument got " + args.length);
			System.out.println("Usage: FilterApplier image_dir source_arff_dir");
			System.exit(1);
		}
		
		String imgDirStr = args[0];
		String sourceArffStr = args[1];
		//String arffDirStr = args[2];
		//String arffName = args[3];
		
		File imageDir = new File(imgDirStr);
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
		
		FilterApplier fa = new FilterApplier(inst,imageDir);
		fa.run(); 
		System.out.println(fa.getFiltered()[0]);
		
		
		
		
		
	}
}
