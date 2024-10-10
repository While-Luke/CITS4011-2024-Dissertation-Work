import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.Collections;


public class QuickAutomataCombiner {
    public static void main(String[] args){

        //Non-word-representable 6-node graph (Wheel graph)
        /*boolean[][] transitionMatrix = new boolean[][]{{false,true,false,false,true,true},
                                                {true,false,true,false,false,true},
                                                {false,true,false,true,false,true},
                                                {false,false,true,false,true,true},
                                                {true,false,false,true,false,true},
                                                {true,true,true,true,true,false}};*/

        //A random 11-node graph
        boolean[][] transitionMatrix = new boolean[][]{{false, true, false, true, false, true, false, true, false, true, false},
                                                {true, false, false, true, false, true, true, false, false, false, true},
                                                {false, false, false, false, false, false, true, false, true, false, false},
                                                {true, true, false, false, true, false, false, true, false, true, true},
                                                {false, false, false, true, false, false, true, false, false, true, true},
                                                {false, true, false, false, false, false, false, false, true, false, false},
                                                {false, true, true, false, true, false, true, false, true, false, false},
                                                {false, false, false, true, false, false, false, true, false, false, true},
                                                {false, true, false, true, false, false, true, false, true, true, false},
                                                {true, false, false, true, false, true, true, false, false, false, true},
                                                {false, false, false, false, false, false, true, false, true, false, false}};

        //A random 12-node graph
        /*boolean[][] transitionMatrix = new boolean[][]{{false, true, false, true, false, true, false, true, false, true, false, true},
                                                {true, false, false, true, false, true, true, false, false, false, true, false},
                                                {false, false, false, false, false, false, true, false, true, false, false, true},
                                                {true, true, false, false, true, false, false, true, false, true, true, false},
                                                {false, false, false, true, false, false, true, false, false, true, true, false},
                                                {false, true, false, false, false, false, false, false, true, false, false, true},
                                                {false, true, true, false, true, false, true, false, true, false, false, false},
                                                {false, false, false, true, false, false, false, true, false, false, true, false},
                                                {false, true, false, true, false, false, true, false, true, true, false, true},
                                                {true, false, false, true, false, true, true, false, false, false, true, false},
                                                {false, false, false, false, false, false, true, false, true, false, false, true},
                                                {false, false, true, false, false, true, false, false, true, false, false, false}};*/

        
        Long start = System.currentTimeMillis();

        if(checkWordRepresentable(transitionMatrix, true)){
            System.out.println("This graph IS word representable");
        }
        else{
            System.out.println("This graph is NOT word representable");
        }
        
        System.out.println("Program Duration: " + (System.currentTimeMillis() - start));
    }

    public static boolean checkWordRepresentable(boolean[][] transitionMatrix){
        return checkWordRepresentable(transitionMatrix, false);
    }

    public static boolean checkWordRepresentable(boolean[][] transitionMatrix, boolean verbose){
        /*
         * Returns true if the given graph is word-representable, else false.
         */

        char[] nodeNames = new char[transitionMatrix.length];

        for(int i = 0; i < transitionMatrix.length; i++){
            nodeNames[i] = (char) ('a' + i);
        }

        List<Automata> automatas = graphToAutomata(nodeNames, transitionMatrix);

        Collections.shuffle(automatas);

        Automata combined = combineAutomatas(automatas.get(0), automatas.get(1));
        combined = trimAutomata(combined);

        if(verbose) System.out.println("Number of combined automatas: 2 - Number of States: " + combined.numStates);

        for(int i = 2; i < automatas.size(); i++){
            combined = combineAutomatas(combined, automatas.get(i));
            combined = trimAutomata(combined);

            if(verbose) System.out.println("Number of combined automatas: " + (i+1) + " - Number of States: " + combined.numStates);
        }

        List<Automata> groups = findGroups(combined);

        for(Automata a : groups){
            if(checkConstraints(a, nodeNames, transitionMatrix)){
                return true;
            }
        }

        return false;
    }

