
package ch.xwr.seicentobilling.dal;

import ch.xwr.seicentobilling.entities.RowLabel;
import com.rapidclipse.framework.server.jpa.dal.JpaDataAccessObject;
import java.lang.Long;

/**
 * Home object for domain model class RowLabel.
 * 
 * @see RowLabel
 */
public class RowLabelDAO extends JpaDataAccessObject.Default<RowLabel, Long> {
	public RowLabelDAO() {
		super(RowLabel.class);
	}
}