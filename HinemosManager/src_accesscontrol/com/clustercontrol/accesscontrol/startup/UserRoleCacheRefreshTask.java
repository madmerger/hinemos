/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.startup;

import com.clustercontrol.accesscontrol.util.UserRoleCache;
import com.clustercontrol.startup.CacheRefreshTask;

/**
 * アクセス制御機能のキャッシュ({@link UserRoleCache})を再構成する{@link CacheRefreshTask}。<br/>
 */
public class UserRoleCacheRefreshTask implements CacheRefreshTask {

	@Override
	public void refresh() {
		UserRoleCache.refresh();
	}

	@Override
	public int getOrder() {
		return 10;
	}
}
