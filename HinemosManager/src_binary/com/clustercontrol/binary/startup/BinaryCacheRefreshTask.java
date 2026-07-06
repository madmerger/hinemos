/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.binary.startup;

import com.clustercontrol.binary.session.BinaryControllerBean;
import com.clustercontrol.startup.CacheRefreshTask;

/**
 * バイナリ監視機能のキャッシュ({@link BinaryControllerBean})を再構成する{@link CacheRefreshTask}。<br/>
 */
public class BinaryCacheRefreshTask implements CacheRefreshTask {

	@Override
	public void refresh() {
		BinaryControllerBean.refreshCache();
	}

	@Override
	public int getOrder() {
		return 90;
	}
}
