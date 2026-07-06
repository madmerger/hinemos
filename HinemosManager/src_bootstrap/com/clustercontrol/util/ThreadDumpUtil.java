/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Hinemos Managerの運用ユーティリティ。<br/>
 * Windows版マネージャ停止時のスレッドダンプ出力を担う。<br/>
 * <br/>
 * ({@code HinemosManagerMain} を環境初期化・ライフサイクル制御に絞るため、<br/>
 *  スレッドダンプ出力処理を本クラスへ抽出したもの)<br/>
 */
public class ThreadDumpUtil {

	public static final Log log = LogFactory.getLog(ThreadDumpUtil.class);

	private ThreadDumpUtil() {
	}

	/**
	 * スレッドダンプをファイルへ出力する。<br/>
	 * Linux版マネージャは停止スクリプト（jvm_stop.sh）で出力しているため、Windows版マネージャの場合のみ出力する。<br/>
	 */
	public static void outputThreadDump() {
		String osName = System.getProperty("os.name");

		if ( osName == null || !osName.toLowerCase().startsWith("windows") ) {
			//Linux版マネージャは停止スクリプト（jvm_stop.sh）で出力しているため、Windows版マネージャの場合のみ出力
			return;
		}

		log.info("output thread dump start");

		//環境変数から取得
		String javaHome = System.getProperty("java.home");
		String hinemosLogDir = System.getProperty("hinemos.manager.log.dir");
		//隠しJVM引数
		String countStr =System.getProperty("hinemos.thread.dump.count");
		String intervalStr = System.getProperty("hinemos.thread.dump.interval");
		int count = 3;
		long interval = 1000L;

		if (countStr != null && !"".equals(countStr)) {
			try {
				count = Integer.parseInt(countStr);
			} catch (NumberFormatException e) {
				//デフォルト値を使用
				log.info("hinemos.thread.dump.count is not number. use default. value=" + countStr);
			}
		}

		if (intervalStr != null && !"".equals(intervalStr)) {
			try {
				interval = Integer.parseInt(intervalStr);
			} catch (NumberFormatException e) {
				//デフォルト値を使用
				log.info("hinemos.thread.dump.interval is not number. use default. value=" + intervalStr);
			}
		}

		String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
		String sep = File.separator;
		String jstackCmd = javaHome + sep + ".." + sep + "bin" + sep + "jstack.exe";

		log.debug("thread dump count:" + String.valueOf(count));
		log.debug("thread dump interval:" + String.valueOf(interval));
		if (!new File(jstackCmd).exists()) {
			log.info("thread dump jstack not exist. path:" + jstackCmd);
			return;
		}

		for (int i = 0; i < count; i++) {
			outThreadDumpSub(jstackCmd, hinemosLogDir, pid);
			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				//ignore
			}

		}
		log.info("output thread dump end.(there's a possible that jstack process ruuning)");
	}

	private static void outThreadDumpSub(String jstackCmd, String hinemosLogDir, String pid) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
		File pidFile = new File(hinemosLogDir + File.separator + "threaddump_" + sdf.format(new Date()));

		List<String> command = new ArrayList<>();
		command.add(jstackCmd);
		command.add(pid);

		log.debug("thread dump command:" + command.get(0) + " " + command.get(1));

		ProcessBuilder pb = new ProcessBuilder(command);
		pb.redirectOutput(pidFile);

		try {
			//終了は待たない
			//起動されたプロセスはJVMが終了しても、プロセスの処理が終了するまで実行され続ける
			pb.start();
		} catch (IOException e) {
			log.warn("thread dump command:" + command.get(0) + " " + command.get(1));
			log.warn(e);
		}
	}
}
