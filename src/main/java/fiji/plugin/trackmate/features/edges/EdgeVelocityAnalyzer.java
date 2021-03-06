package fiji.plugin.trackmate.features.edges;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import net.imglib2.algorithm.MultiThreaded;
import net.imglib2.multithreading.SimpleMultiThreading;

import org.jgrapht.graph.DefaultWeightedEdge;

import fiji.plugin.trackmate.Dimension;
import fiji.plugin.trackmate.FeatureModel;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.TrackmateConstants;
import fiji.plugin.trackmate.tracking.TrackableObject;
import fiji.plugin.trackmate.util.FeatureHolderUtils;

public class EdgeVelocityAnalyzer< T extends TrackableObject< T >> implements
		EdgeAnalyzer< T >, MultiThreaded
{

	public static final String KEY = "Edge velocity";

	/*
	 * FEATURE NAMES
	 */
	public static final String VELOCITY = "VELOCITY";

	public static final String DISPLACEMENT = "DISPLACEMENT";

	public static final List< String > FEATURES = new ArrayList< String >( 2 );

	public static final Map< String, String > FEATURE_NAMES = new HashMap< String, String >( 2 );

	public static final Map< String, String > FEATURE_SHORT_NAMES = new HashMap< String, String >( 2 );

	public static final Map< String, Dimension > FEATURE_DIMENSIONS = new HashMap< String, Dimension >( 2 );

	public static final Map< String, Boolean > IS_INT = new HashMap< String, Boolean >( 2 );

	static
	{
		FEATURES.add( VELOCITY );
		FEATURES.add( DISPLACEMENT );

		FEATURE_NAMES.put( VELOCITY, "Velocity" );
		FEATURE_NAMES.put( DISPLACEMENT, "Displacement" );

		FEATURE_SHORT_NAMES.put( VELOCITY, "V" );
		FEATURE_SHORT_NAMES.put( DISPLACEMENT, "D" );

		FEATURE_DIMENSIONS.put( VELOCITY, Dimension.VELOCITY );
		FEATURE_DIMENSIONS.put( DISPLACEMENT, Dimension.LENGTH );

		IS_INT.put( VELOCITY, Boolean.FALSE );
		IS_INT.put( DISPLACEMENT, Boolean.FALSE );
	}

	private int numThreads;

	private long processingTime;

	/*
	 * CONSTRUCTOR
	 */

	public EdgeVelocityAnalyzer()
	{
		setNumThreads();
	}

	@Override
	public boolean isLocal()
	{
		return true;
	}

	@Override
	public void process( final Collection< DefaultWeightedEdge > edges, final Model< T > model )
	{

		if ( edges.isEmpty() ) { return; }

		final FeatureModel< T > featureModel = model.getFeatureModel();

		final ArrayBlockingQueue< DefaultWeightedEdge > queue = new ArrayBlockingQueue< DefaultWeightedEdge >( edges.size(), false, edges );

		final Thread[] threads = SimpleMultiThreading.newThreads( numThreads );
		for ( int i = 0; i < threads.length; i++ )
		{
			threads[ i ] = new Thread( "EdgeVelocityAnalyzer thread " + i )
			{
				@Override
				public void run()
				{
					DefaultWeightedEdge edge;
					while ( ( edge = queue.poll() ) != null )
					{
						final T source = model.getTrackModel().getEdgeSource( edge );
						final T target = model.getTrackModel().getEdgeTarget( edge );

						final double dx = FeatureHolderUtils.diffTo( target, source, TrackmateConstants.POSITION_X );
						final double dy = FeatureHolderUtils.diffTo( target, source, TrackmateConstants.POSITION_Y );
						final double dz = FeatureHolderUtils.diffTo( target, source, TrackmateConstants.POSITION_Z );
						final double dt = FeatureHolderUtils.diffTo( target, source, TrackmateConstants.POSITION_T );
						final double D = Math.sqrt( dx * dx + dy * dy + dz * dz );
						final double V = D / Math.abs( dt );

						featureModel.putEdgeFeature( edge, VELOCITY, V );
						featureModel.putEdgeFeature( edge, DISPLACEMENT, D );
					}

				}
			};
		}

		final long start = System.currentTimeMillis();
		SimpleMultiThreading.startAndJoin( threads );
		final long end = System.currentTimeMillis();
		processingTime = end - start;
	}

	@Override
	public int getNumThreads()
	{
		return numThreads;
	}

	@Override
	public void setNumThreads()
	{
		this.numThreads = Runtime.getRuntime().availableProcessors();
	}

	@Override
	public void setNumThreads( final int numThreads )
	{
		this.numThreads = numThreads;

	}

	@Override
	public long getProcessingTime()
	{
		return processingTime;
	}

	@Override
	public List< String > getFeatures()
	{
		return FEATURES;
	}

	@Override
	public Map< String, String > getFeatureShortNames()
	{
		return FEATURE_SHORT_NAMES;
	}

	@Override
	public Map< String, String > getFeatureNames()
	{
		return FEATURE_NAMES;
	}

	@Override
	public Map< String, Dimension > getFeatureDimensions()
	{
		return FEATURE_DIMENSIONS;
	}

	@Override
	public Map< String, Boolean > getIsIntFeature()
	{
		return IS_INT;
	}

	@Override
	public boolean isManualFeature()
	{
		return false;
	}

}
