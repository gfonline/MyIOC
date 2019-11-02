package ioc;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyIOC {

    //存储所有bean的名字
    private List<String> beanNames = new ArrayList<>();

    //存储所有bean的beanDefinition信息
    private HashMap<String, BeanDefinition> beanClassMap = new HashMap<>();

    //存储已经初始化好的bean，从缓存中取出可以直接用
    private HashMap<String, Object> singletonObjects = new HashMap<>();

    //存储原始bean对象，尚未填充属性，用于解决依赖循环
    private HashMap<String, Object> earlySingletonObjects = new HashMap<>() ;

    //存储bean的工厂对象，用于解决依赖循环
    private HashMap<String, Object> singletonFactories = new HashMap<>();

    //存储当前正在创建的bean的名称
    private List<String> singletonsCurrentlyInCreation = new ArrayList<>();

    /***
     *
     * 构造函数，
     * @param location
     * @throws Exception
     */
    public MyIOC (String location) throws Exception{
        this.loadBeans(location);
    }

    /***
     *
     * 遍历xml文件，解析bean标签
     * @param location
     * @throws Exception
     */
    private void loadBeans(String location) throws Exception{
        // 加载xml文件，取出所有子节点列表
        InputStream inputStream = new FileInputStream(location);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = factory.newDocumentBuilder();
        Document doc = docBuilder.parse(inputStream);
        Element root = doc.getDocumentElement();
        NodeList nodes = root.getChildNodes();
        // 遍历所有子节点bean，将子节点bean的ID，class，属性加入到beanDefinition
        for (int i = 0; i < nodes.getLength(); i++){
            BeanDefinition beanDefinition = new BeanDefinition();
            // 如果该子节点有元素，则加入
            Node node = nodes.item(i);
            if (node instanceof Element){
                Element ele = (Element) node;
                String id = ele.getAttribute("id");
                String beanName = ele.getAttribute("class");

                beanDefinition.setId(id);
                beanDefinition.setBeanName(beanName);
                // 获取属性
                NodeList properties = ele.getElementsByTagName("property");
                List<HashMap<String,String>> propertyList = new ArrayList<>();
                HashMap<String,String> propertyMap;
                for (int j = 0; j < properties.getLength(); j++){
                   propertyMap = new HashMap<>();
                   Node propertyNode = properties.item(j);
                   if (propertyNode instanceof Element){
                       Element propertyElement = (Element) propertyNode;
                       String name = propertyElement.getAttribute("name");
                       String value = propertyElement.getAttribute("value");
                       propertyMap.put("propertyName", name);
                       if (value != null && value.length() != 0){
                           propertyMap.put("propertyValue", value);
                           propertyMap.put("propertyType", "string");
                       } else {
                           String ref = propertyElement.getAttribute("ref");
                           propertyMap.put("propertyValue", ref);
                           propertyMap.put("propertyType", "ref");
                       }
                   }
                   propertyList.add(propertyMap);
                }
                beanDefinition.setPropertyList(propertyList);
                beanNames.add(id);
                beanClassMap.put(id, beanDefinition);
            }
        }
    }

    /***
     *
     * 遍历Xml中配置的bean，进行实例化和IOC
     * @throws Exception
     */
    private void doLoadBeanDefinitions() throws Exception{
        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = beanClassMap.get(beanName);
            doGetBean(beanDefinition);
        }
    }

    /**
     *
     * 获得Bean实例，为解决循环依赖问题，
     * 如果在singletonObjects中找到则直接返回bean
     * 若没有找到，则检查是否在创建中
     * @param beanDefinition
     * @throws Exception
     * @return
     */
    private Object doGetBean(BeanDefinition beanDefinition) throws Exception{
        Object bean = null;
        String beanName = beanDefinition.getId();
        Object shareInstance = getSingleton(beanName, true);
        if (shareInstance != null){
            bean = shareInstance;
        } else {
            Object singletonObject = getSingleton(beanDefinition);
            bean = singletonObject;
        }
        return bean;
    }

    /***
     *
     * 先从singletonObjects中获取，
     * 若未命中并且没有在创建中，则返回null
     * 如果在创建中，则先在earlySingletonObjects中寻找，
     * 若仍未命中，则在工厂中寻找，获取成功后将其从工厂中移除，加入earlySingletonObjects中
     * @param beanName
     * @param allowEarlyReference
     * @return bean
     * @throws Exception
     */
    private Object getSingleton(String beanName, boolean allowEarlyReference) throws Exception{
        Object beanObject = singletonObjects.get(beanName);
        if (beanObject == null && singletonsCurrentlyInCreation.contains(beanName)){
            beanObject = earlySingletonObjects.get(beanName);
            if (beanObject == null && allowEarlyReference){
                Object singletonFactory = singletonFactories.get(beanName);
                if (singletonFactory != null){
                    beanObject = singletonFactory;
                    singletonFactories.remove(beanName);
                    earlySingletonObjects.put(beanName, beanObject);
                }
            }
        }
        return beanObject;
    }

    /***
     *
     * 先从缓存中取，如果未命中，则直接创建，并把创建完的bean放入singletonObjects
     * @param beanDefinition
     * @return
     * @throws Exception
     */
    private Object getSingleton(BeanDefinition beanDefinition) throws Exception{
        String beanName = beanDefinition.getId();
        Object singletonObject = singletonObjects.get(beanName);
        if (singletonObject == null){
            singletonObject = createBean(beanDefinition);
            singletonObjects.put(beanName, singletonObject);
            singletonFactories.remove(beanName);
            earlySingletonObjects.remove(beanName);
        }
        return singletonObject;
    }

    /***
     *
     * 实际创建bean的方法。
     * 先把自己的id放入singletonsCurrentlyInCreation，说明正在被创建
     * 再把创建好的实例放入工厂，singletonFactories
     * @param beanDefinition
     * @return bean
     * @throws Exception
     */
    private Object createBean (BeanDefinition beanDefinition) throws Exception{
        String beanName = beanDefinition.getId();
        singletonsCurrentlyInCreation.add(beanName);
        Object bean = beanDefinition.getBeanClass().newInstance();
        if (!singletonObjects.containsKey(beanName)){
            singletonFactories.put(beanName, bean);
            earlySingletonObjects.remove(beanName);
        }
        populateBean(bean, beanDefinition.getPropertyList());
        return bean;
    }

    /***
     *
     * 注入依赖，若属性值为ref则再次运行doGetBean
     * @param bean
     * @param pvs
     * @throws Exception
     */
    private void populateBean (Object bean, List<HashMap<String, String>> pvs) throws Exception{
        for (int i = 0; i < pvs.size(); i++){
            HashMap<String, String> property = pvs.get(i);
            String propName = property.get("propertyName");
            String propValue = property.get("propertyValue");
            String propType = property.get("propertyType");
            Field declaredField = bean.getClass().getDeclaredField(propName);
            if ("string".equals(propType)){
                declaredField.set(bean, propValue);
            } else{
                String beanName = propValue;
                Object beanObject = singletonObjects.get(beanName);
                if (beanObject != null){
                    declaredField.set(bean, beanObject);
                } else {
                    Object refBean = doGetBean(beanClassMap.get(beanName));
                    declaredField.set(bean, refBean);
                }
            }
        }
    }


    /***
     *
     * 向外部提供bean的方法
     * @param id
     * @throws Exception
     * @return bean
     */
    public Object getBean (String id) throws Exception{
        doLoadBeanDefinitions();
        return singletonObjects.get(id);
    }
}
