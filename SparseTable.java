package LDA;
import java.util.ArrayList;
import static java.util.Arrays.fill;
import java.util.Random;

public class SparseTable {
	ArrayList<String> wordTypes = new ArrayList<String>();
	ArrayList<String> docTypes = new ArrayList<String>();
	static int D = 0;
	// number of documents in table
	static int V = 0;
	// number of words in vocabulary
	static int N = 0;
	// number of tokens in corpus
	static int minTime = 3000;
	static int maxTime = 0;
	int[] docIDs;
	int[] wordIDs;
	int[] timestamps;
	int[] wordCounts;
	static Random randomize = new Random();

	public SparseTable(String filePath) {
		LineReader inFile = new LineReader(filePath);
		String[] fileLines = inFile.readlines();
		int position = 0;
		int wordCount = 0;

		// we start by scanning the table to establish vocabulary, doc list, and corpus size
		for (String line : fileLines) {
			String[] tokens = line.split("[\t]");
			int tokenCount = tokens.length;
			if (tokenCount != 4) {
				System.out.println("Error: tokenCount not equal to 4 at " + line);
				continue;
			}
			String doc = tokens[0];
			String word = tokens[1];
			String occurs = tokens[2];
			String docTime = tokens[3];
			position = docTypes.indexOf(doc);
			if (position < 0) {
				docTypes.add(doc);
			}
			position = wordTypes.indexOf(word);
			if (position < 0) {
				wordTypes.add(word);
			}
			wordCount = Integer.parseInt(occurs);
			N += wordCount;
			int time = Integer.parseInt(docTime);
			if (time < minTime) {
				minTime = time;
			}
			if (time > maxTime) {
				maxTime = time;
			}
		}
		
		D = docTypes.size();
		V = wordTypes.size();
		docIDs = new int[N];
		wordIDs = new int[N];
		timestamps = new int[N];
		wordCounts = new int[V];
		fill(wordCounts, 0);
		System.out.println("Corpus size will be " + N);

		// now on a second scan, we fill the arrays
		int index = 0;
		int did = 0;
		int vid = 0;
		int stopIndex = 0;
		for (String line : fileLines) {
			String[] tokens = line.split("[\t]");
			int tokenCount = tokens.length;
			if (tokenCount != 4) {
				System.out.println("Error: tokenCount not equal to 4 at " + line);
				continue;
			}
			String doc = tokens[0];
			String word = tokens[1];
			String occurs = tokens[2];
			String docTime = tokens[3];
			int time = Integer.parseInt(docTime);
			did = docTypes.indexOf(doc);
			vid = wordTypes.indexOf(word);
			wordCount = Integer.parseInt(occurs);
			stopIndex = index + wordCount;
			wordCounts[vid] += wordCount;
			if (stopIndex >= N) {
				for (int i = index; i < N; ++i) {
					docIDs[i] = did;
					wordIDs[i] = vid;
					timestamps[i] = time;
				}
				continue;
			}
			fill(docIDs, index, stopIndex + 1, did);
			fill(wordIDs, index, stopIndex + 1, vid);
			fill(timestamps, index, stopIndex + 1, time);
			index = stopIndex;
		}
	}

	public void shuffle() {
		// randomizes the sequence of tokens in the main corpus arrays
		int temp = 0;
		for (int i = 0; i < N; ++i) {
			int randomPos = randomize.nextInt(N);
			temp = docIDs[i];
			docIDs[i] = docIDs[randomPos];
			docIDs[randomPos] = temp;
			temp = wordIDs[i];
			wordIDs[i] = wordIDs[randomPos];
			wordIDs[randomPos] = temp;
			temp = timestamps[i];
			timestamps[i] = timestamps[randomPos];
			timestamps[randomPos] = temp;
		}
	}
	
	public int[] docArray() {
		return docIDs;
	}
	
	public int[] wordArray() {
		return wordIDs;
	}
	
	public int[] timeline() {
		return timestamps;
	}
	
	public int[] constants() {
		int[] fivetuple = new int[5];
		fivetuple[0] = D;
		fivetuple[1] = V;
		fivetuple[2] = N;
		fivetuple[3] = minTime;
		fivetuple[4] = maxTime;
		
		return fivetuple;
	}
	
	public void exportTopics(String outPath, int[][] topicArray) {
		int Z = topicArray.length;
		ArrayList<String> export = new ArrayList<String>();
		for (int t = 0; t < Z; ++t) {
			export.add("Topic " + t);
			for (int ID : topicArray[t]) {
				export.add(wordTypes.get(ID));
			}
			export.add("----------");
		}
		LineWriter outFile = new LineWriter(outPath, false);
		String[] exportArray = export.toArray(new String[export.size()]);
		outFile.send(exportArray);
	}
	
	public void exportWords(String outPath) {
		ArrayList<String> export = new ArrayList<String>();
		for (int w = 0; w < V; ++w) {
			export.add(w + ": " + wordTypes.get(w) + ": " + wordCounts[w]);
		}
		LineWriter outFile = new LineWriter(outPath, false);
		String[] exportArray = export.toArray(new String[export.size()]);
		outFile.send(exportArray);
	}

	public void exportDocs(String outPath) {
		ArrayList<String> export = new ArrayList<String>();
		for (int doc = 0; doc < D; ++doc) {
			export.add(docTypes.get(doc));
		}
		LineWriter outFile = new LineWriter(outPath, false);
		String[] exportArray = export.toArray(new String[export.size()]);
		outFile.send(exportArray);
	}

}
