package ab2.impl.Wachter;

import ab2.NFA;
import ab2.RSA;
import ab2.fa.exceptions.IllegalCharacterException;

import java.util.*;

public class NFAImpl implements NFA {

    protected TupleTransitions transitions;
    private int numStates;
    private Set<Character> symbols;
    private Set<Integer> acceptingStates;
    private Set<Integer> allStates;
    private int initialState;
    private int stateCounter;
    //private Set<String> [][] transitions = new Set[0][];

    public NFAImpl(int numStates, Set<Character> characters, Set<Integer> acceptingStates){
        this.numStates = numStates;
        stateCounter = numStates;
        this.symbols = characters;
        this.acceptingStates = acceptingStates;
        this.allStates = acceptingStates;
        this.initialState = initialState;

        transitions = new TupleTransitions();
    }

    @Override
    public Set<Character> getSymbols() {
        return this.symbols;
    }

    @Override
    public Set<Integer> getAcceptingStates() {
        return this.acceptingStates;
    }

    @Override
    public boolean isAcceptingState(int s) throws IllegalStateException {
        //todo keine ahnung wie ich das anpassen soll wegen der exception
//        boolean accepting;
//        if(allStates.contains(s)){
//            accepting = false;
//        }if(acceptingStates.contains(s)){
//            accepting = true;
//        }else{
//         throw new IllegalStateException("This state doesnt exist.");
//        }
//        return accepting;
        return acceptingStates.contains(s);
    }

    @Override
    public Set<String>[][] getTransitions() {
        Set<String>[][] transitArray = new Set[getNumStates()][getNumStates()];
        Set<String> temporarySet = new HashSet<>(getSymbols().size());

        for (Integer i : transitions.getStateSymbolStatesMap().keySet()) {
            for (String s : transitions.getStateSymbolStatesMap().get(i).keySet()) {
                for (Integer j : transitions.getStateSymbolStatesMap().get(i).get(s)) {
                    if (transitArray[i][j] != null) temporarySet = transitArray[i][j];
                    temporarySet.add(s);
                    transitArray[i][j] = temporarySet;
                }
                temporarySet = new HashSet<>(getSymbols().size());
            }
        }
        return transitArray;
    }

    @Override
    public void setTransition(int fromState, String s, int toState) throws IllegalStateException, IllegalCharacterException {
        if (s.length() <= 1)
            transitions.addTransition(fromState, s, toState);
        else {
            int newState = getNewState();
            numStates++;
            transitions.addTransition(fromState, s.substring(0, 1), newState);
            setTransition(newState, s.substring(1), toState);
        }
    }

    private int getNewState() {
        return stateCounter++;
    }

    @Override
    public void clearTransitions(int fromState, String s) throws IllegalStateException {

    }

    @Override
    public Set<Integer> getNextStates(int state, String s) throws IllegalCharacterException, IllegalStateException {
        Set<Integer> currentStates = epsilonShell(state);
        for (char c : s.toCharArray()) {
            Set<Integer> nextStates = new HashSet<>();
            for (Integer currentState : currentStates) {
                for (Integer equivState : epsilonShell(currentState)) {
                    nextStates.addAll(transitions.getNextStates(equivState, String.valueOf(c)));
                }
            }

            currentStates.clear();
            for (Integer nextState : nextStates) {
                currentStates.addAll(epsilonShell(nextState));
            }
        }
        return currentStates;
    }

    public Set<Integer> getNextStates(Set<Integer> states, String s) {
        Set<Integer> nextStates = new HashSet<>();
        for (Integer state : states) {
            nextStates.addAll(getNextStates(state, s));
        }
        return nextStates;
    }

    @Override
    public int getNumStates() {
        return this.numStates;
    }

    @Override
    public NFA union(NFA a) {
        Set<Character> unionCharachters = new HashSet<>();
        Set<Integer> unionAcceptingStates = new HashSet<>();
        int unionNumberOfStates = getNumStates() + a.getNumStates() + 1;
        unionCharachters.addAll(getSymbols());
        unionCharachters.addAll(a.getSymbols());
        int offset = getNumStates();

        for (Integer i : getAcceptingStates()) {
            unionAcceptingStates.add(i + 1);
        }
        for (Integer i : a.getAcceptingStates()) {
            unionAcceptingStates.add(i + 1 + offset);
        }


        NFA unionNFA = new NFAImpl(unionNumberOfStates, unionCharachters, unionAcceptingStates);
        unionNFA.setTransition(0, "", 1);
        unionNFA.setTransition(0, "", 1 + offset);

        for (Integer fromState : transitions.getStateSymbolStatesMap().keySet()) {
            for (String s : transitions.getStateSymbolStatesMap().get(fromState).keySet()) {
                for (Integer toState : transitions.getStateSymbolStatesMap().get(fromState).get(s)) {
                    unionNFA.setTransition(fromState + 1, s, toState + 1);
                }
            }

        }
        Set<String>[][] transArray = a.getTransitions();
        for (int i = 0; i < transArray.length; i++) {
            for (int j = 0; j < transArray[i].length; j++) {
                if (transArray[i][j] != null) {
                    for (String s : transArray[i][j]) {
                        unionNFA.setTransition(i + 1 + offset, s, j + 1 + offset);
                    }
                }
            }
        }

        return unionNFA;
    }

