package org.processmining.xeslite.plugin;

import java.io.File;
import java.io.InputStream;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.xeslite.common.XESLiteException;
import org.xeslite.external.MapDBStore;
import org.xeslite.external.XFactoryExternalStore;

import com.google.common.base.Throwables;

/**
 * Open XES log using a buffered implementation {@link XFactoryExternalStore}
 * for the attributes of all XES objects. In this implementation all attributes
 * are immutable. Therefore, it does NOT support the MXML parser, which tries to
 * change the values of attributes after creation.
 * 
 * @author F. Mannhardt
 * 
 */
@Plugin(name = "Open XES Log File (XESLite - In-Memory (slow random access))", //
		level = PluginLevel.Regular, parameterLabels = { "Filename" }, returnLabels = {
				"Log (single process)" }, returnTypes = { XLog.class })
@UIImportPlugin(description = "ProM log files (XESLite - In-Memory (slow random access))", //
		extensions = { "gz", "zip", "xes", "xez" }) // does not support MXML parser
public class OpenLogFileDiskMemoryStoreImpl extends AbstractOpenXESFilePlugin {

	@Override
	protected Object importFromFile(PluginContext context, File file) throws Exception {
		XFactoryExternalStore factory = new XFactoryExternalStore.InMemoryStoreImpl();
		try (InputStream is = getInputStream(file)) {
			return importFromStreamWithFactory(context, is, file.getName(), file.length(), factory);
		}
	}

	@Override
	protected Object importFromStreamWithFactory(PluginContext context, InputStream input, String fileName,
			long fileSizeInBytes, XFactory factory) throws Exception {
		try {
			return super.importFromStreamWithFactory(context, input, fileName, fileSizeInBytes, factory);
		} catch (XESLiteException.StringPoolException e) {
			throw new XESLiteException.StringPoolException(
					"<br/>" + "Could not load the XES file. It contains too many distinct literals in the attribute keys (such as 'concept:name', 'time:timestamp', etc.)."
							+ "<br/>"
							+ "One underlying assumption of XESLite is that the number of distinct attribute keys is small."
							+ "<br/>" + "The currently configured maximum number of attribute keys is "
							+ MapDBStore.getDefaultKeyPoolShift(),
					e);
		} catch (Exception e) {
			throw new Exception(Throwables.getRootCause(e).getMessage() + "\n\nDebug message:\n"
					+ Throwables.getStackTraceAsString(e));
		}
	}

	@Override
	protected void beforeParse(XFactory factory) {
		((XFactoryExternalStore) factory).startPump();
	}

	@Override
	protected void afterParse(XFactory factory) {
		try {
			((XFactoryExternalStore) factory).finishPump();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return;
		}
	}

}