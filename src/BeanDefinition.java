import java.util.HashMap;
import java.util.List;

public class BeanDefinition {

    private String id;

    private String beanName;

    private List<HashMap<String, String>> propertyList;

    public List<HashMap<String, String>> getPropertyList() {
        return propertyList;
    }

    public void setPropertyList(List<HashMap<String, String>> propertyList) {
        this.propertyList = propertyList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public Class<?> getBeanClass() throws Exception{
        return Class.forName(beanName);
    }
}
