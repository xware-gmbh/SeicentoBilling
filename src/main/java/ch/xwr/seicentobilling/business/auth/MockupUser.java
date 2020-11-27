
package ch.xwr.seicentobilling.business.auth;

import java.io.Serializable;
import java.util.Set;

import com.rapidclipse.framework.security.authorization.Role;
import com.rapidclipse.framework.security.authorization.Subject;


public class MockupUser implements Subject, Serializable
{
	private static final long serialVersionUID = 7932693413085944575L;
	private final String      name;
	
	public MockupUser(final String name)
	{
		this.name = name;
		
	}
	
	@Override
	public String name()
	{
		return this.name;
	}
	
	@Override
	public Set<Role> roles()
	{
		return null;
	}
	
}
