package ch.xwr.seicentobilling.business;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.jfree.util.Log;

import ch.xwr.seicentobilling.dal.CompanyDAO;
import ch.xwr.seicentobilling.dal.EntityDAO;
import ch.xwr.seicentobilling.dal.PeriodeDAO;
import ch.xwr.seicentobilling.dal.RowObjectDAO;
import ch.xwr.seicentobilling.dal.RowParameterDAO;
import ch.xwr.seicentobilling.entities.Company;
import ch.xwr.seicentobilling.entities.Customer;
import ch.xwr.seicentobilling.entities.Expense;
import ch.xwr.seicentobilling.entities.Order;
import ch.xwr.seicentobilling.entities.Periode;
import ch.xwr.seicentobilling.entities.RowImage;
import ch.xwr.seicentobilling.entities.RowObject;
import ch.xwr.seicentobilling.entities.RowParameter;

public class JasperManager {
	/** Logger initialized */
	private static final org.apache.log4j.Logger _logger = org.apache.log4j.Logger.getLogger(JasperManager.class);

	private Periode selectedPeriod = null;
	private final List<String> keys = new ArrayList<>();
	private final List<String> values = new ArrayList<>();
	private List<File> fileList = new ArrayList<>();

	public static String CustomerReport1 = "Seicento_Kunden_Journal";
	public static String ProjectSummary1 = "Seicento_ProjectSummary";
	public static String ProjectReport1 = "Seicento_ProjectReport";
	public static String ExpenseReport1 = "Seicento_Spesen";
	public static String BillReport1 = "Rechnung_Seicento";
	public static String ProjectLineReport1 = "Seicento_ProjectReport";
	public static String ContactDetails1 = "Nested/Seicento_Contacts_Details";

	public void addParameter(final String name, final String value) {
		this.keys.add(name);
		this.values.add(value);
	}

	/**
	 *
	 * @param report
	 *            static report name
	 * @return the uri to start the report on the jasper Server
	 */
	public String getUri(final String report) {
		final CompanyDAO dao = new CompanyDAO();
		final Company cmp = dao.getActiveConfig();

		String uri = cmp.getCmpJasperUri().trim();
		if (cmp.getCmpReportUsr() != null) {
			uri = uri.replace("{user}", cmp.getCmpReportUsr().trim());
		}
		if (cmp.getCmpReportPwd() != null) {
			uri = uri.replace("{password}", cmp.getCmpReportPwd().trim());
		}
		uri = MessageFormat.format(uri, report);

		addDefaultParams(cmp);
		uri = getParamsToUri(uri);

		return uri;
	}

	private String getParamsToUri(String uriIn) {
		if (this.keys.size() > 0) {
			for (int i = 0; i < this.keys.size(); i++) {
				final String s1 = this.keys.get(i);
				final String s2 = this.values.get(i);

				uriIn = uriIn + "&" + s1 + "=" + s2;
			}
		}

		return uriIn;
	}

