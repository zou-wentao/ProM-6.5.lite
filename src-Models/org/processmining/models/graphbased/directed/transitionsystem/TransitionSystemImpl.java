package org.processmining.models.graphbased.directed.transitionsystem;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingConstants;

import org.apache.commons.collections15.MapIterator;
import org.apache.commons.collections15.keyvalue.MultiKey;
import org.apache.commons.collections15.map.MultiKeyMap;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.AbstractDirectedGraph;
import org.processmining.models.graphbased.directed.DirectedGraph;
import org.processmining.models.graphbased.directed.DirectedGraphEdge;
import org.processmining.models.graphbased.directed.DirectedGraphElement;
import org.processmining.models.graphbased.directed.DirectedGraphNode;


public class TransitionSystemImpl extends AbstractDirectedGraph<State, Transition> implements TransitionSystem {

	private final Map<Object, State> states = new LinkedHashMap<Object, State>();

	@SuppressWarnings("rawtypes")
	private final MultiKeyMap t = new MultiKeyMap(); //3 keys: <State, State, Object> and a value <Transition>, but is set to generics
	
	private Map<Object, Object> proxyMap;
	
	public TransitionSystemImpl(String label) {
		super();
		getAttributeMap().put(AttributeMap.LABEL, label);
		getAttributeMap().put(AttributeMap.PREF_ORIENTATION, SwingConstants.NORTH);
		
		proxyMap = new HashMap<Object, Object>();
	}

	@Override
	protected synchronized Map<DirectedGraphElement, DirectedGraphElement> cloneFrom(
			DirectedGraph<State, Transition> graph) {
		assert (graph instanceof TransitionSystemImpl);
		Map<DirectedGraphElement, DirectedGraphElement> mapping = new HashMap<DirectedGraphElement, DirectedGraphElement>();

		TransitionSystemImpl ts = (TransitionSystemImpl) graph;
		for (Object identifier : ts.states.keySet()) {
			addState(identifier);
			mapping.put(ts.states.get(identifier), getNode(identifier));
		}
//		for (Transition trans : getEdges()) {
		for (Transition trans : ts.getEdges()) {
			addTransition(trans.getSource().getIdentifier(), trans.getTarget().getIdentifier(), trans.getIdentifier());
			mapping.put(trans, findTransition(trans.getSource().getIdentifier(), trans.getTarget().getIdentifier(),
					trans.getIdentifier()));
		}
		return mapping;
	}

	@Override
	protected synchronized AbstractDirectedGraph<State, Transition> getEmptyClone() {
		return new TransitionSystemImpl(getLabel());
	}

	/**
	 * Node/State Handling
	 *
	 */
	
	public synchronized boolean addState(Object identifier) {
		if (!states.containsKey(identifier)) {
			State state = new State(identifier, this);
			states.put(identifier, state);
			graphElementAdded(state);
			return true;
		} else {
			return false;
		}
	}
	
	public synchronized void removeNode(DirectedGraphNode node) {
		if (node instanceof State) {
			removeState(node);
		} else {
			assert (false);
		}
	}
	
	public synchronized Set<State> getNodes() {
		Set<State> nodes = new HashSet<State>();
		nodes.addAll(states.values());
		return nodes;
	}
	
	public synchronized Collection<?> getStates() {
		return states.keySet();
	}

	public synchronized Object removeState(Object state) {
		if (state instanceof State) {
			Object removed = states.get(((State) state).getIdentifier());
			states.remove(((State) state).getIdentifier());
			graphElementRemoved(removed);
			return removed;
		}
		else return null;
	}
	
	public synchronized State getNode(Object identifier) {
		return states.get(getProxy(identifier));
	}
	
	/**
	 * Edge/Transition Handling
	 *
	 */
	
	@SuppressWarnings("unchecked")
	public synchronized boolean addTransition(Object fromState, Object toState, Object identifier) {
		State source = getNode(fromState);
		State target = getNode(toState);
		checkAddEdge(source, target);
		
		if (!t.containsKey(source, target, identifier)) {
			Transition transition = new Transition(source, target, identifier);
			t.put(source, target, identifier, transition); //Stores the Source State, Target State and Object identifier as keys of a Transition.
			graphElementAdded(transition);
			return true;
		} else {
			return false;
		}
	}
	
	@SuppressWarnings("unchecked")
	public synchronized Object removeTransition(Object fromState, Object toState, Object identifier) {
		Object removed = t.get(getNode(fromState), getNode(toState), identifier);
		t.remove(getNode(fromState), getNode(toState), identifier);
		graphElementRemoved(removed);
		return removed;
	}
		
	@SuppressWarnings("unchecked")
	@Override
	public synchronized void removeEdge(@SuppressWarnings("rawtypes") DirectedGraphEdge edge) {
		assert (edge instanceof Transition);
		Transition transition = (Transition) edge;
		t.remove(transition.getSource(),transition.getTarget(),transition.getIdentifier());
	}

	public synchronized Collection<Object> getTransitions() {
		Set<Object> keys = new LinkedHashSet<Object>();
		for(Object trans : t.values())
			keys.add(((Transition) trans).getIdentifier());
		return keys;
	}
	
	@SuppressWarnings("unchecked")
	public synchronized Collection<Object> getTransitionObjects() {
		return t.values();
	}

	@SuppressWarnings("unchecked")
	public synchronized Set<Transition> getEdges() {
		Set<Transition> result = new HashSet<Transition>();
		result.addAll(t.values());
		return Collections.unmodifiableSet(result);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Collection<Transition> getEdges(Object identifier) { 
		Set<Transition> collection = new LinkedHashSet<Transition>();
		MapIterator iterator = t.mapIterator();
		while(iterator.hasNext())
		{
			iterator.next();
			MultiKey key = (MultiKey) iterator.getKey();
			if(identifier.equals(key.getKey(2))){
				collection.add((Transition) t.get(key));
			}			
		}
		return collection;
	}

	@SuppressWarnings("unchecked") //now we do not have to go through the transition lists :)
	public synchronized Transition findTransition(Object fromState, Object toState, Object identifier) {
		return (Transition) t.get(getNode(fromState),getNode(toState),identifier);
	}
	
	/**
	 * Proxy?
	 *
	 */
		
	public void putProxy(Object obj, Object proxy) {
		proxyMap.put(obj, proxy);
	}
	
	private Object getProxy(Object obj) {
		while (proxyMap.containsKey(obj)) {
			obj = proxyMap.get(obj);
		}
		return obj;
	}
	
	public void addProxyMap(TransitionSystemImpl ts) {
		for (Object key: ts.proxyMap.keySet()) {
			proxyMap.put(key, ts.proxyMap.get(key));
		}
	}

}
