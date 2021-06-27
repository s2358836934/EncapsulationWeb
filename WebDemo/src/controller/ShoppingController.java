package controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

public class ShoppingController {

    public String kind(HashMap map){
        System.out.println("这是ShoppingController类中的kind方法:"+map);
        return "";
    }

    public String goods(HttpServletRequest request, HttpServletResponse response){
        System.out.println("这是ShoppingController类中的goods方法");
        System.out.println(request);
        System.out.println(response);
        System.out.println(request.getParameter("name"));
        System.out.println(request.getParameter("pass"));
        return "";
    }
}