    @Override
    public NFA intersection(NFA a) {
        return (this.complement().union(a.complement())).complement();
    }

    @Override
    public NFA minus(NFA a) {
        return (this.complement().union(a)).complement();
    }

    @Override
    public NFA concat(NFA nfaTwo) {
        Set<String>[][] transitionMatrix1 = this.getTransitions();
        Set<String>[][] transitionMatrix2 = nfaTwo.getTransitions();

        Set<Integer> concatAcceptingStates = new TreeSet<>();

        if (this.getAcceptingStates().isEmpty()) {
            NFA returnNFA = new NFAImpl(1, symbols, this.getAcceptingStates());
            return returnNFA;
        }
        if (nfaTwo.getAcceptingStates().isEmpty()) {
            NFA returnNFA = new NFAImpl(1, symbols, nfaTwo.getAcceptingStates());
            return returnNFA;
        }


        for (Integer state : nfaTwo.getAcceptingStates()) {
            concatAcceptingStates.add(state + numStates + 1);
        }

        NFA concatNFA = new NFAImpl(numStates + nfaTwo.getNumStates() + 1, symbols, concatAcceptingStates);

        concatNFA.setTransition(0, "", 1); //neue Anfangszustand e 0 hat eine EpsilonKante zum alten Startzustand vom ersten NFA
        for (int row = 0; row < numStates; row++) {
            for (int column = 0; column < numStates; column++) {
                if (isAcceptingState(column)) {
                    // Achtung richtigen Startyustand berechnen?
                    concatNFA.setTransition(column + 1, "", numStates + 1);
                }

                if (transitionMatrix1[row][column] != null) {
                    for (String symbol : transitionMatrix1[row][column]) {
                        concatNFA.setTransition(row + 1, symbol, column + 1);
                    }
                }
            }
        }

        for (int row = 0; row < nfaTwo.getNumStates(); row++) {
            for (int column = 0; column < nfaTwo.getNumStates(); column++) {
                if (transitionMatrix2[row][column] != null) {
                    for (String symbol : transitionMatrix2[row][column]) {
                        concatNFA.setTransition(row + 1 + numStates, symbol, column + 1 + numStates);
                    }
                }
            }
        }
        return concatNFA;
    }

    @Override
    public NFA complement() {
        RSA rsa = this.toRSA();
        Set<Integer> acceptingStates = rsa.getAcceptingStates();
        Set<Integer> notAcceptingStates = new TreeSet<Integer>();
        for (int i = 0; i < rsa.getNumStates(); i++) {
            if (!acceptingStates.contains(i)) {
                notAcceptingStates.add(i);
            }
        }
        NFAImpl complNFA = new NFAImpl(rsa.getNumStates(), rsa.getSymbols(), notAcceptingStates);
        Set<String>[][] rsaTransitions = rsa.getTransitions();
        for (int row = 0; row < rsa.getNumStates(); row++) {
            for (int column = 0; column < rsa.getNumStates(); column++) {
                if (rsaTransitions[row][column] != null) {
                    for (String s : rsaTransitions[row][column]) {
                        complNFA.setTransition(row, s, column);
                    }
                }
            }
        }
        return complNFA;
    }

    @Override
    public NFA kleeneStar() {
        NFAImpl kleeneNFA;

        Set<Integer> kleeneAcceptingStates = new HashSet<>();
        int kleeneInitialState = 0;

        for (Integer acceptState : this.getAcceptingStates()) {
            kleeneAcceptingStates.add(acceptState + 1);
        }
        kleeneAcceptingStates.add(kleeneInitialState);


        kleeneNFA = new NFAImpl(this.getNumStates() + 1, this.getSymbols(), kleeneAcceptingStates);

        Set<String>[][] thisTransitions = this.getTransitions();
        for (int i = 0; i < thisTransitions.length; i++) {
            for (int j = 0; j < thisTransitions[i].length; j++) {
                if (thisTransitions[i][j] != null) {
                    for (String string : thisTransitions[i][j]) {
                        kleeneNFA.transitions.addTransition(i + 1, string, j + 1);
                    }
                }
            }
        }
        // Anfangszustand plus 1
        kleeneNFA.transitions.addTransition(0, "", 1);

        for (int acceptState : this.getAcceptingStates()) { // EpsilonKanten von Endzustaenden zu Anfangszustand
            kleeneNFA.transitions.addTransition(acceptState + 1, "", 0);

        }
        return kleeneNFA;
    }

