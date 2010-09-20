package fiji.plugin.trackmate;

import java.util.EnumMap;

import mpicbg.imglib.algorithm.math.MathLib;

/**
 * Plain implementation of the {@link Spot} interface.
 * @author Jean-Yves Tinevez <jeanyves.tinevez@gmail.com> Sep 16, 2010
 *
 */
public class SpotImp implements Spot {
	
	/*
	 * FIELDS
	 */
	
	public static int IDcounter = 0;
	
	
	/** Store the individual features, and their values. */
	private EnumMap<Feature, Float> features = new EnumMap<Feature, Float>(Feature.class);
	/** A user-supplied name for this spot. */
	private String name;
	/** This spot ID */
	private int ID;

	/*
	 * CONSTRUCTORS
	 */
	
	/**
	 * Instantiate a Spot. 
	 * <p>
	 * The given coordinate float array <b>must</b> have 3 elements. If the 3rd one is not
	 * used (2D case), it can be set to a constant value 0. This constructor ensures that
	 * none of the {@link Spot#POSITION_FEATURES} will be <code>null</code>, and ensure relevance
	 * when calculating distances and so on.
	 */
	public SpotImp(float[] coordinates, String name) {
		for (int i = 0; i < POSITION_FEATURES.length; i++)
			putFeature(POSITION_FEATURES[i], coordinates[i]);
		this.name = name;
		this.ID = IDcounter;
		IDcounter++;
	}
	
	public SpotImp(float[] coordinates) {
		this(coordinates, null);
	}
	
	/*
	 * PUBLIC METHODS
	 */
	
	/**
	 * Convenience method that returns the X, Y and optionally Z feature in a float array.
	 */
	public void getCoordinates(float[] coords) {
		for (int i = 0; i < coords.length; i++)
			coords[i] = getFeature(POSITION_FEATURES[i]);
	}
	
	/**
	 * Returns the name of this Spot.
	 * @return The String name corresponding to this Spot.
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Set the name of this Spot.
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public int ID() {
		return ID;
	}
	
	/**
	 * Return a string representation of this spot, with calculated features.
	 */
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		
		// Name
		if (null == name) 
			s.append("Spot: <no name>\n");
		else
			s.append("Spot: "+name+"\n");
		
		// Frame
		s.append("Frame: "+getFeature(Feature.POSITION_T)+'\n');

		// Coordinates
		float[] coordinates = getPosition(null);
		if (null == coordinates)
			s.append("Position: <no coordinates>\n");
		else 
			s.append("Position: "+MathLib.printCoordinates(coordinates)+"\n");
		
		// Feature list
		if (null == features || features.size() < 1) 
			s.append("No features calculated\n");
		else {
			s.append("Feature list:\n");
			float val;
			for (Feature key : features.keySet()) {
				s.append("\t"+key.toString()+": ");
				val = features.get(key);
				if (val >= 1e4)
					s.append(String.format("%.1g", val));
				else
					s.append(String.format("%.1f", val));
				s.append('\n');
			}
		}
		return s.toString();
	}
	
	public void setFrame(int frame) {
		putFeature(Feature.POSITION_T, frame);
	}
	
	@Override
	public float[] getPosition(float[] position) {
		if (null == position) 
			position = new float[3];
		for (int i = 0; i < 3; i++) 
			position[i] = getFeature(POSITION_FEATURES[i]);
		return position;
	}
	
	/*
	 * FEATURE RELATED METHODS
	 */
	
	
	@Override
	public EnumMap<Feature, Float> getFeatures() {
		return features;
	}
	
	@Override
	public final Float getFeature(final Feature feature) {
		return features.get(feature);
	}
	
	@Override
	public final void putFeature(final Feature feature, final float value) {
		features.put(feature, value);
	}

	@Override
	public Float diffTo(Spot s, Feature feature) {
		return getFeature(feature) - s.getFeature(feature);
	}
	
	@Override
	public Float normalizeDiffTo(Spot s, Feature feature) {
		final Float a = getFeature(feature);
		final Float b = s.getFeature(feature);
		return Math.abs(a-b)/((a+b)/2);
	}

	@Override
	public Float squareDistanceTo(Spot s) {
		Float sumSquared = 0f;
		Float thisVal, otherVal;
		
		for (Feature f : POSITION_FEATURES) {
			thisVal = getFeature(f);
			otherVal = s.getFeature(f);
			sumSquared += ( otherVal - thisVal ) * ( otherVal - thisVal ); 
		}
		return sumSquared;
	}

}
