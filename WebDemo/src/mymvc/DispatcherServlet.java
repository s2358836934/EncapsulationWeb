package mymvc;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * 单独提取出来的一个新类
 * 类中只存放一个service方法
 * 这个总管提取出来了
 */
public class DispatcherServlet extends HttpServlet {

    private Handler handler = new Handler();

    //重写servlet本身的init方法
    @Override
    public void init(ServletConfig config) {
        handler.loadPropertiesFile();
    }

    //最后这个小总管中的方法肯定需要拆分成小弟  每个小弟分别负责一件小事
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            //还是那个小总管
            //1.获取请求的类  String className = getRequestURI---解析
            String uri = request.getRequestURI();//  /请求名
            System.out.println("我来看看是什么样的"+uri);
            //2.获取方法名    String methodName = getParameter("method")
            String methodName = request.getParameter("method");
            //------------------------------------------
            //小总管就可以找小弟做事啦
            //3.在init加载的时候 先找了0号小弟负责读取配置文件信息 成功加载入缓存
            //4.找1号小弟 负责解析uri
            String className = handler.parseURI(uri);
            //5.找2号小弟 负责根据className获取一个Controller控制层的对象
            Object obj = handler.findObject(className);
            //6.找到方法
            Method method = handler.findMethod(obj,methodName);
            //插入一个事情
            //让这个小总管接收请求发送过来的参数值  值 = request.getParameter();
            //有可能值不止一个   好多接收到的  值  ---->  一个容器里 Object[]
            //在下面invoke时候一并传入
            //7.找一个小弟负责注入参数
            Object[] parameterValues = handler.injectionParameters(method,request,response);
            //8.执行方法啦
            Object result = method.invoke(obj,parameterValues);
            //9.处理响应
            handler.finalHandlerResponse(method,result,request,response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}
