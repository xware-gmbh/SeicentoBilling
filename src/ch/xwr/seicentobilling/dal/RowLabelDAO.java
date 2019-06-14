
package ch.xwr.seicentobilling.dal;

import ch.xwr.seicentobilling.entities.RowLabel;
import com.xdev.dal.JPADAO;
import java.lang.Long;

/**
 * Home object for domain model class RowLabel.
 * 
 * @see RowLabel
 */
public class RowLabelDAO extends JPADAO<RowLabel, Long> {
	public RowLabelDAO() {
		super(RowLabel.class);
	}
}