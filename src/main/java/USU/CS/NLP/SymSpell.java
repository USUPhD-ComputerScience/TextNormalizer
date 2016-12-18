package USU.CS.NLP;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.collections.comparators.ComparatorChain;

import USU.CS.Utils.Util;

public class SymSpell implements Serializable {
	/**
	 * 
	 */
	private int STAT_SuccessfullyCorrected = 0;
	private int STAT_NotCorrected = 0;
	private static final long serialVersionUID = 4601101055683681937L;
	private static final int editDistanceMax = 3;
	private static final int verbose = 2;
	private static final Map<String, String> basewordPOS = new HashMap<>();
	public static final String TRAINDIRECTORY = "dictionary/correctorTraining/";
	private static SymSpell instance = null;

	public static SymSpell getInstance() {
		if (instance == null) {
			instance = new SymSpell();
			long start = System.currentTimeMillis();
			// System.out.println(">>Creating improvised full dictionary ...");
			// instance.createDictionary("lib/dictionary/improvised/",
			// instance.dictionary);
			// System.out.println("Took " + (System.currentTimeMillis() - start)
			// / 1000 + " seconds!");
			// start = System.currentTimeMillis();
			System.out.println(">Creating improvised basic dictionary ...");
			instance.createDictionary("dictionary/baseWord/wordnet/",
					"dictionary/baseWord/newwords/",
					"dictionary/baseWord/misc/", instance.baseDictionary);
			System.out.println(
					"Took " + (System.currentTimeMillis() - start) / 1000
							+ " seconds!");
		}
		return instance;
	}

	private SymSpell() {

	}

	// 0: top suggestion
	// 1: all suggestions of smallest edit distance
	// 2: all suggestions <= editDistanceMax (slower, no early termination)
	private class DictionaryItem implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 5469903422338636116L;
		public String term = "";
		public List<EditItem> suggestions = new ArrayList<>();
		public int count = 0;

		public DictionaryItem() {

		}

		@Override
		public int hashCode() {
			return term.hashCode();
		}

