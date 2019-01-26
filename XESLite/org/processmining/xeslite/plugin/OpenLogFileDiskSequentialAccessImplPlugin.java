package org.processmining.xeslite.plugin;

import org.xeslite.external.MapDBStore.Builder;
import org.xeslite.external.XFactoryExternalStore;

@Deprecated
public class OpenLogFileDiskSequentialAccessImplPlugin extends AbstractMapDBOpenLogFilePlugin {

	protected XFactoryExternalStore createFactory(Builder builder) {
		return new XFactoryExternalStore.MapDBDiskSequentialAccessImpl(builder);
	}

}
