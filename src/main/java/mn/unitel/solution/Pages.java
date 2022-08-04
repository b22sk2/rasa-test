package mn.unitel.solution;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class Pages {
    @XmlElementWrapper(name = "pages")
    @XmlElement(name = "page")
    List<PageInfo> page ;

    public List<PageInfo> getPage() {
        return page;
    }

    public void setPage(List<PageInfo> page) {
        this.page = page;
    }

    public Pages() {
    }

}
