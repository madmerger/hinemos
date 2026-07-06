/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.process.startup;

import com.clustercontrol.process.factory.ProcessMasterCache;
import com.clustercontrol.startup.CacheRefreshTask;

/**
 * プロセス監視機能のマスタキャッシュ({@link ProcessMasterCache})を再構成する{@link CacheRefreshTask}。<br/>
 */
public class ProcessMasterCacheRefreshTask implements CacheRefreshTask {

	@Override
	public void refresh() {
		ProcessMasterCache.refresh();
	}

	@Override
	public int getOrder() {
		return 140;
	}
}
