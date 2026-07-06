/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.calendar.startup;

import com.clustercontrol.calendar.util.CalendarPatternCache;
import com.clustercontrol.startup.CacheRefreshTask;

/**
 * カレンダパターン機能のキャッシュ({@link CalendarPatternCache})を再構成する{@link CacheRefreshTask}。<br/>
 */
public class CalendarPatternCacheRefreshTask implements CacheRefreshTask {

	@Override
	public void refresh() {
		CalendarPatternCache.init();
	}

	@Override
	public int getOrder() {
		return 30;
	}
}
