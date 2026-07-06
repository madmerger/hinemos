/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.custom.startup;

import com.clustercontrol.custom.factory.SelectCustom;
import com.clustercontrol.startup.CacheRefreshTask;

/**
 * カスタム監視機能のキャッシュ({@link SelectCustom})を再構成する{@link CacheRefreshTask}。<br/>
 */
public class SelectCustomRefreshTask implements CacheRefreshTask {

	@Override
	public void refresh() {
		SelectCustom.refreshCache();
	}

	@Override
	public int getOrder() {
		return 50;
	}
}
