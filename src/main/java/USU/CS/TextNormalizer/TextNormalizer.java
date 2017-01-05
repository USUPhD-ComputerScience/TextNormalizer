package USU.CS.TextNormalizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import USU.CS.NLP.CustomStemmer;
import USU.CS.NLP.NatureLanguageProcessor;
import USU.CS.NLP.SymSpell;
import USU.CS.Utils.Util;

public class TextNormalizer {
	private static TextNormalizer instance = null;
	private static String DICTIONARY_DIRECTORY = "dictionary/";
	private static String TRIGRAM_TRAINING_DIRECTORY = "dictionary/trigramTraning/";
	private static boolean DEBUG = true;

	private TextNormalizer() {
		// TODO Auto-generated constructor stub
	}

	private static void debug_println(String msg) {
		if (DEBUG)
			System.out.println(msg);
	}

	public void readConfigINI(String fileName) throws FileNotFoundException {
		System.out.println("Reading configuration file at " + fileName);
		Scanner br = new Scanner(new File(fileName));
		while (br.hasNextLine()) {
			String item = br.nextLine();
			String[] tokens = item.split("=");
			if (tokens.length == 2) {
				String variable = tokens[0].replace(" ", "");
				if (variable.equals("DICTIONARY_DIRECTORY")) {
					String value = tokens[1].replace(" ", "");
					DICTIONARY_DIRECTORY = value;
				}
				if (variable.equals("TRIGRAM_TRAINING_DIRECTORY")) {
					String value = tokens[1].replace(" ", "");
					TRIGRAM_TRAINING_DIRECTORY = value;
				}
				if (variable.equals("DEBUG")) {
					String value = tokens[1].replace(" ", "");
					if (value.equals("0"))
						DEBUG = false;
					if (value.equals("1"))
						DEBUG = true;
				}
			}
		}
		br.close();
		System.out.println("DONE Reading configuration file");
	}

	public static String getTrigramTrainingDirectory() {
		return TRIGRAM_TRAINING_DIRECTORY;
	}

	public static String getDictionaryDirectory() {
		return DICTIONARY_DIRECTORY;
	}

	public static TextNormalizer getInstance() {
		if (instance == null)
			instance = new TextNormalizer();
		return instance;
	}

	// will return null if the text is not english
	public String normalize(String input) {
		String[] taggedTokens = preprocessAndSplitToTaggedTokens(input);
		if (taggedTokens == null)
			return null;
		List<String> correctedTaggedTokens = new ArrayList<>();
		CustomStemmer stemmer = CustomStemmer.getInstance();
		for (String taggedTok : taggedTokens) {
			String[] pair = taggedTok.split("_");
			pair[0] = pair[0].toLowerCase();
			pair = stemmer.stem(pair);
			correctedTaggedTokens.add(pair[0] + "_" + pair[1]);
		}
		String correctedTaggedText = NatureLanguageProcessor
				.mergeIntoText(correctedTaggedTokens);
		debug_println(correctedTaggedText);
		return correctedTaggedText;
	}

	// check for non-english text using the method proposed in our publication
	// biproportionThreshold: ratio of pair of english words to all words,
	// suggest using 0.4
	// uniproportionThreshold: ratio of single english words to all words,
	// suggest using 0.5
	public boolean isNonEnglish(List<String> wordList,
			double biproportionThreshold, double uniproportionThreshold) {
		Set<String> realDictionary = SymSpell.getInstance().getFullDictionary();
		double totalScore = 0, bigramScore = 0, unigramScore = 0;
		boolean previousInDic = false;
		for (String word : wordList) {
			// ignore special characters: , < . > ? / : ; " ' { [ } ] + = _ - ~
			// ` ! @ # $ % ^ & * ( ) | \
			if (isSpecialCharacter(word))
				continue;
			double score = 1.0;
			if (realDictionary.contains(word)) {
				// score /= Math.log(wCount);
				unigramScore += score;
				if (previousInDic)
					bigramScore += score;
				previousInDic = true;
			} else
				previousInDic = false;

			totalScore += score;
		}
		if (totalScore == 0)
			return true;
		double biproportion = bigramScore / totalScore;
		double uniproportion = unigramScore / totalScore;
		if (biproportion < biproportionThreshold
				&& uniproportion < uniproportionThreshold)
			return true;
		return false;
	}

