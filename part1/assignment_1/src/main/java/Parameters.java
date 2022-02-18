import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Parameters {
    /**
     * predefined parameter xml mapping file
     * arg0: maximum number of threads to run (numThreads - max 1024)
     * arg1: number of skier to generate lift rides for (numSkiers - max 100000), This is effectively the skier’s ID (skierID)
     * arg2: number of ski lifts (numLifts - range 5-60, default 40)
     * arg3: mean numbers of ski lifts each skier rides each day (numRuns - default 10, max 20)
     * arg4: IP address and port number of the server
     */
    @XmlElement
    Integer numThreads;
    @XmlElement
    Integer numSkiers;
    @XmlElement
    Integer numLifts;
    @XmlElement
    Integer numRuns;
    @XmlElement
    String InetAddrAndPort;
}
