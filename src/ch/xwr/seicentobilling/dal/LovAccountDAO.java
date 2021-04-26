
package ch.xwr.seicentobilling.dal;

import java.util.ArrayList;
import java.util.List;

import com.xdev.dal.JPADAO;

import ch.xwr.seicentobilling.entities.LovAccount;

/**
 * Dummyklasse für combobox....
 *
 *
 */
public class LovAccountDAO extends JPADAO<LovAccount, String> {
	public LovAccountDAO() {
		super(LovAccount.class);
	}

	public List<LovAccount> findAllMine() {
		final List<LovAccount> lst = new ArrayList<>();
		lst.add(new LovAccount("a.Spesen", "Spesen"));
		lst.add(new LovAccount("a.Weiterbildung", "Weiterbildung"));
		lst.add(new LovAccount("a.Büroaufwand", "Büroaufwand"));
		lst.add(new LovAccount("a.Reisespesen", "Reisespesen"));
		lst.add(new LovAccount("a.Repräsentation", "Repräsentation"));
		lst.add(new LovAccount("a.Werbung / Marketing", "Werbung u. Marketing"));
		lst.add(new LovAccount("a.Mietaufwand", "Miete"));
		lst.add(new LovAccount("a.EDV Unterhalt", "IT Unterhalt"));
		lst.add(new LovAccount("a.Drittleistungen", "Drittleistungen"));

		return lst;
	}
}