	public String getRestPdfUri(final String report) {
		try {
			final String ur = getRestUri(report);
			return ur;
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public String getExpenseZip(final Periode per) {
		String zipFile = "";
		String fileExp = "";
		final String expname = "Spesen_" + per.getPerName();
		int httpcode = 0;
		this.fileList = new ArrayList<>();

		// compute URL for Expense
		resetParams();
		addParameter("Param_Periode", "" + per.getPerId());
		final String resturl = getRestPdfUri(ExpenseReport1);

		try {
			//Spesen
			fileExp = getTempFileName4Zip(expname, ".pdf");
			httpcode = streamToFile(resturl, fileExp);
			if (httpcode == 200) {
				this.fileList.add(new File(fileExp));
			}
			_logger.debug("PDF erstellt..." + httpcode);

			loopAttachments(per);

			zipFile = getTempFileName4Zip(expname, ".zip");
			zip(this.fileList, zipFile);
			return zipFile;

		} catch (final Exception e) {
			_logger.error("Fehler beim Erstellen von PDF Reports");
			e.printStackTrace();
		}

		return "Es ist ein Fehler aufgetreten!";
	}

	private void loopAttachments(final Periode per) {
		//get all Attachments of a specific expense Periode and write it to local file system
		final Periode bean = new PeriodeDAO().find(per.getPerId());

		final Set<Expense> lst = bean.getExpenses();
		for (final Expense expense : lst) {
			lookupAttachments(expense);
		}
	}

	private void lookupAttachments(final Expense expense) {
		final RowObjectDAO row = new RowObjectDAO();
		final RowObject bean = row.getObjectBase(Expense.class.getSimpleName(), expense.getExpId());
		if (bean != null) {
			final Set<RowImage> flst = bean.getRowImages();
			for (final RowImage rowImage : flst) {
				writeAttachment(rowImage);
			}
		}
	}

	private void writeAttachment(final RowImage rowImage) {
		final String tempDir = System.getProperty("java.io.tmpdir");
		final String fname = tempDir + "/Att" + rowImage.getRimId() + "-" + rowImage.getRimName();
		final File fn = Paths.get(fname).toFile();

		FileOutputStream fos = null;
		try {
		    fos = new FileOutputStream(fn);
		    fos.write(rowImage.getRimImage());
		    fos.close();

		    this.fileList.add(fn);
		    Log.debug("Attachment added to list " + fn.getName());

		} catch(final IOException ioe) {
			_logger.error("Can not write attachment", ioe);
		}
	}


	public String getBillingZip(final Order oBean) {
		String fileBill = "";
		String fileProject = "";
		String fileReport = "";
		String zipFile = "";
		final List<File> lst = new ArrayList<>();
		int httpcode = 0;

		final boolean generateSummary = getBillReportOutputOptions(oBean, "reportSummary");
		final boolean generateWorkReport = getBillReportOutputOptions(oBean, "reportWork");

		// compute URL for Bill
		resetParams();
		addParameter("OrderNummer", "" + oBean.getOrdNumber());
		final String urlBill = getRestPdfUri(BillReport1);

		// compute URL for Project Summary
		resetParams();
		if (oBean.getProject() != null && generateSummary) {
			addParameter("Param_Project", "" + oBean.getProject().getProId());
			addParameter("BILL_Print", "true");
		}
		final String urlProject = getRestPdfUri(ProjectSummary1);

		// compute URL for Project Report
		resetParams();
		final long perId = getPeriode(oBean);
		if (oBean.getProject() != null && generateWorkReport) {
			addParameter("Param_Periode", "" + perId); // Periode
			addParameter("Param_ProjectId", "" + oBean.getProject().getProId());
			addParameter("REKAP_Print", "false");
		}
		final String urlReport = getRestPdfUri(ProjectReport1);

		try {
			//Rechnung
			fileBill = getTempFileName4Zip(oBean, 0);
			lst.add(new File(fileBill));
			httpcode = streamToFile(urlBill, fileBill); // Rechnung
			_logger.debug("PDF Rechnung erstellt..." + httpcode);
			//Workreport
			if (oBean.getProject() != null && perId > 0 && generateWorkReport) {
				fileReport = getTempFileName4Zip(oBean, 3);
				httpcode = streamToFile(urlReport, fileReport);
				_logger.debug("PDF Arbeitsrapport erstellt... Code: " + httpcode);

				if (httpcode == 200) { //ok
					lst.add(new File(fileReport));
				}
			}
			//ProjectSummary
			if (oBean.getProject() != null && generateSummary) {
				fileProject = getTempFileName4Zip(oBean, 1);
				httpcode = streamToFile(urlProject, fileProject); // ProjectSummary
				_logger.debug("PDF Projektsummary erstellt... Code: " + httpcode);

				if (httpcode == 200) {
					lst.add(new File(fileProject));
				}
			}

			final String mergePdf = mergeOnePdf(lst, oBean);
			if (!mergePdf.isEmpty()) {
				lst.clear();
				lst.add(new File(mergePdf));
			}

			zipFile = getTempFileName4Zip(oBean, 2);
			zip(lst, zipFile);

		} catch (final Exception e) {
			_logger.error("Fehler beim Erstellen von PDF Reports");
			e.printStackTrace();
		}

		return zipFile;
	}

	private String mergeOnePdf(final List<File> lst, final Order oBean) {
    	final Customer cus = oBean.getCustomer();
    	String filename = "";
    	if (cus.getCusSinglepdf() == null || !cus.getCusSinglepdf().booleanValue()) {
    		return "";
    	} else {
    		filename = getTempFileName4Zip(oBean, 4);
    	    try {
    	        final PDFMergerUtility pdfmerger = new PDFMergerUtility();
    	        for (final File file : lst) {
    	            final PDDocument document = PDDocument.load(file);
    	            pdfmerger.setDestinationFileName(filename);
    	            pdfmerger.addSource(file);
    	            pdfmerger.mergeDocuments(MemoryUsageSetting.setupTempFileOnly());
    	            document.close();
    	        }
    	    } catch (final IOException e) {
    	        _logger.error("Error to merge PDF files. Error: " + e.getMessage());
    	    }
    	}

		return filename;
	}

	private long getPeriode(final Order oBean) {
		return getSelectedPeriod().getPerId();
	}

	private void resetParams() {
		this.keys.clear();
		this.values.clear();
	}

	/**
	 *
	 * @param report
	 *            static report name
	 * @return the uri to start the report on the jasper Server
	 */
	private String getRestUri(final String report) {
		final String path = "/jasperserver/rest_v2/reports/reports/XWare_GmbH/";

		// http://xwrprod-srv1.cloudapp.net:80
		// /jasperserver/rest_v2/reports/reports/XWare_GmbH/Lohn/Salary_Slip.pdf
		// ?j_username=userxware&j_password=userxware&userLocale=de_CH&EmployeeId=2&Param_DateFrom=2016-01-20&Param_DateTo=2016-12-31

		final CompanyDAO dao = new CompanyDAO();
		final Company cmp = dao.getActiveConfig();

		addDefaultParams(cmp);

		URI cb;
		String cbs = "";
		try {
			String jasper = cmp.getCmpJasperUri().trim();
			jasper = jasper.substring(0, 100); // ged rid of {0}

			cb = new URI(jasper);
			cbs = cb.getScheme() + "://" + cb.getHost();
			if (cb.getPort() > 0) {
				cbs = cbs + ":" + cb.getPort();
			}

			cbs = cbs + path + report + ".pdf?";
			cbs = getParamsToUri(cbs);

		} catch (final URISyntaxException e) {
			_logger.error("URI ist fehlerhaft: " + cmp.getCmpJasperUri());
			e.printStackTrace();
		}

		return cbs;
	}

	private void addDefaultParams(final Company cmp) {
		if (cmp.getCmpReportUsr() != null) {
			addParameter("j_username", cmp.getCmpReportUsr().trim());
		}
		if (cmp.getCmpReportPwd() != null) {
			addParameter("j_password", cmp.getCmpReportPwd().trim());
		}

		addParameter("userLocale", "de_CH");
	}

	private int streamToFile(final String urlToRead, final String fileName) throws Exception {
		try {
			final URL url = new URL(urlToRead);
			final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setDoOutput(true);

			copyInputStreamToFile(conn.getInputStream(), new File(fileName));

			return conn.getResponseCode();

		} catch (final Exception e) {
			System.out.println(e.getLocalizedMessage());
			e.printStackTrace();
		}

		return 500;
	}

	private void copyInputStreamToFile(final InputStream in, final File file) {
		try {
			final OutputStream out = new FileOutputStream(file);
			final byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.close();
			in.close();
		} catch (final Exception e) {
			_logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	private File zip(final List<File> files, final String filename) {
		final File zipfile = new File(filename);
		// Create a buffer for reading the files
		final byte[] buf = new byte[1024];
		try {
			// create the ZIP file
			final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipfile));
			// compress the files
			for (int i = 0; i < files.size(); i++) {
				final FileInputStream in = new FileInputStream(files.get(i).getCanonicalFile());
				// add ZIP entry to output stream
				out.putNextEntry(new ZipEntry(files.get(i).getName()));
				// transfer bytes from the file to the ZIP file
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				// complete the entry
				out.closeEntry();
				in.close();
			}
			// complete the ZIP file
			out.close();

			// delete origin files
			for (int i = 0; i < files.size(); i++) {
				files.get(i).delete();
			}

			return zipfile;
		} catch (final IOException ex) {
			_logger.error(ex.getMessage());
		}
		return null;
	}

	private String getTempFileName4Zip(final Order bean, final int iflag) {
		//Default
		String fileExt = ".pdf";
		String prefix = "Rechnung_" + bean.getOrdNumber();

		if (iflag == 1) {
			prefix = "ProjectSummary_#" + bean.getProject().getProId();
		}
		if (iflag == 2) {
			fileExt = ".zip";
			prefix = "XWare_R" + bean.getOrdNumber() + "_" + getTimeStamp();
		}
		if (iflag == 3) {
			prefix = "WorkReport_" + getSelectedPeriod().getPerName();
		}
		if (iflag == 4) {
			prefix = "RechnungLang_" + bean.getOrdNumber();
		}

		return getTempFileName4Zip(prefix, fileExt);
	}

	private String getTempFileName4Zip(final String prefix, final String fileExt) {
		final String tempDir = System.getProperty("java.io.tmpdir");

		final String fname = tempDir + "/" + prefix + fileExt;
		return Paths.get(fname).toFile().toString();
	}

	private String getTimeStamp() {
		final String timeStamp = new SimpleDateFormat("yyMMddHHmmss").format(new Date());
		return timeStamp;
	}

    private boolean getBillReportOutputOptions(final Order obean, final String key)
    {
    	final Customer cus = obean.getCustomer();
    	final Long cusId = cus.getCusId();

    	if (cus.getCusBillingReport() != null) {
    		if (key.equalsIgnoreCase("reportWork")) {
    			if (hasNoReports()) {
    				return false;
    			}
    			if (cus.getCusBillingReport() == LovCrm.BillReport.working) {
					return true;
				}
    			if (cus.getCusBillingReport() == LovCrm.BillReport.project) {
					return true;
				}
    		}
    		if (key.equalsIgnoreCase("reportSummary")) {
    			if (cus.getCusBillingReport() == LovCrm.BillReport.project) {
					return true;
				}
    		}

    		return false;
    	}

    	//fallback
        //final var cus = this.Orders.SelectedItem.Customer.cusId;
        RowObject objRoot = getObjRoot(cusId, "Customer");
        if (objRoot == null) {
			objRoot = new RowObject();
		}
        RowObject cmpRoot = getObjRoot((long) 1, "Company");
        if (cmpRoot == null) {
			cmpRoot = new RowObject();
		}

        final String outputOptions = getRowParameter(objRoot, "pdfmail", "pdfmail", key);
        if (outputOptions == null || outputOptions.isEmpty()) {
			return true;
		}

        if ("true".equalsIgnoreCase(outputOptions)) {
			return true;
		}
        return false;
    }

	private boolean hasNoReports() {
		final PeriodeDAO dao = new PeriodeDAO();
		final Periode per = dao.find(getSelectedPeriod().getPerId());

		if (per.getProjectLines().size() > 0) {
			return true;
		}
		// No Reportlines to print
		return false;
	}

	private String getRowParameter(final RowObject objRoot, final String group, final String subgroup, final String key)
    {
    	final RowParameterDAO dao = new RowParameterDAO();
    	final RowParameter bean = dao.getParameter(objRoot, group, subgroup, key);
        if (bean == null) {
			return "";
		}
        return bean.getPrmValue();
    }

    private RowObject getObjRoot(final Long id, final String entName) {
    	final EntityDAO entDao = new EntityDAO();
    	final RowObjectDAO rooDao = new RowObjectDAO();

    	final ch.xwr.seicentobilling.entities.Entity entBean = entDao.findEntity(entName);
    	final RowObject rooBean = rooDao.findObjectBase(entBean, id).get(0);

    	return rooBean;
    }

	public Periode getSelectedPeriod() {
		return this.selectedPeriod;
	}

	public void setSelectedPeriod(final Periode selectedPeriod) {
		this.selectedPeriod = selectedPeriod;
	}

//	private void downloadToWS(final String name) {
//		final File inp = new File(name);
//
//		final String home = System.getProperty("user.home");
//		final File outf = new File(home+"/Downloads/" + inp.getName());
//
//		try (ReadableByteChannel in = Channels
//				.newChannel(new FileInputStream(inp));
//				FileChannel out = new FileOutputStream(outf).getChannel()) {
//
//			out.transferFrom(in, 0, Long.MAX_VALUE);
//		} catch (final Exception e) {
//			e.printStackTrace();
//		}
//	}

}
