package ab2.impl.Wachter;

import java.util.*;

public class TupleTransitions {
    private HashMap<Integer, HashMap<String, Set<Integer>>> stateSymbolStatesMap
            = new HashMap<>();

    public void addTransition(int state, String toRead, Integer nextState) {
        if (toRead.length() > 1)
            throw new IllegalArgumentException("Cant. Only add one char or epsilon transitions.");
        if (!stateSymbolStatesMap.containsKey(state)) {
            HashMap<String, Set<Integer>> newSymbolStatesMap = new HashMap<>();
            stateSymbolStatesMap.put(state, newSymbolStatesMap);
        }

        HashMap<String, Set<Integer>> symbolStatesMap = stateSymbolStatesMap.get(state);
        if (symbolStatesMap.containsKey(toRead)) {
            Set<Integer> states = symbolStatesMap.get(toRead);
            states.add(nextState);
        } else
            symbolStatesMap.put(toRead, new HashSet<>(Collections.singletonList(nextState)));
    }

    public Set<Integer> getNextStates(int currentState, String toRead) {
        if (stateSymbolStatesMap.containsKey(currentState))
            if (stateSymbolStatesMap.get(currentState).containsKey(toRead))
                return stateSymbolStatesMap.get(currentState).get(toRead);
        return Collections.emptySet();
    }

    public HashMap<Integer, HashMap<String, Set<Integer>>> getStateSymbolStatesMap() {
        return stateSymbolStatesMap;
    }

    @Override
    public String toString() {
        String result = "";
        for (Map.Entry<Integer, HashMap<String, Set<Integer>>> stateSymbolStates : stateSymbolStatesMap.entrySet()) {
            for (Map.Entry<String, Set<Integer>> symbolsStates : stateSymbolStates.getValue().entrySet()) {
                String sTransition = "" + stateSymbolStates.getKey();
                sTransition += " -" + symbolsStates.getKey() + "-> {";
                for (Integer state : symbolsStates.getValue()) {
                    sTransition += state + ",";
                }
                sTransition += "}\n";
                result += sTransition;
            }
        }
        return result;
    }

}
