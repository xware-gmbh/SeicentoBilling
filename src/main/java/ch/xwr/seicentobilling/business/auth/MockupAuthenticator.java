
package ch.xwr.seicentobilling.business.auth;

import com.rapidclipse.framework.security.authentication.AuthenticationFailedException;
import com.rapidclipse.framework.security.authentication.Authenticator;
import com.rapidclipse.framework.security.authentication.CredentialsUsernamePassword;
import com.rapidclipse.framework.security.util.PasswordHasher;


public class MockupAuthenticator implements Authenticator<CredentialsUsernamePassword, CredentialsUsernamePassword>
{
	private final Class<? extends CredentialsUsernamePassword> authenticationEntityType;
	private PasswordHasher                                     hashStrategy = PasswordHasher.Sha2();

	/**
	 *
	 */
	public MockupAuthenticator(
		final Class<? extends CredentialsUsernamePassword> authenticationEntityType)
	{
		this.authenticationEntityType = authenticationEntityType;
	}

	public final CredentialsUsernamePassword authenticate(
		final String username,
		final String password)
		throws AuthenticationFailedException
	{
		return this.authenticate(CredentialsUsernamePassword.New(username, password.getBytes()));
	}

	@Override
	public CredentialsUsernamePassword authenticate(final CredentialsUsernamePassword credentials)
		throws AuthenticationFailedException
	{
		return this.checkCredentials(credentials);
	}

	protected CredentialsUsernamePassword checkCredentials(
		final CredentialsUsernamePassword credentials)
		throws AuthenticationFailedException
	{
		final CredentialsUsernamePassword pwd = new CredentialsUsernamePassword()
		{

			@Override
			public String username()
			{
				return credentials.username();
			}

			@Override
			public byte[] password()
			{
				return credentials.password();
			}
		};

		return pwd;
	}

	public PasswordHasher getHashStrategy()
	{
		return this.hashStrategy;
	}

	public void setHashStrategy(final PasswordHasher hashStrategy)
	{
		this.hashStrategy = hashStrategy;
	}

}
