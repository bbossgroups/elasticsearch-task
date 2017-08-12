import org.frameworkset.task.TaskService;

public class TestTask {
    public static void main(String[] args) throws Exception {
        TaskService.getTaskService().startService();
        //HttpRequestUtil.httpGetforString("http://10.154.50.239:9200/");
    }
}
