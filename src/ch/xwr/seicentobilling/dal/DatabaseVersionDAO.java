
package ch.xwr.seicentobilling.dal;

import ch.xwr.seicentobilling.entities.DatabaseVersion;
import com.xdev.dal.JPADAO;
import java.lang.Long;

/**
 * Home object for domain model class DatabaseVersion.
 * 
 * @see DatabaseVersion
 */
public class DatabaseVersionDAO extends JPADAO<DatabaseVersion, Long> {
	public DatabaseVersionDAO() {
		super(DatabaseVersion.class);
	}
}