package com.secdec.codedx.security;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

/**
 * Represents an ordered list of {@link X509TrustManager}s with additive trust.
 * If any one of the composed managers trusts a certificate chain, then it is
 * trusted by the composite manager.
 * 
 * This is necessary because of the fine-print on {@link SSLContext#init}: Only
 * the first instance of a particular key and/or trust manager implementation
 * type in the array is used. (For example, only the first
 * javax.net.ssl.X509KeyManager in the array will be used.)
 * 
 * <a href=
 * "http://stackoverflow.com/questions/1793979/registering-multiple-keystores-in-jvm"
 * >see StackOverflow</a>
 * 
 * @author codyaray
 * @since 4/22/2013
 */
public class CompositeX509TrustManager implements X509TrustManager {

	private List<X509TrustManager> trustManagers = new LinkedList<X509TrustManager>();

	/**
	 * Initializes the composite trust manager, copying all of the non-null
	 * entries in the given <code>trustManagers</code> list into its own
	 * internal list.
	 * 
	 * @param trustManagers A list of (potentially null) trust managers.
	 */
	public CompositeX509TrustManager(X509TrustManager... trustManagers) {
		this.trustManagers = Arrays.asList(trustManagers);
	}

	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		CertificateException lastException = null;
		for (X509TrustManager trustManager : trustManagers) {
			try {
				trustManager.checkClientTrusted(chain, authType);
				return; // someone trusts them. success!
			} catch (CertificateException e) {
				lastException = e;
			}
		}
		if (lastException != null) {
			throw lastException;
		} else {
			throw new CertificateException("None of the TrustManagers trust this certificate chain");
		}
	}

	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		CertificateException lastException = null;
		for (X509TrustManager trustManager : trustManagers) {
			try {
				trustManager.checkServerTrusted(chain, authType);
				return; // someone trusts them. success!
			} catch (CertificateException e) {
				lastException = e;
			}
		}
		if (lastException != null) {
			throw lastException;
		} else {
			throw new CertificateException("None of the TrustManagers trust this certificate chain");
		}
	}

	public X509Certificate[] getAcceptedIssuers() {
		List<X509Certificate> certificates = new LinkedList<X509Certificate>();
		for (X509TrustManager trustManager : trustManagers) {
			for (X509Certificate cert : trustManager.getAcceptedIssuers()) {
				certificates.add(cert);
			}
		}
		return certificates.toArray(new X509Certificate[certificates.size()]);
	}

}