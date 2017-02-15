package USU.CS.TextNormalizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class TestNormalizer extends TestCase {
	/**
	 * Create the test case
	 *
	 * @param testName
	 *            name of the test case
	 */
	public TestNormalizer(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(TestNormalizer.class);
	}

	/**
	 * Rigourous Test :-)
	 */
	public void testNormarlizing() {

		assertTrue(true);
	}

	
	// TODO: do this!!!!!!
	public static List<String> readTestMaterial(String fileName) throws FileNotFoundException {
		Scanner scanner = new Scanner(new File(fileName));
		scanner.close();
		return null;
	}
}
