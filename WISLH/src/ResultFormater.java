import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class ResultFormater {

	public static void main(String[] args) {
		
		
		File f = new File(args[0]);
		
		String[] splited = args[0].split("/");
		StringBuilder sb = new StringBuilder();
		for (int i = 0 ; i < splited.length - 1 ; i++) {
			sb.append(splited[i]);
			sb.append('/');
		}
		sb.append("Corrected"+splited[splited.length - 1]);
		File fout = new File(sb.toString());
		
		try {
			fout.createNewFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
			fis = new FileInputStream(f);
			fos = new FileOutputStream(fout);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		
		String str = null;
		
		
		try {
			while ((str = br.readLine()) != null) {
				
				sb = new StringBuilder();
				splited = str.split(" ");
				
				sb.append(splited[0]);
				sb.append(";");
				
				for (int i = 1 ; i < splited.length - 1 ; i++) {
					sb.append(splited[i]);
				}
				
				sb.append(";");
				sb.append(splited[splited.length - 1]);
				sb.append('\n');
				bw.write(sb.toString());
			}
			bw.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
}
