package LDA;
import java.io.*;
import java.util.Vector;

public class LineReader {
	File fileName;
	
public LineReader(String dirPath) {
	this.fileName = new File(dirPath);
}

public String[] readlines() {
	try{
		BufferedReader filein = new BufferedReader(new FileReader(fileName));
		Vector<String> holding = new Vector<String>(1000,1000);
		int count = 0;
		while (filein.ready()){
			String line = filein.readLine();
			holding.add(line);
			++ count;
		}
		filein.close();
		String[] lineArray = new String[count];
		for (int i = 0; i < count; ++i) {
			String line = holding.get(i);
			lineArray[i] = line;
		}
		return lineArray;
	}
	catch (IOException e){
		System.out.println("Exception: " + e);
		String[] lineArray = new String[1];
		lineArray[0] = null;
		return lineArray;
	}
}

}
