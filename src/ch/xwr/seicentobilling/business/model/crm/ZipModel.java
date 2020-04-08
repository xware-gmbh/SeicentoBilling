package ch.xwr.seicentobilling.business.model.crm;

public class ZipModel {
	private int rec_art;
	private int bfsnr;
	private int plz_typ;
	private int postleitzahl;
	private String ortbez27;
	private String kanton;
	private String gilt_ab_date;
	private String geo_point_2d;
	private int plz_zz;
	private String plz_coff;

	public static String REC_ART = "rec_art";
	public static String BFSNR = "bfsnr";
	public static String ZIP_TYP = "plz_typ";
	public static String ZIP = "postleitzahl";
	public static String CITY27 = "ortbez27";
	public static String AREA = "kanton";
	public static String VALID_FROM = "gilt_ab_dat";
	public static String GEO_POINT = "geo_point_2d";
	public static String PLZ_ZZ = "plz_zz";
	public static String PLZ_COFF = "plz_coff";

	public int getRec_art() {
		return this.rec_art;
	}
	public void setRec_art(final int rec_art) {
		this.rec_art = rec_art;
	}
	public int getBfsnr() {
		return this.bfsnr;
	}
	public void setBfsnr(final int bfsnr) {
		this.bfsnr = bfsnr;
	}
	public int getPlz_typ() {
		return this.plz_typ;
	}
	public void setPlz_typ(final int plz_typ) {
		this.plz_typ = plz_typ;
	}
	public int getPostleitzahl() {
		return this.postleitzahl;
	}
	public void setPostleitzahl(final int postleitzahl) {
		this.postleitzahl = postleitzahl;
	}
	public String getOrtbez27() {
		return this.ortbez27;
	}
	public void setOrtbez27(final String ortbez27) {
		this.ortbez27 = ortbez27;
	}
	public String getKanton() {
		return this.kanton;
	}
	public void setKanton(final String kanton) {
		this.kanton = kanton;
	}
	public String getGilt_ab_date() {
		return this.gilt_ab_date;
	}
	public void setGilt_ab_date(final String gilt_ab_date) {
		this.gilt_ab_date = gilt_ab_date;
	}
	public String getGeo_point_2d() {
		return this.geo_point_2d;
	}
	public void setGeo_point_2d(final String geo_point_2d) {
		this.geo_point_2d = geo_point_2d;
	}
	public int getPlz_zz() {
		return this.plz_zz;
	}
	public void setPlz_zz(final int plz_zz) {
		this.plz_zz = plz_zz;
	}
	public String getPlz_coff() {
		return this.plz_coff;
	}
	public void setPlz_coff(final String plz_coff) {
		this.plz_coff = plz_coff;
	}


}
