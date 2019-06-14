
package ch.xwr.seicentobilling.business;

import com.xdev.security.authentication.Authenticator;
import com.xdev.security.authentication.AuthenticatorProvider;
import com.xdev.security.authentication.CredentialsUsernamePassword;
import com.xdev.security.authentication.jpa.HashStrategy.SHA2;

public class MyAuthenticationProvider
		implements AuthenticatorProvider<CredentialsUsernamePassword, CredentialsUsernamePassword> {
	private static MyAuthenticationProvider INSTANCE;

	public static MyAuthenticationProvider getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new MyAuthenticationProvider();
		}

		return INSTANCE;
	}

	private MockupAuthenticator authenticator;

	private MyAuthenticationProvider() {
	}

	@Override
	public Authenticator<CredentialsUsernamePassword, CredentialsUsernamePassword> provideAuthenticator() {
		if (this.authenticator == null) {
			final Class<? extends CredentialsUsernamePassword> authenticationEntityType = CredentialsUsernamePassword.class;
			this.authenticator = new MockupAuthenticator(authenticationEntityType);
			this.authenticator.setHashStrategy(new SHA2());
		}

		return this.authenticator;
	}
}
