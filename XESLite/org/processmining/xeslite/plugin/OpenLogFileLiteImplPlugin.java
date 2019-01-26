package org.processmining.xeslite.plugin;

import java.io.File;
import java.io.InputStream;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;

import com.google.common.base.Throwables;

/**
 * Open XES/MXML log using a lightweight implementation {@link XFactoryLiteImpl}
 * for XES objects.
 * 
 * @author F. Mannhardt
 * 
 */
@Plugin(name = "Open XES Log File (XESLite - Sequential XIDs)", //\
		level = PluginLevel.PeerReviewed, parameterLabels = { "Filename" }, returnLabels = {
				"Log (single process)" }, returnTypes = { XLog.class })
@UIImportPlugin(description = "ProM log files (XESLite - Sequential XIDs)", extensions = { "mxml", "xml", "gz",
		"zip", "xes", "xez" })
public class OpenLogFileLiteImplPlugin extends AbstractOpenXESFilePlugin {

	@Override
	protected Object importFromFile(PluginContext context, File file) throws Exception {
		try {
			try (InputStream is = getInputStream(file)) {
				return importFromStreamWithFactory(context, is, file.getName(), file.length(), new org.xeslite.lite.factory.XFactoryLiteImpl());
			}
		} catch (Exception e) {
			throw new Exception(Throwables.getRootCause(e).getMessage() + "\n\nDebug message:\n"
					+ Throwables.getStackTraceAsString(e));
		}
	}

	@Override
	protected void beforeParse(XFactory factory) {
	}

	@Override
	protected void afterParse(XFactory factory) {
	}

}