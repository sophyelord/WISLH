import weka.classifiers.lazy.IBk;
import weka.core.neighboursearch.LinearNNSearch;

public class ImprovedClassifiers {

	public IBk improvedIBk() {
		
		IBk ibk = new IBk();
		LinearNNSearch lnns = new LinearNNSearch() {
			
		};
		
		ibk.setNearestNeighbourSearchAlgorithm(lnns);
		
		return null;
		
	}
}
