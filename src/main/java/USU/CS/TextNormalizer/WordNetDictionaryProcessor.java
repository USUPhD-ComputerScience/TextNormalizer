package USU.CS.TextNormalizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import USU.CS.Wordnet.PartOfSpeech;
import USU.CS.Wordnet.WordNetManager;

// this class is used to get the list of base words from wordnet,
// you will have to install wordnet first before you can use this
public class WordNetDictionaryProcessor {
	public static void main(String[] args) throws IOException {
		System.out.println("Start collecting root words from WordNet 3.0");
		collectRootWordsWithPOS("dictionary/baseWord/wordnet");
		System.out.println("Done");
	}

	// use the code provided by
	// http://www.cs.rochester.edu/research/cisd/wordnet/ to gather words from
	// WordNet 3.0
	private static void collectRootWordsWithPOS(String directory)
			throws IOException {
		Set<String> verbSet = new HashSet<>();
		Set<String> adjSet = new HashSet<>();
		Set<String> nounSet = new HashSet<>();
		Set<String> advSet = new HashSet<>();
		WordNetManager manager = new WordNetManager();
		System.out.println("...collecting ADJ");
		manager.gatherUpSynsets(PartOfSpeech.ADJ, adjSet);
		System.out.println("...collected " + adjSet.size()
				+ " words, writing to file at " + directory + "/adj.txt");
		writeToFile(directory + "/adj.txt", adjSet);
		System.out.println("...collecting ADV");
		manager.gatherUpSynsets(PartOfSpeech.ADV, advSet);
		System.out.println("...collected " + advSet.size() + " words"
				+ " words, writing to file at " + directory + "/adv.txt");
		writeToFile(directory + "/adv.txt", advSet);
		System.out.println("...collecting NOUN");
		manager.gatherUpSynsets(PartOfSpeech.NOUN, nounSet);
		System.out.println("...collected " + nounSet.size() + " words"
				+ " words, writing to file at " + directory + "/noun.txt");
		writeToFile(directory + "/noun.txt", nounSet);
		System.out.println("...collecting VERB");
		manager.gatherUpSynsets(PartOfSpeech.VERB, verbSet);
		System.out.println("...collected " + verbSet.size() + " words"
				+ " words, writing to file at " + directory + "/verb.txt");
		writeToFile(directory + "/verb.txt", verbSet);
	}

	private static void writeToFile(String fileName, Set<String> setOfWords)
			throws FileNotFoundException {
		PrintWriter pw = new PrintWriter(new File(fileName));
		for (String word : setOfWords) {
			pw.println(word);
		}
		pw.close();
	}
}