    public static List<Automata> graphToAutomata(char[] node_names, boolean[][] transitionMatrix){
        /*
         * Converts a given graph represented by the transition matrix and a list of names of the nodes into
         * a list of DFAs to recognise the edges in the graph.
         */

        List<Automata> automatas = new ArrayList<>();

        for(int i = 0; i < node_names.length; i++){
            for(int j = i+1; j < node_names.length; j++){
                if(!transitionMatrix[i][j]) continue;

                char n1 = node_names[i];
                char n2 = node_names[j];

                Set<Character> alphabet = Set.of(n1, n2);

                int numStates = 2;

                Map<Integer, Map<Character, Integer>> transitions = Map.of(0, Map.of(n1, 1),
                                                                           1, Map.of(n2, 0));
                
                automatas.add(new Automata(alphabet, numStates, transitions));
            }
        }

        for(int i = 0; i < node_names.length; i++){
            if(!allFalse(transitionMatrix[i])) continue;

            char n1 = node_names[i];

            Set<Character> alphabet = Set.of(n1);

            int numStates = 1;

            Map<Integer, Map<Character, Integer>> transitions = Map.of(0, Map.of(n1, 0));

            automatas.add(new Automata(alphabet, numStates, transitions));
        }

        return automatas;
    }

    public static boolean allFalse(boolean[] a){
        for(boolean b : a) {
            if(b) return false;
        }
        return true;
    }

    public static Automata combineAutomatas(Automata a1, Automata a2){
        /**
        * Computes the union of two DFAs and returnes the combined result
        */
        
        int newNumStates = a1.numStates * a2.numStates;

        Set<Character> newAlphabet = new HashSet<>(a1.alphabet);
        newAlphabet.addAll(a2.alphabet);

        Map<Integer, Map<Character, Integer>> newTransitions = new HashMap<>(newNumStates);

        for(int state1 = 0; state1 < a1.numStates; state1++){
            for(int state2 = 0; state2 < a2.numStates; state2++){
                Map<Character, Integer> transition = new HashMap<>();
                for(Character a : newAlphabet){
                    if((a1.alphabet.contains(a) && !a1.transitions.get(state1).containsKey(a)) 
                    || (a2.alphabet.contains(a) && !a2.transitions.get(state2).containsKey(a))) continue;
                    int t1 = a1.transitions.get(state1).getOrDefault(a, state1);
                    int t2 = a2.transitions.get(state2).getOrDefault(a, state2);
                    int newT = t1 * a2.numStates + t2;
                    transition.put(a, newT);
                }
                newTransitions.put((state1 * a2.numStates)+state2, transition);
            }
        }

        return new Automata(newAlphabet, newNumStates, newTransitions);
    }

    public static Automata trimAutomata(Automata automata){
        /**
         * Removes any states with no incoming transitions and all subsequent states
         */

        Set<Integer> seen = new HashSet<>(automata.sourceNodes());
        Queue<Integer> next = new LinkedList<>();
        next.addAll(seen);

        while(!next.isEmpty()){
            int current = next.poll();
            for(int s : automata.transitions.get(current).values()){
                if(seen.add(s)){
                    next.add(s);
                }
            }
        }

        List<Integer> states = new ArrayList<>(IntStream.range(0, automata.numStates).boxed().toList());

        states.removeAll(seen);

        return extractAutomata(automata, states);
    }

    public static List<Automata> findGroups(Automata automata) {
        /**
         * Decomposes a given automata into its strongly connected components.
         * Each component is turned into its own automata and they are all returned as a list.
         */

        List<Automata> groups = new ArrayList<>();

        Set<Integer> seen = new HashSet<>();

        for(int s = 0; s < automata.numStates; s++){
            if(seen.contains(s)) continue;

            seen.add(s);
            List<Integer> states = new ArrayList<>();
            states.add(s);

            Queue<Integer> queue = new LinkedList<>();
            queue.add(s);

            while(!queue.isEmpty()){
                int current = queue.poll();

                for(Integer next : automata.transitions.get(current).values()){
                    if(!seen.contains(next)){
                        seen.add(next);
                        queue.add(next);
                        states.add(next);
                    }
                }
            }

            groups.add(extractAutomata(automata, states));
        }

        return groups;
    }

    public static boolean checkConstraints(Automata automata, char[] node_names, boolean[][] transitionMatrix){
        /**
         * Determines whether the given automata follows all of the constraints outlined by the original
         * transition matrix.
         */

        Set<Set<Character>> tMatrixConstraints = new HashSet<>();
        for(int i = 0; i < transitionMatrix.length; i++){
            for(int j = i+1; j < transitionMatrix.length; j++){
                if(!transitionMatrix[i][j]) tMatrixConstraints.add(Set.of(node_names[i], node_names[j]));
            }
        }

        Set<Set<Character>> automataConstraints = new HashSet<>();
        for(Map.Entry<Integer, Map<Character, Integer>> entry : automata.transitions.entrySet()){
            List<Character> chars = new ArrayList<>();
            chars.addAll(entry.getValue().keySet());
            if(chars.size() < 2) continue;

            for(int i = 0; i < chars.size(); i++){
                for(int j = i+1; j < chars.size(); j++){
                    automataConstraints.add(Set.of(chars.get(i), chars.get(j)));
                }
            }
        }

        return tMatrixConstraints.equals(automataConstraints);
    }

