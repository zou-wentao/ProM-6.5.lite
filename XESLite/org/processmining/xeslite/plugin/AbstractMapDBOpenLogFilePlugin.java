package org.processmining.xeslite.plugin;

import java.io.File;
import java.io.InputStream;
import java.text.MessageFormat;

import org.deckfour.xes.factory.XFactory;
import org.processmining.framework.plugin.PluginContext;
import org.xeslite.common.XESLiteException;
import org.xeslite.external.MapDBStore;
import org.xeslite.external.XFactoryExternalStore;

import com.google.common.base.Throwables;

abstract public class AbstractMapDBOpenLogFilePlugin extends AbstractOpenXESFilePlugin {

	private static final int MAX_KEY_POOL_SHIFT = MapDBStore.getMaxKeyPoolShift();

	abstract protected XFactoryExternalStore createFactory(MapDBStore.Builder builder);

	@Override
	protected Object importFromFile(PluginContext context, File file) throws Exception {
		int keyPoolShift = MapDBStore.getDefaultKeyPoolShift();
		log(MessageFormat.format("XESLite: Importing XES file with key pool size {0}", (1 << keyPoolShift)));
		while (keyPoolShift < MAX_KEY_POOL_SHIFT) {
			MapDBStore.Builder builder = new MapDBStore.Builder().withPump().withKeyPoolShift(keyPoolShift);
			XFactoryExternalStore factory = createFactory(builder);
			try {
				try (InputStream is = getInputStream(file)) {
					return importFromStreamWithFactory(context, is, file.getName(), file.length(), factory);
				}
			} catch (XESLiteException.StringPoolException e) {
				// Get rid of the half-initialized factory
				if (factory.isPumping()) {
					factory.finishPump();
				}
				factory.dispose();
				// Increase keypool shift
				keyPoolShift++;
				log(MessageFormat.format("XESLite: Initial key pool size {0} too small. Increasing to {1}",
						(1 << (keyPoolShift - 1)), (1 << keyPoolShift)));
			}
		}
		// We tried to increase the keypool shift until the configured maximum
		throw new XESLiteException.StringPoolException(
				"<br/>" + "Could not load the XES file. It contains too many distinct literals in the attribute keys (such as 'concept:name', 'time:timestamp', etc.)."
						+ "<br/>"
						+ "One underlying assumption of XESLite is that the number of distinct attribute keys is small."
						+ "<br/>" + "The maximum number of attribute keys is " + MAX_KEY_POOL_SHIFT);
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
