import org.apache.spark.api.java.*;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.Function;

public class GameManager_ServerAPI {
  public static void main(String[] args) {
    SparkConf conf = new SparkConf().setAppName("Game Manager Server API");
    JavaSparkContext sc = new JavaSparkContext(conf);

    // Do something

    System.out.println("That's all, folks!");
    sc.stop();
  }
}
