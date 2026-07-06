/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.winevent.startup;

import com.clustercontrol.startup.CacheRefreshTask;
import com.clustercontrol.winevent.session.MonitorWinEventControllerBean;

/**
 * Windowsイベント監視機能のキャッシュ({@link MonitorWinEventControllerBean})を再構成する{@link CacheRefreshTask}。<br/>
 */
public class MonitorWinEventCacheRefreshTask implements CacheRefreshTask {

	@Override
	public void refresh() {
		MonitorWinEventControllerBean.refreshCache();
	}

	@Override
	public int getOrder() {
		return 190;
	}
}
