package ws.web;


import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * User: jkyang
 */
public class Param {
    /**
     * app/module/action?a1=2&accessKey=123456&timestamp=9288387883&hmac=2341&random=1232222
     */
    private String path;

    private String accessKey ;

    private String timestamp ;

    private String hmac ;

    private String random ;

    private Map<String, String> params ;

    /**
     * @return
     */
    public boolean requiredPass() {
       if(StringUtils.isNotEmpty(accessKey) && StringUtils.isNotEmpty(timestamp)
        && StringUtils.isNotEmpty(hmac) && StringUtils.isNotEmpty(random)) {
         return true ;
       }
       return false ;
    }

    public SortedMap<String, String> sortedMapped() {
        SortedMap<String, String> map = new TreeMap<String, String>();
        map.put("accessKey",accessKey);
        map.put("timestamp",timestamp);
        map.put("random",random);
        map.putAll(params);
        return map;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getHmac() {
        return hmac;
    }

    public void setHmac(String hmac) {
        this.hmac = hmac;
    }

    public String getRandom() {
        return random;
    }

    public void setRandom(String random) {
        this.random = random;
    }

    @Override
    public String toString() {
        return "Param{" +
                "path='" + path + '\'' +
                ", accessKey='" + accessKey + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", hmac='" + hmac + '\'' +
                ", random='" + random + '\'' +
                ", params=" + params +
                '}';
    }
}
