package USU.CS.Vocabulary;

import java.util.HashSet;
import java.util.Set;

public class Variant extends Word {
	private BaseWord prioritizedRoot = null;

	@Override
	protected Word doOnCreate() {
		// TODO Auto-generated method stub
		return this;
	}

	// the possible roots of this variant
	private Set<BaseWord> mBaseWordSet = new HashSet<>();

	public void addBase(BaseWord base) {
		mBaseWordSet.add(base);
		if (prioritizedRoot != null) {
			if (prioritizedRoot.getPriority() < base.getPriority())
				prioritizedRoot = base;
		} else
			prioritizedRoot = base;
	}

	public BaseWord getPrioritizedRoot() {
		return prioritizedRoot;
	}

	public Set<BaseWord> getRoots() {
		return mBaseWordSet;
	}
}
