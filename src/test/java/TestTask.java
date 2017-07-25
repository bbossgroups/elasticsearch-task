import com.ai.elasticsearch.job.EsscanTask;
import org.frameworkset.task.TaskService;

public class TestTask {
    public static void main(String[] args){
        TaskService.getTaskService().startService();

        EsscanTask et = new EsscanTask();
//        et.scanIndex("http://elastic:changeme@10.1.236.88:9200","1w");
    }
}
