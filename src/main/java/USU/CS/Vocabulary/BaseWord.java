package USU.CS.Vocabulary;

import java.util.HashSet;
import java.util.Set;

import USU.CS.Utils.POSTagConverter;

public class BaseWord extends Word {

	// priority of the root form helps mapping irregular variants back to its
	// root.
	// this doesn't need to be right, it just needs to be consistent.
	// ADJ = 1
	// VERB = 2
	// NOUN = 3
	// UNKNOWN = 0
	private int mPriority = 0;

	@Override
	protected Word doOnCreate() {
		// TODO Auto-generated method stub
		String pos = POSTagConverter.getInstance().getTag(mPOS);
		if (pos.equals("JJ"))
			mPriority = 1;
		if (pos.equals("VB"))
			mPriority = 2;
		if (pos.equals("NN"))
			mPriority = 3;

		return this;
	}

	public int getPriority() {
		return mPriority;
	}

	private Set<Variant> mVariantSet = new HashSet<>();

	public void addVariant(Variant var) {
		var.addBase(this);
		mVariantSet.add(var);
	}

	public String toFullTextForm() {
		StringBuilder strBld = new StringBuilder();
		strBld.append(mWord);
		for (Variant var : mVariantSet) {
			strBld.append(" ").append(var.toString());
		}
		return strBld.toString();
	}

	public Set<Variant> getVariantSet() {
		return mVariantSet;
	}
}
