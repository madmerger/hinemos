/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.startup;

import com.clustercontrol.repository.util.FacilityTreeCache;
import com.clustercontrol.startup.CacheRefreshTask;

/**
 * リポジトリ機能のファシリティツリーキャッシュ({@link FacilityTreeCache})を再構成する{@link CacheRefreshTask}。<br/>
 */
public class FacilityTreeCacheRefreshTask implements CacheRefreshTask {

	@Override
	public void refresh() {
		FacilityTreeCache.refresh();
	}

	@Override
	public int getOrder() {
		return 170;
	}
}
