// Copyright 2013 Daniel de Kok
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package eu.danieldk.dictomaton;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

/**
 * A finite state dictionary with perfect hashing, that puts right language cardinalities in states.
 * Dictionaries of this type can are constructed using
 * {@link eu.danieldk.dictomaton.DictionaryBuilder#buildPerfectHash()}.
 *
 * @author Daniel de Kok
 */
class PerfectHashDictionaryStateCardIterative extends DictionaryImpl implements PerfectHashDictionary {
    private static final long serialVersionUID = 2L;

    private final CompactIntArray d_stateNSuffixes;

    /**
     * Compute the perfect hash code of the given character sequence.
     *
     * @param seq
     * @return
     */
    public int number(CharSequence seq) {
        StateInfo info = getStateInfo(seq);
        return info.isInFinalState() ? info.getHash() : -1;
    }
    
    public StateInfo getStateInfo(CharSequence seq) {
        return getStateInfo(seq, null);
    }
    
    public StateInfo getStateInfo(CharSequence seq, StateInfo startInfo) {
        
        StateInfo info;
        
        if (startInfo != null) {
            if (!startInfo.isInKnownState()) {
                throw new IllegalStateException("Cannot resume transitions from unknown state. Sequence: " + seq);
            }
            info = new StateInfo(startInfo.num, startInfo.state, startInfo.trans, startInfo.inFinalState);
        } else {
            info = new StateInfo(0, 0, -1, false);
        }
        
        for (int i = 0; i < seq.length(); i++) {
            char ch = seq.charAt(i);
            info.trans = findTransition(info.state, ch);

            if (!info.isInKnownState()) {
                return info;
            }

            // Count the number of preceding suffixes in the preceding transitions.
            for (int j = d_stateOffsets.get(info.state); j < info.trans; j++)
                info.num += d_stateNSuffixes.get(d_transitionTo.get(j));

            // A final state is another suffix.
            if (d_finalStates.get(info.state))
                ++info.num;

            info.state = d_transitionTo.get(info.trans);
        }

        info.inFinalState = d_finalStates.get(info.state);
        
        return info;
    }

    /**
     * Compute the sequence corresponding to the given hash code.
     *
     * @param hashCode
     * @return
     */
    public String sequence(int hashCode) {
        if (hashCode <= 0)
            return null;

        int state = 0;

        // If the hash code is larger than the number of suffixes in the start state,
        // the hash code does not correspond to a sequence.
        if (hashCode > d_stateNSuffixes.get(state))
            return null;

        StringBuilder wordBuilder = new StringBuilder();

        // Stop if we are in a state where we cannot add more characters.
        while (d_stateOffsets.get(state) != transitionsUpperBound(state)) {

            // Obtain the next transition, decreasing the hash code by the number of
            // preceding suffixes.
            int trans;
            for (trans = d_stateOffsets.get(state); trans < transitionsUpperBound(state); ++trans) {
                int stateNSuffixes = d_stateNSuffixes.get(d_transitionTo.get(trans));

                if (hashCode - stateNSuffixes <= 0)
                    break;

                hashCode -= stateNSuffixes;
            }

            // Add the character on the given transition and move.
            wordBuilder.append(d_transitionChars[trans]);
            state = d_transitionTo.get(trans);

            // If we encounter a final state, decrease the hash code, since it represents a
            // suffix. If our hash code is reduced to zero, we have found the sequence.
            if (d_finalStates.get(state)) {
                --hashCode;

                if (hashCode == 0)
                    return wordBuilder.toString();
            }
        }

        // Bad luck, we cannot really get here!
        return null;
    }

    @Override
    public int size() {
        return d_nSeqs;
    }

