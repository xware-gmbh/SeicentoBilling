
package ch.xwr.seicentobilling.dal;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import com.xdev.dal.JPADAO;

import ch.xwr.seicentobilling.entities.Vat;
import ch.xwr.seicentobilling.entities.VatLine;
import ch.xwr.seicentobilling.entities.VatLine_;

/**
 * Home object for domain model class Vat.
 *
 * @see Vat
 */
public class VatLineDAO extends JPADAO<VatLine, Long> {
	public VatLineDAO() {
		super(VatLine.class);
	}

	/**
	 * @queryMethod Do not edit, method is generated by editor!
	 */
	public List<VatLine> findByVat(final Vat vat) {
		final EntityManager entityManager = em();

		final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

		final ParameterExpression<Vat> vatParameter = criteriaBuilder.parameter(Vat.class, "vat");

		final CriteriaQuery<VatLine> criteriaQuery = criteriaBuilder.createQuery(VatLine.class);

		final Root<VatLine> root = criteriaQuery.from(VatLine.class);

		criteriaQuery.where(criteriaBuilder.equal(root.get(VatLine_.vat), vatParameter));

		criteriaQuery.orderBy(criteriaBuilder.desc(root.get(VatLine_.vanValidFrom)));

		final TypedQuery<VatLine> query = entityManager.createQuery(criteriaQuery);
		query.setParameter(vatParameter, vat);
		return query.getResultList();
	}

	/**
	 * @queryMethod Do not edit, method is generated by editor!
	 */
	public List<VatLine> findByVatAndDate(final Vat vat, final Date refDate) {
		final EntityManager entityManager = em();

		final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

		final ParameterExpression<Vat> vatParameter = criteriaBuilder.parameter(Vat.class, "vat");
		final ParameterExpression<Date> refDateParameter = criteriaBuilder.parameter(Date.class, "refDate");

		final CriteriaQuery<VatLine> criteriaQuery = criteriaBuilder.createQuery(VatLine.class);

		final Root<VatLine> root = criteriaQuery.from(VatLine.class);

		criteriaQuery.where(criteriaBuilder.and(
				criteriaBuilder.and(criteriaBuilder.equal(root.get(VatLine_.vat), vatParameter),
						criteriaBuilder.lessThanOrEqualTo(root.get(VatLine_.vanValidFrom), refDateParameter)),
				criteriaBuilder.equal(root.get(VatLine_.vanState), criteriaBuilder.literal(1))));

		criteriaQuery.orderBy(criteriaBuilder.desc(root.get(VatLine_.vanValidFrom)));

		final TypedQuery<VatLine> query = entityManager.createQuery(criteriaQuery);
		query.setParameter(vatParameter, vat);
		query.setParameter(refDateParameter, refDate);
		return query.getResultList();
	}
}