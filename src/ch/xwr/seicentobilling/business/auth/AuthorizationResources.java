
package ch.xwr.seicentobilling.business.auth;

import com.xdev.security.authorization.Resource;
import com.xdev.security.authorization.ResourceEnum;
import com.xdev.security.authorization.ui.Authorization;

/**
 * Central collection of all authorization resources used in the project.
 */
public enum AuthorizationResources implements ResourceEnum<AuthorizationResources> {
	;

	/**
	 * Helper method to export all resource names.
	 * <p>
	 * Right click and select 'Run As' - 'Java Application'
	 * </p>
	 */
	public static void main(String[] args) {
		for (AuthorizationResources value : AuthorizationResources.values()) {
			System.out.println(value.name);
		}
	}

	/////////////////////////////
	// implementation details //
	///////////////////////////

	private final String name;
	private Resource resource;

	private AuthorizationResources(final String name) {
		this.name = name;
	}

	@Override
	public String resourceName() {
		return this.name;
	}

	@Override
	public Resource resource() {
		if (this.resource == null) {
			this.resource = Authorization.resource(this.name);
		}

		return this.resource;
	}
}
