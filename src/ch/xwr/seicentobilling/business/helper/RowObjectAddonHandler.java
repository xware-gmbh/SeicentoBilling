package ch.xwr.seicentobilling.business.helper;

import java.util.List;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.LovState.ValueType;
import ch.xwr.seicentobilling.dal.CompanyDAO;
import ch.xwr.seicentobilling.dal.EntityDAO;
import ch.xwr.seicentobilling.dal.RowObjectDAO;
import ch.xwr.seicentobilling.dal.RowParameterDAO;
import ch.xwr.seicentobilling.dal.RowTextDAO;
import ch.xwr.seicentobilling.entities.Company;
import ch.xwr.seicentobilling.entities.RowObject;
import ch.xwr.seicentobilling.entities.RowParameter;
import ch.xwr.seicentobilling.entities.RowText;

public class RowObjectAddonHandler {
	private final RowObject objRoot;

	public RowObjectAddonHandler(final RowObject obj) {
		if (obj == null) {
			this.objRoot = getCompanyRootObject();
		} else {
			this.objRoot = obj;
		}
	}

    public RowObjectAddonHandler(final Long id, final String entName) {
    	final EntityDAO entDao = new EntityDAO();
    	final RowObjectDAO rooDao = new RowObjectDAO();

    	final ch.xwr.seicentobilling.entities.Entity entBean = entDao.findEntity(entName);
    	final List<RowObject> objlst = rooDao.findObjectBase(entBean, id);
    	if (objlst != null && objlst.size() > 0) {
    		this.objRoot = objlst.get(0);
    	} else {
        	this.objRoot = null;
    	}
    }

	public RowObject getObjRoot() {
		return this.objRoot;
	}

	public String getRowText(final int iNumber) {
    	final RowTextDAO dao = new RowTextDAO();
    	final RowText bean = dao.getText(this.objRoot, iNumber);
    	if (bean == null) {
			return "";
		}
		return bean.getTxtFreetext();
	}

	public Long getRowParameterLong(final String group, final String subgroup, final String key)
    {
		Long value = new Long(0);
		final String retVal = getRowParameter(group, subgroup, key);
		if (retVal != null && retVal.length() > 0) {
			try {
				value = Long.parseLong(retVal);
			} catch (final Exception e) {
				System.out.println("Invalid value for long " + retVal);
			}
		}
		return value;
    }

	public String getRowParameter(final String group, final String subgroup, final String key)
    {
    	final RowParameterDAO dao = new RowParameterDAO();
    	final RowParameter bean = dao.getParameter(this.objRoot, group, subgroup, key);
        if (bean == null) {
			return "";
		}
        return bean.getPrmValue();
    }

	public void putRowParameter(final String group, final String subgroup, final String key, final String value)
    {
    	final RowParameterDAO dao = new RowParameterDAO();
    	RowParameter bean = dao.getParameter(this.objRoot, group, subgroup, key);
        if (bean == null) {
        	bean = new RowParameter();
        	bean.setPrmState(LovState.State.active);
        	bean.setPrmGroup(group);
        	bean.setPrmSubGroup(subgroup);
        	bean.setPrmKey(key);
        	bean.setPrmValueType(ValueType.string);
        	bean.setRowObject(this.objRoot);
		}
        bean.setPrmValue(value);
        dao.save(bean);

    }
	private RowObject getCompanyRootObject() {
    	final Company cmp = new CompanyDAO().getActiveConfig();

        RowObject cmpRoot = getObjRoot(cmp.getCmpId(), "Company");
        if (cmpRoot == null) {
			cmpRoot = new RowObject();
		}
        return cmpRoot;
	}

    private RowObject getObjRoot(final Long id, final String entName) {
    	final EntityDAO entDao = new EntityDAO();
    	final RowObjectDAO rooDao = new RowObjectDAO();

    	final ch.xwr.seicentobilling.entities.Entity entBean = entDao.findEntity(entName);
    	final RowObject rooBean = rooDao.findObjectBase(entBean, id).get(0);

    	return rooBean;
    }

}
