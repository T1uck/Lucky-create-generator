<!DOCTYPE html>
<html>
<head>
    <title>luckyone开始页面</title>
</head>
<body>
<h1>欢迎来到测试界面</h1>
<ul>
    <#-- 循环渲染导航条 -->
    <#list menuItems as item>
        <li><a href="${item.url}">${item.label}</a> </li>
    </#list>
</ul>
<footer>
    ${currentYear}  luckyone开始页面. All rights reserved.
</footer>
</body>
</html>