	private boolean isSpecialCharacter(String word) {
		switch (word) {
		case ",":
		case "<":
		case ".":
		case ">":
		case "?":
		case "/":
		case ":":
		case ";":
		case "\"":
		case "'":
		case "{":
		case "[":
		case "}":
		case "]":
		case "+":
		case "=":
		case "_":
		case "-":
		case "~":
		case "`":
		case "!":
		case "@":
		case "#":
		case "$":
		case "%":
		case "^":
		case "&":
		case "*":
		case "(":
		case ")":
		case "|":
		case "\\":
			return true;
		}
		return false;
	}

	// split text into sentence using end-of-sentence indicator: . ; ! ?
	// The text inside parentheses is accounted as new, separated sentences.
	// The following example shows how it works:
	// Original: Angry birds I love the new levels they (the new level. I meant
	// the new levels) are very challenging.
	// Ordered output:
	// 1. the_NN new_JJ level_NN
	// 2. i_PRP mean_VB the_NN new_JJ level_NN
	// 3. angry_JJ bird_VB i_PRP love_VB the_NN new_JJ level_NN they_PRP be_VB
	// very_NN challenging_JJ
	// will return null if the text is not english
	public List<List<String>> normalize_SplitSentence(String input) {
		String[] taggedTokens = preprocessAndSplitToTaggedTokens(input);
		if (taggedTokens == null)
			return null;
		List<List<String>> correctedTaggedSentences = new ArrayList<>();
		CustomStemmer stemmer = CustomStemmer.getInstance();
		List<String> sentence = null;
		boolean inParentheses = false;
		List<String> inParenthesesSentence = null;
		for (String taggedTok : taggedTokens) {
			String[] pair = taggedTok.split("_");
			pair[0] = pair[0].toLowerCase();
			pair = stemmer.stem(pair);
			if (pair[0].equals(".") || pair[0].equals(";")
					|| pair[0].equals("!") || pair[0].equals("?")
					|| pair[0].equals("-rrb-")) {
				if (inParentheses) {
					if (inParenthesesSentence != null) {
						correctedTaggedSentences
								.add(Util.deepCopyList(inParenthesesSentence));
						inParenthesesSentence = null;
					}
					if (pair[0].equals("-rrb-"))
						inParentheses = false;
				} else {
					if (sentence != null) {
						correctedTaggedSentences
								.add(Util.deepCopyList(sentence));
						sentence = null;
					}
				}
			} else {
				if (pair[0].equals("-lrb-")) {
					inParentheses = true;
				} else {
					List<String> senOI = null;
					if (inParentheses) {

						if (inParenthesesSentence == null)
							inParenthesesSentence = new ArrayList<>();
						senOI = inParenthesesSentence;
					} else {
						if (sentence == null)
							sentence = new ArrayList<>();
						senOI = sentence;
					}
					String taggedWord = pair[0] + "_" + pair[1];
					senOI.add(taggedWord);
				}

			}
		}
		if (inParenthesesSentence != null) {
			correctedTaggedSentences
					.add(Util.deepCopyList(inParenthesesSentence));
			sentence = null;
		}
		if (sentence != null) {
			correctedTaggedSentences.add(Util.deepCopyList(sentence));
			sentence = null;
		}
		for (List<String> sen : correctedTaggedSentences)
			debug_println(sen.toString());
		return correctedTaggedSentences;
	}

	// will return null if this text is not english
	private String[] preprocessAndSplitToTaggedTokens(String input) {
		NatureLanguageProcessor nlp = NatureLanguageProcessor.getInstance();
		// 0th step: lower case
		input = input.toLowerCase();
		// 1st step: replace words with a mapper, keep the whole format
		List<String> tokens = NatureLanguageProcessor.wordSplit(input);
		List<String> correctedTokens = nlp.correctUsingMap(tokens);
		String text = NatureLanguageProcessor.mergeIntoText(correctedTokens);
		// 2nd step: check if this is a non-English text, if yes then
		// discontinue
		if (isNonEnglish(correctedTokens, 0.4, 0.5))
			return null;
		// System.out.println(text);
		// 3rd step: tag the whole thing
		String taggedText = nlp.findPosTag(text);
		debug_println(taggedText);
		// 4th step: stem and correct every words.
		String[] taggedTokens = taggedText.split("\\s+");
		return taggedTokens;
	}

	public static void main(String[] args) throws FileNotFoundException {
		TextNormalizer normalizer = TextNormalizer.getInstance();
		// normalizer.readConfigINI(
		// "D:\\EclipseWorkspace\\TextNormalizer\\config.INI");
		normalizer.normalize_SplitSentence(
				"Angry birds I love the new levels they (the new level. I meant the new levels) are very challenging . You should make more levels . I love angry birds.And you should sign with sponge bob squarepants for an app .And you should youse Billy Joel music for your background sound.");
	}
}
