package ioc;

import ioc.pojo.Role;
import ioc.pojo.User;

import java.util.HashMap;
import java.util.Map;

public class Test {
    public static void main(String[] args){
        String path = "/Users/gfonline/Desktop/project/MyIOC/src/ioc/ioc.xml";
        try {
            MyIOC test = new MyIOC(path);
            // 测试xml读取是否成功
//            for (String id: test.beanNames) {
//                System.out.println(id);
//            }
//            for (Map.Entry<String, BeanDefinition> bean : test.beanClassMap.entrySet()){
//                System.out.println(bean.getKey() + ":" + bean.getValue());
//                for(HashMap<String, String> properties: bean.getValue().getPropertyList()){
//                    for (Map.Entry<String, String> property : properties.entrySet()){
//                        System.out.println(property.getKey() + ":" + property.getValue());
//                    }
//                }
//            }
            // 测试加载bean以及解决循环依赖
            User user = (User)test.getBean("user");
            Role role = (Role)test.getBean("role");
            System.out.println(user.toString());
            System.out.println(role.toString());
        } catch (Exception e){
            e.printStackTrace();
        }

    }
}
