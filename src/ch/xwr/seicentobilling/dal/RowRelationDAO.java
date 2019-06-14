
package ch.xwr.seicentobilling.dal;

import com.xdev.dal.JPADAO;
import java.lang.Long;
import ch.xwr.seicentobilling.entities.RowRelation;

/**
 * Home object for domain model class RowRelation.
 * 
 * @see RowRelation
 */
public class RowRelationDAO extends JPADAO<RowRelation, Long> {
	public RowRelationDAO() {
		super(RowRelation.class);
	}
}