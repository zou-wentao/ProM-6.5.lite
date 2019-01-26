package org.processmining.xeslite.query;

import static com.googlecode.cqengine.query.QueryFactory.deduplicate;
import static com.googlecode.cqengine.query.QueryFactory.queryOptions;

import java.util.Collection;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.util.XAttributeUtils;
import org.processmining.xeslite.query.syntax.ParseException;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.index.Index;
import com.googlecode.cqengine.index.radixinverted.InvertedRadixTreeIndex;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.DeduplicationStrategy;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.resultset.ResultSet;

class XIndexedEventsImpl implements XIndexedEvents {

	private final IndexedCollection<XEvent> indexedCollection;
	
	XIndexedEventsImpl() {
		indexedCollection = new ConcurrentIndexedCollection<>();
		indexedCollection.addIndex(InvertedRadixTreeIndex.onAttribute(eventName()));
	}

	XIndexedEventsImpl(Collection<XEvent> events) {
		indexedCollection = new ConcurrentIndexedCollection<>();
		indexedCollection.addIndex(InvertedRadixTreeIndex.onAttribute(eventName()));
		// Bulk-add
		indexedCollection.addAll(events);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.xeslite.query.XIndexedAttributable#addIndex(com.googlecode
	 * .cqengine.index.Index)
	 */
	public void addIndex(Index<XEvent> index) {
		indexedCollection.addIndex(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.xeslite.query.XIndexedAttributable#retrieve(com.googlecode
	 * .cqengine.query.Query)
	 */
	public ResultSet<XEvent> retrieve(Query<XEvent> query) {
		return indexedCollection.retrieve(query, createQueryOptions());
	}

	private QueryOptions createQueryOptions() {
		return queryOptions(deduplicate(DeduplicationStrategy.LOGICAL_ELIMINATION));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.xeslite.query.XIndexedAttributable#parseQuery(java.
	 * lang.String, org.processmining.xeslite.query.AttributeTypeResolver)
	 */
	public Query<XEvent> parseQuery(String queryString, AttributeTypeResolver attributeTypeResolver)
			throws ParseException {
		return XIndex.buildAttributesOrNamesQueryForEvent(queryString, attributeTypeResolver);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.xeslite.query.XIndexedAttributable#parseQuery(java.
	 * lang.String)
	 */
	public Query<XEvent> parseQuery(String queryString) throws ParseException {
		return parseQuery(queryString, new AttributeTypeResolver() {

			public Class<? extends XAttribute> getAttributeType(final String attributeName) {
				// simply return the first class found
				for (XEvent e : indexedCollection) {
					XAttribute attribute = e.getAttributes().get(attributeName);
					if (attribute != null) {
						return XAttributeUtils.getType(attribute);
					}
				}
				return XAttributeLiteral.class;

			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.xeslite.query.XIndexedAttributable#retrieve(java.lang
	 * .String)
	 */
	public ResultSet<XEvent> retrieve(String queryString) throws ParseException {
		return indexedCollection.retrieve(parseQuery(queryString), createQueryOptions());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.xeslite.query.XIndexedAttributable#add(org.deckfour
	 * .xes.model.XAttributable)
	 */
	public void add(XEvent element) {
		indexedCollection.add(element);
	}

	/* (non-Javadoc)
	 * @see org.processmining.xeslite.query.XIndexedAttributable#addAll(java.util.Collection)
	 */
	public void addAll(Collection<XEvent> events) {
		indexedCollection.addAll(events);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.xeslite.query.XIndexedEvents#eventName()
	 */
	public final Attribute<XEvent, String> eventName() {
		return XIndex.eventName();
	}


}
