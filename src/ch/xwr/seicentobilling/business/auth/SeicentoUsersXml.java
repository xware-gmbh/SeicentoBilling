package ch.xwr.seicentobilling.business.auth;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "seicentoUsersXml")
public class SeicentoUsersXml {
	private List<SeicentoUserXml> users;

    public List<SeicentoUserXml> getUsers() {
        return this.users;
    }

    @XmlElement(name = "user")
    public void setUsers(final List<SeicentoUserXml> users) {
        this.users = users;
    }


}
