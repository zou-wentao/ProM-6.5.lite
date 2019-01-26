package org.processmining.xeslite.plugin;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.xeslite.external.MapDBDatabaseImpl;
import org.xeslite.external.MapDBStore.Builder;
import org.xeslite.external.XFactoryExternalStore;

/**
 * Open XES log using a buffered implementation {@link XFactoryExternalStore}
 * for the attributes of all XES objects. In this implementation all attributes
 * are immutable. Therefore, it does NOT support the MXML parser, which tries to
 * change the values of attributes after creation.
 * 
 * @author F. Mannhardt
 * 
 */
@Plugin(name = "Open XES Log File (XESLite - MapDB (no cache))", //
level = PluginLevel.PeerReviewed, parameterLabels = { "Filename" }, returnLabels = { "Log (single process)" }, returnTypes = { XLog.class })
@UIImportPlugin(description = "ProM log files (XESLite - MapDB (no cache))",//
extensions = { "gz", "zip", "xes", "xez" })
// does not support MXML parser
public class OpenLogFileDiskImplWithoutCachePlugin extends AbstractMapDBOpenLogFilePlugin {

	protected XFactoryExternalStore createFactory(Builder builder) {	
		MapDBDatabaseImpl database = new MapDBDatabaseImpl();
		database.setUseCache(false); // don't cache at all in this implementation
		builder.withDatabase(database);
		return new XFactoryExternalStore.MapDBDiskWithoutCacheImpl(builder);
	}
	
}
