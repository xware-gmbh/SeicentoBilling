
package ch.xwr.seicentobilling.dal;

import com.rapidclipse.framework.server.jpa.dal.JpaDataAccessObject;
import java.lang.Long;
import ch.xwr.seicentobilling.entities.RowRelation;

/**
 * Home object for domain model class RowRelation.
 * 
 * @see RowRelation
 */
public class RowRelationDAO extends JpaDataAccessObject.Default<RowRelation, Long> {
	public RowRelationDAO() {
		super(RowRelation.class);
	}
}