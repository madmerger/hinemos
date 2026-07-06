/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.startup;

/**
 * MAINTENANCE起動モードからNORMAL起動モードへ移行する際に、<br/>
 * DBと等価にマップされた各機能のキャッシュを再構成するためのタスクを表すインタフェース。<br/>
 * <br/>
 * 各機能モジュールはこのインタフェースを実装したクラスを用意し、<br/>
 * {@code META-INF/services/com.clustercontrol.startup.CacheRefreshTask} に<br/>
 * 具象クラス名を登録することで、{@link java.util.ServiceLoader} 経由で自動的に検出・実行される。<br/>
 * これにより、起動中枢({@code HinemosManagerMain})が各機能モジュールへ直接依存することなく、<br/>
 * キャッシュ初期化を各機能側へ委譲できる。<br/>
 */
public interface CacheRefreshTask {

	/** {@link #getOrder()} を明示的に指定しない実装のデフォルト実行順序。 */
	int DEFAULT_ORDER = 1000;

	/**
	 * キャッシュの再構成処理を行う。<br/>
	 */
	void refresh();

	/**
	 * タスクの実行順序を表す値を返す。<br/>
	 * 値が小さいものから順に実行される。<br/>
	 * キャッシュ間に初期化順序の依存がある場合、この値で順序を制御する。<br/>
	 *
	 * @return 実行順序（昇順に実行）
	 */
	default int getOrder() {
		return DEFAULT_ORDER;
	}
}
