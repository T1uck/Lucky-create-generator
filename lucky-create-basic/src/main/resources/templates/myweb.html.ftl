<!DOCTYPE html>
<html>
<head>
    <title>luckyone��ʼҳ��</title>
</head>
<body>
<h1>��ӭ�������Խ���</h1>
<ul>
    <#-- ѭ����Ⱦ������ -->
    <#list menuItems as item>
        <li><a href="${item.url}">${item.label}</a> </li>
    </#list>
</ul>
<footer>
    ${currentYear}  luckyone��ʼҳ��. All rights reserved.
</footer>
</body>
</html>