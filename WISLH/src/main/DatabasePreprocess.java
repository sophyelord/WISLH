package main;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;


public class DatabasePreprocess {

	public static void main(String[] args) {
		
		if (args.length != 3) {
			
			System.out.println("Wrong arg count, expected 3 got " + args.length);
			System.out.println("Usage: DatabasePreprocess originDir targetDir arffname");
			System.exit(1);
		}
	
		String originDir = args[0];
		String targetDir = args[1];
		String arffName = args[2];
		
		File origin = new File(originDir);
		File target = new File(targetDir);
		
		if (!target.exists() || !target.isDirectory())
			target.mkdirs();
		
		File arff = new File(arffName + ".arff");
		
		if (arff.exists())
			arff.delete();
		
		PrintWriter pw = null;
		
		try {
			//arff.createNewFile();
			pw = new PrintWriter(arff);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		pw.println("@RELATION datubasea");
		pw.println("@ATTRIBUTE filename string");
		pw.print("@ATTRIBUTE class {");
		String[] classes = origin.list();
		
		pw.print(classes[0]);
		
		for (int i = 1 ; i < classes.length ; i++) 
		{
			pw.print("," + classes[i]);
			
		}
		
		pw.println("}");
		pw.println("@data");
	
		for (String clas : classes) {
			
			File classDir = new File(origin, clas);
			
			String[] instances = classDir.list();
			
			for (String instance : instances) {
				
				File instanceFile = new File(classDir, instance);
				try {
					Files.copy(instanceFile.toPath(), (new File(target, clas + instance.substring(5)).toPath()));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				pw.println(clas + instance.substring(5) + "," + clas);
			}
		}
		
		pw.close();
	}
	
}
