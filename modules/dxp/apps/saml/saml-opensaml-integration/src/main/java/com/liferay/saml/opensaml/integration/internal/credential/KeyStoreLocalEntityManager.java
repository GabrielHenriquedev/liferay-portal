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

package com.liferay.saml.opensaml.integration.internal.credential;

import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.saml.opensaml.integration.internal.credential.util.KeyStoreUtil;
import com.liferay.saml.persistence.model.SamlSpIdpConnection;
import com.liferay.saml.persistence.service.SamlSpIdpConnectionLocalService;
import com.liferay.saml.runtime.SamlException;
import com.liferay.saml.runtime.configuration.SamlProviderConfiguration;
import com.liferay.saml.runtime.configuration.SamlProviderConfigurationHelper;
import com.liferay.saml.runtime.credential.KeyStoreManager;
import com.liferay.saml.runtime.exception.CredentialAuthException;
import com.liferay.saml.runtime.exception.CredentialException;
import com.liferay.saml.runtime.exception.EntityIdException;
import com.liferay.saml.runtime.metadata.LocalEntityManager;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import java.util.List;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;

import org.apache.xml.security.utils.Base64;

import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.security.credential.CredentialResolver;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.criteria.UsageCriterion;
import org.opensaml.security.x509.X509Credential;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Gabriel Santos
 */
@Component(service = LocalEntityManager.class)
public class KeyStoreLocalEntityManager implements LocalEntityManager {

	@Override
	public void authenticateLocalEntityCertificate(
			String certificateKeyPassword, CertificateUsage certificateUsage,
			String entityId)
		throws CredentialAuthException, CredentialException {

		KeyStore.Entry entry = null;

		if (certificateUsage == CertificateUsage.ENCRYPTION) {
			entry = KeyStoreUtil.getKeyStoreEntry(
				KeyStoreUtil.getAlias(entityId, UsageType.ENCRYPTION),
				certificateKeyPassword);
		}
		else {
			entry = KeyStoreUtil.getKeyStoreEntry(
				KeyStoreUtil.getAlias(entityId, UsageType.SIGNING),
				certificateKeyPassword);
		}

		if (entry == null) {
			throw new CredentialException("Certificate not found");
		}
	}

	@Override
	public void deleteLocalEntityCertificate(CertificateUsage certificateUsage)
		throws KeyStoreException {

		KeyStore keyStore = _keyStoreManager.getKeyStore();

		keyStore.deleteEntry(
			KeyStoreUtil.getAlias(
				getLocalEntityId(), _getUsageType(certificateUsage)));

		try {
			_keyStoreManager.saveKeyStore(keyStore);
		}
		catch (Exception exception) {
			throw new KeyStoreException(exception);
		}
	}

	@Override
	public String getEncodedLocalEntityCertificate(
			CertificateUsage certificateUsage)
		throws SamlException {

		try {
			X509Certificate x509Certificate = getLocalEntityCertificate(
				certificateUsage);

			if (x509Certificate == null) {
				return null;
			}

			return Base64.encode(x509Certificate.getEncoded(), 76);
		}
		catch (CertificateEncodingException certificateEncodingException) {
			throw new SamlException(certificateEncodingException);
		}
	}

	@Override
	public X509Certificate getLocalEntityCertificate(
			CertificateUsage certificateUsage)
		throws SamlException {

		UsageType usageType = _getUsageType(certificateUsage);

		if (usageType == null) {
			return null;
		}

		String entityId = getLocalEntityId();

		if (Validator.isBlank(entityId)) {
			throw new SamlException(
				new EntityIdException("An Entity ID must be configured"));
		}

		UsageCriterion usageCriterion = new UsageCriterion(usageType);

		try {
			X509Credential x509Credential =
				(X509Credential)_credentialResolver.resolveSingle(
					new CriteriaSet(
						new EntityIdCriterion(entityId), usageCriterion));

			if (x509Credential == null) {
				return null;
			}

			return x509Credential.getEntityCertificate();
		}
		catch (ResolverException resolverException) {
			throw new SamlException(resolverException);
		}
	}

	@Override
	public String getLocalEntityId() {
		return _getSamlProviderConfiguration().entityId();
	}

	@Override
	public boolean hasDefaultIdpRole() {
		List<SamlSpIdpConnection> samlSpIdpConnections =
			_samlSpIdpConnectionLocalService.getSamlSpIdpConnections(
				CompanyThreadLocal.getCompanyId());

		if (samlSpIdpConnections.isEmpty()) {
			return false;
		}

		return true;
	}

	@Override
	public void storeLocalEntityCertificate(
			PrivateKey privateKey, String certificateKeyPassword,
			X509Certificate x509Certificate, CertificateUsage certificateUsage)
		throws Exception {

		KeyStore keyStore = _keyStoreManager.getKeyStore();

		keyStore.setEntry(
			KeyStoreUtil.getAlias(
				getLocalEntityId(), _getUsageType(certificateUsage)),
			new KeyStore.PrivateKeyEntry(
				privateKey, new Certificate[] {x509Certificate}),
			new KeyStore.PasswordProtection(
				certificateKeyPassword.toCharArray()));

		_keyStoreManager.saveKeyStore(keyStore);
	}

	private SamlProviderConfiguration _getSamlProviderConfiguration() {
		return _samlProviderConfigurationHelper.getSamlProviderConfiguration();
	}

	private UsageType _getUsageType(CertificateUsage certificateUsage) {
		UsageType usageType = null;

		if (certificateUsage == CertificateUsage.ENCRYPTION) {
			usageType = UsageType.ENCRYPTION;
		}
		else if (certificateUsage == CertificateUsage.SIGNING) {
			usageType = UsageType.SIGNING;
		}

		return usageType;
	}

	@Reference
	private CredentialResolver _credentialResolver;

	@Reference(name = "KeyStoreManager", target = "(default=true)")
	private KeyStoreManager _keyStoreManager;

	@Reference
	private SamlProviderConfigurationHelper _samlProviderConfigurationHelper;

	@Reference
	private SamlSpIdpConnectionLocalService _samlSpIdpConnectionLocalService;

}