    public static Automata extractAutomata(Automata automata, List<Integer> states){
        /*
        * Extracts the given list of states and their transitions from the given automata
        * and returns them as a new automata.
        */
        int newNumStates = states.size();

        Map<Integer, Integer> stateMap = new HashMap<>(newNumStates);
        for(int i = 0; i < newNumStates; i++){
            stateMap.put(states.get(i), i);
        }

        Map<Integer, Map<Character, Integer>> newTransitions = new HashMap<>(newNumStates);
        for(int s : states){
            Map<Character, Integer> newT = new HashMap<>();
            for(Map.Entry<Character, Integer> e : automata.transitions.get(s).entrySet()){
                newT.put(e.getKey(), stateMap.get(e.getValue()));
            }
            newTransitions.put(stateMap.get(s),newT);
        }

        return new Automata(automata.alphabet, newNumStates, newTransitions);
    }


    public static class Automata {

        Set<Character> alphabet;
        int numStates;
        Map<Integer, Map<Character, Integer>> transitions;
        Map<Integer, List<Integer>> reverseTransitions;

        public Automata(Set<Character> alphabet, int numStates, Map<Integer, Map<Character, Integer>> transitions, Map<Integer, List<Integer>> reverseTransitions){
            this.alphabet = alphabet;
            this.numStates = numStates;
            this.transitions = transitions;
            this.reverseTransitions = reverseTransitions;
        }

        public Automata(Set<Character> alphabet, int numStates, Map<Integer, Map<Character, Integer>> transitions){
            this(alphabet, numStates, transitions, null);
        }

        public void calcReverse(){
            /**
             * Calculates the reverse of the transition matrix and saves it in the 
             * reverseTransitions variable.
             * This function must be called before data from reverseTransitions is accessed.
             */

            reverseTransitions = new HashMap<>();

            for(Map.Entry<Integer, Map<Character, Integer>> entry : transitions.entrySet()){
                for(Map.Entry<Character, Integer> entry2 : entry.getValue().entrySet()){
                    reverseTransitions.putIfAbsent(entry2.getValue(), new ArrayList<>());
                    reverseTransitions.get(entry2.getValue()).add(entry.getKey());
                }
            }
        }

        public List<Integer> reverseTransitionList(){
            /**
             * Returns a list containing all the states that have in-transitions.
             */

            Set<Integer> l = new HashSet<>();

            transitions.values().stream().forEach(m -> l.addAll(m.values()));

            return new ArrayList<>(l);
        }

        public List<Integer> sourceNodes(){
            /**
             * Returns a list containing all the states that have no in-transitions.
             */

            BitSet bs = new BitSet(numStates);

            for(Map<Character, Integer> me1 : transitions.values()){
                for(int s : me1.values()) bs.set(s);
            }

            List<Integer> states = new ArrayList<>();
            for (int i = bs.previousClearBit(numStates-1); i != -1; i = bs.previousClearBit(i - 1)) {
                states.add(i);
            }

            return states;
        }

        public void print(){
            /**
             * Prints all of the states, inital state, accepting states, alphabet and transitions
             * of the automata into a format which can be entered and visualised with the following website:
             * https://ivanzuzak.info/noam/webapps/fsm_simulator/
             */

            System.out.println("#states");
            for(int s = 0; s < numStates; s++){
                System.out.println(s);
            }

            System.out.println("#initial");
            System.out.println(0);

            System.out.println("#accepting");
            for(int s = 0; s < numStates; s++){
                System.out.println(s);
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
                for(int s = 0; s < numStates; s++){
                    writer.write(Integer.toString(s));
                    writer.newLine();
                }

                writer.write("#initial");
                writer.newLine();
                writer.write(Integer.toString(0));
                writer.newLine();

                writer.write("#accepting");
                writer.newLine();
                for(int s = 0; s < numStates; s++){
                    writer.write(Integer.toString(s));
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
            
            Map<Integer, Map<Character, Integer>> ctransitions = new HashMap<>();
            for(Map.Entry<Integer, Map<Character, Integer>> m1 : transitions.entrySet()){
                Map<Character, Integer> temp = new HashMap<>();
                for(Map.Entry<Character, Integer> m2 : m1.getValue().entrySet()){
                    temp.put(m2.getKey(), m2.getValue());
                }
                ctransitions.put(m1.getKey(), temp);
            }
            return new Automata(calphabet, numStates, ctransitions);
        }
    }
}
