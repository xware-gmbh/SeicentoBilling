package ch.xwr.seicentobilling.business;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import ch.xwr.seicentobilling.business.helper.RowObjectAddonHandler;
import ch.xwr.seicentobilling.dal.CompanyDAO;
import ch.xwr.seicentobilling.dal.CustomerLinkDAO;
import ch.xwr.seicentobilling.dal.EntityDAO;
import ch.xwr.seicentobilling.dal.RowObjectDAO;
import ch.xwr.seicentobilling.entities.Company;
import ch.xwr.seicentobilling.entities.CustomerLink;
import ch.xwr.seicentobilling.entities.Order;
import ch.xwr.seicentobilling.entities.RowObject;

public class MailManager {

    public String getEmailUrl(final Order obean)
    {
    	final Long cusId = obean.getCustomer().getCusId();
        final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        final String billdate = sdf.format(obean.getOrdBillDate());

        //final var cus = this.Orders.SelectedItem.Customer.cusId;
        RowObject objRoot = getObjRoot(cusId, "Customer");
        if (objRoot == null) {
			objRoot = new RowObject();
		}
        //ObjRoot der aktiven Config Company
        final RowObject cmpRoot = getCompanyRootObject();


        //Mailadressen
        final String toAddress = getMailToAddress(obean, objRoot);
        final String mailcc = getRowParameter(objRoot, "pdfmail", "pdfmail", "mailcc");
        //mailSubject
        final String subject = getMailSubject(cmpRoot, objRoot, obean, billdate);
        //mailText
        final String body = getMailBody(cmpRoot, objRoot, obean, billdate);

        //compose mail URL
        String url = "";
        if (mailcc == null || mailcc.isEmpty()) {
			url = MessageFormat.format("mailto:{0}?subject={1}&body={2}", toAddress, subject, body);
		} else {
			url = MessageFormat.format("mailto:{0}?cc={1}&subject={2}&body={3}", toAddress, mailcc, subject, body);
		}

        //Edge versteht den Link nicht...!!!
        //mailto:office@allaxa.com?cc=juehlinger@allaxa.com&subject=Rechnung XWare GmbH #2017136 per 01.05.2017&body=Sehr geehrte Kundin,%0D%0ASehr geehrter Kunde%0D%0A%0D%0AIn der Beilage erhalten Sie eine neue Rechnung im PDF Format.%0D%0ABemerkung: Gemäss Projektspezifikation Solution Design B333 / WP1340%0D%0A%0D%0AFreundliche Grüsse%0D%0A%0D%0AXWare GmbH%0D%0AAllee 1b%0D%0A6210 Sursee%0D%0A%0D%0A%0D%0ATODO: Check Link: https:\/\/xware.sharepoint.com\/sites\/Intranet\/xwarewiki\/SeicentoNG%20Download%20PDF.aspx
        return url;
    }

    private String getMailBody(final RowObject cmpRoot, final RowObject objRoot, final Order obean, final String billdate) {
        String body = getRowText(objRoot);  //Text von Kunde
        if (body == null || body.isEmpty()) {
			body = getRowText(cmpRoot);	  //Text von Company
		}

        body = body.replace("{ordNumber}",  "" + obean.getOrdNumber());
        body = body.replace("{ordText}", obean.getOrdText());
        body = body.replace("{ordBillDate}", billdate);
        body = body.replace("\r\n","%0D%0A");
        body = body.replace("\r", "%0D%0A");
        body = body.replace("#", "%23");

        return body;
	}

	private String getMailSubject(final RowObject cmpRoot, final RowObject objRoot, final Order obean, final String billdate ) {
		RowObjectAddonHandler objman = new RowObjectAddonHandler(objRoot);
		String subject = objman.getRowParameter("pdfmail", "pdfmail", "subject");
        if (subject == null || subject.isEmpty()) {
        	objman = new RowObjectAddonHandler(cmpRoot);
			subject = objman.getRowParameter("pdfmail", "pdfmail", "subject");
		}

        //replace placeholders
        subject = subject.replace("{ordNumber}", "" + obean.getOrdNumber());
        subject = subject.replace("{ordText}", obean.getOrdText());
        subject = subject.replace("{ordBillDate}", billdate);
        subject = subject.replace("#", "%23");

		return subject;
	}

	private String getMailToAddress(final Order obean, final RowObject objRoot) {
		final CustomerLinkDAO dao = new CustomerLinkDAO();
		final List<CustomerLink> links = dao.findByCustomer(obean.getCustomer());

		//get email from Customer
    	for(final CustomerLink link: links) {
    		if (link.getCnkType() == LovCrm.LinkType.mail) {
    			if (link.getCnkDepartment() == LovCrm.Department.billing) {
    				return link.getCnkLink();
    			}
    		}
    	}

    	//fallback
		final RowObjectAddonHandler objman = new RowObjectAddonHandler(objRoot);
		return objman.getRowParameter("pdfmail", "pdfmail", "mailaddress");
	}


	private String getRowText(final RowObject objRoot) {
		final RowObjectAddonHandler objman = new RowObjectAddonHandler(objRoot);
		return objman.getRowText(1);
	}

	private String getRowParameter(final RowObject objRoot, final String group, final String subgroup, final String key)
    {
		final RowObjectAddonHandler objman = new RowObjectAddonHandler(objRoot);
		return objman.getRowParameter(group, subgroup, key);
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
