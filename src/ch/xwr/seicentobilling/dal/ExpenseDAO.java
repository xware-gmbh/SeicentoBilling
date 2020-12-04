
package ch.xwr.seicentobilling.dal;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import com.xdev.dal.JPADAO;

import ch.xwr.seicentobilling.entities.Expense;
import ch.xwr.seicentobilling.entities.Expense_;
import ch.xwr.seicentobilling.entities.Periode;
import ch.xwr.seicentobilling.entities.Project;

/**
 * Home object for domain model class Expense.
 *
 * @see Expense
 */
public class ExpenseDAO extends JPADAO<Expense, Long> {
	public ExpenseDAO() {
		super(Expense.class);
	}

	/**
	 * @queryMethod Do not edit, method is generated by editor!
	 */
	public List<Expense> findByPeriode(final Periode dao) {
		final EntityManager entityManager = em();

		final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

		final ParameterExpression<Periode> daoParameter = criteriaBuilder.parameter(Periode.class, "dao");

		final CriteriaQuery<Expense> criteriaQuery = criteriaBuilder.createQuery(Expense.class);

		final Root<Expense> root = criteriaQuery.from(Expense.class);

		criteriaQuery.where(criteriaBuilder.equal(root.get(Expense_.periode), daoParameter));

		criteriaQuery.orderBy(criteriaBuilder.desc(root.get(Expense_.expDate)),
				criteriaBuilder.desc(root.get(Expense_.expFlagGeneric)));

		final TypedQuery<Expense> query = entityManager.createQuery(criteriaQuery);
		query.setParameter(daoParameter, dao);
		return query.getResultList();
	}

	/**
	 * @queryMethod Do not edit, method is generated by editor!
	 */
	public List<Expense> findByProject(final Project dao) {
		final EntityManager entityManager = em();

		final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

		final ParameterExpression<Project> daoParameter = criteriaBuilder.parameter(Project.class, "dao");

		final CriteriaQuery<Expense> criteriaQuery = criteriaBuilder.createQuery(Expense.class);

		final Root<Expense> root = criteriaQuery.from(Expense.class);

		criteriaQuery.where(criteriaBuilder.equal(root.get(Expense_.project), daoParameter));

		criteriaQuery.orderBy(criteriaBuilder.desc(root.get(Expense_.expDate)),
				criteriaBuilder.desc(root.get(Expense_.expFlagGeneric)));

		final TypedQuery<Expense> query = entityManager.createQuery(criteriaQuery);
		query.setParameter(daoParameter, dao);
		return query.getResultList();
	}

	public double sumAmount(final Periode dao) {
		final String sql = "select sum(" + Expense_.expAmount.getName() + ") as total from Expense where expperid = :perid";

	    final Query qry = em().createNativeQuery(sql);
	    qry.setParameter("perid", dao.getPerId());

	    final BigDecimal value = (BigDecimal) qry.getSingleResult();
	    return value.doubleValue();
	}

}