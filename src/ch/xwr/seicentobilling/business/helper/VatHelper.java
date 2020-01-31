package ch.xwr.seicentobilling.business.helper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import ch.xwr.seicentobilling.dal.VatLineDAO;
import ch.xwr.seicentobilling.entities.Vat;
import ch.xwr.seicentobilling.entities.VatLine;

public class VatHelper {
	private static final org.apache.log4j.Logger _logger = org.apache.log4j.Logger.getLogger(VatHelper.class);


    public Double getVatAmount(final Date date, final Double val1, final Vat vat) {
    	if (vat == null  || val1 == null) {
			return new Double(0);
		}

    	final Date refDate = getRawDate(date);
    	final VatLine vl = lookupVatLine(vat, refDate);
    	if (vl == null) {
    		return new Double(0.);
    	}

    	final double rate = vl.getVanRate().doubleValue();
    	double base = 100.;

		if (vat.getVatInclude() == true) {
			base = base + rate;
		}
    	final Double vat1 = val1 / base * rate;
    	return vat1;

		//return swissCommercialRound(new BigDecimal(vat1));
	}

    private VatLine lookupVatLine(final Vat vat, final Date refDate) {
    	//TFS-240
    	final VatLineDAO dao = new VatLineDAO();
    	final List<VatLine> lst = dao.findByVatAndDate(vat, refDate);
    	if (lst == null || lst.isEmpty()) {
    		System.out.println("No VatLine found");
    		_logger.warn("no VatLine found for vat: " + vat.getVatName() + " and Date: " + refDate);
    		return null;
    	}

    	final VatLine vl = lst.get(0);
    	return vl;
    }

    //remove time
    private Date getRawDate(final Date date) {
    	try {
    		final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
    		final Date rawdate = sdf.parse(sdf.format(date));
    		return rawdate;
    	} catch (final Exception e) {

    	}

    	return date;

    }

}