    /**
     * Give the Graphviz dot representation of this automaton. States will also list the
     * number of suffixes 'under' that state.
     *
     * @return
     */
    @Override
    public String toDot() {
        StringBuilder dotBuilder = new StringBuilder();

        dotBuilder.append("digraph G {\n");

        for (int state = 0; state < d_stateOffsets.size(); ++state) {
            for (int trans = d_stateOffsets.get(state); trans < transitionsUpperBound(state); ++trans)
                dotBuilder.append(String.format("%d -> %d [label=\"%c\"]\n",
                        state, d_transitionTo.get(trans), d_transitionChars[trans]));

            if (d_finalStates.get(state))
                dotBuilder.append(String.format("%d [peripheries=2,label=\"%d (%d)\"];\n", state, state, d_stateNSuffixes.get(state)));
            else
                dotBuilder.append(String.format("%d [label=\"%d (%d)\"];\n", state, state, d_stateNSuffixes.get(state)));
        }

        dotBuilder.append("}");

        return dotBuilder.toString();
    }

    /**
     * @see DictionaryImpl#DictionaryImpl(CompactIntArray, char[], CompactIntArray, java.util.BitSet, int)
     */
    protected PerfectHashDictionaryStateCardIterative(CompactIntArray stateOffsets, char[] transitionChars,
                                             CompactIntArray transitionTo, BitSet finalStates,
                                             int nSeqs) {
        super(stateOffsets, transitionChars, transitionTo, finalStates, nSeqs);

        // Marker that indicates that the number of suffixes of a state is not yet computed. We cannot
        // use -1, since CompactIntArray would then require 32-bit per value.
        final int magicMarker = nSeqs + 1;

        d_stateNSuffixes = new CompactIntArray(d_stateOffsets.size(), CompactIntArray.width(magicMarker));
        for (int i = 0; i < d_stateNSuffixes.size(); ++i)
            d_stateNSuffixes.set(i, magicMarker);

        computeStateSuffixesTopological(0, magicMarker);
    }

    /**
     * Iteratively computes the number of suffixes by topological order 
     * 
     * @param initialState the root of the graph
     * @param magicMarker the value in d_stateNSuffixes indicating that the value has not yet been computed  
     */
    private void computeStateSuffixesTopological(final int initialState, final int magicMarker) {
    	for (Iterator<Integer> iterator = sortStatesTopological(initialState).iterator(); iterator.hasNext();) {
			Integer currentState = iterator.next();

			int currentSuffixes = d_stateNSuffixes.get(currentState);
			if (currentSuffixes == magicMarker) { // is not yet computed
    			int trans = d_stateOffsets.get(currentState);
        		int transUpperBound = transitionsUpperBound(currentState);
    			if (trans < transUpperBound) { // has children
    				int suffixes = d_finalStates.get(currentState) ? 1 : 0; // add one if current state is final
    				for (; trans < transUpperBound; ++trans) { // add known number of suffixes of children
    					int childState = d_transitionTo.get(trans);
    					assert d_stateNSuffixes.get(childState) != magicMarker : "suffxies should have been calculated for state "+childState;
    					suffixes += d_stateNSuffixes.get(childState);
    				}
    				d_stateNSuffixes.set(currentState, suffixes);
    			} else {
    				d_stateNSuffixes.set(currentState, d_finalStates.get(currentState) ? 1 : 0);
    			}
    		} // else already computed from a different path in the DAG
		}
    }

	private Collection<Integer> sortStatesTopological(final int initialState) {

		List<Integer> reverseTopologicalOrder = new ArrayList<>(d_stateNSuffixes.size());
    	boolean[] marked = new boolean[d_stateNSuffixes.size()];
    	Deque<Integer> stack = new ArrayDeque<>(d_stateNSuffixes.size());
    	Deque<Integer> head = new ArrayDeque<>(d_stateNSuffixes.size());

    	stack.push(initialState);
    	while (!stack.isEmpty()) {
    		Integer currentState = stack.peek();
    		if (currentState == head.peek()) {
    			stack.pop();
    			head.pop();
    			marked[currentState] = true;
    			reverseTopologicalOrder.add(currentState);
    		} else {
    			head.push(currentState);
        		int trans = d_stateOffsets.get(currentState);
        		int transUpperBound = transitionsUpperBound(currentState);
    			if (trans < transUpperBound) // has children
    				for (; trans < transUpperBound; ++trans) {
    					int nextState = d_transitionTo.get(trans);
    					if (!marked[nextState]) {
    						stack.push(nextState);
    					}
    				}
    		}
    	}

		return reverseTopologicalOrder;
	}
    
}