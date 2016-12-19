package USU.CS.TextNormalizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import USU.CS.Vocabulary.BaseWord;
import USU.CS.Vocabulary.Variant;

public class DictionaryChecker {
	private static Set<BaseWord> findDuplications(String fileName,
			boolean writeDown) throws FileNotFoundException {
		int wordcount = 0;
		Set<BaseWord> basewordSet = new HashSet<>();
		Scanner br = null;
		br = new Scanner(new FileReader(fileName));
		String POS = getPOSfromFilename(fileName);
		while (br.hasNextLine()) {
			String[] words = br.nextLine().split("\\s+");
			try {
				BaseWord bword = new BaseWord();
				bword.onCreate(words[0], POS);
				for (int i = 1; i < words.length; i++) {
					if (words[i].length() > 1) {
						Variant var = new Variant();
						var.onCreate(words[i], "UNKNOWN");
						bword.addVariant(var);
						wordcount++;
					}
				}
				if (basewordSet.contains(bword)) {
					System.out.println(bword.toFullTextForm());
				} else {
					basewordSet.add(bword);
					wordcount++;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		br.close();
		if (writeDown) {
			sortAndWriteWordListToFile(fileName, basewordSet);
		}
		System.out.println(wordcount);
		return basewordSet;
	}

	private static void reworkWordNetIrregulars(String fileName,
			boolean writeDown) throws FileNotFoundException {
		int wordcount = 0;
		Map<String, BaseWord> basewordSet = new HashMap<>();
		Scanner br = null;
		br = new Scanner(new FileReader(fileName));
		String POS = getPOSfromFilename(fileName);
		while (br.hasNextLine()) {
			String[] words = br.nextLine().split("\\s+");
			try {
				BaseWord bword = basewordSet.get(words[0]);
				if (bword == null) {
					bword = new BaseWord();
					bword.onCreate(words[0], POS);
					basewordSet.put(words[0], bword);
					wordcount++;
				}
				for (int i = 1; i < words.length; i++) {
					if (words[i].length() > 1) {
						Variant var = new Variant();
						var.onCreate(words[i], "UNKNOWN");
						bword.addVariant(var);
						wordcount++;
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (writeDown)
			sortAndWriteWordListToFile(fileName, basewordSet.values());
		System.out.println("Done!");
	}

	private static void sortAndWriteWordListToFile(String fileName,
			Collection<BaseWord> basewordList) throws FileNotFoundException {
		System.out.println("write to file!");
		List<BaseWord> theList = new ArrayList<>(basewordList);
		Collections.sort(theList);
		PrintWriter pw = new PrintWriter(new File(fileName));
		for (BaseWord bw : theList) {
			pw.println(bw.toFullTextForm());
		}
		pw.close();
	}

	// elimination: remove the overlapped words in input file
	private static void findOverlappedWords(String fileName,
			Set<BaseWord> baseWords, boolean elimination)
			throws FileNotFoundException {
		Set<String> wordSet = new HashSet<String>();
		Scanner br = null;
		br = new Scanner(new FileReader(fileName));
		String POS = getPOSfromFilename(fileName);
		while (br.hasNextLine()) {
			String[] words = br.nextLine().split("\\s+");
			try {
				BaseWord bword = new BaseWord();
				bword.onCreate(words[0], POS);
				if (baseWords.contains(bword)) {
					System.out.println(bword.toFullTextForm());
				} else {
					wordSet.add(words[0]);
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		br.close();
		if (elimination) {
			PrintWriter pw = new PrintWriter(new File(fileName));
			for (String word : wordSet) {
				pw.println(word);
			}
			pw.close();
		}
		System.out.println("Done!");
	}

	private static String getPOSfromFilename(String fileName) {
		if (fileName.contains("adj"))
			return "JJ";
		if (fileName.contains("adv"))
			return "JJ";
		if (fileName.contains("noun"))
			return "NN";
		if (fileName.contains("verb"))
			return "VB";
		return "UNKNOWN";
	}

	public static void main(String[] args) throws FileNotFoundException {

		Set<BaseWord> basewordSet = findDuplications(
				"dictionary/baseWord/newwords/adj.txt", true);
		findOverlappedWords("dictionary/baseWord/wordnet/adv.txt", basewordSet,
				false);
		// reworkWordNetIrregulars(
		// "D:\\EclipseWorkspace\\KeywordAnalysisForReview\\lib\\dictionary\\baseWord\\wordnet\\verb_irr.txt",
		// true);
	}
}
