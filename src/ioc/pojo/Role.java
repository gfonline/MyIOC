package ioc.pojo;

public class Role {

    public String id;

    public String name;

    public User user;

    public String toString(){
        return this.id + ":" + this.name + ":" + this.user.name;
    }
}
