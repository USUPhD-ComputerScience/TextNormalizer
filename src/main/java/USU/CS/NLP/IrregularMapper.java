package USU.CS.NLP;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import USU.CS.TextNormalizer.TextNormalizer;
import USU.CS.Utils.POSTagConverter;
import USU.CS.Vocabulary.BaseWord;
import USU.CS.Vocabulary.Variant;

public class IrregularMapper {

	private static final String FILENAME_IRR_VERB = TextNormalizer.getDictionaryDirectory()+"baseWord/wordnet/verb_irr.txt";
	private static final String FILENAME_IRR_VERB_NEW =TextNormalizer.getDictionaryDirectory()+ "baseWord/newwords/verb.txt";
	private static final String FILENAME_IRR_NOUN = TextNormalizer.getDictionaryDirectory()+"baseWord/wordnet/noun_irr.txt";
	private static final String FILENAME_IRR_NOUN_NEW = TextNormalizer.getDictionaryDirectory()+"baseWord/newwords/noun.txt";
	private static final String FILENAME_IRR_ADJ = TextNormalizer.getDictionaryDirectory()+"baseWord/wordnet/adj_irr.txt";
	private static final String FILENAME_IRR_ADJ_NEW = TextNormalizer.getDictionaryDirectory()+"baseWord/newwords/adj.txt";
	private static final String FILENAME_IRR_ADV = TextNormalizer.getDictionaryDirectory()+"baseWord/wordnet/adv_irr.txt";
	private Set<BaseWord> mRootSet = new HashSet<>();
	private Map<String, Variant> mVariantMap = new HashMap<>();
	private static IrregularMapper instance = null;

	private IrregularMapper() {
		// TODO Auto-generated constructor stub
		try {
			readIrregularVoc(FILENAME_IRR_VERB);
			readIrregularVoc(FILENAME_IRR_VERB_NEW);
			readIrregularVoc(FILENAME_IRR_NOUN);
			readIrregularVoc(FILENAME_IRR_NOUN_NEW);
			readIrregularVoc(FILENAME_IRR_ADJ);
			readIrregularVoc(FILENAME_IRR_ADJ_NEW);
			readIrregularVoc(FILENAME_IRR_ADV);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static IrregularMapper getInstance() {
		if (instance == null)
			instance = new IrregularMapper();
		return instance;
	}

	public String[] MapIrregular(String[] pair) {
		String word = pair[0];
		Variant var = mVariantMap.get(word);
		if (var == null)
			return null;
		BaseWord root = var.getPrioritizedRoot();
		pair[0] = root.toString();
		pair[1] = POSTagConverter.getInstance().getTag(root.getPOS());
		return pair;
	}

	private void readIrregularVoc(String fileName)
			throws FileNotFoundException {
		System.out.println(">> Reading irregular dictionary...");
		int wordcount = 0;
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
						Variant var = mVariantMap.get(words[i]);
						if (var == null) {
							var = new Variant();
							var.onCreate(words[i], "UNKNOWN");
							mVariantMap.put(words[i], var);
							wordcount++;
						}
						bword.addVariant(var);
					}
				}
				mRootSet.add(bword);
				wordcount++;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		br.close();
		System.out.println(
				">> " + wordcount + " root words and variants are collected!");
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
	public static void main(String[] args) {
		IrregularMapper.getInstance().MapIrregular(new String[]{"emailing","NNS"});
	}
}
