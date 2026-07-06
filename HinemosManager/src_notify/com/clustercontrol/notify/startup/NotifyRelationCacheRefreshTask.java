/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.startup;

import com.clustercontrol.notify.util.NotifyRelationCache;
import com.clustercontrol.startup.CacheRefreshTask;

/**
 * 通知関連情報キャッシュ({@link NotifyRelationCache})を再構成する{@link CacheRefreshTask}。<br/>
 */
public class NotifyRelationCacheRefreshTask implements CacheRefreshTask {

	@Override
	public void refresh() {
		NotifyRelationCache.refresh();
	}

	@Override
	public int getOrder() {
		return 120;
	}
}
