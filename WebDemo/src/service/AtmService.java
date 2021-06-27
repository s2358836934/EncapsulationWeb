package service;

public class AtmService {

    //具体的登录业务方法支持
    public String login(String name,String pass){
        //找寻dao来进行数据查询
        //Atm atm = dao.selectOne(name);
        System.out.println("业务层登录方法执行啦");
        if("kl".equals(name) && "123".equals(pass)){
            return "登录成功";
        }
        return "用户名或密码错误";
    }
}
