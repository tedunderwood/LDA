package LDA;
import java.io.*;

public class LineWriter {
	File fileName;
	boolean append;
	
public LineWriter(String dirPath, boolean append) {
	this.fileName = new File(dirPath);
	this.append = append;
}

public void send(String[] lineArray) {
	try {
		BufferedWriter fileout = new BufferedWriter(new FileWriter(fileName, append));
		// The boolean argument to FileWriter is whether to append.
		for(String line : lineArray) {
			fileout.write(line + "\n");
		}
		fileout.close();
	}
	catch (IOException e){
		System.out.println("Exception: " + e);
	}
	}
}

