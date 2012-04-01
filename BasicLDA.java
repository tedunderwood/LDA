package LDA;
import java.util.Scanner;
import java.util.Random;

public class BasicLDA {
	
	static int[] corpusConstants;

	public static void main(String[] args) {
		
		System.out.println(java.lang.Runtime.getRuntime().maxMemory());
		System.out.print("Source file for corpus (in LDA dir, omit .txt extension): ");
		Scanner keyboard = new Scanner(System.in);
		String userInput = keyboard.nextLine();
		String inPath = "/Users/tunderwood/LDA/" + userInput + ".txt";
		String outPath = "/Users/tunderwood/LDA/" + userInput + "Model.txt";
		
		System.out.print("Number of topics? ");
		userInput = keyboard.nextLine();
		int Z = Integer.parseInt(userInput);
		
		System.out.print("Number of iterations? ");
		userInput = keyboard.nextLine();
		int iterations = Integer.parseInt(userInput);
		
		SparseTable corpus = new SparseTable(inPath);
		corpus.exportWords("/Users/tunderwood/LDA/Words.txt");
		corpus.exportDocs("/Users/tunderwood/LDA/DocIDs.txt");
		corpus.shuffle();
		
		corpusConstants = corpus.constants();
		int D = corpusConstants[0];
		int V = corpusConstants[1];
		int N = corpusConstants[2];
		int minTime = corpusConstants[3];
		int maxTime = corpusConstants[4];
		int[] parameters = {D, V, N, Z, minTime, maxTime};
		System.out.println("D " + D + " V " + V + " N " + N + " Z " + Z + " min " + minTime + " max " + maxTime);
		
		Random randomize = new Random();
		int[] randomTopics = new int[N];
		for (int i = 0; i < N; ++i) {
			randomTopics[i] = randomize.nextInt(Z);
		}
		
		Gibbs sampler = new Gibbs(corpus.wordArray(), corpus.docArray(), randomTopics, corpus.timeline(), parameters);
		sampler.exportVinT("/Users/tunderwood/LDA/Vint.txt");
		
		double perplexity = 0;
		System.out.println("Begin iterations.");
		for (int i = 0; i < iterations; ++i) {
			sampler.cycle();
			perplexity = sampler.perplex();
			System.out.println("Iter " + i + ": " + perplexity);
			// String cyclePath = "/Users/tunderwood/LDA/iter" + i + ".txt";
			// sampler.exportTimeline(cyclePath);
			if (i % 20 == 1) {
				corpus.exportTopics(outPath, sampler.topics(100));
				sampler.exportKL("/Users/tunderwood/LDA/KLdivergence.txt");
				sampler.exportTinD("/Users/tunderwood/LDA/ThetaDistrib.txt");
			}
		}
		
		corpus.exportTopics(outPath, sampler.topics(100));
		sampler.exportKL("/Users/tunderwood/LDA/KLdivergence.txt");
		sampler.exportTinD("/Users/tunderwood/LDA/ThetaDistrib.txt");
		
	}

}
