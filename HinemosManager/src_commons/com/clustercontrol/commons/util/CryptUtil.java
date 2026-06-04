/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 暗号化・復号ユーティリティ。
 *
 * <p>新規暗号化は AES-256-GCM で行い、復号時には AES-GCM をまず試み、
 * 失敗した場合に旧 Blowfish/ECB で復号を試みる後方互換フォールバックを持つ。</p>
 *
 * <h3>使用例</h3>
 * <pre>{@code
 * // 暗号化（AES-256-GCM）
 * String encrypted = CryptUtil.encrypt("my secret text");
 *
 * // 復号（AES-GCM を試み、失敗時は旧 Blowfish フォールバック）
 * String decrypted = CryptUtil.decrypt(encrypted);
 *
 * // CLI からの利用
 * // java CryptUtil encrypt <key> <word>
 * // java CryptUtil decrypt <key> <word>
 * }</pre>
 */
public class CryptUtil {
	
	private static final Log m_log = LogFactory.getLog( CryptUtil.class );
	
	/** AES-256-GCM アルゴリズム */
	private static final String ALGORITHM_AES = "AES";
	private static final String TRANSFORMATION_AES_GCM = "AES/GCM/NoPadding";
	
	/** GCM IV サイズ (12 バイト) */
	private static final int GCM_IV_LENGTH = 12;
	
	/** GCM 認証タグ長 (128 bit) */
	private static final int GCM_TAG_LENGTH = 128;
	
	/** 旧 Blowfish アルゴリズム (後方互換用) */
	private static final String ALGORITHM_BLOWFISH = "BLOWFISH";
	
	/** セキュアな乱数生成器 */
	private static final SecureRandom secureRandom = new SecureRandom();
	
	/** 暗号鍵（鍵ファイルから読み込み、未設定時は null） */
	private static String cryptKey = null;
	
	static {
		String etcdir = System.getProperty("hinemos.manager.etc.dir");
		String keyFile = "db_crypt.key";
		String keyPath = etcdir + File.separator + keyFile;
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		
		try {
			fileReader = new FileReader(keyPath);
			bufferedReader = new BufferedReader(fileReader);
			cryptKey = bufferedReader.readLine();
			m_log.info("crypt key file loaded successfully. (" + keyFile + ")");
		} catch (Exception e){
			m_log.error("crypt key file not readable. (" + keyFile + ") : " + e.getMessage()
					+ " Encryption/decryption will not be available without a valid key file.", e);
		} finally {
			try {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
			} catch (IOException e) {
			}
			try {
				if (fileReader != null) {
					fileReader.close();
				}
			} catch (IOException e) {
			}
		}
	}
	
	/**
	 * 鍵が設定されていることを検証する。
	 * 鍵ファイルが未設定の場合は {@link IllegalStateException} をスローする。
	 *
	 * @throws IllegalStateException 鍵が設定されていない場合
	 */
	private static void requireKey() {
		if (cryptKey == null || cryptKey.isEmpty()) {
			String msg = "Encryption key is not configured. "
					+ "Ensure the key file (db_crypt.key) is present and readable.";
			m_log.error(msg);
			throw new IllegalStateException(msg);
		}
	}
	
