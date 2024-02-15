# ${name}

> ${description}
>
> 作者：${author}
>
> 基于 luckyone 的 [lucky生成器项目](https://github.com/T1uck/Lucky-create-generator) 制作，感谢您的使用！

可以通过命令行交互式输入的方式动态生成想要的项目代码

## 使用说明

执行项目根目录下的脚本文件：

```
generator <命令> <选项参数>
```

示例命令：

```
generator generate <#list modelConfig.models as modelInfo><#if modelInfo.abbr??>-${modelInfo.abbr}</#if> </#list>
```

## 参数说明
<#macro generateDes modelInfo>
类型：${modelInfo.type}

描述：${modelInfo.description}

默认值：${modelInfo.defaultValue?c}

<#if modelInfo.abbr??>
缩写： -${modelInfo.abbr}
</#if>

</#macro>

<#list modelConfig.models as modelInfo>
<#if modelInfo.groupKey??>

${modelInfo?index + 1}）${modelInfo.groupKey}

分组名称：${modelInfo.groupName}

类型：${modelInfo.type}

描述：${modelInfo.description}

<#if modelInfo.condition??>
是否生成组内模版文件：${modelInfo.condition}
</#if>

组内参数如下：
<#list modelInfo.models as subModelInfo>

${subModelInfo?index + 1}）${subModelInfo.fieldName}

<@generateDes modelInfo=subModelInfo/>
</#list>
<#else>

${modelInfo?index + 1}）${modelInfo.fieldName}

<@generateDes modelInfo=modelInfo/>
</#if>
</#list>