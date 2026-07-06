/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.systemlog.startup;

import com.clustercontrol.startup.CacheRefreshTask;
import com.clustercontrol.systemlog.util.SystemlogCache;

/**
 * システムログ監視機能のキャッシュ({@link SystemlogCache})を再構成する{@link CacheRefreshTask}。<br/>
 */
public class SystemlogCacheRefreshTask implements CacheRefreshTask {

	@Override
	public void refresh() {
		SystemlogCache.refresh();
	}

	@Override
	public int getOrder() {
		return 180;
	}
}
