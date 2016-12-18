package USU.CS.Vocabulary;

import USU.CS.Utils.POSTagConverter;

public abstract class Word implements Comparable<Word> {
	static private int INCREMENTOR = 0;

	private int assignID() {
		return INCREMENTOR++;
	}

	protected byte mPOS;
	protected int mID;
	protected String mWord = null;

	public int compareTo(Word w) {
		return this.mWord.compareTo(w.mWord);
	}

	// This is the implementation of the interface method
	// Note it's final so it can't be overridden
	public final Word onCreate(String w, String POS) throws Exception {
		// Hence any logic right here always gets run
		// INSERT LOGIC
		if (w == null)
			throw new Exception("Can't create word from a NULL object");
		if (w.length() == 0)
			throw new Exception("Can't create word from an empty String");
		mPOS = POSTagConverter.getInstance().getCode(POS);
		mID = assignID();
		mWord = w.intern();
		return doOnCreate();

		// If you wanted you could instead create a reference to the
		// object returned from the subclass, and then do some
		// post-processing logic here
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if (obj instanceof Word) {
			Word w = (Word) obj;
			return mWord.equals(w.toString()) && mPOS == w.getPOS();
		} else
			return false;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return mWord.hashCode();
	}

	public int getID() {
		return mID;
	}

	public byte getPOS() {
		return mPOS;
	}

	public String toString() {
		return mWord;
	}

	protected abstract Word doOnCreate();
}
