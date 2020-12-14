
package ch.xwr.seicentobilling.business.auth;

import com.rapidclipse.framework.security.authentication.Authenticator;
import com.rapidclipse.framework.security.authentication.AuthenticatorProvider;
import com.rapidclipse.framework.security.authentication.CredentialsUsernamePassword;
import com.rapidclipse.framework.security.util.PasswordHasher;
import com.rapidclipse.framework.server.security.authentication.jpa.JPAAuthenticator;

import ch.xwr.seicentobilling.entities.AppUser;


public class DbAuthenticationProvider
	implements AuthenticatorProvider<CredentialsUsernamePassword, CredentialsUsernamePassword>
{
	private static class InitializationOnDemandHolder
	{
		final static DbAuthenticationProvider INSTANCE = new DbAuthenticationProvider();
	}
	
	public static DbAuthenticationProvider getInstance()
	{
		return InitializationOnDemandHolder.INSTANCE;
	}
	
	private final PasswordHasher passwordHasher = PasswordHasher.Sha2();
	private JPAAuthenticator     authenticator;
	
	private DbAuthenticationProvider()
	{
	}
	
	@Override
	public Authenticator<CredentialsUsernamePassword, CredentialsUsernamePassword> provideAuthenticator()
	{
		if(this.authenticator == null)
		{
			this.authenticator = new JPAAuthenticator(AppUser.class);
			this.authenticator.setPasswordHasher(this.getPasswordHasher());
		}
		
		return this.authenticator;
	}
	
	public PasswordHasher getPasswordHasher()
	{
		return this.passwordHasher;
	}
}
