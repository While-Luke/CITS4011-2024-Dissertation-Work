This repository contains the practical work for my thesis. This work is split up into 3 separate programs:

# FullAutomataCombiner.java

This program makes use of finite state automata to check whether a graph is word-representable or not. The user can input a graph as a transition matrix at the top of the program, then run the program to check if the graph is word-representable. The automaton that gets constructed from the graph is saved to a file named "automata.txt".

# QuickAutomataCombiner.java

This works the same as the previous program except it is much faster and does not construct the full automaton from a graph.

# AutomataVisualiser

This program allows the user to visualise the automata that is created from the previous two programs. It enables the user to move around and hide each state in order to best visualise the automaton.
