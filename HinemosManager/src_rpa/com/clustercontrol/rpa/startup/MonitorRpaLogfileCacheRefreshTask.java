/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.startup;

import com.clustercontrol.rpa.monitor.session.MonitorRpaLogfileControllerBean;
import com.clustercontrol.startup.CacheRefreshTask;

/**
 * RPAログファイル監視機能のキャッシュ({@link MonitorRpaLogfileControllerBean})を再構成する{@link CacheRefreshTask}。<br/>
 */
public class MonitorRpaLogfileCacheRefreshTask implements CacheRefreshTask {

	@Override
	public void refresh() {
		MonitorRpaLogfileControllerBean.refreshCache();
	}

	@Override
	public int getOrder() {
		return 200;
	}
}
