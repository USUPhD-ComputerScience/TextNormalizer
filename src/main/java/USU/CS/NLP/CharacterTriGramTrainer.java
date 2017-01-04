package USU.CS.NLP;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import USU.CS.TextNormalizer.TextNormalizer;
import USU.CS.Utils.Util;


public class CharacterTriGramTrainer {
	long[][][][] count = new long[27][27][27][27];
	long[][][] total = new long[27][27][27];
	long totalCount = 0;
	private static CharacterTriGramTrainer instance = null;
	 public static final String INPUTFILE = TextNormalizer.getTrigramTrainingDirectory();
	//public static final String INPUTFILE = "text8";
	public static final String OUTPUTFILE = TextNormalizer.getDictionaryDirectory()+"trigramData/trainedData.txt";

	public static CharacterTriGramTrainer getInstance() {
		if (instance == null) {
			File fcheckExist = new File(OUTPUTFILE);
			instance = new CharacterTriGramTrainer();
			if (fcheckExist.exists() && !fcheckExist.isDirectory()) {
				System.err.println("> reading trained nGram data");
				instance.readTrainedData(OUTPUTFILE);
			} else {
				System.out.println("Trainning...");
				long startTime = System.nanoTime();
				System.out.println("Number of word trained: "
						+ instance.train(INPUTFILE, OUTPUTFILE));
				System.out
						.println("total time: "
								+ ((double) (System.nanoTime() - startTime) / 1000000 / 1000)
								+ " seconds");
			}
			instance.computeTotal();
		}
		return instance;
	}

	public double getProbability(char a, char b, char c, char d) {
		return (double) count[code(a)][code(b)][code(c)][code(d)]
				/ total[code(a)][code(b)][code(c)];
	}

	public long getCount(char a, char b, char c, char d) {
		return count[code(a)][code(b)][code(c)][code(d)];
	}

	private int code(char c) {
		return c == ' ' ? 26 : c - 'a';
	}

	public void update(String word) {
		char[] charArray = word.toCharArray();
		if (charArray.length < 4)
			return;
		count[26][charArray[0] - 'a'][charArray[1] - 'a'][charArray[2] - 'a']++;
		for (int i = 1; i < charArray.length - 2; i++) {
			count[charArray[i - 1] - 'a'][charArray[i] - 'a'][charArray[i + 1] - 'a'][charArray[i + 2] - 'a']++;
			totalCount++;
		}
		count[charArray[charArray.length - 3] - 'a'][charArray[charArray.length - 2] - 'a'][charArray[charArray.length - 1] - 'a'][26]++;
		totalCount += 2;
	}

	public long train(String inputDirectory, String outputFile) {
		String words[] = null;
		Scanner br = null;
		long wordCount = 0;
		try {
			List<String> filePaths = Util.listFilesForFolder(inputDirectory);
			for (String fileName : filePaths) {
				br = new Scanner(new FileReader(fileName));
				while (br.hasNextLine()) {
					words = Util.removeNonChars(br.nextLine()).toLowerCase()
							.split(" ");
					for (String w : words) {
						wordCount++;
						update(w);
					}
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

		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new FileWriter(outputFile));
			for (int i = 0; i < 27; i++)
				for (int j = 0; j < 27; j++)
					for (int k = 0; k < 27; k++)
						for (int h = 0; h < 27; h++) {
							pw.write(String.valueOf(count[i][j][k][h]));
							pw.write("\n");
						}
			pw.write(String.valueOf(totalCount));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (pw != null)
				pw.close();
		}
		return wordCount;
	}

	public void computeTotal() {
		for (int i = 0; i < 27; i++)
			for (int j = 0; j < 27; j++)
				for (int k = 0; k < 27; k++)
					for (int h = 0; h < 27; h++) {
						total[i][j][k] += count[i][j][k][h];
					}
	}

	public long[][][][] readTrainedData(String fileName) {
		Scanner br = null;
		try {
			br = new Scanner(new FileReader(fileName));
			for (int i = 0; i < 27; i++)
				for (int j = 0; j < 27; j++)
					for (int k = 0; k < 27; k++)
						for (int h = 0; h < 27; h++)
							count[i][j][k][h] = Long.parseLong(br.nextLine());
			totalCount = Long.parseLong(br.nextLine());
		} catch (NumberFormatException ne) {
			// TODO Auto-generated catch block
			ne.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (br != null)
				br.close();
		}
		return count;
	}
}