		@Override
		public boolean equals(Object arg0) {
			return term.equals(((DictionaryItem) arg0).term);
		}
	}

	private class EditItem implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2723131945556114372L;
		public String term = "";
		public int distance = 0;

		public EditItem() {

		}

		@Override
		public int hashCode() {
			return term.hashCode();
		}

		@Override
		public boolean equals(Object arg0) {
			return term.equals(((EditItem) arg0).term);
		}

	}

	private class SuggestItem implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3500731792693009030L;
		public String term = "";
		public int distance = 0;
		public int count = 0;

		public SuggestItem() {
			// TODO Auto-generated constructor stub
		}

		@Override
		public int hashCode() {
			return term.hashCode();
		}

		@Override
		public boolean equals(Object arg0) {
			return term.equals(((SuggestItem) arg0).term);
		}

	}

	private Map<String, DictionaryItem> baseDictionary = new HashMap<String, DictionaryItem>();

	// create a non-unique wordlist from sample text
	// language independent (e.g. works with Chinese characters)
	private static Iterable<String> parseWords(String text) {

		return Arrays
				.asList(Util.removeNonChars(text).toLowerCase().split(" "));
	}

	// for every word there all deletes with an edit distance of
	// 1..editDistanceMax created and added to the dictionary
	// every delete entry has a suggestions list, which points to the original
	// term(s) it was created from
	// The dictionary may be dynamically updated (word frequency and new words)
	// at any time by calling createDictionaryEntry
	private boolean createDictionaryEntry(String key,
			Map<String, DictionaryItem> dictionary) {
		boolean result = false;
		if (key.length() < 3)
			return result;
		DictionaryItem value;
		value = dictionary.get(key);
		if (value != null) {
			// already exists:
			// 1. word appears several times
			// 2. word1==deletes(word2)
			value.count += 1;
		} else {
			value = new DictionaryItem();
			value.count += 1;
		}
		dictionary.put(key, value);

		// edits/suggestions are created only once, no matter how often word
		// occurs
		// edits/suggestions are created only as soon as the word occurs in the
		// corpus,
		// even if the same term existed before in the dictionary as an edit
		// from another word

		if (value.term.isEmpty()) {
			result = true;
			value.term = key;
			for (EditItem delete : edits(key, 0, true)) {
				EditItem suggestion = new EditItem();
				suggestion.term = key;
				suggestion.distance = delete.distance;

				DictionaryItem value2;
				value2 = dictionary.get(delete.term);
				if (value2 != null) {
					// already exists:
					// 1. word1==deletes(word2)
					// 2. deletes(word1)==deletes(word2)
					if (!value2.suggestions.contains(suggestion))
						addLowestDistance(value2.suggestions, suggestion);
				} else {
					value2 = new DictionaryItem();
					value2.suggestions.add(suggestion);
					dictionary.put(delete.term, value2);
				}
			}
		}

		return result;
	}

	// create a frequency disctionary from a corpus
	private void createDictionary(String corpus, String corpus2, String misc,
			Map<String, DictionaryItem> dictionary) {
		File fcheckExist = new File(corpus);
		if (!fcheckExist.exists()) {
			System.err.println("File not found: " + corpus);
			return;
		}
		long wordCount = 0;
		Scanner br = null;
		try {
			List<String> fileLists = Util.listFilesForFolder(corpus);
			for (String fileName : fileLists) {
				try {
					br = new Scanner(new FileReader(fileName));
					while (br.hasNextLine()) {
						for (String key : parseWords(br.nextLine())) {
							if (key.length() < 3)
								continue;

							if (createDictionaryEntry(key, dictionary))
								wordCount++;
							if (fileName.contains("adj"))
								basewordPOS.put(key, "JJ");
							if (fileName.contains("adv"))
								basewordPOS.put(key, "JJ");
							if (fileName.contains("noun"))
								basewordPOS.put(key, "NN");
							if (fileName.contains("verb"))
								basewordPOS.put(key, "VB");
						}
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					if (br != null)
						br.close();
				}

			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (br != null)
				br.close();
		}

		fcheckExist = new File(corpus2);
		if (!fcheckExist.exists()) {
			System.err.println("File not found: " + corpus2);
			return;
		}
		br = null;
		try {
			List<String> fileLists = Util.listFilesForFolder(corpus2);
			for (String fileName : fileLists) {
				try {
					br = new Scanner(new FileReader(fileName));
					while (br.hasNextLine()) {
						for (String key : parseWords(br.nextLine())) {
							if (key.length() < 3)
								continue;

							if (createDictionaryEntry(key, dictionary))
								wordCount++;
							if (fileName.contains("adj"))
								basewordPOS.put(key, "JJ");
							if (fileName.contains("adv"))
								basewordPOS.put(key, "JJ");
							if (fileName.contains("noun"))
								basewordPOS.put(key, "NN");
							if (fileName.contains("verb"))
								basewordPOS.put(key, "VB");
						}
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					if (br != null)
						br.close();
				}

			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (br != null)
				br.close();
		}

		fcheckExist = new File(misc);
		if (!fcheckExist.exists()) {
			System.err.println("File not found: " + misc);
			return;
		}
		br = null;
		try {
			List<String> fileLists = Util.listFilesForFolder(misc);
			for (String fileName : fileLists) {
				try {
					br = new Scanner(new FileReader(fileName));
					while (br.hasNextLine()) {
						for (String key : parseWords(br.nextLine())) {
							if (key.length() < 3)
								continue;

							if (createDictionaryEntry(key, dictionary))
								wordCount++;
						}
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					if (br != null)
						br.close();
				}

			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (br != null)
				br.close();
		}

		System.out.println(">>Dictionary created: " + wordCount + " words, "
				+ dictionary.size() + " entries, for edit distance="
				+ editDistanceMax);
		System.out.print("----------- Training dictionary");
		train(dictionary);
		System.out.println("---> Done!");
		// System.out.println(">>Writing SymSpell data to file..");
		// try {
		// FileOutputStream fout = new FileOutputStream(FILENAME);
		// ObjectOutputStream oos = new ObjectOutputStream(fout);
		// oos.writeObject(instance);
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

	private void train(Map<String, DictionaryItem> dictionary) {
		File fcheckExist = new File(TRAINDIRECTORY);
		if (!fcheckExist.exists()) {
			System.err.println("File not found: " + TRAINDIRECTORY);
			return;
		}

		long wordCount = 0;
		Scanner br = null;
		try {
			List<String> fileLists = Util.listFilesForFolder(TRAINDIRECTORY);
			for (String fileName : fileLists) {
				try {
					br = new Scanner(new FileReader(fileName));
					while (br.hasNextLine()) {
						for (String key : parseWords(br.nextLine())) {
							DictionaryItem value = dictionary.get(key);
							if (value != null) {
								if (value.count > 0) {
									createDictionaryEntry(key, dictionary);
									wordCount++;
								}
							}
						}
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					if (br != null)
						br.close();
				}

			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (br != null)
				br.close();
		}
		System.out.print("<" + wordCount + " Words> ");
	}

	// save some time and space
	private static void addLowestDistance(List<EditItem> suggestions,
			EditItem suggestion) {
		// remove all existing suggestions of higher distance, if verbose<2
		if ((verbose < 2) && (suggestions.size() > 0)
				&& (suggestions.get(0).distance > suggestion.distance))
			suggestions.clear();
		// do not add suggestion of higher distance than existing, if verbose<2
		if ((verbose == 2) || (suggestions.size() == 0)
				|| (suggestions.get(0).distance >= suggestion.distance))
			suggestions.add(suggestion);
	}

	// inexpensive and language independent: only deletes, no transposes +
	// replaces + inserts
	// replaces and inserts are expensive and language dependent (Chinese has
	// 70,000 Unicode Han characters)
	private List<EditItem> edits(String word, int editDistance,
			boolean recursion) {
		editDistance++;
		List<EditItem> deletes = new ArrayList<>();
		if (word.length() > 1) {
			for (int i = 0; i < word.length(); i++) {
				EditItem delete = new SymSpell.EditItem();
				delete.term = word.substring(0, word.length() - 1);
				if (delete.term.length() < 3)
					continue;
				delete.distance = editDistance;
				if (!deletes.contains(delete)) {
					deletes.add(delete);
					// recursion, if maximum edit distance not yet reached
					if (recursion && (editDistance < editDistanceMax)) {

						for (EditItem edit1 : edits(delete.term, editDistance,
								recursion)) {
							if (!deletes.contains(edit1))
								deletes.add(edit1);
						}
					}
				}
			}
		}

		return deletes;
	}

	private static int trueDistance(EditItem dictionaryOriginal,
			EditItem inputDelete, String inputOriginal) {
		// We allow simultaneous edits (deletes) of editDistanceMax on on both
		// the dictionary and the input term.
		// For replaces and adjacent transposes the resulting edit distance
		// stays <= editDistanceMax.
		// For inserts and deletes the resulting edit distance might exceed
		// editDistanceMax.
		// To prevent suggestions of a higher edit distance, we need to
		// calculate the resulting edit distance, if there are simultaneous
		// edits on both sides.
		// Example: (bank==bnak and bank==bink, but bank!=kanb and bank!=xban
		// and bank!=baxn for editDistanceMaxe=1)
		// Two deletes on each side of a pair makes them all equal, but the
		// first two pairs have edit distance=1, the others edit distance=2.

		if (dictionaryOriginal.term == inputOriginal)
			return 0;
		else if (dictionaryOriginal.distance == 0)
			return inputDelete.distance;
		else if (inputDelete.distance == 0)
			return dictionaryOriginal.distance;
		else
			return computeDamerauLevenshteinDistance(dictionaryOriginal.term,
					inputOriginal);// adjust distance, if both distances>0
	}

	class NameComparator implements Comparator {
		public int compare(Object arg0, Object arg1) {
			if (arg0 instanceof SuggestItem && arg1 instanceof SuggestItem) {

				return ((SuggestItem) arg0).distance
						- ((SuggestItem) arg1).distance;
			}
			return 0;
		}
	}

	class NumberComparator implements Comparator {
		public int compare(Object arg0, Object arg1) {
			if (arg0 instanceof SuggestItem && arg1 instanceof SuggestItem) {
				return ((SuggestItem) arg1).count - ((SuggestItem) arg0).count;
			}
			return 0;
		}
	}

	private static void loadBlackList(HashSet<String> blackList, File file)
			throws FileNotFoundException {
		// TODO Auto-generated method stub
		System.out.println(">>Loading Black List of Words");
		Scanner br = new Scanner(new FileReader(file));
		while (br.hasNext())
			blackList.add(br.next().toLowerCase());
		br.close();
	}

	private List<SuggestItem> lookup(String input, int editDistanceMax,
			Map<String, DictionaryItem> dictionary) {
		List<EditItem> candidates = new ArrayList<>();

		// add original term
		EditItem item = new EditItem();
		item.term = input;
		item.distance = 0;
		candidates.add(item);

		List<SuggestItem> suggestions = new ArrayList<>();
		DictionaryItem value;

		while (candidates.size() > 0) {
			EditItem candidate = candidates.get(0);
			candidates.remove(0);

			// save some time
			// early termination
			// suggestion distance=candidate.distance...
			// candidate.distance+editDistanceMax
			// if canddate distance is already higher than suggestion distance,
			// than there are no better suggestions to be expected
			if ((verbose < 2) && (suggestions.size() > 0)
					&& (candidate.distance > suggestions.get(0).distance)) {
				ComparatorChain chain = new ComparatorChain();
				chain.addComparator(new NameComparator());
				chain.addComparator(new NumberComparator());
				Collections.sort(suggestions, chain);

				if ((verbose == 0) && (suggestions.size() > 1))
					return suggestions.subList(0, 2);
				else
					return suggestions;
			}
			if (candidate.distance > editDistanceMax) {
				ComparatorChain chain = new ComparatorChain();
				chain.addComparator(new NameComparator());
				chain.addComparator(new NumberComparator());
				Collections.sort(suggestions, chain);

				if ((verbose == 0) && (suggestions.size() > 1))
					return suggestions.subList(0, 2);
				else
					return suggestions;
			}
			value = null;
			value = dictionary.get(candidate.term);
			if (value != null) {
				if (!value.term.isEmpty()) {
					// correct term
					SuggestItem si = new SuggestItem();
					si.term = value.term;
					si.count = value.count;
					si.distance = candidate.distance;

					if (!suggestions.contains(si)) {
						suggestions.add(si);
						// early termination
						if ((verbose < 2) && (candidate.distance == 0)) {
							ComparatorChain chain = new ComparatorChain();
							chain.addComparator(new NameComparator());
							chain.addComparator(new NumberComparator());
							Collections.sort(suggestions, chain);

							if ((verbose == 0) && (suggestions.size() > 1))
								return suggestions.subList(0, 2);
							else
								return suggestions;
						}
					}
				}

				// edit term (with suggestions to correct term)
				DictionaryItem value2;

				for (EditItem suggestion : value.suggestions) {
					// save some time
					// skipping double items early
					SuggestItem temp = new SuggestItem();
					temp.term = suggestion.term;
					if (!suggestions.contains(temp)) {
						int distance = trueDistance(suggestion, candidate,
								input);

						// save some time.
						// remove all existing suggestions of higher distance,
						// if verbose<2
						if ((verbose < 2) && (suggestions.size() > 0)
								&& (suggestions.get(0).distance > distance))
							suggestions.clear();
						// do not process higher distances than those already
						// found, if verbose<2
						if ((verbose < 2) && (suggestions.size() > 0)
								&& (distance > suggestions.get(0).distance))
							continue;

						if (distance <= editDistanceMax) {
							value2 = null;
							value2 = dictionary.get(suggestion.term);
							if (value2 != null) {
								SuggestItem si = new SuggestItem();
								si.term = value2.term;
								si.count = value2.count;
								si.distance = distance;

								suggestions.add(si);
							}
						}
					}
				}
			}

			// add edits
			if (candidate.distance < editDistanceMax) {
				for (EditItem delete : edits(candidate.term, candidate.distance,
						false)) {
					if (!candidates.contains(delete))
						candidates.add(delete);
				}
			}
		} // end while
		ComparatorChain chain = new ComparatorChain();
		chain.addComparator(new NameComparator());
		chain.addComparator(new NumberComparator());
		Collections.sort(suggestions, chain);

		if ((verbose == 0) && (suggestions.size() > 1))
			return suggestions.subList(0, 2);
		else
			return suggestions;
	}

	public String correctThisWord(String input) {
		List<SuggestItem> suggestions = null;
		suggestions = lookup(input, editDistanceMax, baseDictionary);
		// display term and frequency
		if (suggestions.size() == 0) {
			return input;
		}
		return suggestions.get(0).term;
	}

	public String[] correctThisWord_POS(String[] input) {
		List<SuggestItem> suggestions = null;
		suggestions = lookup(input[0], editDistanceMax, baseDictionary);
		String[] result = new String[] { "", "" };
		// display term and frequency
		if (suggestions.size() == 0) {
			STAT_NotCorrected++;
			return input;
		}
		String suggestedWord = suggestions.get(0).term;
		String pos = basewordPOS.get(suggestedWord);
		if (pos == null) {
			STAT_NotCorrected++;
			return input;
		}
		result[0] = suggestedWord;
		result[1] = basewordPOS.get(suggestedWord);
		STAT_SuccessfullyCorrected++;
		return result;
	}

	public String getStatisticString() {
		return "Corrected: " + STAT_SuccessfullyCorrected + " - Can't correct: "
				+ STAT_NotCorrected;
	}

	private void correct(String input) {
		List<SuggestItem> suggestions = null;

		/*
		//Benchmark: 1000 x Lookup
		Stopwatch stopWatch = new Stopwatch();
		stopWatch.Start();
		for (int i = 0; i < 1000; i++)
		{
		    suggestions = Lookup(input,language,editDistanceMax);
		}
		stopWatch.Stop();
		Console.WriteLine(stopWatch.ElapsedMilliseconds.ToString());
		*/

		// check in dictionary for existence and frequency; sort by edit
		// distance, then by word frequency
		suggestions = lookup(input, editDistanceMax, baseDictionary);

		// display term and frequency
		if (suggestions.size() == 0) {
			System.out.println("Can't find corrections for this word!");
		}
		for (SuggestItem suggestion : suggestions) {
			System.out.println(suggestion.term + " " + suggestion.distance + " "
					+ suggestion.count);
		}
		if (verbose == 2)
			System.out.println(suggestions.size() + " suggestions");
	}

	// Damerau�Levenshtein distance algorithm and code
	// from http://en.wikipedia.org/wiki/Damerau%E2%80%93Levenshtein_distance
	public static int computeDamerauLevenshteinDistance(String str1,
			String str2) {
		int[][] distance = new int[str1.length() + 1][str2.length() + 1];

		for (int i = 0; i <= str1.length(); i++)
			distance[i][0] = i;
		for (int j = 1; j <= str2.length(); j++)
			distance[0][j] = j;

		for (int i = 1; i <= str1.length(); i++)
			for (int j = 1; j <= str2.length(); j++)
				distance[i][j] = minimum(distance[i - 1][j] + 1,
						distance[i][j - 1] + 1,
						distance[i - 1][j - 1]
								+ ((str1.charAt(i - 1) == str2.charAt(j - 1))
										? 0 : 1));

		return distance[str1.length()][str2.length()];
	}

	private static int minimum(int a, int b, int c) {
		return Math.min(Math.min(a, b), c);
	}

	public static void main(String[] args) {
		// e.g. http://norvig.com/big.txt , or any other large text corpus
		SymSpell symSpell = SymSpell.getInstance();
		Scanner in = new Scanner(System.in);
		while (true) {
			System.out.println("Word to correct: ");
			String word = in.next();
			symSpell.correct(word);
		}
	}
}
