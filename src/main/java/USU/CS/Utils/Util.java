package USU.CS.Utils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class Util {
	public static List<String> listFilesForFolder(final String folderName)
			throws IOException {
		List<String> filePaths = new ArrayList<>();

		Files.walk(Paths.get(folderName)).forEach(filePath -> {
			if (Files.isRegularFile(filePath)) {
				filePaths.add(filePath.toString());
			}
		});
		return filePaths;
	}
	public static String convertTime(long time) {
        Date date = new Date(time);
        SimpleDateFormat df2 = new SimpleDateFormat("dd/MMM/yyyy");
        return df2.format(date);
    }
        public static String convertTimeDetail(long time) {
        Date date = new Date(time);
        SimpleDateFormat df2 = new SimpleDateFormat("hh:mm dd/MMM/yyyy");
        return df2.format(date);
    }
	public static <T> List<T> deepCopyList(List<T> input) {
		List<T> output = new ArrayList<>();
		for (T item : input)
			output.add(item);
		return output;
	}

	public static <T> String collectionToCSVprintable(Collection<T> list) {
		StringBuilder printable = new StringBuilder();
		for (T item : list) {
			printable.append("[").append(item.toString()).append("]");
		}
		return printable.toString();
	}

	public static String replaceSomeWords(final String text) {
		return text.replace("n't", "not").replace("'s", "is");
	}

	public static String StripDot(final CharSequence input) {
		final StringBuilder sb = new StringBuilder(input.length());
		for (int i = 0; i < input.length(); i++) {
			final char c = input.charAt(i);
			if (c != 46) {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	static public double log(int x, int base) {
		return (Math.log(x) / Math.log(base));
	}

	public static double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	public static String ReplaceNonInterestingChars(final CharSequence input) {
		final StringBuilder sb = new StringBuilder(input.length());
		for (int i = 0; i < input.length(); i++) {
			final char c = input.charAt(i);
			if ((c > 32 && c < 48) || (c > 57 && c < 65) || (c > 90 && c < 97)
					|| (c > 122)) {
				sb.append(". ");
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static long normalizeDate(long date) throws ParseException {
		Date d = new Date(date);
		SimpleDateFormat df2 = new SimpleDateFormat("dd-MM-yy");
		String dateText = df2.format(d);
		return df2.parse(dateText).getTime();
	}

	public static String getMonthYear(long date) throws ParseException {
		Date d = new Date(date);
		SimpleDateFormat df2 = new SimpleDateFormat("MMM-yyyy");
		return df2.format(d);
	}

	public static int getMonth(long date) throws ParseException {
		Date d = new Date(date);
		SimpleDateFormat df2 = new SimpleDateFormat("MMM");
		String month = df2.format(d);
		switch (month) {
		case "Jan":
			return 1;
		case "Feb":
			return 2;
		case "Mar":
			return 3;
		case "Apr":
			return 4;
		case "May":
			return 5;
		case "Jun":
			return 6;
		case "Jul":
			return 7;
		case "Aug":
			return 8;
		case "Sep":
			return 9;
		case "Nov":
			return 11;
		case "Dec":
			return 12;
		default:
			return -1;
		}
	}

	public static int[] toIntArray(List<Integer> list) {
		int[] ret = new int[list.size()];
		for (int i = 0; i < ret.length; i++)
			ret[i] = list.get(i);
		return ret;
	}

	public static boolean isNumeric(String str) {
		try {
			double d = Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public static String removeNonChars(final CharSequence input) {
		final StringBuilder sb = new StringBuilder(input.length());
		for (int i = 0; i < input.length(); i++) {
			final char c = input.charAt(i);
			if ((c > 64 && c < 91) || (c > 96 && c < 123) || (c == ' ')) {
				sb.append(c);
			} else {
				if (c == '.' || c == ',')
					sb.append(' ');
				else
					sb.append(" ");
			}

		}
		return sb.toString();
	}

	public static boolean hasNumeric(final CharSequence input) {
		// TODO Auto-generated method stub
		for (int i = 0; i < input.length(); i++) {
			final char c = input.charAt(i);
			if (c >= '0' && c <= '9') {
				return true;
			}

		}
		return false;
	}
	public static boolean hasSpecialCharacters(final CharSequence input){
		for (int i = 0; i < input.length(); i++) {
			final char c = input.charAt(i);
			if (!(c >= '0' && c <= '9')&&!(c >= 'a' && c <= 'z')&&!(c >= 'A' && c <= 'Z')) {
				return true;
			}

		}
		return false;
	}
	public static boolean isSpecialCharacter(String word) {
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
}
