package com.lasu.hyperduty.workflow.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.zip.ZipInputStream;

/**
 * 工作流初始化服务
 * 应用启动时自动部署示例流程
 */
@Slf4j
@Component
public class WorkflowInitService implements CommandLineRunner {

    @Autowired
    private RepositoryService repositoryService;

    @Value("classpath*:processes/*.bpmn20.xml")
    private Resource[] processResources;

    @Override
    public void run(String... args) throws Exception {
        log.info("========== 开始初始化工作流流程 ==========");
        
        if (processResources == null || processResources.length == 0) {
            log.info("未找到流程定义文件，跳过部署");
            return;
        }

        for (Resource resource : processResources) {
            try {
                String filename = resource.getFilename();
                log.info("正在部署流程: {}", filename);

                Deployment deployment = repositoryService.createDeployment()
                        .name("示例流程-" + filename)
                        .addInputStream(filename, resource.getInputStream())
                        .deploy();

                log.info("流程部署成功! 部署ID: {}, 部署名称: {}", 
                        deployment.getId(), deployment.getName());
                
            } catch (Exception e) {
                log.error("部署流程失败: {}", resource.getFilename(), e);
            }
        }

        log.info("========== 工作流流程初始化完成 ==========");
    }
}
