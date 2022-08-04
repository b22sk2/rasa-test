package mn.unitel.solution;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class PageInfo {
    @XmlAttribute
    String id;
    public PageInfo() {
    }

    @XmlElement(name = "access-token")
    String accessToken;

    @XmlElement(name = "maintenance-token")
    String maintenanceText;

    @XmlElement(name = "maintenance-mode")
    String maintenanceMode;

    public String getMaintenanceMode() {
        return maintenanceMode;
    }

    public void setMaintenanceMode(String maintenanceMode) {
        this.maintenanceMode = maintenanceMode;
    }

    @XmlElement(name = "operation")
    String operation;
    @XmlElement(name = "url")
    String url;

    public void setMaintenanceText(String maintenanceText) {
        this.maintenanceText = maintenanceText;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getMaintenanceText() {
        return maintenanceText;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    
}
