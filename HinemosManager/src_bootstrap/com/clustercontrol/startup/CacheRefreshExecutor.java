/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.startup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@link CacheRefreshTask} を {@link ServiceLoader} 経由で収集し、<br/>
 * {@link CacheRefreshTask#getOrder()} の昇順で実行するユーティリティ。<br/>
 */
public class CacheRefreshExecutor {

	public static final Log log = LogFactory.getLog(CacheRefreshExecutor.class);

	private CacheRefreshExecutor() {
	}

	/**
	 * 登録されている全ての{@link CacheRefreshTask}を、実行順序({@link CacheRefreshTask#getOrder()})の昇順で返す。<br/>
	 *
	 * @return 実行順序でソートされたタスクのリスト
	 */
	public static List<CacheRefreshTask> loadTasks() {
		List<CacheRefreshTask> tasks = new ArrayList<CacheRefreshTask>();
		for (CacheRefreshTask task : ServiceLoader.load(CacheRefreshTask.class)) {
			tasks.add(task);
		}
		tasks.sort(Comparator.comparingInt(CacheRefreshTask::getOrder));
		return tasks;
	}

	/**
	 * 登録されている全ての{@link CacheRefreshTask}を、実行順序の昇順で実行する。<br/>
	 * <br/>
	 * 従来のハードコードされたキャッシュ初期化と同じく、いずれかのタスクが例外を送出した場合は<br/>
	 * その時点で呼び出し元へ伝播する（後続のタスクは実行されない）。<br/>
	 */
	public static void refreshAll() {
		List<CacheRefreshTask> tasks = loadTasks();
		log.info("refreshing caches via " + tasks.size() + " registered CacheRefreshTask(s)...");
		for (CacheRefreshTask task : tasks) {
			log.debug("refreshing cache : " + task.getClass().getName() + " (order=" + task.getOrder() + ")");
			task.refresh();
		}
	}
}
