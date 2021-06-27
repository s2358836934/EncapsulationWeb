package mymvc;

import java.util.HashMap;
import java.util.Map;

/**
 * model模型   数据模型(注意跟我们自己的MVC区分  数据 用来存入request作用于带走的  map集合)
 * view视图    用来转发展示用的(注意跟我们自己的MVC区分  转发路径-->展示的视图层资源  )
 */
public class ModelAndView {

    //目的是为了将一个map集合和一个String路径包装在一起的

    //属性
    private String viewName;//视图的响应路径
    //属性
    private Map<String,Object> attributeMap = new HashMap();


    //如下这两个方法是给Controller用户使用的
    //设计方法给两个属性存入具体的数据
    public void setViewName(String viewName){
        this.viewName = viewName;
    }
    public void addAttribute(String key,Object value){
        this.attributeMap.put(key,value);
    }


    //如下这三个方法是留给框架小总管使用的
    String getViewName(){
        return this.viewName;
    }
    Object getAttribute(String key){
        return this.attributeMap.get(key);
    }
    Map<String,Object> getAttributeMap(){
        return this.attributeMap;
    }

}
