package mn.unitel.solution;/*
 * @created_at 28/07/2022 7:32 PM
 * @project rasa-gw
 * @author baasankhuu.d
 */

public class DataStore {
    String value ;
    String sha1;
    String sha256;

    public DataStore(String value, String sha1, String sha256) {
        this.value = value;
        this.sha1 = sha1;
        this.sha256 = sha256;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getSha1() {
        return sha1;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    public String getSha256() {
        return sha256;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }
}
