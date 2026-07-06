/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.logfile.startup;

import com.clustercontrol.logfile.session.MonitorLogfileControllerBean;
import com.clustercontrol.startup.CacheRefreshTask;

/**
 * ログファイル監視機能のキャッシュ({@link MonitorLogfileControllerBean})を再構成する{@link CacheRefreshTask}。<br/>
 */
public class MonitorLogfileCacheRefreshTask implements CacheRefreshTask {

	@Override
	public void refresh() {
		MonitorLogfileControllerBean.refreshCache();
	}

	@Override
	public int getOrder() {
		return 80;
	}
}
