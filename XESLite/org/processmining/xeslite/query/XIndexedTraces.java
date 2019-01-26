package org.processmining.xeslite.query;

import org.deckfour.xes.model.XTrace;

import com.googlecode.cqengine.attribute.Attribute;

/**
 * Collection of {@link XTrace} that can be searched and indexed for efficient
 * search.
 * 
 * @author F. Mannhardt
 *
 */
public interface XIndexedTraces extends XIndexedAttributable<XTrace> {

	Attribute<XTrace, String> eventNames();

	Attribute<XTrace, String> traceName();

}
