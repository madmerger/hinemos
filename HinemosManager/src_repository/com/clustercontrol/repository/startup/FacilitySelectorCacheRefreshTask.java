/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.startup;

import com.clustercontrol.repository.factory.FacilitySelector;
import com.clustercontrol.startup.CacheRefreshTask;

/**
 * リポジトリ機能のファシリティツリーキャッシュ({@link FacilitySelector})を再構成する{@link CacheRefreshTask}。<br/>
 */
public class FacilitySelectorCacheRefreshTask implements CacheRefreshTask {

	@Override
	public void refresh() {
		FacilitySelector.initCacheFacilityTree();
	}

	@Override
	public int getOrder() {
		return 150;
	}
}
