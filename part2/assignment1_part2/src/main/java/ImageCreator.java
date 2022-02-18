import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.File;
import java.io.IOException;


public class ImageCreator {
//    public static void main(String[] args) throws IOException {
//        int[] throughput = {431,789,1450,2108};
//        int[] meanResponse = {29,32,39,67};
//        createLineGraph(throughput,"Throughput VS Number of Threads" ,
//                "Number of Threads", "Throughput");
//        createLineGraph(meanResponse,"Mean Response Time VS Number of Threads" ,
//                "Number of Threads", "Mean Response Time");
//    }

    /**
     * A function to produce line graph based on parameters (number of threads is hard coded)
     * @param data the y data corresponding to each column key
     * @param chartName the chart name
     * @param xName the x-axis name
     * @param yName the y-axis name
     */
    public static void createLineGraph(int[] data, String chartName, String xName, String yName) throws IOException {
        DefaultCategoryDataset line_chart_dataset = new DefaultCategoryDataset();
        line_chart_dataset.addValue( data[0] , xName , "32" );
        line_chart_dataset.addValue( data[1] , xName , "64" );
        line_chart_dataset.addValue( data[2] , xName , "128" );
        line_chart_dataset.addValue( data[3] , xName , "256" );

        JFreeChart lineChartObject = ChartFactory.createLineChart(
                chartName,xName,
                yName,
                line_chart_dataset,PlotOrientation.VERTICAL,
                true,true,false);

        int width = 1280;    /* Width of the image */
        int height = 800;   /* Height of the image */
        File lineChart = new File( chartName + ".jpeg");
        ChartUtils.saveChartAsJPEG(lineChart ,lineChartObject, width ,height);
    }

    /**
     * A function to produce time graph based on input
     * @param data the y data corresponding to each time slot
     * @param idName the chart identifier string (e.g., "32 threads")
     */
    public static void createTimeGraph(int[] data, String idName) throws IOException {
        final XYSeries dataSet = new XYSeries("Response Time");
        for ( int i = 0 ; i < data.length ; i++ ) {
            if (data[i] != 0) {
                dataSet.add(i, data[i]);
            }
        }
        final XYSeriesCollection dataset = new XYSeriesCollection( );
        dataset.addSeries( dataSet );
        JFreeChart xylineChart = ChartFactory.createXYLineChart(
                idName + " Response Time VS Time",
                "Time",
                "Response Time",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        int width = 1280;   /* Width of the image */
        int height = 800;  /* Height of the image */
        File XYChart = new File( idName + " Response Time VS Time.jpeg" );
        ChartUtils.saveChartAsJPEG( XYChart, xylineChart, width, height);
    }


}
