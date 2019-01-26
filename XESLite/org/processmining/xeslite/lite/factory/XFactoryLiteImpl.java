package org.processmining.xeslite.lite.factory;

import org.deckfour.xes.factory.XFactoryRegistry;

/**
 * Use org.xeslite.lite.factory.XFactoryLiteImpl!
 * 
 * @author F. Mannhardt
 * 
 */
@Deprecated
public final class XFactoryLiteImpl extends org.xeslite.lite.factory.XFactoryLiteImpl {

	public static void register() {
		XFactoryRegistry.instance().register(new XFactoryLiteImpl());
	}

	public XFactoryLiteImpl() {
		super();
	}

	public XFactoryLiteImpl(boolean useInterner) {
		super(useInterner);
	}
	
}