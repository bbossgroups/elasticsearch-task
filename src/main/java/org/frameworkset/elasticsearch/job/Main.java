package org.frameworkset.elasticsearch.job;

import org.frameworkset.task.TaskService;

public class Main {
    public static void main(String[] args){
        TaskService.getTaskService().startService();
    }
}
