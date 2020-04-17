
package ch.xwr.seicentobilling.business.auth;

import com.xdev.security.authentication.Authenticator;
import com.xdev.security.authentication.AuthenticatorProvider;
import com.xdev.security.authentication.CredentialsUsernamePassword;
import com.xdev.security.authentication.jpa.HashStrategy;
import com.xdev.security.authentication.jpa.JPAAuthenticator;

import ch.xwr.seicentobilling.entities.AppUser;

public class DbAuthenticationProvider
		implements AuthenticatorProvider<CredentialsUsernamePassword, CredentialsUsernamePassword> {
	private static DbAuthenticationProvider INSTANCE;

	public static DbAuthenticationProvider getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new DbAuthenticationProvider();
		}

		return INSTANCE;
	}

	private final HashStrategy hashStrategy = new HashStrategy.SHA2();
	private JPAAuthenticator authenticator;

	private DbAuthenticationProvider() {
	}

	@Override
	public Authenticator<CredentialsUsernamePassword, CredentialsUsernamePassword> provideAuthenticator() {
		if (this.authenticator == null) {
			this.authenticator = new JPAAuthenticator(AppUser.class);
			this.authenticator.setHashStrategy(getHashStrategy());
		}

		return this.authenticator;
	}

	public HashStrategy getHashStrategy() {
		return this.hashStrategy;
	}
}
