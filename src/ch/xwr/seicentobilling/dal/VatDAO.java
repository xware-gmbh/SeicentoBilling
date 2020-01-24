
package ch.xwr.seicentobilling.dal;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.xdev.dal.JPADAO;

import ch.xwr.seicentobilling.entities.Vat;
import ch.xwr.seicentobilling.entities.Vat_;

/**
 * Home object for domain model class Vat.
 *
 * @see Vat
 */
public class VatDAO extends JPADAO<Vat, Long> {
	public VatDAO() {
		super(Vat.class);
	}

	/**
	 * @queryMethod Do not edit, method is generated by editor!
	 */
	public List<Vat> findAllInclusive() {
		final EntityManager entityManager = em();

		final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

		final CriteriaQuery<Vat> criteriaQuery = criteriaBuilder.createQuery(Vat.class);

		final Root<Vat> root = criteriaQuery.from(Vat.class);

		criteriaQuery
				.where(criteriaBuilder.and(criteriaBuilder.equal(root.get(Vat_.vatInclude), criteriaBuilder.literal(true)),
						criteriaBuilder.equal(root.get(Vat_.vatState), criteriaBuilder.literal(1))));

		final TypedQuery<Vat> query = entityManager.createQuery(criteriaQuery);
		return query.getResultList();
	}
}