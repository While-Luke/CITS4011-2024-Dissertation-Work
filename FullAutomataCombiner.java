import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class FullAutomataCombiner {
    public static void main(String[] args){

        //Simple 4-node graph
        /*int[][] transitionMatrix = new int[][]{{0,1,1,0},
                                                {1,0,1,0},
                                                {1,1,0,1},
                                                {0,0,1,0}};*/


        //Non-word-representable 6-node graph (Wheel graph)
        /*int[][] transitionMatrix = new int[][]{{0,1,0,0,1,1},
                                                {1,0,1,0,0,1},
                                                {0,1,0,1,0,1},
                                                {0,0,1,0,1,1},
                                                {1,0,0,1,0,1},
                                                {1,1,1,1,1,0}};*/


        //Word-representable 6-node graph
        /*int[][] transitionMatrix = new int[][]{{0,1,1,0,1,1},
                                                {1,0,1,0,0,1},
                                                {1,1,0,1,0,1},
                                                {0,0,1,0,1,1},
                                                {1,0,0,1,0,1},
                                                {1,1,1,1,1,0}};*/


        //Word-representable 7-node graph
        int[][] transitionMatrix = new int[][]{{0,1,0,1,1,0,0},
                                                {1,0,0,1,1,1,0},
                                                {0,0,0,0,0,0,1},
                                                {1,1,0,0,0,1,0},
                                                {1,1,0,0,0,0,0},
                                                {0,1,0,1,0,0,1},
                                                {0,0,1,0,0,1,0}};

        Long start = System.currentTimeMillis();
        
        if(checkWordRepresentable(transitionMatrix, true)){
            System.out.println("This graph IS word representable");
        }
        else{
            System.out.println("This graph is NOT word representable");
        }

        System.out.println("Program Duration: " + (System.currentTimeMillis() - start));
    }

    public static boolean checkWordRepresentable(int[][] transitionMatrix){
        return checkWordRepresentable(transitionMatrix, false);
    }

    public static boolean checkWordRepresentable(int[][] transitionMatrix, boolean verbose){
        /*
         * Returns true if the given graph is word-representable, else false.
         */

        List<Automata> automatas = graphToAutomata(transitionMatrix);

        Automata combined = combineAutomatas(automatas.get(0), automatas.get(1));
        trimAutomata(combined);
        renameAutomata(combined);

        //hopcroft(combined);

        if(verbose) System.out.println("Number of combined automatas: 2 - Number of States: " + combined.states.length);

        for(int i = 2; i < automatas.size(); i++){
            combined = combineAutomatas(combined, automatas.get(i));
            trimAutomata(combined);
            renameAutomata(combined);

            if(combined.acceptingStates.size() == 0){
                return false;
            }
            
            //hopcroft(combined);

            if(verbose) System.out.println("Number of combined automatas: " + (i+1) + " - Number of States: " + combined.states.length);
        }

        //combined.print();
        combined.writeToFile();

        return true;
    }

    public static List<Automata> graphToAutomata(int[][] transitionMatrix){
        /*
         * Converts a given graph represented by the given transition matrix into
         * a list of DFAs to recognise the edges in the graph.
         */

        List<Automata> automatas = new ArrayList<>();

        char[] nodeNames = new char[transitionMatrix.length];
        for(int i = 0; i < transitionMatrix.length; i++){
            nodeNames[i] = (char) ('a' + i);
        }

        for(int i = 0; i < nodeNames.length; i++){
            for(int j = i+1; j < nodeNames.length; j++){
                char n1 = nodeNames[i];
                char n2 = nodeNames[j];

                Set<Character> alphabet = Set.of(n1, n2);

                int[] states = new int[]{0, 1, 2, 3};

                int startState = 0;

                List<Integer> acceptingStates;
                if(transitionMatrix[i][j] == 1){
                    acceptingStates = List.of(1, 2);
                }
                else{
                    acceptingStates = List.of(3);
                }

                Map<Integer, Map<Character, Integer>> transitions = Map.of(0, Map.of(n1, 1, n2, 2),
                                                                           1, Map.of(n1, 3, n2, 2),
                                                                           2, Map.of(n1, 1, n2, 3),
                                                                           3, Map.of(n1, 3, n2, 3));

                automatas.add(new Automata(alphabet, states, startState, acceptingStates, transitions));
            }
        }

        return automatas;
    }

    public static Automata combineAutomatas(Automata a1, Automata a2){
        /**
        * Computes the union of two DFAs and returns the combined result
        */
        
        int[] newStates = IntStream.range(0, a1.states.length * a2.states.length).toArray();

        int newStartState = (a1.startState * a2.states.length) + a2.startState;

        List<Integer> newAcceptingStates = new ArrayList<>();
        for(int i = 0; i < a1.acceptingStates.size(); i++){
            for(int j = 0; j < a2.acceptingStates.size(); j++){
                newAcceptingStates.add((a1.acceptingStates.get(i) * a2.states.length) + a2.acceptingStates.get(j));
            }
        }

        Set<Character> newAlphabet = new HashSet<>(a1.alphabet);
        newAlphabet.addAll(a2.alphabet);

        Map<Integer, Map<Character, Integer>> newTransitions = new HashMap<>();

        for(int state1 : a1.states){
            for(int state2 : a2.states){
                Map<Character, Integer> transition = new HashMap<>();
                for(Character a : newAlphabet){
                    int t1 = a1.transitions.get(state1).getOrDefault(a, state1);
                    int t2 = a2.transitions.get(state2).getOrDefault(a, state2);
                    int newT = t1 * a2.states.length + t2;
                    transition.put(a, newT);
                }
                newTransitions.put((state1 * a2.states.length)+state2, transition);
            }
        }

        return new Automata(newAlphabet, newStates, newStartState, newAcceptingStates, newTransitions);
    }

    public static Automata trimAutomata(Automata automata){
        /**
         * Removes unreachable states (states that cannot be reached from the starting state)
         * and dead states (states that cannot reach an accepting state)
         * from an automaton.
         */

        //Trim unreachable states
        //Performs a DFA starting from the start state

        Set<Integer> seen = new HashSet<Integer>();
        seen.add(automata.startState);
        Queue<Integer> next = new LinkedList<Integer>();
        next.add(automata.startState);

        while(!next.isEmpty()){
            Integer current = next.poll();
            for(int state : automata.transitions.get(current).values()){
                if(!seen.contains(state)) next.offer(state);
                seen.add(state);
            }
        }

        //All states that are not found during the DFA are unreachable
        automata.states = seen.stream().mapToInt(Integer::intValue).toArray();
        automata.acceptingStates.retainAll(seen);
        automata.transitions.keySet().retainAll(seen);

        //Find the sink state
        Integer sink = null;
        for (int state : automata.states) {
            Map<Character, Integer> edges = automata.transitions.get(state);
            if (edges.values().stream().allMatch(target -> target.equals(state))) {
                sink = state;
                break;
            }
        }
        if(sink == null) throw new RuntimeException("Sink not found");

        //Trim dead nodes
        //Performs a reverse DFA starting from the accepting states
        seen.clear();
        seen.addAll(automata.acceptingStates);
        next.clear();
        next.addAll(automata.acceptingStates);

        automata.calcReverse();

        while(!next.isEmpty()){
            int current = next.poll();
            for(List<Integer> states : automata.reverseTransitions.get(current).values()){
                for(int state : states){
                    if(!seen.contains(state)) next.offer(state);
                    seen.add(state);
                }
            }
        }

        //All states that are not found during the DFA (except the sink state) are dead
        seen.add(sink);
        automata.states = seen.stream().mapToInt(Integer::intValue).toArray();
        automata.transitions.keySet().retainAll(seen);
        final Integer fsink = sink;
        automata.transitions.forEach((k,v) -> v.replaceAll((k2,v2) -> seen.contains(v2) ? v2 : fsink));

        return automata;
    }

    public static BitSet listToBitSet(List<Integer> list){
        /**
         * Converts a list of integers to a bitset where the index of each integer
         * in the list is set to true
         */
        BitSet bs = new BitSet(list.size());
        for(int s : list){
            bs.set(s);
        }
        return bs;
    }
    public static List<Integer> bitSetToList(BitSet bs){
        /**
         * Returns a list of integers representing all of the true bits
         * in a bitset.
         */
        List<Integer> list = new ArrayList<>();
        for (int i = bs.nextSetBit(0); i != -1; i = bs.nextSetBit(i + 1)) {
            list.add(i);
        }
        return list;
    }

    public static Automata hopcroft(Automata automata){
        /**
         * An implementation of Hopcrofts DFA minimisation algorithm. It can
         * find and merge all equivalent states in an automaton.
         * Bitsets are used in place of lists of integers so that their
         * union, difference and intersection methods can be used.
         * Pseudocode found from the wikipedia:
         * https://en.wikipedia.org/wiki/DFA_minimization
         */

        automata.calcReverse();

        List<Integer> nonAccepting = Arrays.stream(automata.states).boxed().collect(Collectors.toList());
        nonAccepting.removeAll(automata.acceptingStates);

        List<BitSet> p = new ArrayList<>();
        p.add(listToBitSet(automata.acceptingStates));
        p.add(listToBitSet(nonAccepting));

        List<BitSet> w = new ArrayList<>();
        w.add(listToBitSet(automata.acceptingStates));
        w.add(listToBitSet(nonAccepting));

        while(!w.isEmpty()){
            List<Integer> a = bitSetToList(w.remove(0));

            for(Character c : automata.alphabet){

                BitSet x = new BitSet();
                
                for(int s : a){                    
                    for(int s2 : automata.reverseTransitions.get(s).get(c)){
                        x.set(s2);
                    }
                }

                List<BitSet> addToP = new ArrayList<>();
                List<BitSet> removeFromP = new ArrayList<>();

                for(BitSet y : p){
                    
                    BitSet inters = (BitSet) y.clone();
                    inters.and(x);
                    if(inters.isEmpty()) continue;

                    BitSet diff = (BitSet) y.clone();
                    diff.andNot(x);
                    if(diff.isEmpty()) continue;

                    removeFromP.add(y);
                    addToP.add(inters);
                    addToP.add(diff);

                    if(w.contains(y)){
                        w.remove(y);
                        w.add(inters);
                        w.add(diff);
                    }
                    else{
                        if(inters.size() < diff.size()) w.add(inters);
                        else w.add(diff);
                    }
                }

                p.removeAll(removeFromP);
                p.addAll(addToP);
            }
        }

        Map<Integer, Integer> stateMap = new HashMap<>();
        for(int i = 0; i < p.size(); i++){
            for(int s : bitSetToList(p.get(i))) stateMap.put(s, i);
        }

        automata.states = IntStream.range(0, p.size()).toArray();
        automata.startState = stateMap.get(automata.startState);
        automata.acceptingStates = automata.acceptingStates.stream().map(stateMap::get).toList();

        Map<Integer, Map<Character, Integer>> newTransitions = new HashMap<>();
        for(int i = 0; i < p.size(); i++){
            Map<Character, Integer> newT =  new HashMap<>();
            for(Character c : automata.alphabet){
                newT.put(c, stateMap.get(automata.transitions.get(bitSetToList(p.get(i)).get(0)).get(c)));
            }
            newTransitions.put(i, newT);
        }
        automata.transitions = newTransitions;

        return automata;
    }

    

    public static Automata renameAutomata(Automata automata){
        /*
        * Renames the states of a DFA to be simply numbered from 0 to n (n being the number of states)
        * Also renames the start state, accepting states, and transitions appropriately
        */
        Map<Integer, Integer> stateMap = new HashMap<>();
        for(int i = 0; i < automata.states.length; i++){
            stateMap.put(automata.states[i], i);
        }

        automata.states = IntStream.range(0, automata.states.length).toArray();
        automata.startState = stateMap.get(automata.startState);
        automata.acceptingStates = automata.acceptingStates.stream().map(stateMap::get).toList();

        Map<Integer, Map<Character, Integer>> newTransitions = new HashMap<>();
        automata.transitions.forEach((state,transition) -> {
            Map<Character, Integer> newT2 = new HashMap<>();
            transition.forEach((key, state2) -> newT2.put(key, stateMap.get(state2)));
            newTransitions.put(stateMap.get(state), newT2);
        });
        automata.transitions = newTransitions;

        return automata;
    }


    public static class Automata {

        Set<Character> alphabet;
        int[] states;
        int startState;
        List<Integer> acceptingStates;
        Map<Integer, Map<Character, Integer>> transitions;
        Map<Integer, Map<Character, List<Integer>>> reverseTransitions;

        public Automata(Set<Character> alphabet, int[] states, int startState, List<Integer> acceptingStates, Map<Integer, Map<Character, Integer>> transitions, Map<Integer, Map<Character, List<Integer>>> reverseTransitions){
            this.alphabet = alphabet;
            this.states = states;
            this.startState = startState;
            this.acceptingStates = acceptingStates;
            this.transitions = transitions;
            this.reverseTransitions = reverseTransitions;
        }

        public Automata(Set<Character> alphabet, int[] states, int startState, List<Integer> acceptingStates, Map<Integer, Map<Character, Integer>> transitions){
            this(alphabet, states, startState, acceptingStates, transitions, null);
        }

        public void calcReverse(){
            /**
             * Calculates the reverse of the transition matrix and saves it in the 
             * reverseTransitions variable.
             * This function must be called before data from reverseTransitions is accessed.
             */

            reverseTransitions = new HashMap<>();

            for(int s : states){
                Map<Character, List<Integer>> temp = new HashMap<>();
                for(Character c : alphabet){
                    temp.put(c, new ArrayList<>());
                }
                reverseTransitions.put(s, temp);
            }
            for(Map.Entry<Integer, Map<Character, Integer>> entry : transitions.entrySet()){
                for(Map.Entry<Character, Integer> entry2 : entry.getValue().entrySet()){
                    reverseTransitions.get(entry2.getValue()).get(entry2.getKey()).add(entry.getKey());
                }
            }
        }

        public void print(){
            /**
             * Prints all of the states, inital state, accepting states, alphabet and transitions
             * of the automata into a format which can be entered and visualised with the following website:
             * https://ivanzuzak.info/noam/webapps/fsm_simulator/
             */

            System.out.println("#states");
            for(int state : states){
                System.out.println(state);
            }

            System.out.println("#initial");
            System.out.println(startState);

            System.out.println("#accepting");
            for(int state : acceptingStates){
                System.out.println(state);
            }

            System.out.println("#alphabet");
            for(Character a : alphabet){
                System.out.println(a);
            }

            System.out.println("#transitions");
            for(Map.Entry<Integer, Map<Character, Integer>> e1 : transitions.entrySet()){
                for(Map.Entry<Character, Integer> e2 : e1.getValue().entrySet()){
                    System.out.println(e1.getKey() + ":" + e2.getKey() + ">" + e2.getValue());
                }
            }
        }

        public void writeToFile(){
            /**
             * Performs the same as the print() function except it saves the output to a file
             * titled "automata.txt" rather than printing to the console.
             * https://ivanzuzak.info/noam/webapps/fsm_simulator/
             */

            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter("automata.txt"));
            
                writer.write("#states");
                writer.newLine();
                for(int state : states){
                    writer.write(Integer.toString(state));
                    writer.newLine();
                }

                writer.write("#initial");
                writer.newLine();
                writer.write(Integer.toString(startState));
                writer.newLine();

                writer.write("#accepting");
                writer.newLine();
                for(int state : acceptingStates){
                    writer.write(Integer.toString(state));
                    writer.newLine();
                }

                writer.write("#alphabet");
                writer.newLine();
                for(Character a : alphabet){
                    writer.write(a);
                    writer.newLine();
                }

                writer.write("#transitions");
                for(Map.Entry<Integer, Map<Character, Integer>> e1 : transitions.entrySet()){
                    for(Map.Entry<Character, Integer> e2 : e1.getValue().entrySet()){
                        writer.newLine();
                        writer.write(e1.getKey() + ":" + e2.getKey() + ">" + e2.getValue());
                    }
                }

                writer.close();
                System.out.println("File write successfull");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public Automata clone(){
            /**
             * Returns a copy of the current automata.
             */

            Set<Character> calphabet = new HashSet<Character>(alphabet);
            int[] cstates = Arrays.copyOf(states, states.length);
            int cstartState = startState;
            List<Integer> cacceptingStates = new ArrayList<>(acceptingStates);

            Map<Integer, Map<Character, Integer>> ctransitions = new HashMap<>();
            for(Map.Entry<Integer, Map<Character, Integer>> m1 : transitions.entrySet()){
                Map<Character, Integer> temp = new HashMap<>();
                for(Map.Entry<Character, Integer> m2 : m1.getValue().entrySet()){
                    temp.put(m2.getKey(), m2.getValue());
                }
                ctransitions.put(m1.getKey(), temp);
            }

            return new Automata(calphabet, cstates, cstartState, cacceptingStates, ctransitions);
        }
    }
}
