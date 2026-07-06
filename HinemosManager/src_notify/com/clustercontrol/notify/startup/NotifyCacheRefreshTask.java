/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.startup;

import com.clustercontrol.notify.util.NotifyCache;
import com.clustercontrol.startup.CacheRefreshTask;

/**
 * 通知機能のキャッシュ({@link NotifyCache})を再構成する{@link CacheRefreshTask}。<br/>
 */
public class NotifyCacheRefreshTask implements CacheRefreshTask {

	@Override
	public void refresh() {
		NotifyCache.refresh();
	}

	@Override
	public int getOrder() {
		return 110;
	}
}
