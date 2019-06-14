
package ch.xwr.seicentobilling.dal;

import ch.xwr.seicentobilling.entities.Communication;
import com.xdev.dal.JPADAO;
import java.lang.Long;

/**
 * Home object for domain model class Communication.
 * 
 * @see Communication
 */
public class CommunicationDAO extends JPADAO<Communication, Long> {
	public CommunicationDAO() {
		super(Communication.class);
	}
}