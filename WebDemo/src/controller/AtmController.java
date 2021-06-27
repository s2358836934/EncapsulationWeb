package controller;

import domain.User;
import mymvc.ModelAndView;
import service.AtmService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AtmController {//继承HttpServlet ---> 单例机制

    private AtmService service = new AtmService();

    //浏览器发送 登录请求(类名 方法名 参数name pass)
    //1.第一个接收请求的人是谁???   DispatcherServlet---->service方法
    //      其实这个小总管有request的，值 = request.getParameter();
    //      让小总管将接受到的请求值  反射调用方法之前传递给方法即可
    //2.小总管通过反射invoke帮我们调用执行的

    //普通Controller类方法中的值通常几种可能
    //1.普通变量  String  int  Integer   自定义注解
    //2.如果传递的参数刚好可以组合成一个对象   domain  对象中属性名字  请求名字对应
    //3.如果组合不成一个对象   Map<String Object>

    //这个小弟处理登录请求
    public ModelAndView login(User user){// 如果这个方法想要接受请求的值 自己写一个变量 用于让框架帮我们做自动注入IOC+DI
        System.out.println("请求到达登录控制层啦"+user);
        //1.接收请求携带的信息

        //2.调用业务层的方法处理真实登录业务
        String result = service.login(user.getName(),user.getPass());

        //3.根据登录结果做响应信息----转发
        ModelAndView mv = new ModelAndView();
        if(result.equals("登录成功")) {
            //return "welcome.jsp";//返回值需要一个处理
            mv.setViewName("welcome.jsp");
        }else{
            //把这一堆键值对  存入一个容器(集合)    返回值
            //想办法把这个存着值的容器交给小总管
            //让小总管将这个值存入request    request.setAttribute("result",result);
            //如果返回给小总管的返回值不止一个   对象(集合 + 路径)
            //现在利用框架提供的一个新的对象来实现ModelAndView
            mv.addAttribute("result",result);
            mv.setViewName("redirect:index.jsp");
        }
        return mv;
    }

    //这个小弟处理查询请求
    public String query(HttpServletResponse response){
        //System.out.println("AtmController的query方法"+name+"--"+pass);
        //1.获取请求信息
//        request.getParameter("name");
        //2.调用业务层方法
        //service.query(name);
        //3.根据查询结果 响应-->什么时候给响应?  方法执行完之后给
        return "";
    }


    //设计一个小弟方法  负责帮我们进行转发
    //是否需要条件?    转发路径告诉我?
    //是否需要返回值?
    private void xiaoDi(String path,HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher(path).forward(request,response);
    }

}
