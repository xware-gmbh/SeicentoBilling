package ch.xwr.seicentobilling.business;

import java.util.Date;

import javax.persistence.EntityManager;

import com.xdev.persistence.PersistenceUtils;

import ch.xwr.seicentobilling.dal.EntityDAO;
import ch.xwr.seicentobilling.dal.RowObjectDAO;
import ch.xwr.seicentobilling.entities.Entity;
import ch.xwr.seicentobilling.entities.RowObject;

public class RowObjectManager {
	private String username = null;

	public void updateObject(final Long id, final String name) {
		final Entity ent = getEntityBean(name);
		if (ent.getEntHasrowobject() == false)
		 {
			return; //no Objectroot
		}

		final RowObject obj = getRowObject(name, id, ent);
		obj.setObjChanged(new Date());
		obj.setObjChangedBy(getUserName());
		obj.setObjChngcnt(obj.getObjChngcnt() + 1);

		//PersistenceUtils.getEntityManager(RowObject.class).persist(obj);
		final EntityManager em = PersistenceUtils.getEntityManager(RowObject.class);
		em.flush();
		em.persist(obj);
	}

	private Entity getEntityBean(final String name) {
		final EntityDAO entDao = new EntityDAO();
		final Entity ent = entDao.findEntity(name);
		return ent;
	}

	public RowObject getRowObject(final String entName, final Long id) {
		if (id == null || id.longValue() < 1) {
			return null;
		}

		final Entity ent = getEntityBean(entName);
		final RowObject obj = getRowObject(entName, id, ent);
		return obj;
	}

	private RowObject getRowObject(final String name, final Long id, final Entity ent) {
		final RowObjectDAO objDao = new RowObjectDAO();
		RowObject obj = null;
		if (id != null) {
			obj = objDao.getObjectBase(name, id);
		}

		if (obj == null) {
			obj = new RowObject();
			obj.setObjAdded(new Date());
			obj.setObjAddedBy(getUserName());
			obj.setObjRowId(id);
			obj.setObjChngcnt((long) 0);
			obj.setObjState(LovState.State.active);
			obj.setEntity(ent);
			obj.setDatabaseVersion(null);
		}
		if (obj.getObjChngcnt() == null) {
			obj.setObjChngcnt((long) 0);
		}

		return obj;
	}

	public void deleteObject(final Long id, final String name) {
		final EntityDAO entDao = new EntityDAO();
		final Entity ent = entDao.findEntity(name);
		if (ent.getEntHasrowobject() == false)
		 {
			return; //no Objectroot
		}

		final RowObject obj = getRowObject(name, id, ent);
		obj.setObjDeleted(new Date());
		obj.setObjDeletedBy(getUserName());
		obj.setObjState(LovState.State.locked);
		obj.setObjChngcnt(obj.getObjChngcnt() + 1);

		PersistenceUtils.getEntityManager(RowObject.class).persist(obj);
	}

	private String getUserName() {
		if (this.username != null) {
			return this.username;
		}

		this.username = Seicento.getUserName();
		return this.username;
	}

}