	/**
	 * 任意長の鍵文字列から AES-256 用の 32 バイト鍵を導出する。
	 * SHA-256 ダイジェストを使用する。
	 *
	 * @param key 元の鍵文字列
	 * @return AES-256 用の {@link SecretKeySpec}
	 */
	private static SecretKeySpec deriveAesKey(String key) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] keyBytes = digest.digest(key.getBytes());
			return new SecretKeySpec(keyBytes, ALGORITHM_AES);
		} catch (NoSuchAlgorithmException e) {
			// SHA-256 は Java 標準で必ず利用可能
			throw new RuntimeException("SHA-256 algorithm not available", e);
		}
	}
	
	/**
	 * デフォルト鍵で AES-256-GCM により暗号化する。
	 *
	 * <p>12 バイトのランダム IV を生成し、暗号文の先頭に付加する。</p>
	 *
	 * @param word 暗号化する平文
	 * @return Base64 エンコードされた暗号文 (IV + ciphertext)、word が null の場合は null
	 * @throws IllegalStateException 鍵が設定されていない場合
	 */
	public static String encrypt(String word) {
		requireKey();
		return encryptAesGcm(cryptKey, word);
	}
	
	/**
	 * 指定鍵で AES-256-GCM により暗号化する。
	 *
	 * @param key 暗号鍵文字列
	 * @param word 暗号化する平文
	 * @return Base64 エンコードされた暗号文 (IV + ciphertext)
	 */
	private static String encryptAesGcm(String key, String word) {
		if (word == null) {
			return null;
		}
		
		SecretKeySpec sksSpec = deriveAesKey(key);
		
		// 12 バイトのランダム IV を生成
		byte[] iv = new byte[GCM_IV_LENGTH];
		secureRandom.nextBytes(iv);
		GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
		
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance(TRANSFORMATION_AES_GCM);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			m_log.warn("encrypt : " + (e.getClass().getName()) + "," + e.getMessage(), e);
			return null;
		}
		try {
			cipher.init(Cipher.ENCRYPT_MODE, sksSpec, gcmSpec);
		} catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
			m_log.warn("encrypt : " + (e.getClass().getName()) + "," + e.getMessage(), e);
			return null;
		}
		
		byte[] encrypted = null;
		try {
			encrypted = cipher.doFinal(word.getBytes());
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			m_log.warn("encrypt : " + (e.getClass().getName()) + "," + e.getMessage(), e);
			return null;
		}
		
		// IV + 暗号文を結合
		ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encrypted.length);
		byteBuffer.put(iv);
		byteBuffer.put(encrypted);
		
		return Base64.encodeBase64String(byteBuffer.array());
	}
	
	/**
	 * デフォルト鍵で復号する。
	 *
	 * <p>まず AES-256-GCM での復号を試み、失敗した場合は旧 Blowfish/ECB で
	 * 復号を試みるフォールバックを行う。旧方式で復号が成功した場合は
	 * WARNING ログを出力し、マイグレーション未完了を示唆する。</p>
	 *
	 * @param word Base64 エンコードされた暗号文
	 * @return 復号された平文、word が null の場合は null
	 * @throws IllegalStateException 鍵が設定されていない場合
	 */
	public static String decrypt(String word) {
		requireKey();
		return decrypt(cryptKey, word);
	}
	
	/**
	 * 指定鍵で復号する。AES-GCM を試み、失敗時に Blowfish フォールバック。
	 *
	 * @param key 暗号鍵文字列
	 * @param word Base64 エンコードされた暗号文
	 * @return 復号された平文
	 */
	public static String decrypt(String key, String word) {
		if (word == null) {
			return null;
		}
		
		// まず AES-GCM での復号を試みる
		String result = decryptAesGcm(key, word);
		if (result != null) {
			return result;
		}
		
		// AES-GCM 失敗時は旧 Blowfish で復号を試みる
		result = decryptBlowfish(key, word);
		if (result != null) {
			m_log.warn("decrypt : Legacy Blowfish decryption was used. "
					+ "This indicates data has not been migrated to AES-GCM. "
					+ "Please re-encrypt the data with the current algorithm.");
			return result;
		}
		
		m_log.warn("decrypt : Failed to decrypt with both AES-GCM and legacy Blowfish.");
		return null;
	}
	
	/**
	 * AES-256-GCM で復号する。
	 *
	 * @param key 暗号鍵文字列
	 * @param word Base64 エンコードされた暗号文 (IV + ciphertext)
	 * @return 復号された平文、失敗時は null
	 */
	private static String decryptAesGcm(String key, String word) {
		try {
			byte[] decoded = Base64.decodeBase64(word);
			
			if (decoded.length < GCM_IV_LENGTH) {
				return null;
			}
			
			// IV と暗号文を分離
			ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
			byte[] iv = new byte[GCM_IV_LENGTH];
			byteBuffer.get(iv);
			byte[] cipherText = new byte[byteBuffer.remaining()];
			byteBuffer.get(cipherText);
			
			SecretKeySpec sksSpec = deriveAesKey(key);
			GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
			
			Cipher cipher = Cipher.getInstance(TRANSFORMATION_AES_GCM);
			cipher.init(Cipher.DECRYPT_MODE, sksSpec, gcmSpec);
			byte[] decrypted = cipher.doFinal(cipherText);
			
			return new String(decrypted);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidKeyException | InvalidAlgorithmParameterException
				| IllegalBlockSizeException | BadPaddingException e) {
			// AES-GCM での復号失敗は想定内（旧データの可能性）
			m_log.debug("decryptAesGcm : AES-GCM decryption failed, "
					+ "will attempt legacy Blowfish. " + e.getMessage());
			return null;
		}
	}
	
	/**
	 * 旧 Blowfish/ECB アルゴリズムで復号する（後方互換用）。
	 *
	 * <p>既存の Blowfish で暗号化されたデータの復号用に残す。
	 * 新規暗号化には使用しないこと。</p>
	 *
	 * @param key 暗号鍵文字列
	 * @param word Base64 エンコードされた暗号文
	 * @return 復号された平文、失敗時は null
	 * @deprecated Blowfish/ECB は非推奨。新規暗号化には AES-GCM を使用すること。
	 */
	@Deprecated
	private static String decryptBlowfish(String key, String word) {
		try {
			byte[] encrypted = Base64.decodeBase64(word);
			SecretKeySpec sksSpec = new SecretKeySpec(key.getBytes(), ALGORITHM_BLOWFISH);
			Cipher cipher = Cipher.getInstance(ALGORITHM_BLOWFISH);
			cipher.init(Cipher.DECRYPT_MODE, sksSpec);
			byte[] decrypted = cipher.doFinal(encrypted);
			return new String(decrypted);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidKeyException
				| IllegalBlockSizeException | BadPaddingException e) {
			m_log.debug("decryptBlowfish : Legacy Blowfish decryption failed. " + e.getMessage());
			return null;
		}
	}
	
	/**
	 * バージョンアップツールから利用する
	 * @param args
	 */
	public static void main(String args[]) {
		if (args.length != 3) {
			System.out.println("usage CryptUtil encrypt <key> <word>");
			System.out.println("usage CryptUtil decrypt <key> <word>");
				System.exit(1);
		}
		String mode = args[0];
		String key = args[1];
		String word = args[2];
		if ("decrypt".equals(mode)) {
			System.out.println(decrypt(key, word));
		} else {
			System.out.println(encryptAesGcm(key, word));
		}
	}
}
