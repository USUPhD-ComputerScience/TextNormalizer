package USU.CS.TextNormalizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import USU.CS.NLP.CustomStemmer;
import USU.CS.NLP.NatureLanguageProcessor;
import USU.CS.Utils.Util;

public class TextNormalizer {
	public static String normalize(String input) {
		String[] taggedTokens = preprocessAndSplitToTaggedTokens(input);
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
		System.out.println(correctedTaggedText);
		return correctedTaggedText;
	}

	
	// split text into sentence using end-of-sentence indicator: . ; ! ?
	// The text inside parentheses is accounted as new, separated sentences. 
	// The following example shows how it works:
	// Original: Angry birds I love the new levels they (the new level. I meant the new levels) are very challenging.
	// Ordered output:
	//	1. the_NN new_JJ level_NN
	//  2. i_PRP mean_VB the_NN new_JJ level_NN
	//  3. angry_JJ bird_VB i_PRP love_VB the_NN new_JJ level_NN they_PRP be_VB very_NN challenging_JJ
	public static List<List<String>> normalize_SplitSentence(String input) {
		String[] taggedTokens = preprocessAndSplitToTaggedTokens(input);

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
		if (sentence != null) {
			correctedTaggedSentences.add(Util.deepCopyList(sentence));
			sentence = null;
		}
		for (List<String> sen : correctedTaggedSentences)
			System.out.println(sen);
		return correctedTaggedSentences;
	}

	private static String[] preprocessAndSplitToTaggedTokens(String input) {
		NatureLanguageProcessor nlp = NatureLanguageProcessor.getInstance();
		// 1st step: replace words with a mapper, keep the whole format
		List<String> tokens = NatureLanguageProcessor.wordSplit(input);
		List<String> correctedTokens = nlp.correctUsingMap(tokens);
		String text = NatureLanguageProcessor.mergeIntoText(correctedTokens);
		// System.out.println(text);
		// 2nd step: tag the whole thing
		String taggedText = nlp.findPosTag(text);
		
		System.out.println(taggedText);
		// 3rd step: stem and correct every words.
		String[] taggedTokens = taggedText.split("\\s+");
		return taggedTokens;
	}

	public static void main(String[] args) {
		normalize_SplitSentence(
				"Angry birds I love the new levels they (the new level. I meant the new levels) are very challenging . You should make more levels . I love angry birds.And you should sign with sponge bob squarepants for an app .And you should youse Billy Joel music for your background sound.");
	}
}
