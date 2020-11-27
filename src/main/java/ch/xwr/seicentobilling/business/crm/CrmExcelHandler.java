package ch.xwr.seicentobilling.business.crm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jfree.util.Log;

import ch.xwr.seicentobilling.business.LovCrm.BillReport;
import ch.xwr.seicentobilling.business.LovCrm.BillTarget;
import ch.xwr.seicentobilling.business.LovCrm.Department;
import ch.xwr.seicentobilling.business.LovCrm.LinkType;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.LovState.AccountType;
import ch.xwr.seicentobilling.business.NumberRangeHandler;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.dal.CityDAO;
import ch.xwr.seicentobilling.dal.CostAccountDAO;
import ch.xwr.seicentobilling.dal.CustomerDAO;
import ch.xwr.seicentobilling.dal.CustomerLinkDAO;
import ch.xwr.seicentobilling.dal.LabelAssignmentDAO;
import ch.xwr.seicentobilling.dal.LabelDefinitionDAO;
import ch.xwr.seicentobilling.dal.PaymentConditionDAO;
import ch.xwr.seicentobilling.entities.City;
import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.Customer;
import ch.xwr.seicentobilling.entities.CustomerLink;
import ch.xwr.seicentobilling.entities.LabelAssignment;
import ch.xwr.seicentobilling.entities.LabelDefinition;
import ch.xwr.seicentobilling.entities.PaymentCondition;

