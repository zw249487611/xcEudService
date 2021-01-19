<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Hello World!</title>
</head>
<body>
Hello ${name}!
</br>
    遍历数据模型中的list学生信息（数据模型中的名称为stus
<table>
    <tr>
        <td>序号</td>
        <td>姓名</td>
        <td>年龄</td>
        <td>金额</td>
        <td>出生日期</td>
    </tr>
    <#if stus??>
    <#list stus as stu>
        <tr>
            <td>${stu_index}</td>
            <td <#if stu.name =='小明'>style="background: blue" </#if>>${stu.name}</td>
            <td>${stu.age}</td>
            <#--<td <#if stu.mondy gt 300 >style="background: chartreuse" </#if>>${stu.mondy}</td>-->
            <td <#if (stu.mondy > 300) >style="background: chartreuse" </#if>>${stu.mondy}</td>
            <#--<td>${stu.birthday?date}</td> -->
            <td>${stu.birthday?string("YYYY年MM月dd日")}</td>
        </tr>
    </#list>
        <br>
        学生的个数：${stus?size}
    </#if>
</table>
</br>
遍历数据模型中的stuMap数据（map数据）
</br>
姓名：${(stuMap['stu1'].name)!''}<br>
年龄：${(stuMap['stu1'].age)!''}<br>
姓名：${stuMap.stu1.name}<br>
年龄：${stuMap.stu1.age}<br>
遍历map中的key.stuMap?keys就是key列表（是一个list)
<br>
<#list stuMap?keys as k>
    姓名：${stuMap[k].name}<br>
    年龄：${stuMap[k].age}<br>

</#list>
<br>
长数字问题:
<#--${point?c}-->
</body>
</html>