<%--
  Created by IntelliJ IDEA.
  User: 星星眼
  Date: 2021/6/27 0027
  Time: 16:39
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <title>$Title$</title>
</head>
<body>
<!--
    发送请求的时候 携带三个信息
    请求类型.do
    请求类名
    请求方法名

    AtmController.do?method=login

    请求名字+方法名字
    发送出去目的
    请求名字--类名字--类--对象----对象调用里面的方法--方法执行的结果

-->

${requestScope.result}

<a href="AtmController.do?method=login&name=kl&pass=666">测试1(模拟一个ATM的登录功能)</a><br>
<a href="AtmController.do?method=query&name=kl&pass=123">测试2(模拟一个ATM的查询余额)</a><br>

<a href="ShoppingController.do?method=kind&name=kl&pass=123">测试3(模拟一个购物系统的种类查询)</a><br>
<a href="ShoppingController.do?method=goods&name=kl&pass=123">测试4(模拟一个购物系统的种类添加)</a><br>
</body>
</html>