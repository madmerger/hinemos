/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.calendar.startup;

import com.clustercontrol.calendar.util.CalendarCache;
import com.clustercontrol.startup.CacheRefreshTask;

/**
 * カレンダ機能のキャッシュ({@link CalendarCache})を再構成する{@link CacheRefreshTask}。<br/>
 */
public class CalendarCacheRefreshTask implements CacheRefreshTask {

	@Override
	public void refresh() {
		CalendarCache.init();
	}

	@Override
	public int getOrder() {
		return 20;
	}
}
