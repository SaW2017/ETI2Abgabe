package ab2.impl.Wachter;

import ab2.RSA;

import java.util.Set;

public class RSAImpl extends DFAImpl implements RSA {

    public RSAImpl(int numStates, Set<Character> characters, Set<Integer> acceptingStates) {
        super(numStates, characters, acceptingStates);
    }

    public RSAImpl(int numStates, Set<Character> characters, Set<Integer> acceptingStates, int initialState, TupleTransitions transitions) {
        this(numStates, characters, acceptingStates);
        this.transitions = transitions;
    }

    @Override
    public RSA minimize() {
        return null;
    }
}
