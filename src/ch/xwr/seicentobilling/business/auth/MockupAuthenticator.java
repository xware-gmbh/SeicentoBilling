package ch.xwr.seicentobilling.business.auth;

import com.xdev.security.authentication.AuthenticationFailedException;
import com.xdev.security.authentication.Authenticator;
import com.xdev.security.authentication.CredentialsUsernamePassword;
import com.xdev.security.authentication.jpa.HashStrategy;

public class MockupAuthenticator implements Authenticator<CredentialsUsernamePassword, CredentialsUsernamePassword>
 {
	private final Class<? extends CredentialsUsernamePassword>	authenticationEntityType;
	private HashStrategy	hashStrategy	= new HashStrategy.SHA2();

	/**
	 *
	 */
	public MockupAuthenticator (
			final Class<? extends CredentialsUsernamePassword> authenticationEntityType)
	{
		this.authenticationEntityType = authenticationEntityType;
	}


	public final CredentialsUsernamePassword authenticate(final String username,
			final String password) throws AuthenticationFailedException
	{
		return this.authenticate(CredentialsUsernamePassword.New(username,password.getBytes()));
	}


	@Override
	public CredentialsUsernamePassword authenticate(final CredentialsUsernamePassword credentials)
			throws AuthenticationFailedException
	{
		return checkCredentials(credentials);
	}

	protected CredentialsUsernamePassword checkCredentials(
			final CredentialsUsernamePassword credentials) throws AuthenticationFailedException
	{
		final CredentialsUsernamePassword pwd = new CredentialsUsernamePassword() {

			@Override
			public String username() {
				return credentials.username();
			}

			@Override
			public byte[] password() {
				return credentials.password();
			}
		};

		return pwd;
	}

	public HashStrategy getHashStrategy()
	{
		return this.hashStrategy;
	}


	public void setHashStrategy(final HashStrategy hashStrategy)
	{
		this.hashStrategy = hashStrategy;
	}


}
