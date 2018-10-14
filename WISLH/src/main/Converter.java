package main;

import java.io.File;
import java.io.IOException;

public class Converter {

	public static void main(String[] args) {
		
		if (args.length != 3) {
			System.out.println("Unexpected argument count, expecting 3 got " + args.length);
			System.out.println("Usage: Converter convert_args originDir targetDir");
			System.exit(1);
		}
		
		String convert_args = args[0];
		String originDirStr = args[1];
		String targetDirStr = args[2];
		
		File originDir = new File(originDirStr);
		File targetDir = new File(targetDirStr);
		
		if (!targetDir.exists() || !targetDir.isDirectory())
			targetDir.mkdirs();
		
		String[] images = originDir.list();
		
		Runtime run = Runtime.getRuntime();
		try {
			for (String image : images) {	
				
					
					run.exec("convert " + convert_args + " " 
					+ originDirStr + "/" + image + " " 
							+ targetDirStr + "/" + image);
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
