package org.processmining.xeslite.query;

import static com.googlecode.cqengine.query.QueryFactory.deduplicate;
import static com.googlecode.cqengine.query.QueryFactory.queryOptions;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.XVisitor;
import org.deckfour.xes.util.XAttributeUtils;
import org.processmining.xeslite.query.syntax.ParseException;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ForwardingList;
import com.google.common.collect.Iterators;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.index.Index;
import com.googlecode.cqengine.index.radixinverted.InvertedRadixTreeIndex;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.DeduplicationStrategy;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.resultset.ResultSet;
import com.googlecode.cqengine.resultset.common.WrappedResultSet;

/**
 * @author F. Mannhardt
 *
 */
class XIndexedTracesImpl implements XIndexedTraces {

	private static final AtomicInteger TRACE_COUNTER = new AtomicInteger();

	private static final class XUnwrappingResultSet extends WrappedResultSet<XTrace> {

		public XUnwrappingResultSet(ResultSet<XTrace> wrappedResultSet, Query<XTrace> query, QueryOptions queryOptions) {
			super(wrappedResultSet, query, queryOptions);
		}

		public Iterator<XTrace> iterator() {
			return Iterators.transform(super.iterator(), new Function<XTrace, XTrace>() {

				public XTrace apply(XTrace trace) {
					return unwrapTrace((XWrappedTrace) trace);
				}
			});
		}

		public boolean contains(XTrace object) {
			return super.contains(wrapTrace(object));
		}

	}

	private static final class XWrappedTrace extends ForwardingList<XEvent> implements XTrace {

		private final XTrace trace;
		private final int id;

		public XWrappedTrace(XTrace trace) {
			this.trace = trace;
			this.id = TRACE_COUNTER.getAndIncrement();
		}

		public XTrace getWrappedTrace() {
			return trace;
		}

		@Override
		public XAttributeMap getAttributes() {
			return trace.getAttributes();
		}

		@Override
		public Set<XExtension> getExtensions() {
			return trace.getExtensions();
		}

		@Override
		public boolean hasAttributes() {
			return trace.hasAttributes();
		}

		@Override
		public void setAttributes(XAttributeMap arg0) {
			trace.setAttributes(arg0);
		}

		@Override
		public void accept(XVisitor arg0, XLog arg1) {
			trace.accept(arg0, arg1);
		}

		@Override
		public int insertOrdered(XEvent arg0) {
			return trace.insertOrdered(arg0);
		}

		@Override
		public Object clone() {
			return trace.clone();
		}

		@Override
		protected List<XEvent> delegate() {
			return trace;
		}

		public int hashCode() {
			return id;
		}

		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (getClass() != obj.getClass())
				return false;
			XWrappedTrace other = (XWrappedTrace) obj;
			if (id != other.id)
				return false;
			return true;
		}

	}

	private static XTrace wrapTrace(XTrace element) {
		return new XWrappedTrace(element);
	}

	private static XTrace unwrapTrace(XWrappedTrace trace) {
		return trace.getWrappedTrace();
	}

	private final IndexedCollection<XTrace> indexedCollection;

	XIndexedTracesImpl() {
		indexedCollection = new ConcurrentIndexedCollection<>();
		indexedCollection.addIndex(InvertedRadixTreeIndex.onAttribute(eventNames()));
	}

	XIndexedTracesImpl(Collection<XTrace> log) {
		indexedCollection = new ConcurrentIndexedCollection<>();
		indexedCollection.addIndex(InvertedRadixTreeIndex.onAttribute(eventNames()));
		// Bulk-add
		indexedCollection.addAll(Collections2.transform(log, new Function<XTrace, XTrace>() {

			public XTrace apply(XTrace trace) {
				return wrapTrace(trace);
			}
		}));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.xeslite.query.XIndexedAttributable#addIndex(com.
	 * googlecode.cqengine.index.Index)
	 */
	public void addIndex(Index<XTrace> index) {
		indexedCollection.addIndex(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.xeslite.query.XIndexedAttributable#retrieve(com.
	 * googlecode.cqengine.query.Query)
	 */
	public ResultSet<XTrace> retrieve(Query<XTrace> query) {
		QueryOptions options = createQueryOptions();
		return unwrapResult(indexedCollection.retrieve(query, options), query, options);
	}

	private QueryOptions createQueryOptions() {
		return queryOptions(deduplicate(DeduplicationStrategy.LOGICAL_ELIMINATION));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.xeslite.query.XIndexedAttributable#parseQuery(java.lang
	 * .String, org.processmining.xeslite.query.AttributeTypeResolver)
	 */
	public Query<XTrace> parseQuery(String queryString, AttributeTypeResolver attributeTypeResolver)
			throws ParseException {
		return XIndex.buildAttributesOrNamesQueryForTrace(queryString, attributeTypeResolver);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.xeslite.query.XIndexedAttributable#parseQuery(java.lang
	 * .String)
	 */
	public Query<XTrace> parseQuery(String queryString) throws ParseException {
		return parseQuery(queryString, new AttributeTypeResolver() {

			public Class<? extends XAttribute> getAttributeType(final String attributeName) {
				// simply return the first class found
				for (XTrace t : indexedCollection) {
					XAttribute attribute = t.getAttributes().get(attributeName);
					if (attribute != null) {
						return XAttributeUtils.getType(attribute);
					}
					for (XEvent e : t) {
						attribute = e.getAttributes().get(attributeName);
						if (attribute != null) {
							return XAttributeUtils.getType(attribute);
						}
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
	 * org.processmining.xeslite.query.XIndexedAttributable#retrieve(java.lang.
	 * String)
	 */
	public ResultSet<XTrace> retrieve(String queryString) throws ParseException {
		Query<XTrace> query = parseQuery(queryString);
		QueryOptions options = createQueryOptions();
		return unwrapResult(indexedCollection.retrieve(query, options), query, options);
	}

	private ResultSet<XTrace> unwrapResult(ResultSet<XTrace> result, Query<XTrace> query, QueryOptions queryOptions) {
		return new XUnwrappingResultSet(result, query, queryOptions);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.xeslite.query.XIndexedAttributable#add(org.deckfour.xes
	 * .model.XAttributable)
	 */
	public void add(XTrace element) {
		indexedCollection.add(wrapTrace(element));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.xeslite.query.XIndexedAttributable#addAll(java.util.
	 * Collection)
	 */
	public void addAll(Collection<XTrace> traces) {
		indexedCollection.addAll(Collections2.transform(traces, new Function<XTrace, XTrace>() {

			public XTrace apply(XTrace trace) {
				return wrapTrace(trace);
			}
		}));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.xeslite.query.XIndexedTraces#traceName()
	 */
	public final Attribute<XTrace, String> traceName() {
		return XIndex.traceName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.xeslite.query.XIndexedTraces#eventNames()
	 */
	public final Attribute<XTrace, String> eventNames() {
		return XIndex.allEventNames();
	}

}
