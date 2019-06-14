
package ch.xwr.seicentobilling.dal;

import ch.xwr.seicentobilling.entities.PaymentCondition;
import com.xdev.dal.JPADAO;
import java.lang.Long;

/**
 * Home object for domain model class PaymentCondition.
 * 
 * @see PaymentCondition
 */
public class PaymentConditionDAO extends JPADAO<PaymentCondition, Long> {
	public PaymentConditionDAO() {
		super(PaymentCondition.class);
	}
}