    @Override
    public NFA plus() {
        NFAImpl plusNFA;
        Set<Integer> plusAcceptingStates = new HashSet<>();
        plusAcceptingStates.addAll(this.getAcceptingStates());
        plusNFA = new NFAImpl(this.getNumStates(), this.getSymbols(), plusAcceptingStates);

        Set<String>[][] thisTransitions = this.getTransitions();
        for (int i = 0; i < thisTransitions.length; i++) {
            for (int j = 0; j < thisTransitions[i].length; j++) {
                if (thisTransitions[i][j] != null) {
                    for (String string : thisTransitions[i][j]) {
                        plusNFA.transitions.addTransition(i, string, j);
                    }
                }
            }
        }

        for (int acceptState : this.getAcceptingStates()) {//EpsilonKante yum Startzustand
            plusNFA.transitions.addTransition(acceptState, "", 0);
        }

        return plusNFA;
    }

    @Override
    public RSA toRSA() {
        int rsaStateCounter = 0;
        TupleTransitions rsaTransitions = new TupleTransitions();
        Set<Integer> rsaAcceptingStates = new HashSet<>();
        Map<Set<Integer>, Integer> nfaRsaMap = new HashMap<>();
        Set<Set<Integer>> nfaStatesToDo = new HashSet<>();


        Set<Integer> initialEpsilonHull = epsilonShell(initialState);
        nfaRsaMap.put(initialEpsilonHull, rsaStateCounter);
        nfaStatesToDo.add(initialEpsilonHull);

        while (!nfaStatesToDo.isEmpty()) {
            Set<Integer> currentNfaStates = nfaStatesToDo.iterator().next();
            int currentRsaState = nfaRsaMap.get(currentNfaStates);

            if (isAcceptingState(currentNfaStates))
                rsaAcceptingStates.add(currentRsaState);

            for (Character symbol : symbols) {
                Set<Integer> nextNfaStates = getNextStates(currentNfaStates, symbol.toString());

                if (nfaRsaMap.containsKey(nextNfaStates)) {
                    rsaTransitions.addTransition(
                            currentRsaState,
                            symbol.toString(),
                            nfaRsaMap.get(nextNfaStates)
                    );
                } else {
                    int nextRsaState = ++rsaStateCounter;
                    rsaTransitions.addTransition(
                            currentRsaState,
                            symbol.toString(),
                            nextRsaState
                    );
                    nfaRsaMap.put(nextNfaStates, nextRsaState);
                    nfaStatesToDo.add(nextNfaStates);
                }
            }

            nfaStatesToDo.remove(currentNfaStates);
        }

        return new RSAImpl(
                rsaStateCounter + 1,
                symbols,
                rsaAcceptingStates,
                0,
                rsaTransitions
        );
    }

    @Override
    public Boolean accepts(String w) throws IllegalCharacterException {
        Set<Integer> states = getNextStates(initialState, w);
        return isAcceptingState(states);
    }

    @Override
    public Boolean acceptsNothing() {
        return getAcceptingStates().isEmpty();
    }

    @Override
    public Boolean acceptsEpsilonOnly() {
        if (!acceptsEpsilon())
            return false;
        // Epislonhuelle des Startzustands
        Set<Integer> initialEpsilonHull = epsilonShell(0);
        Set<Integer> statesReached = new HashSet<>();
        Set<Integer> currentStates = new HashSet<>(initialEpsilonHull);

        while (true) {
            Set<Integer> nextReachableStates = new HashSet<>();
            for (Integer state : currentStates)
                nextReachableStates.addAll(getNextReachableStates(state));


            if (isAcceptingState(nextReachableStates))
                return false;

            int stateCountBefore = statesReached.size();
            statesReached.addAll(nextReachableStates);
            currentStates.addAll(nextReachableStates);
            int stateCountAfter = statesReached.size();
            if (stateCountAfter == stateCountBefore)
                break;
        }
        return true;
    }

    @Override
    public Boolean acceptsEpsilon() {
        Set<Integer> initialEpsilonHull = epsilonShell(0);
        return !Collections.disjoint(initialEpsilonHull, getAcceptingStates());
    }

