package it.cnr.isti.hpclab.example.search.pesos;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import it.cnr.isti.hpclab.example.search.Shard;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

/**
 * This component mimicks a linear regressor with a single variable. 
 * 
 * @author Matteo Catena
 *
 */
class Regressor {

	private double a, b, c;

	public Regressor(double a, double b, double c) {
		super();
		this.a = a;
		this.b = b;
		this.c = c;
	}

	public double regress(int x) {

		return (a * x) + b + c;

	}
}

/**
 * Query efficiency predictors (QEPs) are component used to estimate the processing cost of a query.
 * Processing cost can be expressed as the number of postings to score in order to complete
 * the processing of a query; or directly in term of time (e.g., milliseconds).
 * 
 * See [1] for more details.
 * 
 * [1] Macdonald et al.: Learning to predict response times for online query scheduling. SIGIR 2012
 * 
 * @author Matteo Catena
 */
public class QueryEfficiencyPredictors {

	/**
	 * A map which associate the <number of query terms,CPU core frequency> to the related QEPs.
	 */
	protected Int2ObjectMap<Int2ObjectMap<Regressor>> regressorsMap;

	/**
	 * 
	 * @param frequencies The CPU core frequencies available on the simulated system
	 */
	public QueryEfficiencyPredictors(int[] frequencies) {

		Properties p = new Properties();
		try {
			InputStream is1 = Shard.class.getClassLoader().getResourceAsStream("regressors.txt");
			p.load(new InputStreamReader(is1));
			int queryClasses = Integer.parseInt(p.getProperty("query.classes"));

			regressorsMap = new Int2ObjectArrayMap<>(queryClasses);

			for (int i = 1; i <= queryClasses; i++) {

				regressorsMap.put(i, new Int2ObjectArrayMap<Regressor>(frequencies.length));

				for (int f : frequencies) {

					double a = Double.parseDouble(p.getProperty(f + "." + i + ".alpha"));
					double b = Double.parseDouble(p.getProperty(f + "." + i + ".beta"));
					double c = Double.parseDouble(p.getProperty(f + "." + i + ".rmse"));

					regressorsMap.get(i).put(f, new Regressor(a, b, c));
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Given the number of query term, the number of postings to score, and a CPU core frequency,
	 * estimate the processing time for a query.
	 * @param numOfTerms The number of terms in the query
	 * @param postings The number of postings to score (estimated)
	 * @param frequency The operating frequency of the CPU cores
	 * @return
	 */
	public double regress(int numOfTerms, int postings, int frequency) {

		Regressor regressor = regressorsMap.get(numOfTerms).get(frequency);
		if (regressor != null) {

			return regressor.regress(postings);

		} else {

			return Double.MAX_VALUE;
		}
	}
}