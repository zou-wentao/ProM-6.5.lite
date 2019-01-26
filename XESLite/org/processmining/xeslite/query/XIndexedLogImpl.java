package org.processmining.xeslite.query;

import java.util.AbstractList;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.XVisitor;
import org.deckfour.xes.model.impl.XLogImpl;
import org.processmining.xeslite.query.syntax.ParseException;

import com.googlecode.cqengine.query.Query;

/**
 * Implementation of an {@link XIndexedLog} supporting a simple query language.
 * 
 * @author F. Mannhardt
 * 
 */
class XIndexedLogImpl extends AbstractList<XTrace> implements XIndexedLog {

	private final XLog sourceLog;
	private XIndexedTraces indexedTraces;

	XIndexedLogImpl(XLog log) {
		indexedTraces = new XIndexedTracesImpl(log);
		sourceLog = log;
	}

	public XIndexedTraces getIndexedCollection() {
		return indexedTraces;
	}

	public Iterable<XTrace> retrieve(Query<XTrace> query) {
		return indexedTraces.retrieve(query);
	}

	public Iterable<XTrace> retrieve(String query) throws ParseException {
		return indexedTraces.retrieve(query);
	}
	
	public XTrace get(int index) {
		return sourceLog.get(index);
	}

	public XTrace set(int index, XTrace element) {
		throw new UnsupportedOperationException("XIndexedLog is append only!");
	}

	public void add(int index, XTrace element) {
		sourceLog.add(index, element);
		indexedTraces.add(element);
	}

	public XTrace remove(int index) {
		throw new UnsupportedOperationException("XIndexedLog is append only!");
	}

	public int size() {
		return sourceLog.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		XLogImpl newLog = new XLogImpl(getAttributes());
		newLog.addAll(sourceLog);
		return newLog;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.deckfour.xes.model.XAttributable#getAttributes()
	 */
	public XAttributeMap getAttributes() {
		return sourceLog.getAttributes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.deckfour.xes.model.XAttributable#getExtensions()
	 */
	public Set<XExtension> getExtensions() {
		return sourceLog.getExtensions();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.deckfour.xes.model.XAttributable#hasAttributes()
	 */
	public boolean hasAttributes() {
		return sourceLog.hasAttributes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.deckfour.xes.model.XAttributable#setAttributes(org.deckfour.xes.model
	 * .XAttributeMap)
	 */
	public void setAttributes(XAttributeMap arg0) {
		sourceLog.setAttributes(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.deckfour.xes.model.XLog#accept(org.deckfour.xes.model.XVisitor)
	 */
	public boolean accept(XVisitor arg0) {
		return sourceLog.accept(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.deckfour.xes.model.XLog#getClassifiers()
	 */
	public List<XEventClassifier> getClassifiers() {
		return sourceLog.getClassifiers();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.deckfour.xes.model.XLog#getGlobalEventAttributes()
	 */
	public List<XAttribute> getGlobalEventAttributes() {
		return sourceLog.getGlobalEventAttributes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.deckfour.xes.model.XLog#getGlobalTraceAttributes()
	 */
	public List<XAttribute> getGlobalTraceAttributes() {
		return sourceLog.getGlobalTraceAttributes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.deckfour.xes.model.XLog#getInfo(org.deckfour.xes.classification.
	 * XEventClassifier)
	 */
	public XLogInfo getInfo(XEventClassifier arg0) {
		return sourceLog.getInfo(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.deckfour.xes.model.XLog#setInfo(org.deckfour.xes.classification.
	 * XEventClassifier, org.deckfour.xes.info.XLogInfo)
	 */
	public void setInfo(XEventClassifier arg0, XLogInfo arg1) {
		sourceLog.setInfo(arg0, arg1);
	}

}
