
package ch.xwr.seicentobilling;

import ch.xwr.seicentobilling.entities.AppUser;
import com.rapidclipse.framework.security.authentication.Authenticator;
import com.rapidclipse.framework.security.authentication.AuthenticatorProvider;
import com.rapidclipse.framework.security.authentication.CredentialsUsernamePassword;
import com.rapidclipse.framework.security.util.PasswordHasher;
import com.rapidclipse.framework.server.security.authentication.jpa.JPAAuthenticator;


public class MyAuthenticationProvider
	implements AuthenticatorProvider<CredentialsUsernamePassword, CredentialsUsernamePassword>
{
	private static class InitializationOnDemandHolder
	{
		final static MyAuthenticationProvider INSTANCE = new MyAuthenticationProvider();
	}
	
	public static MyAuthenticationProvider getInstance()
	{
		return InitializationOnDemandHolder.INSTANCE;
	}
	
	private final PasswordHasher passwordHasher = PasswordHasher.Sha2();
	private JPAAuthenticator     authenticator;
	
	private MyAuthenticationProvider()
	{
	}
	
	@Override
	public Authenticator<CredentialsUsernamePassword, CredentialsUsernamePassword> provideAuthenticator()
	{
		if(this.authenticator == null)
		{
			this.authenticator = new JPAAuthenticator(AppUser.class);
			this.authenticator.setPasswordHasher(getPasswordHasher());
		}
		
		return this.authenticator;
	}
	
	public PasswordHasher getPasswordHasher()
	{
		return this.passwordHasher;
	}
}
