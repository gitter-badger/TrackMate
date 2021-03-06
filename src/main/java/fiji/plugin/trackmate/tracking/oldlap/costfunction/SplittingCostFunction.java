package fiji.plugin.trackmate.tracking.oldlap.costfunction;

import static fiji.plugin.trackmate.tracking.TrackerKeys.KEY_ALLOW_TRACK_SPLITTING;
import static fiji.plugin.trackmate.tracking.TrackerKeys.KEY_BLOCKING_VALUE;
import static fiji.plugin.trackmate.tracking.TrackerKeys.KEY_SPLITTING_FEATURE_PENALTIES;
import static fiji.plugin.trackmate.tracking.TrackerKeys.KEY_SPLITTING_MAX_DISTANCE;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicInteger;

import net.imglib2.algorithm.MultiThreadedBenchmarkAlgorithm;
import net.imglib2.algorithm.OutputAlgorithm;
import net.imglib2.multithreading.SimpleMultiThreading;
import Jama.Matrix;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.tracking.TrackableObject;
import fiji.plugin.trackmate.tracking.oldlap.LAPTracker;
import fiji.plugin.trackmate.util.LAPUtils;

/**
 * <p>
 * Splitting cost function used with {@link LAPTracker}.
 *
 * <p>
 * The <b>cost function</b> is determined by the default equation in the
 * TrackMate trackmate, see below.
 * <p>
 * It slightly differs from the Jaqaman article, see equation (5) and (6) in the
 * paper.
 * <p>
 * The <b>thresholds</b> used are:
 * <ul>
 * <li>Must be within a certain number of frames.</li>
 * <li>Must be within a certain distance.</li>
 * </ul>
 *
 * @see LAPUtils#computeLinkingCostFor(Spot, Spot, double, double, Map)
 * @author Nicholas Perry
 * @author Jean-Yves Tinevez
 *
 */
public class SplittingCostFunction< T extends TrackableObject< T >> extends
		MultiThreadedBenchmarkAlgorithm implements OutputAlgorithm< Matrix >
{

	private static final boolean DEBUG = false;

	/** The distance threshold. */
	protected final double maxDist;

	/** The value used to block an assignment in the cost matrix. */
	protected final double blockingValue;

	/** Thresholds for the feature ratios. */
	protected final Map< String, Double > featurePenalties;

	private final boolean allowSplitting;

	protected final List< SortedSet< T >> trackSegments;

	protected final List< T > middlePoints;

	protected Matrix m;

	protected CostCalculator< T > costCalculator;

	/*
	 * CONSTRUCTOR
	 */

	@SuppressWarnings( "unchecked" )
	public SplittingCostFunction( final CostCalculator< T > costCalculator,
			final Map< String, Object > settings,
			final List< SortedSet< T >> trackSegments, final List< T > middlePoints )
	{
		this.maxDist = ( Double ) settings.get( KEY_SPLITTING_MAX_DISTANCE );
		this.blockingValue = ( Double ) settings.get( KEY_BLOCKING_VALUE );
		this.featurePenalties = ( Map< String, Double > ) settings
				.get( KEY_SPLITTING_FEATURE_PENALTIES );
		this.allowSplitting = ( Boolean ) settings.get( KEY_ALLOW_TRACK_SPLITTING );
		this.trackSegments = trackSegments;
		this.middlePoints = middlePoints;
		this.costCalculator = costCalculator;
	}

	/*
	 * METHODS
	 */

	@Override
	public boolean process()
	{
		final long start = System.currentTimeMillis();
		if ( DEBUG )
			System.out
					.println( "-- DEBUG information from SplittingCostFunction --" );

		if ( !allowSplitting )
		{

			m = new Matrix( middlePoints.size(), trackSegments.size(),
					blockingValue );

		}
		else
		{

			// Prepare threads
			final Thread[] threads = SimpleMultiThreading
					.newThreads( numThreads );

			m = new Matrix( middlePoints.size(), trackSegments.size() );

			// Prepare the thread array
			final AtomicInteger ai = new AtomicInteger( 0 );
			for ( int ithread = 0; ithread < threads.length; ithread++ )
			{

				threads[ ithread ] = new Thread(
						"LAPTracker splitting cost thread " + ( 1 + ithread )
								+ "/" + threads.length )
				{

					@Override
					public void run()
					{

						for ( int i = ai.getAndIncrement(); i < middlePoints
								.size(); i = ai.getAndIncrement() )
						{

							final T middle = middlePoints.get( i );

							for ( int j = 0; j < trackSegments.size(); j++ )
							{

								final SortedSet< T > track = trackSegments.get( j );
								final T start = track.first();

								if ( DEBUG )
									System.out.println( "Segment " + j );
								if ( track.contains( middle ) )
								{
									m.set( i, j, blockingValue );
									continue;
								}

								// Frame threshold - middle Spot must be one
								// frame behind of the start Spot
								final int startFrame = start.frame();
								final int middleFrame = middle.frame();
								if ( startFrame - middleFrame != 1 )
								{
									m.set( i, j, blockingValue );
									continue;
								}

								final double cost = costCalculator
										.computeLinkingCostFor( start, middle,
												maxDist, blockingValue,
												featurePenalties );
								m.set( i, j, cost );
							}
						}
					}
				};
			}
			SimpleMultiThreading.startAndJoin( threads );
		}

		final long end = System.currentTimeMillis();
		processingTime = end - start;
		return true;
	}

	@Override
	public boolean checkInput()
	{
		return true;
	}

	@Override
	public Matrix getResult()
	{
		return m;
	}
}
