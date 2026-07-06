/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.startup;

import com.clustercontrol.jobmanagement.factory.FullJob;
import com.clustercontrol.startup.CacheRefreshTask;

/**
 * ジョブ管理機能のジョブ情報キャッシュ({@link FullJob})を再構成する{@link CacheRefreshTask}。<br/>
 */
public class FullJobRefreshTask implements CacheRefreshTask {

	@Override
	public void refresh() {
		FullJob.init();
	}

	@Override
	public int getOrder() {
		return 60;
	}
}
