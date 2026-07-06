/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.startup;

import com.clustercontrol.commons.bean.SettingUpdateInfo;
import com.clustercontrol.startup.CacheRefreshTask;

/**
 * 設定更新情報({@link SettingUpdateInfo})を再構成する{@link CacheRefreshTask}。<br/>
 */
public class SettingUpdateInfoRefreshTask implements CacheRefreshTask {

	@Override
	public void refresh() {
		SettingUpdateInfo.init();
	}

	@Override
	public int getOrder() {
		return 40;
	}
}
