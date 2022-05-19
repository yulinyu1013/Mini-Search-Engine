package indexerwithparser;

public class JobController {

    public static void main(String[] args) {
        //input for parser
        //output for parser -> also input for indexer
        //output for indexer -> no actually need
        //total num of files
        
        
        if(args.length < 4) {
            System.out.println("not enough args provided");
        }
        
        String inputdirForParser = args[0];
        String outputdirForParser = args[1];
        String inputdirForIndexer = args[1];
        String outputdirForIndexer = args[2];
        String totalNumofFile = args[3];
        
        try {
            boolean res = ParserJob.startParseJob(inputdirForParser, outputdirForParser);
            if(!res) {
               System.out.println("parserjob failed");
               return;
            }
            
            boolean indexerres = IndexerJob.startIndexerJob(inputdirForIndexer, outputdirForIndexer, totalNumofFile);
            
            if(!indexerres) {
                System.out.println("indexerjob failed");
            } else {
                System.out.println("jobs finished!!");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        

    }

}
