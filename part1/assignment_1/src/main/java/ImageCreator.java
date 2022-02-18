import java.io.*;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;


public class ImageCreator {
    /**
     * A function to produce hard-coded line graph
     */
    public static void createLineGraph() throws IOException {
        DefaultCategoryDataset line_chart_dataset = new DefaultCategoryDataset();
        line_chart_dataset.addValue( 431425 , "wall time" , "32" );
        line_chart_dataset.addValue( 214369 , "wall time" , "64" );
        line_chart_dataset.addValue( 124637 , "wall time" , "128" );
        line_chart_dataset.addValue( 86116 , "wall time" , "256" );

        JFreeChart lineChartObject = ChartFactory.createLineChart(
                "Wall Time Vs Number of Threads","Number of Threads",
                "Wall Time",
                line_chart_dataset,PlotOrientation.VERTICAL,
                true,true,false);

        int width = 1280;    /* Width of the image */
        int height = 800;   /* Height of the image */
        File lineChart = new File( "part1_wall_time_VS_threads.jpeg" );
        ChartUtils.saveChartAsJPEG(lineChart ,lineChartObject, width ,height);
    }




}
