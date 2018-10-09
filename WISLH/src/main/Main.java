package main;

import java.io.File;

import core.SpecInterpreter;
import spec.SpecParser;
import spec.WekaSpec;

public class Main {

	public static void main(String[] args) {
		
		
		if (args.length != 2) {
			System.out.println("Unexpected argument count, expecting 1 got " + (args.length-1));
			System.out.println("Usage: wislh filename");
			System.out.println("File must be a xml with proper format, see documentation");
		}

		
		File f = new File(args[1]);
		
		if (!f.exists()) {
			System.out.println("File " + args[1] + "does not exist");
		}
		
		
		SpecParser sp = new SpecParser();
		WekaSpec ws = sp.getSpecFromFile(f);
		
		SpecInterpreter interpreter = new SpecInterpreter();
		//interpreter.interpret(ws);
		
		
	}

}
