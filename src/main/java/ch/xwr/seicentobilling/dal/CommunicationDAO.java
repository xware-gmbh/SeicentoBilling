
package ch.xwr.seicentobilling.dal;

import ch.xwr.seicentobilling.entities.Communication;
import com.rapidclipse.framework.server.jpa.dal.JpaDataAccessObject;
import java.lang.Long;

/**
 * Home object for domain model class Communication.
 * 
 * @see Communication
 */
public class CommunicationDAO extends JpaDataAccessObject.Default<Communication, Long> {
	public CommunicationDAO() {
		super(Communication.class);
	}
}