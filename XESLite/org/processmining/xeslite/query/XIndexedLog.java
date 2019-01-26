package org.processmining.xeslite.query;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.xeslite.query.syntax.ParseException;

import com.googlecode.cqengine.query.Query;

public interface XIndexedLog extends XLog {
	
	XIndexedTraces getIndexedCollection();

	Iterable<XTrace> retrieve(Query<XTrace> query);

	Iterable<XTrace> retrieve(String searchQuery) throws ParseException;

}
