package LDA;
import java.util.ArrayList;
import java.util.Arrays;

public class Gibbs {
	int[] vid;
	int[] did;
	int[] zid;
	int[] timestamp;
	int n;
	int v;
	int d;
	int z;
	int minTime;
	int maxTime;
	int timespan;
	int[][] VinT;
	int[][] TinD;
	int[] Zct;
	int[] docSize;
	int[] wordSize;
	int[] timeTotals;
	double[][] timeDist;
	
public Gibbs(int[] words, int[] docs, int[] topics, int[] timestamp, int[] parameters){
	d = parameters[0];
	v = parameters[1];
	n = parameters[2];
	z = parameters[3];
	minTime = parameters[4];
	maxTime = parameters[5];
	timespan = (maxTime - minTime) + 1;
	vid = words;
	did = docs;
	zid = topics;
	this.timestamp = timestamp;
	VinT = new int[v][z];
	for (int[] row : VinT){
		Arrays.fill(row, 0);
	}
	TinD = new int[z][d];
	for (int[] row: TinD){
		Arrays.fill(row, 0);
	}
	Zct = new int[z];
	Arrays.fill(Zct, 0);
	
	timeTotals = new int[timespan];
	Arrays.fill(timeTotals, 0);
	
	docSize = new int[d];
	Arrays.fill(docSize, 0);
	
	wordSize = new int[v];
	Arrays.fill(wordSize, 0);
	
	int[][] topicTimes = new int[z][timespan];
	for (int t = 0; t < z; ++t) {
		Arrays.fill(topicTimes[t], 0);
	}
	
	for (int i = 0; i < n; ++i){
		try{
		++VinT[vid[i]][zid[i]];
		++TinD[zid[i]][did[i]];
		++Zct[zid[i]];
		++docSize[did[i]];
		++wordSize[vid[i]];
		int startPt = timestamp[i] - 5;
		int endPt = timestamp[i] + 5;
		if (startPt < minTime) {
			startPt = minTime;
		}
		if (endPt > maxTime) {
			endPt = maxTime;
		}
		startPt -= minTime;
		endPt -= minTime;
		for (int j = startPt; j < endPt + 1; ++j) {
			++timeTotals[j];
			++topicTimes[zid[i]][j];
		}
		}

		catch (ArrayIndexOutOfBoundsException e){
			System.out.println("i" + "=" + i);
			System.out.println("time" + "=" + timestamp[i]);
			System.out.println("zid" + "=" + zid[i]);
		}
	}
	timeDist = new double [z][timespan];
	for (int t = 0; t < z; ++ t) {
		for (int year = 0; year < timespan; ++year) {
			timeDist[t][year] = Math.log((((topicTimes[t][year] + 10) / ( (double) Zct[t] + 10)) * 100) + 5);
		}
	}
}


public void cycle(){
	for (int i = 0; i < n; ++i){
		int t = zid[i];
		-- TinD[t][did[i]];
		-- VinT[vid[i]][t];
		-- Zct[t];
		double[] Prob = new double[z];
		for (int top = 0; top < z; ++top){
			Prob[top] = (( (VinT[vid[i]][top] + .05) * (TinD[top][did[i]] + 1) ) / (double) (Zct[top] + d));
		}
		Multinomial distribution = new Multinomial(Prob);
		t = distribution.sample();
		zid[i] = t;
		++ TinD[t][did[i]];
		++ VinT[vid[i]][t];
		++ Zct[t];
	}
	int[][] topicTimes = new int[z][timespan];
	for (int t = 0; t < z; ++t) {
		Arrays.fill(topicTimes[t], 0);
	}
	for (int i = 0; i < n; ++i){
		int startPt = timestamp[i] - 5;
		int endPt = timestamp[i] + 5;
		if (startPt < minTime) {
			startPt = minTime;
		}
		if (endPt > maxTime) {
			endPt = maxTime;
		}
		startPt -= minTime;
		endPt -= minTime;
		for (int j = startPt; j < endPt + 1; ++j) {
			++topicTimes[zid[i]][j];
		}
	}
	for (int t = 0; t < z; ++ t) {
		for (int year = 0; year < timespan; ++year) {
			timeDist[t][year] = Math.log((((topicTimes[t][year] + 10) / ( (double) Zct[t] + 10)) * 100) + 5);
		}
	}
}

public String topicmap(){
	String map = "Topic One";
	for (int i = 0; i < v; ++i){
		int rounded = (int) (VinT[i][0]);
		map = map + " " + rounded;
	}
	map = map + "\nTopic Two:";
	for (int i = 0; i < v; ++i){
		int rounded = (int) (VinT[i][1]);
		map = map + " " + rounded;
	}
	map = map + "\nTopic Three:";
	for (int i = 0; i < v; ++i){
		int rounded = (int) (VinT[i][2]);
		map = map + " " + rounded;
	}
	return map + "\n";
}
	
public double perplex() {
	double sumLog = 0;
	for (int i = 0; i < n; ++i) {
		int word = vid[i];
		int doc = did[i];
		int t = zid[i];
		double prob = ((TinD[t][doc] + 1) / (double) (docSize[doc] + d)) * ((VinT[word][t] + 1) / (double) (Zct[t] + v));
		sumLog += Math.log(prob);
	}
	sumLog = 0-(sumLog / (double) n);
	double perplexity = Math.exp(sumLog);
	return perplexity;
}

public int[][] topics(int topNwords) {
	// Extracts a visualization of each topic according to the formula in
	// Blei and Lafferty, 2009. Essentially, it ranks words by this weighting:
	// probability in topic * log (prob in topic / geometric mean prob in all topics)
	// We begin by converting the integer counts to a probability matrix, phi.
	
	double[][] phi = this.phi();
	
	// Then we calculate the geometric mean of each row in phi, which
	// is a vector of per-topic probabilities for a specific word.
	
	double[] geomMean = new double[v];
	for (int w = 0; w < v; ++w) {  
		// double prod = 1.0;
		// for (double prob : phi[w]) { 
		// 	prod *= prob; 
		// }
		// double inverseZ = 1 / (double) z;
		// geomMean[w] = Math.pow(prod, inverseZ);
		double logSum = 0.0;
		for (double prob: phi[w]) {
			logSum += Math.log(prob);
		}
		geomMean[w] = Math.exp(logSum/z);
	}
	
	// The method is going to return a matrix listing the indices of
	// the top-ranked N words in each topic. To achieve that it produces
	// a vector of weighted scores for words in each topic and then sends
	// them to a method that simultaneously sorts scores and original indices.
	// Note that we are now proceeding through phi column-wise.
	
	int[][] topicArray = new int [z][topNwords];
	for (int t = 0; t < z; ++t) {
		double[] weightedTopic = new double[v];
		for (int w = 0; w < v; ++w) {
			weightedTopic[w] = phi[w][t] * Math.log(phi[w][t] / geomMean[w]);
		}
		int[] ranking = ranks(weightedTopic);
		// for (int i = 0; i < v; ++i) {
		// 	System.out.print(ranking[i] + "," + weightedTopic[i] + " ");
		// }
		// System.out.print("\n");
		topicArray[t] = Arrays.copyOf(ranking, topNwords);
	}
	return topicArray;
}

protected double[][] phi() {
	// turns the matrix of words-in-topics into a probability
	// matrix describing the probability that a given word will be produced
	// by a given topic
	double[][] phiArray = new double [v][z];
	for (int t = 0; t < z; ++t) {
		for (int w = 0; w < v; ++ w) {
			phiArray[w][t] = (VinT[w][t] + 1) / (double) (Zct[t] + 1);     // # of this word in this topic over total # in topic
		}
	}
	return phiArray;
}

private static int[] ranks(double[] weightedTopic) {
	int len = weightedTopic.length;
	double temp = 0;
	int tempInt = 0;
	int[] indices = new int[len];
	for (int i = 0; i < len; ++i) {
		indices[i] = i;
	}
	boolean doMore = true;
	while (doMore == true) {
		doMore = false;
		for (int i = 0; i < len-1; ++i) {
			if (weightedTopic[i] < weightedTopic[i + 1]) {
				temp = weightedTopic[i];
				weightedTopic[i] = weightedTopic[i+1];
				weightedTopic[i+1] = temp;
				tempInt = indices[i];
				indices[i] = indices[i + 1];
				indices[i + 1] = tempInt;
				doMore = true;
			}
		}
	}
	return indices;
}

public void exportVinT(String outPath) {
	ArrayList<String> export = new ArrayList<String>();
	for (int t = 0; t < z; ++t) {
		String outLine = t + ",";
		for (int w = 0; w < v; ++w) {
			outLine = outLine + VinT[w][t] + ",";
		}
		export.add(outLine + "\n");
	}
	LineWriter outFile = new LineWriter(outPath, false);
	String[] exportArray = export.toArray(new String[export.size()]);
	outFile.send(exportArray);
}

public void exportTinD(String outPath) {
	ArrayList<String> export = new ArrayList<String>();
	for (int t = 0; t < z; ++t) {
		String outLine = "";
		for (int doc = 0; doc < d; ++doc) {
			outLine = outLine + TinD[t][doc];
			if (doc < (d-1)) {
				outLine = outLine + ",";
			}
		}
		export.add(outLine + "\n");
	}
	LineWriter outFile = new LineWriter(outPath, false);
	String[] exportArray = export.toArray(new String[export.size()]);
	outFile.send(exportArray);
}

public void exportKL(String outPath) {
	// export the 5 topics closest to each topic by KL divergence
	
	ArrayList<String> export = new ArrayList<String>();
	double[][] phi = this.phi();
	
	double[][] klDist = new double[z][z];
	for (int t1 = 0; t1 < z; ++t1) {
		for (int t2 = 0; t2 < z; ++t2) {
			double distance = 0.0;
			for (int w = 0; w < v; ++w) {
				distance += phi[w][t1] * Math.log(phi[w][t1] / phi[w][t2]);
			}
			klDist[t1][t2] = 0 - distance;
			// make it negative so lowest distances rank highest
		}
	}
	for (int t = 0; t < z; ++t) {
		export.add("Topic " + t);
		int[] ranking = ranks(klDist[t]);
		export.add(ranking[0] + "\n" + ranking[1] + "\n" + ranking[2] + "\n" + ranking[3] + "\n" + ranking[4] + "\n" + ranking[5]);
	}
	LineWriter outFile = new LineWriter(outPath, false);
	String[] exportArray = export.toArray(new String[export.size()]);
	outFile.send(exportArray);
}

public void exportTimeline(String outPath) {
	
	ArrayList<String> export = new ArrayList<String>();
	for (int t = 0; t < z; ++t){
		for (int year = 0; year < timespan; ++ year) {
			String outLine = t + ": " + year + ": " + timeDist[t][year];
			export.add(outLine + "\n");
		}
	}
	LineWriter outFile = new LineWriter(outPath, false);
	String[] exportArray = export.toArray(new String[export.size()]);
	outFile.send(exportArray);
}

public void topicTimelines(String outPath) {
	
	ArrayList<String> export = new ArrayList<String>();
	
	int[][] topicTimes = new int[z][timespan];
	for (int t = 0; t < z; ++t) {
		Arrays.fill(topicTimes[t], 0);
	}
	double[][] topicProminence = new double[z][timespan];
	for (int t = 0; t < z; ++t) {
		Arrays.fill(topicProminence[t], 0);
	}
	
	for (int i = 0; i < n; ++i){
		int startPt = timestamp[i] - 5;
		int endPt = timestamp[i] + 5;
		if (startPt < minTime) {
			startPt = minTime;
		}
		if (endPt > maxTime) {
			endPt = maxTime;
		}
		startPt -= minTime;
		endPt -= minTime;
		for (int j = startPt; j < endPt + 1; ++j) {
			++topicTimes[zid[i]][j];
		}
	}
	for (int t = 0; t < z; ++ t) {
		for (int year = 0; year < timespan; ++year) {
			topicProminence[t][year] = (topicTimes[t][year] + 1) / ( (double) timeTotals[t] + 1);
		}
	}
	
	for (int t = 0; t < z; ++t){
		String outLine = Double.toString(topicProminence[t][0]);
		for (int year = 1; year < timespan; ++ year) {
			outLine = outLine + "," + Double.toString(topicProminence[t][year]);
		}
		export.add(outLine);
	}
	
	LineWriter outFile = new LineWriter(outPath, false);
	String[] exportArray = export.toArray(new String[export.size()]);
	outFile.send(exportArray);
}

}
