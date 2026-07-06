/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance.startup;

import com.clustercontrol.maintenance.factory.HinemosPropertyInfoCache;
import com.clustercontrol.startup.CacheRefreshTask;

/**
 * Hinemosプロパティ情報キャッシュ({@link HinemosPropertyInfoCache})を再構成する{@link CacheRefreshTask}。<br/>
 */
public class HinemosPropertyInfoCacheRefreshTask implements CacheRefreshTask {

	@Override
	public void refresh() {
		HinemosPropertyInfoCache.refresh();
	}

	@Override
	public int getOrder() {
		return 100;
	}
}
