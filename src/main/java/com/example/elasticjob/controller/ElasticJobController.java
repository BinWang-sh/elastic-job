package com.example.elasticjob.controller;

import com.dangdang.ddframe.job.api.ElasticJob;
import com.example.elasticjob.job.MyDataFlowJob;
import com.example.elasticjob.job.ShowInfoSimpleJob;
import com.example.elasticjob.service.ElasticJobService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class ElasticJobController {

    @Resource
    private ElasticJobService taskJobService;

    @RequestMapping("/addJob")
    public String addJob(@RequestParam("cron") String cron,
                         @RequestParam("jobName") String jobName,
                         @RequestParam("jobType") Integer jobType,
                         @RequestParam("shardCount") Integer shardCount,
                         @RequestParam("shardItem") String shardItem) {
        ElasticJob elJob;

        if(jobType == 0) {
            elJob = new ShowInfoSimpleJob();
        } else if(jobType == 1) {
            elJob = new MyDataFlowJob();
        } else {
            return "JobType error!";
        }

        taskJobService.addTaskJob(jobName, elJob, cron, shardCount, shardItem);
        return "success";
    }
}
