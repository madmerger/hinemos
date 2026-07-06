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
import java.nio.charset.StandardCharsets;
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

public class CryptUtil {
	
	private static final Log m_log = LogFactory.getLog( CryptUtil.class );
	
	// AES/GCM/NoPadding (AEAD)
	private static final String ALGORITHM = "AES";
	private static final String TRANSFORMATION = "AES/GCM/NoPadding";
	private static final int GCM_IV_LENGTH_BYTES = 12;   // 96ビットIV
	private static final int GCM_TAG_LENGTH_BITS = 128;   // 128ビット認証タグ
	private static final int AES_KEY_LENGTH_BYTES = 32;    // AES-256

	// AES/GCM暗号文のプレフィックス（レガシーBlowfish暗号文との識別用）
	private static final String AES_PREFIX = "AES:";

	// レガシーBlowfish復号用（後方互換性）
	private static final String LEGACY_ALGORITHM = "BLOWFISH";

	private static final String cryptKey;
	
	private static final SecureRandom secureRandom = new SecureRandom();

	static {
		String etcdir = System.getProperty("hinemos.manager.etc.dir");
		String keyFile = "db_crypt.key";

		if (etcdir == null) {
			// CLIツール（mainメソッド）から呼ばれた場合は鍵ファイルを使用しない
			cryptKey = null;
		} else {
			String keyPath = etcdir + File.separator + keyFile;
			FileReader fileReader = null;
			BufferedReader bufferedReader = null;
			String loadedKey = null;

			try {
				fileReader = new FileReader(keyPath);
				bufferedReader = new BufferedReader(fileReader);
				loadedKey = bufferedReader.readLine();
			} catch (Exception e){
				m_log.error("encryption key file not readable. (" + keyFile + ") : " + e.getMessage()
						+ " - CryptUtil requires a valid key file for secure operation.", e);
				throw new RuntimeException("CryptUtil initialization failed: encryption key file ("
						+ keyFile + ") is not readable. "
						+ "Please ensure the key file exists at: " + keyPath, e);
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

			if (loadedKey == null || loadedKey.isEmpty()) {
				throw new RuntimeException("CryptUtil initialization failed: encryption key file ("
						+ keyFile + ") is empty. "
						+ "Please provide a valid encryption key in: " + keyPath);
			}
			cryptKey = loadedKey;
		}
	}
	
	/**
	 * AES-256鍵を導出する（鍵文字列のSHA-256ハッシュ）
	 */
	private static SecretKeySpec deriveAesKey(String key) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] keyBytes = digest.digest(key.getBytes(StandardCharsets.UTF_8));
			return new SecretKeySpec(keyBytes, 0, AES_KEY_LENGTH_BYTES, ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("SHA-256 not available", e);
		}
	}

	public static String encrypt(String word) {
		return encrypt(cryptKey, word);
	}
	
	private static String encrypt(String key, String word) {
		if (word == null) {
			return null;
		}
		
		try {
			SecretKeySpec keySpec = deriveAesKey(key);

			byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
			secureRandom.nextBytes(iv);

			GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

			byte[] encrypted = cipher.doFinal(word.getBytes(StandardCharsets.UTF_8));

			// IV + 暗号文（認証タグ含む）を連結
			byte[] ivAndCiphertext = new byte[iv.length + encrypted.length];
			System.arraycopy(iv, 0, ivAndCiphertext, 0, iv.length);
			System.arraycopy(encrypted, 0, ivAndCiphertext, iv.length, encrypted.length);

			return AES_PREFIX + Base64.encodeBase64String(ivAndCiphertext);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidKeyException | InvalidAlgorithmParameterException
				| IllegalBlockSizeException | BadPaddingException e) {
			m_log.warn("encrypt : " + e.getClass().getName() + "," + e.getMessage(), e);
			return null;
		}
	}

	public static String decrypt(String word) {
		return decrypt(cryptKey, word);
	}
	
	public static String decrypt(String key, String word) {
		if (word == null) {
			return null;
		}
		
		if (word.startsWith(AES_PREFIX)) {
			return decryptAesGcm(key, word.substring(AES_PREFIX.length()));
		}

		// レガシーBlowfish/ECB暗号文の後方互換復号
		return decryptLegacyBlowfish(key, word);
	}

	/**
	 * AES/GCM/NoPaddingで復号する
	 */
	private static String decryptAesGcm(String key, String base64Data) {
		try {
			byte[] ivAndCiphertext = Base64.decodeBase64(base64Data);

			if (ivAndCiphertext.length < GCM_IV_LENGTH_BYTES) {
				m_log.warn("decryptAesGcm : data too short");
				return null;
			}

			byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
			System.arraycopy(ivAndCiphertext, 0, iv, 0, GCM_IV_LENGTH_BYTES);

			byte[] ciphertext = new byte[ivAndCiphertext.length - GCM_IV_LENGTH_BYTES];
			System.arraycopy(ivAndCiphertext, GCM_IV_LENGTH_BYTES, ciphertext, 0, ciphertext.length);

			SecretKeySpec keySpec = deriveAesKey(key);
			GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

			byte[] decrypted = cipher.doFinal(ciphertext);
			return new String(decrypted, StandardCharsets.UTF_8);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidKeyException | InvalidAlgorithmParameterException
				| IllegalBlockSizeException | BadPaddingException e) {
			m_log.warn("decryptAesGcm : " + e.getClass().getName() + "," + e.getMessage(), e);
			return null;
		}
	}

	/**
	 * レガシーBlowfish/ECBで復号する（既存データの後方互換性用）
	 */
	private static String decryptLegacyBlowfish(String key, String word) {
		try {
			byte[] encrypted = Base64.decodeBase64(word);
			SecretKeySpec sksSpec = new SecretKeySpec(key.getBytes(), LEGACY_ALGORITHM);
			Cipher cipher = Cipher.getInstance(LEGACY_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, sksSpec);

			byte[] decrypted = cipher.doFinal(encrypted);
			return new String(decrypted);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidKeyException
				| IllegalBlockSizeException | BadPaddingException e) {
			m_log.warn("decryptLegacyBlowfish : " + e.getClass().getName() + "," + e.getMessage(), e);
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
			System.out.println(encrypt(key, word));
		}
	}
}
