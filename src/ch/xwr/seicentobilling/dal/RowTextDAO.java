
package ch.xwr.seicentobilling.dal;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import com.xdev.dal.JPADAO;

import ch.xwr.seicentobilling.entities.RowObject;
import ch.xwr.seicentobilling.entities.RowText;
import ch.xwr.seicentobilling.entities.RowText_;

/**
 * Home object for domain model class RowText.
 *
 * @see RowText
 */
public class RowTextDAO extends JPADAO<RowText, Long> {
	public RowTextDAO() {
		super(RowText.class);
	}

	public RowText getText(final RowObject rowObj, final int numberTxt) {
		final List<RowText> li = findByObject(rowObj, numberTxt);
		if (li.size() == 0) {
			return null;
		}
		return li.get(0);

	}

	/**
	 * @queryMethod Do not edit, method is generated by editor!
	 */
	public List<RowText> findByObject(final RowObject rowObj, final int numberTxt) {
		final EntityManager entityManager = em();

		final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

		final ParameterExpression<RowObject> rowObjParameter = criteriaBuilder.parameter(RowObject.class, "rowObj");
		final ParameterExpression<Integer> numberTxtParameter = criteriaBuilder.parameter(Integer.class, "numberTxt");

		final CriteriaQuery<RowText> criteriaQuery = criteriaBuilder.createQuery(RowText.class);

		final Root<RowText> root = criteriaQuery.from(RowText.class);

		criteriaQuery.where(criteriaBuilder.and(criteriaBuilder.equal(root.get(RowText_.rowObject), rowObjParameter),
				criteriaBuilder.equal(root.get(RowText_.txtNumber), numberTxtParameter)));

		final TypedQuery<RowText> query = entityManager.createQuery(criteriaQuery);
		query.setParameter(rowObjParameter, rowObj);
		query.setParameter(numberTxtParameter, numberTxt);
		return query.getResultList();
	}
}