public class CrmExcelHandler {
	/** Logger initialized */
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CrmExcelHandler.class);

	/** the workbook */
	protected XSSFWorkbook hssfworkbook = null;

	private List<CustomerDto> exList = null;


	public List<CustomerDto> readContactsToList(final File file) {
		this.exList = new ArrayList<>();
		final String filename = file.getPath();
		//final POIFSFileSystem fs;
		InputStream fs1 = null;
		try {
			//fs = new POIFSFileSystem(new FileInputStream(filename));
			fs1 = new FileInputStream(filename);
			this.hssfworkbook = new XSSFWorkbook(fs1);

			//HSSFSheet sheet = null;
			XSSFSheet sheet = null;
			sheet = this.hssfworkbook.getSheetAt(0);

		    //final HSSFRow row = null;
		    XSSFRow row = null;
			//ignore first line
		    for (int i = 1; i < sheet.getLastRowNum(); i++) {
		    	LOG.info("Start processing Excel-line " + (i + 1));
		    	row = sheet.getRow(i);
		    	loopExcelRow(row);
			}

		    //persistList(periode);

		} catch (final FileNotFoundException e) {
			LOG.error(e.getMessage());
		} catch (final IOException e) {
			LOG.error(e.getMessage());
		} finally {
			closeFile(fs1);
		}

		return this.exList;

	}

	private void closeFile(final InputStream fs1) {

		try {
			fs1.close();
			this.hssfworkbook.close();
		} catch (final IOException e) {
			//ignore
		}

	}

	private void loopExcelRow(final XSSFRow row) {
		if (row == null) {
			return;
		}
		XSSFCell cell;
		String zip = "";
		CustomerLink lnk = null;
		LabelDefinition label = null;

		final CustomerDto dto = new CustomerDto();
		final Customer bean = new Customer();

		for (int i = 0; i < row.getLastCellNum(); i++) {
	        cell = row.getCell((short) i);
	        //final int type = cell.getCellType();
	        //final String myval = cell.getStringCellValue();

	        final DataFormatter formatter = new DataFormatter();
	        final String cellValue = formatter.formatCellValue(cell).trim();

	        System.out.println("Feld: " + i + " Wert: " + cellValue);

	        switch (i) {
			case 0:
			case 1:
				final CostAccount cst = lookupCst(cellValue);
				if (cst != null && cst.getCsaId() > 0) {
					dto.getCostaccounts().add(cst);
				}
				break;
			case 2:
				bean.setCusName(getMaxLength(cellValue, 40));
			case 3:
				bean.setCusFirstName(getMaxLength(cellValue,40));
				break;
			case 4:
				bean.setCusCompany(getMaxLength(cellValue, 40));
				break;
			case 5:
				break;
			case 6:
				bean.setCusAddress(getMaxLength(cellValue, 40));
				break;
			case 7:
				zip = cellValue;
				break;
			case 8:
				bean.setCity(fetchCity(zip, cellValue));
				break;
			case 9:
				break;
			case 10:
				lnk = getCustomerLink(cellValue, 1);
				if (lnk != null) {
					dto.getClinks().add(lnk);
				}
				break;
			case 11:
				lnk = getCustomerLink(cellValue, 1);
				if (lnk != null) {
					dto.getClinks().add(lnk);
				}
				break;
			case 12:
				lnk = getCustomerLink(cellValue, 1);
				if (lnk != null) {
					dto.getClinks().add(lnk);
				}
				break;
			case 14:
				lnk = getCustomerLink(cellValue, 5);
				if (lnk != null) {
					dto.getClinks().add(lnk);
				}
				break;
			case 15:
				lnk = getCustomerLink(cellValue, 6);
				if (lnk != null) {
					dto.getClinks().add(lnk);
				}
				break;
			case 17:
				//label
				label = getLabelDef(cellValue, "Newsletter");
				if (label != null) {
					dto.getLabels().add(label);
				}
				break;
			case 18:
				//label
				label = getLabelDef(cellValue, "Weihnachtskarte");
				if (label != null) {
					dto.getLabels().add(label);
				}
				break;
			case 19:
				//label
				label = lookupLabelDef(cellValue);
				if (label != null) {
					dto.getLabels().add(label);
				}
				break;
			case 20:
				bean.setCusInfo(cellValue);
				break;

			default:
				break;
			}
		}

		dto.setCustomer(enrichBean(bean));
		this.exList.add(dto);
	}

	private String getMaxLength(String cellValue, final int ilen) {
		if (cellValue == null || cellValue.length() < ilen) {
			return cellValue;
		}

		cellValue = cellValue.substring(0, ilen-1);
		return cellValue;
	}

	private LabelDefinition getLabelDef(final String cellValue, final String name) {
		if (cellValue.toLowerCase().equals("falsch")) {
			return null;
		}
		if (cellValue.toLowerCase().equals("false")) {
			return null;
		}

		return lookupLabelDef(name);
	}

	private LabelDefinition lookupLabelDef(final String name) {
		final LabelDefinitionDAO dao = new LabelDefinitionDAO();
		final List<LabelDefinition> lst = dao.findByName(name);

		if (!lst.isEmpty()) {
			return lst.get(0);
		}

		return null;
	}

	private Customer enrichBean(final Customer bean) {
		final Customer cusExist = existCustomer(bean);
		if (cusExist != null) {
			return cusExist;
		}

		bean.setCusState(LovState.State.active);
		bean.setCusBillingTarget(BillTarget.pdf);
		bean.setCusBillingReport(BillReport.working);
		bean.setCusAccountType(AccountType.juristisch);
		if (bean.getCusCompany().trim().isEmpty()) {
			bean.setCusAccountType(AccountType.natürlich);
		}
		bean.setPaymentCondition(lookupPc());

		return bean;
	}

	private PaymentCondition lookupPc() {
		final PaymentConditionDAO dao = new PaymentConditionDAO();
		final List<PaymentCondition> lst = dao.findByCode("30N");
		if (!lst.isEmpty()) {
			return lst.get(0);
		}

		final PaymentCondition pac = dao.findAll().get(0);
		return pac;
	}

	private City checkCity(final City city) {
		if (city.getCtyId() !=  null && city.getCtyId() > 0) {
			return city;
		}
		//check if city was saved in the meantime
		final City cty2 = fetchCity(city.getCtyZip().toString(), city.getCtyName());
		if (cty2.getCtyId() !=  null && cty2.getCtyId() > 0) {
			return cty2;
		}

		//create new city
		final CityDAO dao = new CityDAO();
		//dao.beginTransaction();
		dao.save(city);
		//dao.commit();

		return city;
	}

	private CustomerLink getCustomerLink(String cellValue, final int i) {
		cellValue = cellValue.trim();
		if (cellValue.isEmpty()) {
			return null;
		}

		final CustomerLink link = new CustomerLink();
		link.setCnkLink(cellValue);
		link.setCnkState(LovState.State.active);
		link.setCnkValidFrom(new Date());
		link.setCnkIndex((short) i);
		link.setCnkDepartment(Department.misc);
		link.setCnkRemark("imported");
		link.setCnkType(LinkType.phone);


		 switch (i) {
		 	case 5:
				link.setCnkType(LinkType.mail);
				break;
		 	case 6:
				link.setCnkType(LinkType.web);
				break;
		 }

		return link;
	}

	private City fetchCity(final String zip, final String cellValue) {
		int iZip = 0;
		try {
			iZip = Integer.parseInt(zip);
		} catch (final Exception e) {
			LOG.warn("Could not parse ZIP to Int: " + zip);
		}

		final CityDAO dao = new CityDAO();
		final List<City> list = dao.findByZip(iZip);
		if (list.isEmpty()) {
			final City cty = new City();
			cty.setCtyName(cellValue);
			cty.setCtyZip(iZip);
			cty.setCtyCountry("CH");
			cty.setCtyState(LovState.State.active);
			return cty;
		}

		return list.get(0);
	}

	private CostAccount lookupCst(final String cellValue) {
		final List<String> cstList = Arrays.asList(cellValue.split(";"));
		final CostAccountDAO dao = new CostAccountDAO();

		if (cstList == null || cstList.size() < 1) {
			return null;
		}

		final List<CostAccount> lst = dao.findByName(cstList.get(0));
		if (lst != null && lst.size() > 0) {
			return lst.get(0);
		}

		return null;
	}

	private Customer existCustomer(final Customer cus) {
		final CustomerDAO dao = new CustomerDAO();
		final List<Customer> lst = dao.findByCompanyAndName(cus.getCusCompany(), cus.getCusName());

		if (lst != null && lst.size() > 0) {
			return lst.get(0);
		}

		return null;
	}

	public boolean saveDto(final CustomerDto dto) {
		LOG.debug("Start saving to db....");

		final CustomerDAO dao = new CustomerDAO();
		try {
			//dao.beginTransaction();

			//save customer
			final Customer cus = dto.getCustomer();
			checkCity(cus.getCity());
			cus.setCusNumber(getNextCustomerNumber(false, new Integer(0)));
			cus.setCusAccountManager(computeAccountManagers(dto));

			dao.save(cus);
			cus.setCusNumber(getNextCustomerNumber(true, cus.getCusNumber()));

			//create objRoot
			final RowObjectManager man = new RowObjectManager();
			man.updateObject(cus.getCusId(), cus.getClass().getSimpleName());

			//save CustomerLink
			saveCustomerLink(dto, cus);
			saveLabels(dto, cus);

			//dao.commit();
			LOG.info("New Contact created with number: " + cus.getCusNumber());
			return true;

		} catch (final Exception e) {
			LOG.error("failed while saving Dto");
			Log.error(e);
			return false;
		}

	}

	private String computeAccountManagers(final CustomerDto dto) {
		String result = "";
		int icount = 0;

		for (final Iterator<CostAccount> iterator = dto.getCostaccounts().iterator(); iterator.hasNext();) {
			final CostAccount bean = iterator.next();

			if (icount  > 0) {
				result = result + ", ";
			}

			result = result + bean.getCsaCode();

			icount++;
		}
		return result;
	}

	private void saveLabels(final CustomerDto dto, final Customer cus) {
		final LabelAssignmentDAO dao = new LabelAssignmentDAO();
		LabelAssignment lblbean;
		int icount = 1;

		for (final Iterator<LabelDefinition> iterator = dto.getLabels().iterator(); iterator.hasNext();) {
			final LabelDefinition bean = iterator.next();

			lblbean = new LabelAssignment();
			lblbean.setClaIndex((long) icount);
			lblbean.setCustomer(cus);
			lblbean.setLabelDefinition(bean);

			dao.save(lblbean);
			icount++;
		}

	}

	private void saveCustomerLink(final CustomerDto dto, final Customer cus) {
		final CustomerLinkDAO dao = new CustomerLinkDAO();

		for (final Iterator<CustomerLink> iterator = dto.getClinks().iterator(); iterator.hasNext();) {
			final CustomerLink bean = iterator.next();

			bean.setCustomer(cus);
			dao.save(bean);

			//create objRoot
			final RowObjectManager man = new RowObjectManager();
			man.updateObject(bean.getCnkId(), bean.getClass().getSimpleName());
		}

	}

	private int getNextCustomerNumber(final boolean commitNbr, final Integer nbr) {
		final NumberRangeHandler handler = new NumberRangeHandler();
		return handler.getNewCustomerNumber(commitNbr, nbr);
	}

//    protected HSSFCell getCellObj(final HSSFSheet sheet, final int irow, final String scol) {
//    	final int[] iref = getInternalCellRef(irow, scol);
//
//    	if (scol == "AD") {
//    		iref[1] = 29;
//    	}
//    	if (scol == "AC") {
//    		iref[1] = 28;
//    	}
//        final HSSFRow  row  = sheet.getRow(iref[0]);
//        final HSSFCell cell = row.getCell(iref[1], HSSFRow.CREATE_NULL_AS_BLANK);
//
//        return cell;
//	}
//
//    private int[] getInternalCellRef(final int irow, String scol) {
//    	final int[] ival = {0,0};
//    	int icol = 0;
//
//    	scol = scol.toUpperCase();
//    	icol = scol.charAt(0);
//    	icol = icol - 65;
//
//    	ival[0] = irow - 1;
//    	ival[1] = icol;
//
//		return ival;
//    }


}
