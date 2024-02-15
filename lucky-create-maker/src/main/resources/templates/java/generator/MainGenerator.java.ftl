package ${basePackage}.generator;

import ${basePackage}.model.DataModel;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

<#-- 代码复用组件 indent为缩进 fileInfo为复用文件-->
<#macro generatrFile indent fileInfo>
${indent}inputPath = new File(inputRootPath, "${fileInfo.inputPath}").getAbsolutePath();
${indent}outputPath = new File(outputRootPath, "${fileInfo.outputPath}").getAbsolutePath();
<#if fileInfo.generateType == "static">
${indent}StaticGenerator.copyFilesByHutool(inputPath, outputPath);
<#else>
${indent}DynamicGenerator.doGenerate(inputPath, outputPath, model);
</#if>
</#macro>

/**
 * 核心代码生成器
 */
public class MainGenerator {
    public static void doGenerate(DataModel model) throws TemplateException, IOException {
        // 输入路径
        String inputRootPath = "${fileConfig.inputRootPath}";
        String outputRootPath = "${fileConfig.outputRootPath}";

        String inputPath;
        String outputPath;

    <#list modelConfig.models as modelInfo>
        <#--有分组-->
        <#if modelInfo.groupKey??>
        <#list modelInfo.models as subModelInfo>
        ${subModelInfo.type} ${subModelInfo.fieldName} = model.${modelInfo.groupKey}.${subModelInfo.fieldName};
        </#list>
        <#else>
        ${modelInfo.type} ${modelInfo.fieldName} = model.${modelInfo.fieldName};
        </#if>
    </#list>

    <#list fileConfig.files as fileInfo>
        <#if fileInfo.groupKey??>

        <#if fileInfo.condition??>
        if(${fileInfo.condition}) {
            <#list fileInfo.files as fileInfo>
            <@generatrFile fileInfo=fileInfo indent="            "/>
            </#list>
        }

        <#else>
        <#list fileInfo.files as fileInfo>
        <@generatrFile fileInfo=fileInfo indent="        "/>
        </#list>
        </#if>
        <#else>
        <#if fileInfo.condition??>
        if(${fileInfo.condition}) {
           <@generatrFile fileInfo=fileInfo indent="            "/>
        }
        <#else>
            <@generatrFile fileInfo=fileInfo indent="        "/>
        </#if>
        </#if>
    </#list>
    System.out.println("文件生成完毕！文件生成路径为：${fileConfig.outputRootPath}");
    }
}