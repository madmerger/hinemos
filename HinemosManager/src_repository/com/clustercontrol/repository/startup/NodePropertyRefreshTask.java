/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.startup;

import com.clustercontrol.repository.factory.NodeProperty;
import com.clustercontrol.startup.CacheRefreshTask;

/**
 * リポジトリ機能のノードプロパティキャッシュ({@link NodeProperty})を再構成する{@link CacheRefreshTask}。<br/>
 */
public class NodePropertyRefreshTask implements CacheRefreshTask {

	@Override
	public void refresh() {
		NodeProperty.init();
	}

	@Override
	public int getOrder() {
		return 160;
	}
}
