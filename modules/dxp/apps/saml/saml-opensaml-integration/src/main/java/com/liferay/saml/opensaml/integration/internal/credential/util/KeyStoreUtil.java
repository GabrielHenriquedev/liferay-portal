/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.saml.opensaml.integration.internal.credential.util;

import com.liferay.osgi.util.service.Snapshot;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.saml.runtime.credential.KeyStoreManager;
import com.liferay.saml.runtime.exception.CredentialAuthException;

import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.UnrecoverableKeyException;

import org.opensaml.security.credential.UsageType;

/**
 * @author Gabriel Santos
 */
public class KeyStoreUtil {

	public static String getAlias(String entityId, UsageType usageType) {
		if (usageType.equals(UsageType.SIGNING)) {
			return entityId;
		}
		else if (usageType.equals(UsageType.ENCRYPTION)) {
			return entityId + "-encryption";
		}

		return entityId;
	}

	public static <T> T getCauseThrowable(
		Throwable throwable, Class<T> exceptionClass) {

		if (throwable == null) {
			return null;
		}

		Throwable causeThrowable = throwable.getCause();

		while (causeThrowable != null) {
			if (exceptionClass.isInstance(causeThrowable)) {
				return (T)causeThrowable;
			}

			causeThrowable = causeThrowable.getCause();
		}

		return null;
	}

	public static KeyStore.Entry getKeyStoreEntry(
			String alias, String certificateKeyPassword)
		throws CredentialAuthException {

		KeyStore.PasswordProtection keyStorePasswordProtection = null;

		if (certificateKeyPassword != null) {
			keyStorePasswordProtection = new KeyStore.PasswordProtection(
				certificateKeyPassword.toCharArray());
		}

		KeyStoreManager keyStoreManager = _keyStoreManagerSnapshot.get();

		try {
			KeyStore keyStore = keyStoreManager.getKeyStore();

			return keyStore.getEntry(alias, keyStorePasswordProtection);
		}
		catch (GeneralSecurityException generalSecurityException) {
			Class<? extends KeyStoreManager> clazz = keyStoreManager.getClass();
			long companyId = CompanyThreadLocal.getCompanyId();

			if (generalSecurityException instanceof KeyStoreException) {
				UnrecoverableKeyException unrecoverableKeyException =
					getCauseThrowable(
						generalSecurityException,
						UnrecoverableKeyException.class);

				if (unrecoverableKeyException != null) {
					throw new CredentialAuthException.InvalidKeyStorePassword(
						String.format(
							"Company %s used an incorrect password to access " +
								"the key store provided by %s",
							companyId, clazz.getSimpleName()),
						unrecoverableKeyException);
				}

				throw new CredentialAuthException.InvalidKeyStore(
					String.format(
						"Company %s could not load the SAML key store " +
							"provided by %s",
						companyId, clazz.getSimpleName()),
					generalSecurityException);
			}

			if (generalSecurityException instanceof UnrecoverableKeyException) {
				throw new CredentialAuthException.InvalidCredentialPassword(
					String.format(
						"Company %s used an incorrect key credential " +
							"password to an entry in the SAML key store " +
								"provided by %s",
						companyId, clazz.getSimpleName()),
					(UnrecoverableKeyException)generalSecurityException);
			}

			throw new CredentialAuthException.GeneralCredentialAuthException(
				String.format(
					"Unknown exception thrown for company %s using %s",
					companyId, clazz.getSimpleName()),
				generalSecurityException);
		}
	}

	private static final Snapshot<KeyStoreManager> _keyStoreManagerSnapshot =
		new Snapshot<>(
			KeyStoreUtil.class, KeyStoreManager.class, "(default=true)");

}