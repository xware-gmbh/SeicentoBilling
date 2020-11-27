
package ch.xwr.seicentobilling.dal;

import com.rapidclipse.framework.server.jpa.dal.JpaDataAccessObject;
import java.lang.Long;
import ch.xwr.seicentobilling.entities.Bank;

/**
 * Home object for domain model class Bank.
 * 
 * @see Bank
 */
public class BankDAO extends JpaDataAccessObject.Default<Bank, Long> {
	public BankDAO() {
		super(Bank.class);
	}
}