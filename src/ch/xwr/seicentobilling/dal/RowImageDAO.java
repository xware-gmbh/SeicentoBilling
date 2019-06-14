
package ch.xwr.seicentobilling.dal;

import com.xdev.dal.JPADAO;
import java.lang.Long;
import ch.xwr.seicentobilling.entities.RowImage;

/**
 * Home object for domain model class RowImage.
 * 
 * @see RowImage
 */
public class RowImageDAO extends JPADAO<RowImage, Long> {
	public RowImageDAO() {
		super(RowImage.class);
	}
}