package ab2.impl.Wachter;

import java.util.Set;

import ab2.DFA;
import ab2.FAFactory;
import ab2.NFA;
import ab2.RSA;

public class FAFactoryImpl implements FAFactory {

	@Override
	public NFA createNFA(int numStates, Set<Character> characters, Set<Integer> acceptingStates) {
		return new NFAImpl(numStates, characters, acceptingStates);
	}

	@Override
	public DFA createDFA(int numStates, Set<Character> characters, Set<Integer> acceptingStates) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RSA createRSA(int numStates, Set<Character> characters, Set<Integer> acceptingStates) {
		return new RSAImpl(numStates, characters, acceptingStates);
	}

	@Override
	public RSA createPatternMatcher(String pattern) {
		return NFAImpl.fromPattern(pattern).toRSA();
	}

}
