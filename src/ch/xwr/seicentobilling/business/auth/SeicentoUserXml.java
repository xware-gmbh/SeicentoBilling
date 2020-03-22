package ch.xwr.seicentobilling.business.auth;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "user")
public class SeicentoUserXml {
    String name;
	String uid;
    String password;
    private String role;

    public String getName() {
        return this.name;
    }

    @XmlElement
    public void setName(final String name) {
        this.name = name;
    }
    public String getUid() {
		return this.uid;
	}
    @XmlElement
	public void setUid(final String uid) {
		this.uid = uid;
	}
	public String getPassword() {
		return this.password;
	}
    @XmlElement
	public void setPassword(final String password) {
		this.password = password;
	}
	public String getRole() {
		return this.role;
	}
    @XmlElement
	public void setRole(final String role) {
		this.role = role;
	}

}
