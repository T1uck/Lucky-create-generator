package com.luckyone.maker.meta;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.luckyone.maker.meta.enums.FileGenerateTypeEnum;
import com.luckyone.maker.meta.enums.FileTypeEnum;
import com.luckyone.maker.meta.enums.ModelTypeEnum;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class MetaValidator {
    public static void doValidAndFill(Meta meta) {
        validAndFillMetaRoot(meta);
        validAndFillFileConfig(meta);
        validAndFillModelConfig(meta);
    }

    private static void validAndFillModelConfig(Meta meta) {
        // modelConfig 校验和默认值
        Meta.ModelConfig modelConfig = meta.getModelConfig();
        if (modelConfig == null) {
            return;
        }
        List<Meta.ModelConfig.ModelInfo> modelInfoList = modelConfig.getModels();
        if (!CollectionUtil.isNotEmpty(modelInfoList)) {
            return;
        }
        for (Meta.ModelConfig.ModelInfo modelInfo : modelInfoList) {
            // 如果文件类别为 group, 不校验
            // todo: 可以将 modelInfo 的校验逻辑抽出方法，然后再 group 中复用
            if (StrUtil.isNotEmpty(modelInfo.getGroupKey())) {
                // 生成中间参数
                List<Meta.ModelConfig.ModelInfo> subModelInfoList = modelInfo.getModels();
                String allArgsStr = modelInfo.getModels().stream()
                        .map(subModelInfo -> String.format("\"--%s\"", subModelInfo.getFieldName()))
                        .collect(Collectors.joining(", "));
                modelInfo.setAllArgsStr(allArgsStr);
                continue;
            }
            // 输出路径默认值: 必填
            String fieldName = modelInfo.getFieldName();
            if (StrUtil.isBlank(fieldName)) {
                throw new MetaException("未填写 fieldName");
            }
            String modelInfoType = modelInfo.getType();
            if (StrUtil.isEmpty(modelInfoType)) {
                modelInfo.setType(ModelTypeEnum.STRING.getValue());
            }
        }
    }

    private static void validAndFillFileConfig(Meta meta) {
        // FileConfig 校验和默认值
        Meta.FileConfig fileConfig = meta.getFileConfig();
        if (fileConfig == null) {
            return;
        }
        // SourceRootPath:必填
        String sourceRootPath = fileConfig.getSourceRootPath();
        if (StrUtil.isBlank(sourceRootPath)) {
            throw new MetaException("未填写 sourceRootPath");
        }
        // inputRootPath : .source + sourceRootPath 的最后一个层级
        String inputRootPath = fileConfig.getInputRootPath();
        String defaultInputRootPath = ".source/" + FileUtil.getLastPathEle(Paths.get(sourceRootPath)).getFileName().toString();
        if (StrUtil.isEmpty(inputRootPath)) {
            fileConfig.setInputRootPath(defaultInputRootPath);
        }
        // outputRootPath: 默认为当前路径下的 generated
        String outputRootPath = fileConfig.getOutputRootPath();
        String defaultOutputRootPath = "generated";
        if (StrUtil.isEmpty(outputRootPath)) {
            fileConfig.setOutputRootPath(defaultOutputRootPath);
        }
        // type: 默认为dir
        String fileConfigType = fileConfig.getType();
        String defaultType = FileTypeEnum.DIR.getValue();
        if (StrUtil.isEmpty(fileConfigType)) {
            fileConfig.setType(defaultType);
        }

        // fileInfo 默认值
        List<Meta.FileConfig.FileInfo> files = fileConfig.getFiles();
        if (!CollectionUtil.isNotEmpty(files)) {
            return;
        }
        for (Meta.FileConfig.FileInfo fileInfo : files) {
            // 如果文件类别为 group, 不校验
            // todo: 可以将 fileinfo 的校验逻辑抽出方法，然后再 group 中复用
            if (FileTypeEnum.GROUP.getValue().equals(fileInfo.getType())) {
                continue;
            }
            // inputPath: 必填
            String inputPath = fileInfo.getInputPath();
            if (StrUtil.isBlank(inputPath)) {
                throw new MetaException("未填写 inputPath");
            }
            // outputPath: 默认与 inputPath 相同
            String outputPath = fileInfo.getOutputPath();
            if (StrUtil.isEmpty(outputPath)) {
                fileInfo.setOutputPath(inputPath);
            }
            // type：默认 inputPath 有文件后缀（如 .java）为 file,否则为 dir
            String type = fileInfo.getType();
            if (StrUtil.isBlank(type)) {
                if (StrUtil.isBlank(FileUtil.getSuffix(inputPath))) {
                    fileInfo.setType(FileTypeEnum.DIR.getValue());
                } else {
                    fileInfo.setType(FileTypeEnum.FILE.getValue());
                }
            }
            // generateType: 如果文件结尾不为 ftl ，则默认为 static 否则为 Dynamic
            String generateType = fileInfo.getGenerateType();
            if (StrUtil.isBlank(generateType)) {
                if (inputPath.endsWith(".ftl")) {
                    fileInfo.setGenerateType(FileGenerateTypeEnum.DYNAMIC.getValue());
                } else {
                    fileInfo.setGenerateType(FileGenerateTypeEnum.STATIC.getValue());
                }
            }
        }
    }

    private static void validAndFillMetaRoot(Meta meta) {
        // 基础信息校验和默认值
        String name = StrUtil.blankToDefault(meta.getName(), "my-generator");
        String description = StrUtil.emptyToDefault(meta.getDescription(), "我的模版代码生成器");
        String basePackage = StrUtil.blankToDefault(meta.getBasePackage(), "com.luckyone");
        String version = StrUtil.emptyToDefault(meta.getVersion(), "1.0");
        String author = StrUtil.emptyToDefault(meta.getAuthor(), "luckyone");
        String createTime = StrUtil.emptyToDefault(meta.getCreateTime(), DateUtil.now());

        meta.setDescription(description);
        meta.setBasePackage(basePackage);
        meta.setVersion(version);
        meta.setAuthor(author);
        meta.setName(name);
        meta.setCreateTime(createTime);

    }
}
