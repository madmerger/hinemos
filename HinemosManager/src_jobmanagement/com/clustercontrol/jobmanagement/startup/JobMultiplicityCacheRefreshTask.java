/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.startup;

import com.clustercontrol.jobmanagement.util.JobMultiplicityCache;
import com.clustercontrol.startup.CacheRefreshTask;

/**
 * ジョブ多重度キャッシュ({@link JobMultiplicityCache})を再構成する{@link CacheRefreshTask}。<br/>
 */
public class JobMultiplicityCacheRefreshTask implements CacheRefreshTask {

	@Override
	public void refresh() {
		JobMultiplicityCache.refresh();
	}

	@Override
	public int getOrder() {
		return 70;
	}
}
