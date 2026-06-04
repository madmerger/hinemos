/*
 * Copyright (c) 2024 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.client5.http.ssl.HttpsSupport;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.TrustStrategy;

/**
 * SSL/TLS接続における証明書検証の有効/無効を一元管理するユーティリティクラス。
 *
 * <p>デフォルトでは証明書検証が有効（セキュアデフォルト）です。
 * 検証を無効にする場合は、Hinemosプロパティで設定してください。</p>
 *
 * <ul>
 *   <li>グローバル設定: {@code common.ssl.trust.all} (デフォルト: false)</li>
 *   <li>モジュール別オーバーライド:
 *     {@code infra.winrm.ssl.trustall},
 *     {@code monitor.winservice.ssl.trustall},
 *     {@code monitor.http.ssl.trustall},
 *     {@code notify.rest.ssl.trustall},
 *     {@code notify.message.ssl.trustall},
 *     {@code rpa.management.rest.client.config.ssl.trustall},
 *     {@code access.ldap.ssl.trustall}
 *   </li>
 * </ul>
 *
 * @version 7.2.1
 * @since 7.2.1
 */
public class SslTrustConfig {

	private static final Log log = LogFactory.getLog(SslTrustConfig.class);

	private SslTrustConfig() {
	}

	/**
	 * モジュール固有プロパティまたはグローバルプロパティに基づき、
	 * SSL証明書検証を無効化するかどうかを判定します。
	 *
	 * @param moduleProperty モジュール固有の ssl.trustall プロパティ（nullの場合はグローバルのみ参照）
	 * @return true: 全証明書を信頼する（検証無効）, false: 証明書検証を行う
	 */
	public static boolean isTrustAll(HinemosPropertyCommon moduleProperty) {
		if (moduleProperty != null && moduleProperty.getBooleanValue()) {
			return true;
		}
		return HinemosPropertyCommon.common_ssl_trust_all.getBooleanValue();
	}

	/**
	 * SSL検証が無効化されている場合にWARNINGレベルのログを出力します。
	 *
	 * @param target 接続先の識別情報
	 */
	public static void logTrustAllWarning(String target) {
		log.warn("SSL certificate verification is disabled for [" + target + "]. This is a security risk.");
	}

	/**
	 * 証明書検証を行わない {@link X509TrustManager} を生成します。
	 *
	 * @return 全証明書を信頼するTrustManager
	 */
	public static X509TrustManager createTrustAllManager() {
		return new X509TrustManager() {
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType)
					throws CertificateException {
			}

			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType)
					throws CertificateException {
			}
		};
	}

	/**
	 * 設定に基づいて {@link HostnameVerifier} を返します。
	 *
	 * @param trustAll true: {@link NoopHostnameVerifier}, false: デフォルトのHostnameVerifier
	 * @return HostnameVerifier
	 */
	public static HostnameVerifier getHostnameVerifier(boolean trustAll) {
		if (trustAll) {
			return NoopHostnameVerifier.INSTANCE;
		}
		return HttpsSupport.getDefaultHostnameVerifier();
	}

	/**
	 * 全証明書を信頼する Apache HttpClient 用の {@link SSLConnectionSocketFactory} を生成します。
	 *
	 * @return 証明書検証を行わないSSLConnectionSocketFactory
	 * @throws KeyManagementException 鍵管理エラー
	 * @throws NoSuchAlgorithmException アルゴリズムが見つからない
	 * @throws KeyStoreException キーストアエラー
	 */
	public static SSLConnectionSocketFactory createTrustAllSSLSocketFactory()
			throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		TrustStrategy trustStrategy = new TrustStrategy() {
			@Override
			public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				return true;
			}
		};
		return new SSLConnectionSocketFactory(
				new SSLContextBuilder().loadTrustMaterial(null, trustStrategy).build(),
				NoopHostnameVerifier.INSTANCE);
	}
}
