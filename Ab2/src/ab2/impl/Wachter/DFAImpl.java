package ab2.impl.Wachter;

import ab2.DFA;
import ab2.fa.exceptions.IllegalCharacterException;

import java.util.Set;

public class DFAImpl extends NFAImpl implements DFA {
    int currentState;

    public DFAImpl(int numStates, Set<Character> characters, Set<Integer> acceptingStates) {
        super(numStates, characters, acceptingStates);
       // 0 ist Startzustand
        currentState = 0;
    }

    @Override
    public void reset() {
        currentState = 0;
    }

    @Override
    public int getActState() {
        return currentState;
    }

    @Override
    public int doStep(char c) throws IllegalArgumentException, IllegalStateException {
        currentState = transitions.getNextStates(currentState, String.valueOf(c)).iterator().next();
        return currentState;
    }

    @Override
    public Integer getNextState(int s, char c) throws IllegalCharacterException, IllegalStateException {
        Set<Integer> nextStates = transitions.getNextStates(s, String.valueOf(c));
        return nextStates.iterator().next();
    }

    @Override
    public boolean isAcceptingState() {
        return isAcceptingState(currentState);
    }

    @Override
    public void setTransition(int fromState, char c, int toState) throws IllegalStateException, IllegalCharacterException {
        setTransition(fromState, String.valueOf(c), toState);
    }
}
