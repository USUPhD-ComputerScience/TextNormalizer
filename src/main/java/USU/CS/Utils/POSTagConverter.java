package USU.CS.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class POSTagConverter {
	private static String[] POSLIST = { "EOS", "UH", "VB", "VBD", "VBG", "VBN",
			"VBP", "VBZ", "WDT", "WP", "WP$", "WRB", "NNPS", "NNS",
			"PDT", "PRP", "PRP$", "RB", "RBR", "RBS", "RP", "SYM", "TO",
			"CC", "CD", "DT", "EX", "FW", "IN", "JJ", "JJR", "JJS", "LS", "MD",
			"NN", "NNP", "UNKNOWN", "ADJP", "@VP","VP" , "NP"};
	private static Map<String, Byte> tag2code = new HashMap<String, Byte>();
	// private static Map<Byte, String> code2tag = new HashMap<Byte, String>();
	private static POSTagConverter instance = null;

	public static String[] getPOSLIST() {
		return POSLIST;
	}

	public static POSTagConverter getInstance() {
		if (instance == null)
			instance = new POSTagConverter();
		return instance;
	}

	private POSTagConverter() {
		for (int i = 0; i < POSLIST.length; i++) {
			tag2code.put(POSLIST[i], (byte) i);
		}
	}

	public void extendPOSLIST(Set<String> extendedList) {
		String[] temp = new String[POSLIST.length + extendedList.size()];
		for (int i = 0; i < POSLIST.length; i++) {
			temp[i] = POSLIST[i];
		}
		int index = POSLIST.length;
		for (String newpos : extendedList) {
			temp[index++] = newpos;
		}
		POSLIST = temp;

		for (int i = 0; i < POSLIST.length; i++) {
			tag2code.put(POSLIST[i], (byte) i);
		}
	}

	public long string2long(String posSeq) {
		String[] patternArr = posSeq.split("_");
		long tagsequence = 0l;
		for (int i = 0; i < patternArr.length; i++) {
			byte code = getCode(patternArr[i]);
			tagsequence = setTagAt(tagsequence, i, code);
		}
		return tagsequence;
	}

	public byte getCode(String tag) {
		Byte code = tag2code.get(tag);
		if (code != null)
			return code;
		else
			return (byte) -1; // error!!!
	}

	public String getTag(int code) {
		return POSLIST[code];
	}

	public long setTagAt(long tagseq, int pos, byte code) {
		pos *= 8; // convert position from byte to bit
		return (tagseq | (0xFF << pos)) & (code << pos);
	}

	public byte getTagAt(long tagseq, int pos) {
		pos *= 8; // convert position from byte to bit
		return (byte) (tagseq >> pos);
	}
}