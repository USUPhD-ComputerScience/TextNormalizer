package USU.CS.TextNormalizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import USU.CS.NLP.CustomStemmer;
import USU.CS.NLP.NatureLanguageProcessor;

public class TextNormalizer {
	public static String normalize(String input) {
		CustomStemmer stemmer = CustomStemmer.getInstance();
		NatureLanguageProcessor nlp = NatureLanguageProcessor.getInstance();
		// 1st step: replace words with a mapper, keep the whole format
		List<String> tokens = NatureLanguageProcessor.wordSplit(input);
		List<String> correctedTokens = nlp.correctUsingMap(tokens);
		String text = NatureLanguageProcessor.mergeIntoText(correctedTokens);
		System.out.println(text);
		// 2nd step: tag the whole thing
		String taggedText = nlp.findPosTag(input);
		System.out.println(taggedText);
		// 3rd step: stem and correct every words.
		List<String> correctedTaggedTokens = new ArrayList<>();
		String[] taggedTokens = taggedText.split("\\s+");
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

	public static void main(String[] args) {
		normalize(
				"Angry birds I love the new levels they are very challenging . You should make more levels . I love angry birds.And you should sign with sponge bob squarepants for an app .And you should youse Billy Joel music for your background sound.");
	}
}
