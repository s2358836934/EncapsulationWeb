package mymvc;

import com.alibaba.fastjson.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.*;
import java.util.*;

/**
 * 隐藏在小总管背后的坚强小弟类
 * 承担了所以事情
 */
public class Handler {

    //属性--->存储请求名字和真实类全名之间的对应关系
    private Map<String,String> realClassNameMap = new HashMap();
    //属性--->存储请求名字和每一个Controller控制层类的对象之间的关系(想让这个对象实现单例管理机制 延迟加载)
    private Map<String,Object> objectMap = new HashMap();
    //属性--->存储某一个Controller对象中的所有方法   对象  一堆方法
    private Map<Object,Map<String,Method>> objectMethodMap = new HashMap();

    //块
    //静态块 那么上面的集合也需要是静态的
    //构造方法
    //本身这个类就是一个标准的Servlet(是被Tomcat容器管理的 自己本身就有机制 单例 生命周期方法 init service destroy)

    //0号小弟
    //单独设计一个小弟方法
    //目的是一次性读取配置文件
    //文件中的请求名和类全名的对应关系读出来 存入缓存
    void loadPropertiesFile(){
        try {
            Properties properties = new Properties();
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("ApplicationContext.properties");
            properties.load(inputStream);
            //获取文件中的全部信息
            Enumeration en = properties.propertyNames();// map.keySet();
            //遍历en中的每一个key
            while(en.hasMoreElements()){
                String key = (String)en.nextElement();//获取某一个key
                String value = properties.getProperty(key);
                //存入那个缓存集合realClassNameMap
                realClassNameMap.put(key,value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //1号小弟
    //负责解析读取到的uri
    //方法的参数 提供一个String类型的uri
    //方法返回值 解析后的请求名
    String parseURI(String uri){
        //解析请求
        String className = uri.substring(uri.lastIndexOf("/")+1,uri.indexOf("."));
        return className;
    }

    //2号小弟
    //负责根据请求名字 最终找到一个Controller控制层的对象
    //方法参数 提供一个String类型的className请求名
    //方法返回值 一个Object对象
    Object findObject(String className) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        //1.上来先去集合里找对象
        Object obj = objectMap.get(className);
        //2.如果obj没有  反射创建啦
        if(obj==null){
            //想要反射找对象 先获取类全名
            String realClassName = realClassNameMap.get(className);
            //可以在这做一个严谨的判断  自定义异常
            if(realClassName==null){
                throw new ControllerNotFoundException(className+"不存在");
            }
            //用类名字获取Class
            Class clazz = Class.forName(realClassName);
            //可以用clazz反射创建对象啦
            obj = clazz.newInstance();
            //这个obj对象存入集合管理起来(单例)
            objectMap.put(className,obj);
            //----------------->
            //创建对象之后 可以解析这个对象中的所有方法啦
            //获取当前clazz中的所有方法
            Method[] methods = clazz.getDeclaredMethods();
            //创建一个map用于存储所有的方法
            Map<String,Method> methodMap = new HashMap();
            for(Method method : methods){
                methodMap.put(method.getName(),method);
            }
            //这个存有所有方法的map集合绑定在对象的后面
            objectMethodMap.put(obj,methodMap);
        }
        return obj;
    }

    //3号小弟
    //负责反射找寻方法
    //方法参数    Object对象(反射获取类)   String类型的方法名
    //方法返回值  Method
    Method findMethod(Object obj, String methodName){
        //可以去那个(对象--方法)集合中寻找方法
        Map<String,Method> methodMap = this.objectMethodMap.get(obj);
        Method method = methodMap.get(methodName);
        return method;
    }

//    //4号小弟
//    //负责处理最终的响应
//    void handleResponse(String result, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        //处理响应
//        request.getRequestDispatcher(result).forward(request,response);
//        //未完待续。。。
//        //做响应信息处理方式的判断
//        //直接响应  String
//        //转发     String路径
//        //重定向    解析冒号  String路径
//        //JSON形式
//    }


    //4.1小弟 负责给4号小弟做支持的  注入基础类型
    private Object injectionNormal(Class parameterClazz,RequestParam paramAnnotation,HttpServletRequest request){
        Object result = null;
        //获取注解里面的key
        String key = paramAnnotation.value();
        //从请求中获取key对应的值
        String value = request.getParameter(key);
        //根据不同类型判断 存就可以啦
        if(parameterClazz==String.class){
            result = value;
        }else if(parameterClazz==int.class || parameterClazz==Integer.class){
            result = new Integer(value);
        }else if(parameterClazz==float.class || parameterClazz==Float.class){
            result = new Float(value);
        }else if(parameterClazz==double.class || parameterClazz==Double.class){
            result = new Double(value);
        }else if(parameterClazz==boolean.class || parameterClazz==Boolean.class){
            result = new Boolean(value);
        }else{
            //如果不行 抛异常
        }
        return result;
    }
    //4.2小弟 负责给4号小弟做支持的  注入map类型
    private Map injectionMap(Object obj,HttpServletRequest request){
        //类型还原
        Map map = (Map)obj;
        //因为map中本身是没有key
        //用请求过来的所有信息作为key就可以啦
        Enumeration en = request.getParameterNames();//获取请求全部的key
        while(en.hasMoreElements()){
            String key = (String)en.nextElement();
            String value = request.getParameter(key);
            map.put(key,value);
        }
        return map;
    }
    //4.3小弟 负责给4号小弟做支持的  注入domain类型
    private Object injectionDomain(Object obj,Class parameterClazz,HttpServletRequest request) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        //domain  属性才是key  分析对象 获取里面的属性名字作为key  去请求中找
        Field[] fields = parameterClazz.getDeclaredFields();
        //获取每一个属性的名字作为key
        for(Field field : fields){
            //直接操作私有属性吧
            field.setAccessible(true);
            //获取每一个私有属性名字
            String filedName = field.getName();
            //用request去取值
            String value = request.getParameter(filedName);
            //将这个value存入domain中的某一个属性里
            Class fieldType = field.getType();
            //找寻属性类型对应的构造方法(基础的时候)
            Constructor con = fieldType.getConstructor(String.class);//char
            //执行这个属性的构造方法 将String类型转化成属性对应的类型
            field.set(obj,con.newInstance(value));
        }
        return obj;
    }

    //4号小弟
    //负责用request接收请求发送过来的参数
    //把这些个参数存入到一个Object[] 交给后面的方法执行时候使用
    //是否需要参数    Method(分析方法的参数 参数回头找请求是否有给我传递过来) request?
    //是否需要返回值  Object[]
    Object[] injectionParameters(Method method , HttpServletRequest request,HttpServletResponse response) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        //1.解析方法上面的所有参数
        Parameter[] parameters = method.getParameters();
        //2.在之前做一个严谨的判断
        if(parameters==null || parameters.length==0){
            return null;
        }
        //3.证明方法是有参数的  肯定需要一个Object[]数组 留着用来装最终的返回值
        Object[] resultValue = new Object[parameters.length];
        //4.遍历方法的每一个参数
        for(int i=0;i<parameters.length;i++){
            Parameter parameter = parameters[i];//某一个参数
            //分析这个参数的数据类型    基础类型String  int  Integer  domain类型   map类型
            Class parameterClazz = parameter.getType();
            //5.先找寻当前这个参数前面是否带有注解
            RequestParam paramAnnotation = parameter.getAnnotation(RequestParam.class);
            //判断注解是否存在
            if(paramAnnotation!=null){//有注解  某一个基础类型 String int
                //找个小弟  负责存入值
                resultValue[i] = this.injectionNormal(parameterClazz,paramAnnotation,request);
            }else{//没有注解 复合类型 map domain
                if(parameterClazz==Map.class || parameterClazz== List.class || parameterClazz==Set.class){
                    //自定义异常 这些类型我处理不了
                }else{//某一种具体的map集合 某一个domain
                    if(parameterClazz==HttpServletRequest.class){
                        resultValue[i] = request;continue;
                    }
                    if(parameterClazz==HttpServletResponse.class){
                        resultValue[i] = response;continue;
                    }
                    Object obj = parameterClazz.newInstance();
                    if(obj instanceof Map){
                        //找小弟 负责存入值
                        resultValue[i] = this.injectionMap(obj,request);
                    }else{//domain
                        //找小弟 负责存入值
                        resultValue[i] = this.injectionDomain(obj,parameterClazz,request);
                    }
                }
            }
        }
        return resultValue;
    }



    //5.1小弟 负责给5号小弟做支持的  负责mv里面的解析
    private void parseModelAndView(ModelAndView mv,HttpServletRequest request){
        //获取mv中的map集合
        Map<String,Object> mvMap = mv.getAttributeMap();
        //集合里面挨个遍历 存入request范畴之内
        Iterator<String> it = mvMap.keySet().iterator();
        while(it.hasNext()){
            String key = it.next();
            Object value = mvMap.get(key);
            //将这一组key-value存入request作用域中
            request.setAttribute(key,value);
        }
    }

    //5.2小弟 负责给5号小弟做支持的  负责String解析
    private void parseString(String methodResult,HttpServletRequest request,HttpServletResponse response) throws IOException, ServletException {
        //严谨性的判断
        if("".equals(methodResult) || "null".equals(methodResult)){
            //抛异常 响应路径有误 请确认
            return ;
        }
        //正常的路径
        String[] value = methodResult.split(":");
        if(value.length == 1){//没有冒号 转发
            request.getRequestDispatcher(methodResult).forward(request,response);
        }else{//重定向
            if("redirect".equals(value[0])){
                response.sendRedirect(value[1]);
            }
        }
    }

    //5号小弟
    //负责处理最终method执行后的返回值
    //  ModelAndView   其实也有一个String   +  集合
    //  String  ---->  路径       AJAX   String不是路径  就是一个单纯的信息(利用注解做标识)   out.write("阿拓")
    //  路径本身还有问题  转发/重定向
    //  JSON
    //  Void

    //分析参数
    // 1.要一个处理结果的返回值 Object result
    // 2.需要那个方法本身(方法上面可能会存在一个ResponseBody的注解标识)
    // 3.正常处理转发重定向 request response
    void finalHandlerResponse(Method method,Object methodResult,HttpServletRequest request,HttpServletResponse response) throws IOException, ServletException {
        //1.判断方法是否有返回值(是否需要框架来帮忙处理返回值)
        if(methodResult!=null){//方法是有返回值 需要我们框架来帮忙处理
            //先判断是否应用了我们提供的方式(ModelAndView)
            if(methodResult instanceof ModelAndView){
                //解析ModelAndView  map集合   String路径
                ModelAndView mv = (ModelAndView)methodResult;
                //找一个小弟 负责将mv搞定  其实只是将map搞定了
                this.parseModelAndView(mv,request);
                //找一个小弟 负责解析String路径
                this.parseString(mv.getViewName(),request,response);
            }else if(methodResult instanceof String){
                //先看一看方法上面是否有注解
                ResponseBody responseBody = method.getAnnotation(ResponseBody.class);
                if(responseBody!=null){//带有注解  用流对象写回浏览器
                    response.setContentType("text/html;charset=UTF-8");
                    response.getWriter().write((String)methodResult);
                }else{//没有注解 就是一个普通的路径
                    String viewName = (String)methodResult;
                    this.parseString(viewName,request,response);
                }
            }else{//可能是个对象 集合 。。。  JSON
                //将返回值转化为一个JSON形式
                ResponseBody responseBody = method.getAnnotation(ResponseBody.class);
                if(responseBody!=null){
                    //用一个JSON转化啦
                    JSONObject jsonObject = new JSONObject();//相当于是一个容器
                    jsonObject.put("jsonObject",methodResult);
                    response.getWriter().write(jsonObject.toJSONString());
                }
            }
        }else{
            System.out.println("OK 不需要我帮忙 那你自己干吧");
        }
    }




}
