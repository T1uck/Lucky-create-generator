package com.luckyone.maker.template;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.luckyone.maker.meta.Meta;
import com.luckyone.maker.meta.enums.FileGenerateTypeEnum;
import com.luckyone.maker.meta.enums.FileTypeEnum;
import com.luckyone.maker.template.enums.CodeCheckTypeEnums;
import com.luckyone.maker.template.model.*;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class TemplateMaker {

    /**
     * 工作空间的目录
     */
    public static final String WORKSPACE_DIRECTORY = ".temp";

    /**
     * 模板文件的后缀
     */
    public static final String TEMPLATE_FILE_SUFFIX = ".ftl";

    /**
     * 元信息名称
     */
    public static final String META_INFORMATION_NAME = "meta.json";

    /**
     * 制作模版
     * @param templateMakerConfig 模版制作配置
     * @return id
     */
    public static long makeTemplate(TemplateMakerConfig templateMakerConfig){
        Long id = templateMakerConfig.getId();
        Meta meta = templateMakerConfig.getMeta();
        String originProjectPath = templateMakerConfig.getOriginProjectPath();
        TemplateMakerFileConfig templateMakerFileConfig = templateMakerConfig.getFileConfig();
        TemplateMakerModelConfig templateMakerModelConfig = templateMakerConfig.getModelConfig();
        TemplateMakerOutputConfig templateMakerOutputConfig = templateMakerConfig.getOutputConfig();
        return makeTemplate(meta, originProjectPath, templateMakerFileConfig, templateMakerModelConfig,templateMakerOutputConfig, id);
    }

    /**
     * 制作模板
     *
     * @param newMeta 元信息
     * @param originProjectPath 原始项目目录
     * @param templateMakerFileConfig 原始文件列表 + 过滤配置
     * @param templateMakerModelConfig 样式模型参数列表 + 替换配置
     * @param templateMakerOutputConfig 文件输出路径列表
     * @param id id
     * @return id
     */
    public static long makeTemplate(Meta newMeta, String originProjectPath, TemplateMakerFileConfig templateMakerFileConfig, TemplateMakerModelConfig templateMakerModelConfig, TemplateMakerOutputConfig templateMakerOutputConfig, Long id) {
        // 没有 id 则生成
        if (id == null) {
            id = IdUtil.getSnowflakeNextId();
        }

        // 复制目录
        String projectPath = System.getProperty("user.dir");
        // 工作空间目录
        String templatePath = FileUtil.normalize(projectPath + File.separator + WORKSPACE_DIRECTORY + File.separator + id);

        // 是否为首次制作模板
        // 目录不存在，则是首次制作
        if (!FileUtil.exist(templatePath)) {
            FileUtil.mkdir(templatePath);
            FileUtil.copy(originProjectPath, templatePath, true);
        }

        // 一、输入信息
        // 输入文件信息
        String sourceRootPath = FileUtil.loopFiles(new File(templatePath), 1 ,null)
                .stream()
                .filter(File::isDirectory)
                .findFirst()
                .orElseThrow(RuntimeException::new)
                .getAbsolutePath();

        // 注意 win 系统需要对路径进行转义
        sourceRootPath = sourceRootPath.replaceAll("\\\\", "/");

        // 二、生成文件模板
        List<Meta.FileConfig.FileInfo> newFileInfoList = makeFileTemplates(templateMakerFileConfig, templateMakerModelConfig, sourceRootPath);

        // 处理模型信息
        List<Meta.ModelConfig.ModelInfo> newModelInfoList = getModelInfoList(templateMakerModelConfig);

        // 三、使用输入信息来创建meta.json元信息文件
        String metaOutputPath = FileUtil.normalize(templatePath + File.separator + META_INFORMATION_NAME);

        // 如果已有 meta 文件，说明不是第一次制作，则在 meta 基础上进行修改
        if (FileUtil.exist(metaOutputPath)) {
            Meta oldMeta = JSONUtil.toBean(FileUtil.readUtf8String(metaOutputPath), Meta.class);
            BeanUtil.copyProperties(newMeta, oldMeta, CopyOptions.create().ignoreNullValue());
            newMeta = oldMeta;

            // 1. 追加配置参数
            List<Meta.FileConfig.FileInfo> fileInfoList = newMeta.getFileConfig().getFiles();
            fileInfoList.addAll(newFileInfoList);
            List<Meta.ModelConfig.ModelInfo> modelInfoList = newMeta.getModelConfig().getModels();
            modelInfoList.addAll(newModelInfoList);

            // 配置去重
            newMeta.getFileConfig().setFiles(distinctFiles(fileInfoList));
            newMeta.getModelConfig().setModels(distinctModels(modelInfoList));
        } else {
            // 1. 构造配置参数
            Meta.FileConfig fileConfig = new Meta.FileConfig();
            newMeta.setFileConfig(fileConfig);
            fileConfig.setSourceRootPath(sourceRootPath);
            List<Meta.FileConfig.FileInfo> fileInfoList = new ArrayList<>();
            fileConfig.setFiles(fileInfoList);
            fileInfoList.addAll(newFileInfoList);

            Meta.ModelConfig modelConfig = new Meta.ModelConfig();
            newMeta.setModelConfig(modelConfig);
            List<Meta.ModelConfig.ModelInfo> modelInfoList = new ArrayList<>();
            modelConfig.setModels(modelInfoList);
            modelInfoList.addAll(newModelInfoList);
        }
        // 2. 额外的输出配置（文件外层和分组去重）
        if (templateMakerOutputConfig != null){
            if (templateMakerOutputConfig.isRemoveFileFromGroup()) {
                List<Meta.FileConfig.FileInfo> fileInfoList = newMeta.getFileConfig().getFiles();
                newMeta.getFileConfig().setFiles(TemplateMakerUtils.removeFileFromGroup(fileInfoList));
            }
            // 文件外层和分组去重
            if (templateMakerOutputConfig.isRemoveGroupGilesFromRoot()){
                List<Meta.FileConfig.FileInfo> fileInfoList = newMeta.getFileConfig().getFiles();
                newMeta.getFileConfig().setFiles(TemplateMakerUtils.removeGroupFilesFromRoot(fileInfoList));
            }
        }

        // 3. 输出元信息文件
        FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(newMeta), metaOutputPath);
        return id;
    }

    /**
     * 获取模型配置信息
     * @param templateMakerModelConfig 模型配置
     * @return 模型信息
     */
    private static List<Meta.ModelConfig.ModelInfo> getModelInfoList(TemplateMakerModelConfig templateMakerModelConfig) {
        List<TemplateMakerModelConfig.ModelInfoConfig> models = templateMakerModelConfig.getModels();
        // 本次新增的模型配置列表
        List<Meta.ModelConfig.ModelInfo> newModelInfoList = new ArrayList<>();
        // 非空校验判断
        if (templateMakerModelConfig == null) {
            return newModelInfoList;
        }
        if (CollUtil.isEmpty(models)) {
            return newModelInfoList;
        }

        // - 转换为配置接受的 ModelInfo 对象
        List<Meta.ModelConfig.ModelInfo> inputModelInfoList = models.stream().map(modelInfoConfig -> {
            Meta.ModelConfig.ModelInfo modelInfo = new Meta.ModelConfig.ModelInfo();
            BeanUtil.copyProperties(modelInfoConfig, modelInfo);
            return modelInfo;
        }).collect(Collectors.toList());

        // - 如果是模型组
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = templateMakerModelConfig.getModelGroupConfig();
        if (modelGroupConfig != null) {
            Meta.ModelConfig.ModelInfo groupModelInfo = new Meta.ModelConfig.ModelInfo();
            BeanUtil.copyProperties(modelGroupConfig, groupModelInfo);

            // 模型全放到一个分组内
            groupModelInfo.setModels(inputModelInfoList);
            newModelInfoList.add(groupModelInfo);
        } else {
            // 不分组，添加所有的模型信息到列表
            newModelInfoList.addAll(inputModelInfoList);
        }
        return newModelInfoList;
    }

    /**
     * 生成
     * @param templateMakerFileConfig 文件配置
     * @param templateMakerModelConfig 模型配置
     * @param sourceRootPath 源文件配置
     * @return 文件列表
     */
    private static List<Meta.FileConfig.FileInfo> makeFileTemplates(TemplateMakerFileConfig templateMakerFileConfig, TemplateMakerModelConfig templateMakerModelConfig, String sourceRootPath) {
        List<Meta.FileConfig.FileInfo> newFileInfoList = new ArrayList<>();
        // 非空校验
        if (templateMakerFileConfig == null){
            return newFileInfoList;
        }
        List<TemplateMakerFileConfig.FileInfoConfig> fileConfigInfoList = templateMakerFileConfig.getFiles();
        if (CollUtil.isEmpty(fileConfigInfoList)){
            return newFileInfoList;
        }

        // 生成模版文件
        for (TemplateMakerFileConfig.FileInfoConfig fileInfoConfig : fileConfigInfoList) {
            String inputFilePath = fileInfoConfig.getPath();

            // 如果填的是相对路径，要改为绝对路径
//            if (!inputFilePath.startsWith(sourceRootPath)) {
//                inputFilePath = FileUtil.normalize(sourceRootPath + File.separator + inputFilePath);
//            }
            inputFilePath = FileUtil.normalize(sourceRootPath + File.separator + inputFilePath);

            // 获取过滤后的文件列表（不会存在目录）
            List<File> fileList = FileFilter.doFilter(inputFilePath, fileInfoConfig.getFilterConfigList());
            // 不处理已经生成的 FTL 模版文件
            fileList = fileList.stream()
                    .filter(file -> !file.getAbsolutePath().endsWith(TEMPLATE_FILE_SUFFIX))
                    .collect(Collectors.toList());
            for (File file : fileList) {
                Meta.FileConfig.FileInfo fileInfo = makeFileTemplate(templateMakerModelConfig, sourceRootPath, file, fileInfoConfig);
                newFileInfoList.add(fileInfo);
            }
        }

        // 如果是文件组
        TemplateMakerFileConfig.FileGroupConfig fileGroupConfig = templateMakerFileConfig.getFileGroupConfig();
        if (fileGroupConfig != null) {
            String condition = fileGroupConfig.getCondition();
            String groupKey = fileGroupConfig.getGroupKey();
            String groupName = fileGroupConfig.getGroupName();

            // 新增分组配置
            Meta.FileConfig.FileInfo groupFileInfo = new Meta.FileConfig.FileInfo();
            groupFileInfo.setType(FileTypeEnum.GROUP.getValue());
            groupFileInfo.setCondition(condition);
            groupFileInfo.setGroupKey(groupKey);
            groupFileInfo.setGroupName(groupName);
            // 文件全放到一个分组内
            groupFileInfo.setFiles(newFileInfoList);
            newFileInfoList = new ArrayList<>();
            newFileInfoList.add(groupFileInfo);
        }
        return newFileInfoList;
    }

    /**
     * 制作文件模板
     *
     * @param templateMakerModelConfig 模型参数
     * @param sourceRootPath 项目源目录
     * @param inputFile 需要制作模版的文件对象
     * @return 文件信息
     */
    private static Meta.FileConfig.FileInfo makeFileTemplate(TemplateMakerModelConfig templateMakerModelConfig, String sourceRootPath, File inputFile, TemplateMakerFileConfig.FileInfoConfig fileInfoConfig) {
        // 要挖坑的文件绝对路径（用于制作模板）
        // 注意 win 系统需要对路径进行转义
        String fileInputAbsolutePath = FileUtil.normalize(inputFile.getAbsolutePath().replace("\\\\","/"));
        String fileOutputAbsolutePath = FileUtil.normalize(fileInputAbsolutePath + TEMPLATE_FILE_SUFFIX);

        // 文件输入输出相对路径（用于生成配置）
        String fileInputPath = FileUtil.normalize(fileInputAbsolutePath.replace(sourceRootPath + "/", ""));
        String fileOutputPath = FileUtil.normalize(fileInputPath + TEMPLATE_FILE_SUFFIX);

        // 2.基于字符串替换算法，使用模型参数的字段名称来替换原始文件的指定内容，并使用替换后的内容来创建FTL动态模版文件
        String fileContent;
        // 如果已有模板文件，说明不是第一次制作，则在模板基础上再次挖坑
        boolean hasTemplateFile = FileUtil.exist(fileOutputAbsolutePath);
        if (hasTemplateFile) {
            fileContent = FileUtil.readUtf8String(fileOutputAbsolutePath);
        } else {
            fileContent = FileUtil.readUtf8String(fileInputAbsolutePath);
        }

        // 支持多个模型：对同一个文件的内容，遍历模型进行多轮替换
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = templateMakerModelConfig.getModelGroupConfig();
        String newFileContent = fileContent;
        for (TemplateMakerModelConfig.ModelInfoConfig modelInfoConfig : templateMakerModelConfig.getModels()) {
            String fieldName = modelInfoConfig.getFieldName();
            String replacement;
            // 不是分组
            if (modelGroupConfig == null) {
                replacement = String.format("${%s}", fieldName);
            } else {
                // 是分组
                String groupKey = modelGroupConfig.getGroupKey();
                // 注意挖坑要多一个层级
                replacement = String.format("${%s.%s}", groupKey, fieldName);
            }
            // 多次替换
            newFileContent = StrUtil.replace(newFileContent, modelInfoConfig.getReplaceText(), replacement);
        }

        // 路径替换
        TemplateMakerModelConfig.ModelInfoConfig fileDirPathConfig = templateMakerModelConfig.getFileDirPathConfig();
        if(fileDirPathConfig!=null){
            String[] inputPathAndFileSuffix = fileInputPath.split("\\.");
            if(inputPathAndFileSuffix.length > 1){
                fileInputPath =
                        inputPathAndFileSuffix[0].replace("/", ".")
                                .replace(fileDirPathConfig.getReplaceText(), "{" + fileDirPathConfig.getFieldName() + "}")
                                .replace(".", "/");
                for (int i = 1; i < inputPathAndFileSuffix.length; i++) {
                    fileInputPath += "." + inputPathAndFileSuffix[i];
                }
            }

        }
        // 控制代码是否生成 逻辑
        List<TemplateMakerFileConfig.ControlCodeInfoConfig> codeConfigList = fileInfoConfig.getControlCodeConfigList();
        if (CollUtil.isNotEmpty(codeConfigList)) {
            for (TemplateMakerFileConfig.ControlCodeInfoConfig infoConfig : codeConfigList) {
                String controlCode = infoConfig.getControlCode();
                boolean conditionExist = infoConfig.isConditionExist();
                String condition = infoConfig.getCondition();
                String codeCheckType = infoConfig.getCodeCheckType();
                CodeCheckTypeEnums codeCheckTypeEnum = CodeCheckTypeEnums.getEnumByValue(codeCheckType);
                String replaceCodeContext = null;
                switch (codeCheckTypeEnum){
                    case EQUALS:
                        replaceCodeContext = String.format("<#if %s>\n %s \n</#if>", conditionExist ? condition : "!" + condition, controlCode);
                        break;
                    case REGEX:
                        // 字符串中根据正则表达式查找内容  如果是正则 我们需要先找到正则匹配的内容 然后替换
                        String fullControlCode = ReUtil.get(controlCode, fileContent, 0);
                        replaceCodeContext = String.format("<#if %s>\n %s \n</#if>", conditionExist ? condition : "!" + condition, fullControlCode);
                        controlCode = fullControlCode;
                        break;
                    case REGEX_ALL:
                        List<String> fullControlCodes = ReUtil.findAll( controlCode,fileContent,0);
                        for (String code : fullControlCodes) {
                            replaceCodeContext = String.format("<#noparse> %s </#noparse>", code);
                            controlCode = code;
                            // 判断目前内容中是否已有我们需要生成的内容 如果有就不操作了 否则执行替换
                            boolean contains = StrUtil.contains(newFileContent, replaceCodeContext);
                            if (!contains) {
                                newFileContent = StrUtil.replace(newFileContent, controlCode, replaceCodeContext);
                            }
                        }
                        continue;
                    default:
                        break;
                }
                // 判断目前内容中是否已有我们需要生成的内容 如果有就不操作了 否则执行替换
                boolean contains = StrUtil.contains(newFileContent, replaceCodeContext);
                if (!contains) {
                    newFileContent = StrUtil.replace(newFileContent, controlCode, replaceCodeContext);
                }

            }
        }

        // 文件配置信息
        Meta.FileConfig.FileInfo fileInfo = new Meta.FileConfig.FileInfo();
        // 注意文件输入路径和输出路径要反转
        fileInfo.setInputPath(fileOutputPath);
        fileInfo.setOutputPath(fileInputPath);
        fileInfo.setCondition(fileInfoConfig.getCondition());
        fileInfo.setType(FileTypeEnum.FILE.getValue());
        // 默认生成类型为动态
        fileInfo.setGenerateType(FileGenerateTypeEnum.DYNAMIC.getValue());

        // 和原文件一致，没有挖坑，则为静态生成
        boolean contentEquals = newFileContent.equals(fileContent);
        // 之前不存在模版文件，并且没有更改文件内容，则为静态生成
        // 存在模板
        if (hasTemplateFile) {
            // 存在模板 而且又新增了新的“坑”
            if (!contentEquals) {
                FileUtil.writeUtf8String(newFileContent, fileOutputAbsolutePath);
            }
        } else {
            // 不存在模板 而且没有更改过文件内容
            if (contentEquals) {
                fileInfo.setGenerateType(FileGenerateTypeEnum.STATIC.getValue());
                fileInfo.setInputPath(fileInputPath);
            } else {
                FileUtil.writeUtf8String(newFileContent, fileOutputAbsolutePath);
            }
        }
        return fileInfo;
    }

    /**
     * 模型去重
     *
     * @param modelInfoList 模型列表
     * @return 去重后的模型列表
     */
    private static List<Meta.ModelConfig.ModelInfo> distinctModels(List<Meta.ModelConfig.ModelInfo> modelInfoList) {
        // 策略：同分组内模型 merge，不同分组保留

        // 1. 有分组的，以组为单位划分
        Map<String, List<Meta.ModelConfig.ModelInfo>> groupKeyModelInfoListMap = modelInfoList
                .stream()
                .filter(modelInfo -> StrUtil.isNotBlank(modelInfo.getGroupKey()))
                .collect(
                        Collectors.groupingBy(Meta.ModelConfig.ModelInfo::getGroupKey)
                );


        // 2. 同组内的模型配置合并
        // 保存每个组对应的合并后的对象 map
        Map<String, Meta.ModelConfig.ModelInfo> groupKeyMergedModelInfoMap = new HashMap<>();
        for (Map.Entry<String, List<Meta.ModelConfig.ModelInfo>> entry : groupKeyModelInfoListMap.entrySet()) {
            List<Meta.ModelConfig.ModelInfo> tempModelInfoList = entry.getValue();
            // 将每个组里面的很多models合并成一个集合 如果有重复的模型 保留后面出现的那个
            List<Meta.ModelConfig.ModelInfo> newModelInfoList = new ArrayList<>(tempModelInfoList.stream()
                    .flatMap(modelInfo -> modelInfo.getModels().stream())
                    .collect(
                            Collectors.toMap(Meta.ModelConfig.ModelInfo::getFieldName, o -> o, (e, r) -> r)
                    ).values());

            // 获取最后一个组 因为可能groupKey 相同但是我修改了groupName 所以我们使用最后一次更新的groupN
            Meta.ModelConfig.ModelInfo newModelInfo = CollUtil.getLast(tempModelInfoList);
            newModelInfo.setModels(newModelInfoList);
            String groupKey = entry.getKey();
            groupKeyMergedModelInfoMap.put(groupKey, newModelInfo);
        }

        // 3. 将模型分组添加到结果列表
        List<Meta.ModelConfig.ModelInfo> resultList = new ArrayList<>(groupKeyMergedModelInfoMap.values());

        // 4. 将未分组的模型添加到结果列表
        List<Meta.ModelConfig.ModelInfo> noGroupModelInfoList = modelInfoList.stream()
                .filter(modelInfo -> StrUtil.isBlank(modelInfo.getGroupKey()))
                .collect(Collectors.toList());
        resultList.addAll(new ArrayList<>(noGroupModelInfoList.stream()
                .collect(
                        Collectors.toMap(Meta.ModelConfig.ModelInfo::getFieldName, o -> o, (e, r) -> r)
                ).values()));
        return resultList;
    }

    /**
     * 文件去重
     *
     * @param fileInfoList 文件列表
     * @return 去重后的文件列表
     */
    private static List<Meta.FileConfig.FileInfo> distinctFiles(List<Meta.FileConfig.FileInfo> fileInfoList) {
        // 策略：同分组内文件 merge，不同分组保留
        // 1. 有分组的，以组为单位划分
        Map<String, List<Meta.FileConfig.FileInfo>> groupKeyFileInfoListMap = fileInfoList
                .stream()
                .filter(fileInfo -> StrUtil.isNotBlank(fileInfo.getGroupKey()))
                .collect(
                        Collectors.groupingBy(Meta.FileConfig.FileInfo::getGroupKey)
                );

        // 2. 同组内的文件配置合并
        // 保存每个组对应的合并后的对象 map
        Map<String, Meta.FileConfig.FileInfo> groupKeyMergedFileInfoMap = new HashMap<>();
        for (Map.Entry<String, List<Meta.FileConfig.FileInfo>> entry : groupKeyFileInfoListMap.entrySet()) {
            List<Meta.FileConfig.FileInfo> tempFileInfoList = entry.getValue();
            List<Meta.FileConfig.FileInfo> newFileInfoList = new ArrayList<>(tempFileInfoList.stream()
                    .flatMap(fileInfo -> fileInfo.getFiles().stream())
                    .collect(
                            Collectors.toMap(Meta.FileConfig.FileInfo::getOutputPath, o -> o, (e, r) -> r)
                    ).values());

            // 使用新的 group 配置
            Meta.FileConfig.FileInfo newFileInfo = CollUtil.getLast(tempFileInfoList);
            newFileInfo.setFiles(newFileInfoList);
            String groupKey = entry.getKey();
            groupKeyMergedFileInfoMap.put(groupKey, newFileInfo);
        }

        // 3. 将文件分组添加到结果列表
        List<Meta.FileConfig.FileInfo> resultList = new ArrayList<>(groupKeyMergedFileInfoMap.values());

        // 4. 将未分组的文件添加到结果列表
        List<Meta.FileConfig.FileInfo> noGroupFileInfoList = fileInfoList.stream().filter(fileInfo -> StrUtil.isBlank(fileInfo.getGroupKey()))
                .collect(Collectors.toList());
        resultList.addAll(new ArrayList<>(noGroupFileInfoList.stream()
                .collect(
                        Collectors.toMap(Meta.FileConfig.FileInfo::getInputPath, o -> o, (e, r) -> r)
                ).values()));
        return resultList;
    }


}
