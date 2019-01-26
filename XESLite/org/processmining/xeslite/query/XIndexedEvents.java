package org.processmining.xeslite.query;

import org.deckfour.xes.model.XEvent;

import com.googlecode.cqengine.attribute.Attribute;

public interface XIndexedEvents extends XIndexedAttributable<XEvent> {

	Attribute<XEvent, String> eventName();
	
}
