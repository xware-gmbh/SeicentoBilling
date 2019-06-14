
package ch.xwr.seicentobilling.dal;

import com.xdev.dal.JPADAO;
import java.lang.Long;
import ch.xwr.seicentobilling.entities.StateCode;

/**
 * Home object for domain model class StateCode.
 * 
 * @see StateCode
 */
public class StateCodeDAO extends JPADAO<StateCode, Long> {
	public StateCodeDAO() {
		super(StateCode.class);
	}
}