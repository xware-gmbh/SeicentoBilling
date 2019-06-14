
package ch.xwr.seicentobilling.dal;

import com.xdev.dal.JPADAO;
import java.lang.Long;
import ch.xwr.seicentobilling.entities.Bank;

/**
 * Home object for domain model class Bank.
 * 
 * @see Bank
 */
public class BankDAO extends JPADAO<Bank, Long> {
	public BankDAO() {
		super(Bank.class);
	}
}