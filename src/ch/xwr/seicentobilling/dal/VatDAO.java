
package ch.xwr.seicentobilling.dal;

import com.xdev.dal.JPADAO;
import java.lang.Long;
import ch.xwr.seicentobilling.entities.Vat;

/**
 * Home object for domain model class Vat.
 * 
 * @see Vat
 */
public class VatDAO extends JPADAO<Vat, Long> {
	public VatDAO() {
		super(Vat.class);
	}
}