    @Override
    public Boolean isInfinite() {
        Map<Integer, Set<Integer>> stateReachableStatesMap = new HashMap<>();
        for (int s = 0; s < getNumStates(); s++) {
            stateReachableStatesMap.put(s, getNextReachableStates(s));
        }

        // testen welche Zustaende mit noch einem weiteren Symbol erreichbar werden
        // falls keine Zustaende mehr erreichbar sind endet der Algorithmus
        while (true) {
            int newStatesReached = 0;

            for (int fromState = 0; fromState < getNumStates(); fromState++) {
                Set<Integer> reachableStates = stateReachableStatesMap.get(fromState);
                for (Integer reachableState : reachableStates) {
                    if (stateReachableStatesMap.get(reachableState).contains(fromState)) {
                        // falls ein Pfad von fromState ueber* reachableState nach fromState existiert gibt es eine Schleife
                        if (canReachAcceptingState(fromState))
                            return true;
                    }
                }

                // testen was noch erreicht werden kann
                int reachableStatesBefore = reachableStates.size();
                Set<Integer> nextReachableStates = new HashSet<>();
                for (Integer reachableState : reachableStates)
                    nextReachableStates.addAll(getNextReachableStates(reachableState));
                reachableStates.addAll(nextReachableStates);
                newStatesReached += reachableStates.size() - reachableStatesBefore;
            }

            if (newStatesReached == 0)
                break;
        }

        return false;
    }

    @Override
    public Boolean isFinite() {
        return !isInfinite();
    }

    @Override
    public Boolean subSetOf(NFA a) { //todo man muesste die equals umschreiben erst checken ob es ein NFA ist usw
        boolean checkSubset = false;
        if (this.intersection(a).equals(a) && a.minus(this).acceptsNothing()) {
            checkSubset = false;
        }
        if (a.intersection(this).equals(this) && this.minus(a).acceptsNothing()) {
            checkSubset = true;
        }
        return checkSubset;
    }

    @Override
    public Boolean equalsPlusAndStar() {
        return this.acceptsEpsilon();
    }

    // Hilfsmethoden

    private Set<Integer> epsilonShell(int s) {
        Set<Integer> shell = new HashSet<>();
        Set<Integer> shellExtension = new HashSet<>();
        shellExtension.add(s);
        // Epsilonhuelle solange erweitern bis sich nichts mehr aendert
        while (!shellExtension.isEmpty()) {
            shell.addAll(shellExtension);
            shellExtension.clear();
            for (Integer state : shell) {
                for (Integer equivState : transitions.getNextStates(state, "")) {
                    if (!shell.contains(equivState))
                        shellExtension.add(equivState);
                }
            }
        }
        return shell;
    }

    public boolean isAcceptingState(Set<Integer> states) {
        for (Integer state : states) {
            if (isAcceptingState(state))
                return true;
        }
        return false;
    }

    private boolean canReachAcceptingState(int fromState) {
        Set<Integer> reachableStates = epsilonShell(fromState);
        while (true) {
            if (isAcceptingState(fromState)){
                return true;
            }

            Set<Integer> nextStates = new HashSet<>();
            int stateCountBefore = reachableStates.size();
            for (Integer reachableState : reachableStates) {
                nextStates.addAll(getNextReachableStates(reachableState));
            }
            reachableStates.addAll(nextStates);
            if (reachableStates.size() == stateCountBefore)
                break;
        }
        return false;
    }

    // von einem Zustand ausgehend berechnen wir alle Zustaende welche mittels einem Symbol erreichbar sind
    private Set<Integer> getNextReachableStates(int fromState) {
        Set<Integer> nextReachableStates = new HashSet<>();
        for (Character symbol : getSymbols()) {
            nextReachableStates.addAll(
                    getNextStates(fromState, symbol.toString())
            );
        }
        return nextReachableStates;
    }

    private int addNewState() {
        numStates++;
        return stateCounter++;
    }

    public static NFAImpl fromPattern(String pattern) {
        Set<Character> characters = new HashSet<Character>(Arrays.asList('a', 'b'));
        Set<Integer> acceptingStates = new HashSet<>();
        NFAImpl nfa = new NFAImpl(1, characters, acceptingStates);

        //start anywhere
        for (Character symbol : characters)
            nfa.setTransition(0, symbol, 0);

        int previousState = 0;
        char previousChar = '.';
        for (char c : pattern.toCharArray()) {
            int newState = nfa.addNewState();

            switch (c) {
                case '.':
                    for (Character symbol : characters)
                        nfa.setTransition(previousState, symbol, newState);
                    break;
                case '*':
                    nfa.setTransition(previousState, "", newState);
                    nfa.setTransition(newState, previousChar, previousState);
                    break;
                default:
                    nfa.setTransition(previousState, c, newState);
            }

            previousChar = c;
            previousState = newState;
        }
        acceptingStates.add(previousState);
        return nfa;
    }

    public void setTransition(int fromState, char symbol, int toState) {
        setTransition(fromState, String.valueOf(symbol), toState);
    }
}
