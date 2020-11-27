
package ch.xwr.seicentobilling.dal;

import com.rapidclipse.framework.server.jpa.dal.JpaDataAccessObject;
import java.lang.Long;
import ch.xwr.seicentobilling.entities.StateCode;

/**
 * Home object for domain model class StateCode.
 * 
 * @see StateCode
 */
public class StateCodeDAO extends JpaDataAccessObject.Default<StateCode, Long> {
	public StateCodeDAO() {
		super(StateCode.class);
	}
}