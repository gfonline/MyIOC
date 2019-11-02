package ioc.pojo;

public class User {
    public String id;

    public String name;

    public String age;

    public Role role;

    public String toString(){
        return this.id + ":" + this.name + ":" + this.age + ":" + role.id + ":" + role.name;
    }
}
