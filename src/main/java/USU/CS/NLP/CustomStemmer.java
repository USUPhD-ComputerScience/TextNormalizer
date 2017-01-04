package USU.CS.NLP;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import USU.CS.Utils.POSTagConverter;
import USU.CS.Utils.Util;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class CustomStemmer {
	private int STAT_mapperTimes = 0;
	private int STAT_StemmingTimes = 0;
	private int STAT_KeepOriginalTimes = 0;
	private static CustomStemmer instance = null;
	private static final Set<Integer> vowelSet = new HashSet<>(
			Arrays.asList(new Integer[] { (int) 'a', (int) 'e', (int) 'i',
					(int) 'o', (int) 'u', (int) 'y' }));
	private static final Set<String> doubleSet = new HashSet<>(
			Arrays.asList(new String[] { "bb", "dd", "ff", "gg", "mm", "nn",
					"pp", "rr", "tt" }));

	private static final Set<String> specialSet = new HashSet<>(Arrays.asList(
			new String[] { "at", "bl", "iz", "dl", "gl", "pl", "tl", "kl", "tl",
					"dg", "iv", "tr", "dg", "uc", "rc", "ev", "rg", "fl", "ib",
					"av", "ng", "um", "ul", "lv", "nc", "rv", "rs", "ur" }));

	public static CustomStemmer getInstance() {
		if (instance == null)
			instance = new CustomStemmer();
		return instance;
	}

	private CustomStemmer() {
	}

	private static int countSyllable(char[] charlist) {
		int count = 0;
		for (char c : charlist) {
			switch (c) {
			case 'a':
			case 'e':
			case 'i':
			case 'o':
			case 'u':
			case 'A':
			case 'E':
			case 'I':
			case 'O':
			case 'U':
				count++;
				break;
			}
		}
		return count;
	}

	// rule set:
	// http://www.dailywritingtips.com/comparative-forms-of-adjectives/
	public String stemComparativeADJ(String comparativeADJ) {
		int type = -1; // -1: not applicable, 0: more, 1: most
		if (comparativeADJ.endsWith("er"))
			type = 0;
		if (comparativeADJ.endsWith("est"))
			type = 1;
		if (type == -1) {
			STAT_KeepOriginalTimes++;
			return comparativeADJ;
		}
		if (comparativeADJ.length() < 4) {
			STAT_KeepOriginalTimes++;
			return comparativeADJ;
		}
		StringBuilder ADJ = new StringBuilder();
		char[] seqWithoutPostfix = null;
		if (type == 0)
			seqWithoutPostfix = comparativeADJ
					.substring(0, comparativeADJ.length() - 2).toCharArray();
		if (type == 1)
			seqWithoutPostfix = comparativeADJ
					.substring(0, comparativeADJ.length() - 3).toCharArray();
		if (countSyllable(seqWithoutPostfix) == 1) {
			// rule 1: One syllable words form the comparative by adding -er and
			// -est:
			// brave, braver, bravest
			// small, smaller, smallest
			// dark, darker, darkest.
			ADJ.append(seqWithoutPostfix);
			STAT_StemmingTimes++;
			return ADJ.toString(); // Hope the corrector will fix the lack of
									// ending 'e'
		}
		if (countSyllable(seqWithoutPostfix) == 2) {
			// Rule 2: Two-syllable words that end in -y, -le, and -er form the
			// comparative by adding -er and -est:
			// pretty, prettier, prettiest
			// happy, happier, happiest
			// noble, nobler, noblest --> NOTE: Can't find a way to reverse
			// engineer this yet
			// clever, cleverer, cleverest

			char c1 = seqWithoutPostfix[seqWithoutPostfix.length - 1]; // find
																		// 'i'
			if (c1 == 'i') {
				ADJ.append(seqWithoutPostfix);
				ADJ.deleteCharAt(seqWithoutPostfix.length - 1);
				ADJ.append("y");
				STAT_StemmingTimes++;
				return ADJ.toString();
			}
			ADJ.append(seqWithoutPostfix);
			STAT_StemmingTimes++;
			return ADJ.toString();
		}
		// rule 3, 4, 5 mean there is no need for conversion
		// quiet, pleasant are irregular
		STAT_KeepOriginalTimes++;
		return comparativeADJ;
	}

	public String stemNNS(String pluralNoun) {

		if (!pluralNoun.endsWith("s")) {
			STAT_KeepOriginalTimes++;
			return pluralNoun;
		}
		if (pluralNoun.length() < 4) {
			STAT_KeepOriginalTimes++;
			return pluralNoun;
		}
		StringBuilder noun = new StringBuilder();
		char[] seq = pluralNoun.substring(0, pluralNoun.length() - 1)
				.toCharArray();
		char c1 = seq[seq.length - 1]; // e
		if (c1 != 'e') {
			noun.append(seq);
			STAT_StemmingTimes++;
			return noun.toString();
		}
		char c2 = seq[seq.length - 2]; // before e
		char c3 = seq[seq.length - 3];
		if (specialSet.contains("" + c3 + c2)) {
			noun.append(pluralNoun.subSequence(0, pluralNoun.length() - 2));
			noun.append("e");
			STAT_StemmingTimes++;
			return noun.toString();
		}
		noun.append(pluralNoun.subSequence(0, pluralNoun.length() - 3));

		switch (c2) {
		case 'i':
			if (!isVowel(c3)) {
				noun.append('y');
			} else {
				noun.append(c3);
				noun.append(c2);
			}
			STAT_StemmingTimes++;
			return noun.toString();
		/*
		if (!vowelSet.contains(pluralNoun
				.charAt(length - 4))) {
			noun.append(pluralNoun.subSequence(0,
					length - 3));
			noun.append('y');
		} else
			noun.append(pluralNoun.subSequence(0,
					length - 1));
		break;*/
		case 's':
			if (c3 == 's')
				noun.append(c2);
			else {
				noun.append(c2);
				noun.append(c1);// e
			}
			STAT_StemmingTimes++;
			return noun.toString();
		// if (pluralNoun.charAt(length - 4) == 's')
		// noun.append(pluralNoun.subSequence(0,
		// length - 2));
		// else
		// noun.append(pluralNoun.subSequence(0,
		// length - 1));
		// break;
		case 'x':
		case 'o':
		case 'z':

			noun.append(c2);
			STAT_StemmingTimes++;
			return noun.toString();
		// noun.append(pluralNoun.subSequence(0,
		// length - 2));
		// break;
		case 'h':
			switch (c3) {
			case 'c':
			case 's':
				noun.append(c2);
				STAT_StemmingTimes++;
				return noun.toString();
			// noun.append(pluralNoun.subSequence(0,
			// length - 2));
			// break;
			default:
				noun.append(c2);
				noun.append(c1);// e
				STAT_StemmingTimes++;
				return noun.toString();
			// noun.append(pluralNoun.subSequence(0,
			// length - 1));
			}
			// break;
		default:
			noun.append(c2);
			noun.append(c1);// e
			STAT_StemmingTimes++;
			return noun.toString();
		// noun.append(pluralNoun.subSequence(0,
		// length - 1));

		}

	}

	public String stemVBD(String verb) {
		CharacterTriGramTrainer trainer = CharacterTriGramTrainer.getInstance();
		if (verb.length() < 5) {
			STAT_KeepOriginalTimes++;
			return verb;
		}
		if (!verb.endsWith("ed")) {
			STAT_KeepOriginalTimes++;
			return verb;
		}

		StringBuilder v = new StringBuilder();
		char[] seq = verb.substring(0, verb.length() - 2).toCharArray();
		int vowelCount = countVowel(seq);
		char c1 = seq[seq.length - 1];
		char c2 = seq[seq.length - 2];
		char c3 = seq[seq.length - 3];
		if (vowelCount == 0) {
			STAT_KeepOriginalTimes++;
			return verb;
		}
		v.append(verb.substring(0, verb.length() - 3));
		if (seq.length < 4 && isVowel(seq[seq.length - 2])) {
			v.append(c1);
			v.append('e');
			STAT_StemmingTimes++;
			return v.toString();
		}
		if (c1 == 'i') {
			v.append('y');
			return v.toString();
		}
		if (vowelCount == 1 && c2 == c1 && isRemovableDoubleConsonents(c1)) {
			STAT_StemmingTimes++;
			return v.toString();
		}
		v.append(c1);
		try {
			double p0 = trainer.getProbability(c3, c2, c1, 'e')
					* trainer.getProbability(c2, c1, 'e', ' ');
			double p1 = trainer.getProbability(c3, c2, c1, ' ');
			if (p0 > p1)
				v.append('e');
		} catch (java.lang.ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			System.out.println("" + c3 + c2 + c1 + 'e');
			STAT_KeepOriginalTimes++;
			return verb;
		}

		STAT_StemmingTimes++;
		return v.toString();
	}

	public String verbPastSimpleStem(final CharSequence pastSimpleVerb) {
		int length = pastSimpleVerb.length();
		StringBuilder verb = new StringBuilder();
		if (pastSimpleVerb.charAt(length - 1) == 'd') {
			if (pastSimpleVerb.charAt(length - 2) == 'e') {
				int vowelCount = 0;
				for (int i = 0; i < length - 2; i++) {
					int c = pastSimpleVerb.charAt(i);
					if (vowelSet.contains(c)) {
						vowelCount++;
					}
				}
				if (vowelCount > 0) {
					if ((pastSimpleVerb.subSequence(0, length - 2).length() < 4
							&& vowelCount == 1)
							|| (pastSimpleVerb.charAt(length - 3) == 's'
									&& pastSimpleVerb
											.charAt(length - 4) != 's')) {
						verb.append(pastSimpleVerb.subSequence(0, length - 2));
						verb.append("e");
					} else {
						if (doubleSet.contains(pastSimpleVerb
								.subSequence(length - 4, length - 2)))
							verb.append(
									pastSimpleVerb.subSequence(0, length - 3));
						else {
							if (specialSet.contains(pastSimpleVerb
									.subSequence(length - 4, length - 2))) {
								verb.append(pastSimpleVerb.subSequence(0,
										length - 2));
								verb.append("e");
							} else {
								if (pastSimpleVerb.charAt(length - 3) == 'i') {
									verb.append(pastSimpleVerb.subSequence(0,
											length - 3));
									verb.append("y");
								} else
									verb.append(pastSimpleVerb.subSequence(0,
											length - 2));
							}
						}
					}
					STAT_StemmingTimes++;
					return verb.toString();
				} else {
					STAT_KeepOriginalTimes++;
					return (String) pastSimpleVerb;
				}
			} else {
				STAT_KeepOriginalTimes++;
				return (String) pastSimpleVerb;
			}
		} else {
			STAT_KeepOriginalTimes++;
			return (String) pastSimpleVerb;
		}
	}

	public String stemVBG(String verb) {
		CharacterTriGramTrainer trainer = CharacterTriGramTrainer.getInstance();
		if (verb.length() < 6) {
			STAT_KeepOriginalTimes++;
			return verb;
		}
		if (!verb.endsWith("ing")) {
			STAT_KeepOriginalTimes++;
			return verb;
		}

		StringBuilder v = new StringBuilder();
		char[] seq = verb.substring(0, verb.length() - 3).toCharArray();
		int vowelCount = countVowel(seq);
		char c1 = seq[seq.length - 1];
		char c2 = seq[seq.length - 2];
		char c3 = seq[seq.length - 3];
		if (vowelCount == 0) {
			STAT_KeepOriginalTimes++;
			return verb;
		}
		v.append(verb.substring(0, verb.length() - 4));
		if (seq.length < 4 && isVowel(seq[seq.length - 2])) {
			v.append(c1);
			v.append('e');
			STAT_StemmingTimes++;
			return v.toString();
		}
		if (vowelCount == 1 && c2 == c1 && isRemovableDoubleConsonents(c1)) {
			STAT_StemmingTimes++;
			return v.toString();
		}
		v.append(c1);
		try {
			double p0 = trainer.getProbability(c3, c2, c1, 'e')
					* trainer.getProbability(c2, c1, 'e', ' ');

			double p1 = trainer.getProbability(c3, c2, c1, ' ');
			if (p0 > p1)
				v.append('e');
		} catch (java.lang.ArrayIndexOutOfBoundsException e) {
			// e.printStackTrace();
			System.out.println("No tri-gram for " + c3 + c2 + c1 + 'e');
		}

		STAT_StemmingTimes++;
		return v.toString();
	}

	private int countVowel(char[] seq) {
		int count = 0;
		for (int i = 0; i < seq.length; i++) {
			if (isVowel(seq[i]))
				count++;
		}
		return count;
	}

	private boolean isRemovableDoubleConsonents(char c) {
		return c == 't' || c == 'n' || c == 'r' || c == 'd' || c == 'm'
				|| c == 'f' || c == 'g' || c == 'p' || c == 'b';
	}

	private boolean isVowel(char c) {
		return c == 'e' || c == 'a' || c == 'o' || c == 'i' || c == 'u'
				|| c == 'y';
	}

	public String verbPresentParticipleStem(
			final CharSequence presentParticipleVerb) {
		int length = presentParticipleVerb.length();
		StringBuilder verb = new StringBuilder();

		if (presentParticipleVerb.charAt(length - 1) == 'g') {
			if (presentParticipleVerb.charAt(length - 2) == 'n') {
				if (presentParticipleVerb.charAt(length - 3) == 'i') {
					int vowelCount = 0;
					for (int i = 0; i < length - 3; i++) {
						int c = presentParticipleVerb.charAt(i);
						if (vowelSet.contains(c)) {
							vowelCount++;
						}
					}
					if (vowelCount > 0) {
						if ((presentParticipleVerb.subSequence(0, length - 3)
								.length() < 4
								&& vowelCount == 1
								&& vowelSet.contains(presentParticipleVerb
										.charAt(length - 5)))
								|| (presentParticipleVerb
										.charAt(length - 4) == 's'
										&& presentParticipleVerb
												.charAt(length - 5) != 's')) {
							verb.append(presentParticipleVerb.subSequence(0,
									length - 3));
							verb.append("e");
						} else {
							if (doubleSet.contains(presentParticipleVerb
									.subSequence(length - 5, length - 3)))
								verb.append(presentParticipleVerb.subSequence(0,
										length - 4));
							else {
								if (specialSet.contains(presentParticipleVerb
										.subSequence(length - 5, length - 3))) {
									verb.append(presentParticipleVerb
											.subSequence(0, length - 3));
									verb.append("e");
								} else {
									verb.append(presentParticipleVerb
											.subSequence(0, length - 3));
								}
							}
						}
						STAT_StemmingTimes++;
						return verb.toString();
					} else {
						STAT_KeepOriginalTimes++;
						return (String) presentParticipleVerb;
					}
				} else {
					STAT_KeepOriginalTimes++;
					return (String) presentParticipleVerb;
				}
			} else {
				STAT_KeepOriginalTimes++;
				return (String) presentParticipleVerb;
			}
		} else {
			STAT_KeepOriginalTimes++;
			return (String) presentParticipleVerb;
		}
	}

	public String getStatisticString() {
		return "Mapped: " + STAT_mapperTimes + " - Stemmed: "
				+ STAT_StemmingTimes + " - Passed: " + STAT_KeepOriginalTimes;
	}

	public String verbPastParticipleStem(
			final CharSequence pastParticipleVerb) {
		return verbPastSimpleStem(pastParticipleVerb);
	}

	public String stemVBZ(String thirdPersonSingularVerb) {
		return stemNNS(thirdPersonSingularVerb);
	}

	public String[] stem(String[] pair) {
		// Do not stem if this word contains number, we have no apparent rules
		// for such words
		if(Util.hasNumeric(pair[0]))
			return pair;
		String mappedPair[] = IrregularMapper.getInstance().MapIrregular(pair);
		if (mappedPair != null) {
			STAT_mapperTimes++;
			return mappedPair;
		}
		String result[] = { null, null };
		if (pair[1].equals("JJR") || pair[1].equals("JJS")) {
			result[0] = stemComparativeADJ(pair[0]).intern();
			result[1] = "JJ".intern();
			result = SymSpell.getInstance().correctThisWord_POS(result);
			return result;
		}

		if (pair[1].equals("NNS") || pair[1].equals("NNPS")) {
			result[0] = stemNNS(pair[0]).intern();
			result[1] = "NN".intern();
			result = SymSpell.getInstance().correctThisWord_POS(result);
			return result;
		} else {
			if (pair[1].equals("VBD") || pair[1].equals("VBG")
					|| pair[1].equals("VBN") || pair[1].equals("VBZ")) {
				switch (pair[1]) {
				case "VBD":
					result[0] = stemVBD(pair[0]).intern();
					break;
				case "VBG":
					result[0] = stemVBG(pair[0]).intern();
					break;
				case "VBN":
					result[0] = stemVBN(pair[0]).intern();
					break;
				case "VBZ":
					result[0] = stemVBZ(pair[0]).intern();
					break;

				}
				result[1] = "VB".intern();
				result = SymSpell.getInstance().correctThisWord_POS(result);
				return result;

			} else {
				if (POSTagConverter.getInstance().getCode(result[1]) != 0xFF)
					result = SymSpell.getInstance().correctThisWord_POS(pair);
				return result;
			}

		}
	}

	private String stemVBN(String verb) {
		// TODO Auto-generated method stub
		return stemVBD(verb);
	}

	// private void irregularVerbRedundancyTest() {
	// Map<String, StringBuilder> irregularSet = new HashMap<>();
	// for (Entry<String, String> pair : irregularVerbMap.entrySet()) {
	// boolean redundant = false;
	// if (pair.getValue().equals(verbPastSimpleStem(pair.getKey()))) {
	// redundant = true;
	// }
	// if (pair.getValue().equals(stemVBZ(pair.getKey()))) {
	// redundant = true;
	// }
	// if (pair.getValue()
	// .equals(verbPresentParticipleStem(pair.getKey()))) {
	// redundant = true;
	// }
	// if (!redundant) {
	// StringBuilder vlist = irregularSet.get(pair.getValue());
	// if (vlist == null)
	// vlist = new StringBuilder();
	// vlist.append(' ');
	// vlist.append(pair.getKey());
	// irregularSet.put(pair.getValue(), vlist);
	// }
	// }
	//
	// PrintWriter pw = null;
	// try {
	// pw = new PrintWriter(new FileWriter(FILENAME));
	// for (Entry<String, StringBuilder> pair : irregularSet.entrySet()) {
	// pw.write(pair.getKey());
	// pw.write(pair.getValue().toString());
	// pw.write('\n');
	// }
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// if (pw != null)
	// pw.close();
	// }
	//
	// private static void prepareTestData() {
	// Map<String, String> inputMap = new HashMap<>();
	// Scanner br = null;
	// try {
	// br = new Scanner(new FileReader("stemmingInput.txt"));
	// while (br.hasNextLine()) {
	// String[] words = br.nextLine().split(" ");
	// for (int i = 1; i < words.length; i++) {
	// inputMap.put(words[i].intern(), words[0].intern());
	// }
	// }
	// } catch (FileNotFoundException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// if (br != null)
	// br.close();
	//
	// MaxentTagger PoSTagger = new MaxentTagger(
	// "lib/english-left3words-distsim.tagger");
	// Set<String> POSN = new HashSet<>(Arrays.asList(new String[] { "NNS",
	// "NN", "NNPS", "NNP" }));
	// Set<String> POSV = new HashSet<>(Arrays.asList(new String[] { "VB",
	// "VBG", "VBZ", "VBN", "VBP" }));
	// int countNNS = 0;
	// int countNN = 0;
	// int countNNPS = 0;
	// int countNNP = 0;
	// int countVB = 0;
	// int countVBG = 0;
	// int countVBZ = 0;
	// int countVBN = 0;
	// int countVBP = 0;
	// Map<String, String> outputMap = new HashMap<>();
	// for (Entry<String, String> pair : inputMap.entrySet()) {
	// String tagged = PoSTagger.tagString(pair.getKey());
	// // Output the result
	// // System.out.println(tagged);
	// String noSpace[] = tagged.split(" ");
	// String[] results = noSpace[0].split("_");
	// if (results.length == 2) {
	// switch (results[1]) {
	// case "NNS":
	// if (countNNS < 500 && results[0].length() > 1) {
	// countNNS++;
	// outputMap.put(pair.getValue(), pair.getKey() + " "
	// + results[1]);
	// }
	// break;
	// case "NN":
	// if (countNN < 500 && results[0].length() > 1) {
	// countNN++;
	// outputMap.put(pair.getKey(), pair.getKey() + " "
	// + results[1]);
	// }
	// break;
	// case "NNPS":
	// if (countNNPS <= 500 && results[0].length() > 1) {
	// countNNPS++;
	// outputMap.put(pair.getValue(), pair.getKey() + " "
	// + results[1]);
	// }
	// break;
	// case "NNP":
	// if (countNNP < 500 && results[0].length() > 1) {
	// countNNP++;
	// outputMap.put(pair.getKey(), pair.getKey() + " "
	// + results[1]);
	// }
	// break;
	// case "VB":
	// if (countVB < 500 && results[0].length() > 1) {
	// countVB++;
	// outputMap.put(pair.getKey(), pair.getKey() + " "
	// + results[1]);
	// }
	// break;
	// case "VBG":
	// if (countVBG < 500 && results[0].length() > 1) {
	// countVBG++;
	// outputMap.put(pair.getValue(), pair.getKey() + " "
	// + results[1]);
	// }
	// break;
	// case "VBN":
	// if (countVBN < 500 && results[0].length() > 1) {
	// countVBN++;
	// outputMap.put(pair.getValue(), pair.getKey() + " "
	// + results[1]);
	// }
	// break;
	// case "VBZ":
	// if (countVBZ < 500 && results[0].length() > 1) {
	// countVBZ++;
	// outputMap.put(pair.getValue(), pair.getKey() + " "
	// + results[1]);
	// }
	// break;
	// case "VBP":
	// if (countVBP < 500 && results[0].length() > 1) {
	// countVBP++;
	// outputMap.put(pair.getValue(), pair.getKey() + " "
	// + results[1]);
	// }
	// break;
	// }
	// }
	// }
	// System.out.println("Number of NN = " + countNN);
	// System.out.println("Number of NNS = " + countNNS);
	// System.out.println("Number of NNP = " + countNNP);
	// System.out.println("Number of NNPS = " + countNNPS);
	// System.out.println("Number of VB = " + countVB);
	// System.out.println("Number of VBP = " + countVBP);
	// System.out.println("Number of VBN = " + countVBN);
	// System.out.println("Number of VBG = " + countVBG);
	// System.out.println("Number of VBZ = " + countVBZ);
	// PrintWriter pw = null;
	// try {
	// pw = new PrintWriter(new FileWriter("test.txt"));
	// for (Entry<String, String> pair : outputMap.entrySet()) {
	// pw.write(pair.getKey());
	// pw.write(" ");
	// pw.write(pair.getValue());
	// pw.write('\n');
	// }
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// if (pw != null)
	// pw.close();
	// }
	public static void main(String[] args) {
		CustomStemmer stemmer = CustomStemmer.getInstance();
		System.out.println(stemmer.stemComparativeADJ("quieter"));
		System.out.println(stemmer.stemComparativeADJ("quietest"));
		System.out.println(stemmer.stemComparativeADJ("pleasanter"));
		System.out.println(stemmer.stemComparativeADJ("pleasantest"));
	}
}