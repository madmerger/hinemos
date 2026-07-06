/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.performance.startup;

import com.clustercontrol.performance.util.CollectorMasterCache;
import com.clustercontrol.startup.CacheRefreshTask;

/**
 * 性能管理機能の収集項目マスタキャッシュ({@link CollectorMasterCache})を再構成する{@link CacheRefreshTask}。<br/>
 */
public class CollectorMasterCacheRefreshTask implements CacheRefreshTask {

	@Override
	public void refresh() {
		CollectorMasterCache.refresh();
	}

	@Override
	public int getOrder() {
		return 130;
	}
}
