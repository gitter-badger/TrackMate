package fiji.plugin.trackmate.features.spot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fiji.plugin.trackmate.Dimension;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.TrackMateModel;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.ImgPlus;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.HyperSliceImgPlus;

public class SpotMorphologyAnalyzerFactory<T extends RealType<T> & NativeType<T>> implements EdgeFeatureAnalyzerFactory<T> {


	/*
	 * CONSTANTS
	 */

	public final static String[] featurelist_sa 	= { "ELLIPSOIDFIT_SEMIAXISLENGTH_C", "ELLIPSOIDFIT_SEMIAXISLENGTH_B", 	"ELLIPSOIDFIT_SEMIAXISLENGTH_A" };
	public final static String[] featurelist_phi 	= { "ELLIPSOIDFIT_AXISPHI_C", 		"ELLIPSOIDFIT_AXISPHI_B", 			"ELLIPSOIDFIT_AXISPHI_A" };
	public final static String[] featurelist_theta = { "ELLIPSOIDFIT_AXISTHETA_C", 	"ELLIPSOIDFIT_AXISTHETA_B", 		"ELLIPSOIDFIT_AXISTHETA_A" }; 
	/** The key name of the morphology feature this analyzer computes. */
	public final static String MORPHOLOGY = "MORPHOLOGY";
	
	public static final ArrayList<String> 			FEATURES = new ArrayList<String>(10);
	public static final HashMap<String, String> 	FEATURE_NAMES = new HashMap<String, String>(10);
	public static final HashMap<String, String> 	FEATURE_SHORT_NAMES = new HashMap<String, String>(10);
	public static final HashMap<String, Dimension> FEATURE_DIMENSIONS = new HashMap<String, Dimension>(10);
	static {
		FEATURES.add(MORPHOLOGY);
		FEATURES.addAll(Arrays.asList(featurelist_sa));
		FEATURES.addAll(Arrays.asList(featurelist_phi));
		FEATURES.addAll(Arrays.asList(featurelist_theta));
		
		FEATURE_NAMES.put(MORPHOLOGY, "Morphology");
		FEATURE_NAMES.put(featurelist_sa[0], "Ellipsoid C semi-axis length");
		FEATURE_NAMES.put(featurelist_sa[1], "Ellipsoid B semi-axis length");
		FEATURE_NAMES.put(featurelist_sa[2], "Ellipsoid A semi-axis length");
		FEATURE_NAMES.put(featurelist_phi[0], "Ellipsoid C axis φ azimuth");
		FEATURE_NAMES.put(featurelist_phi[1], "Ellipsoid B axis φ azimuth");
		FEATURE_NAMES.put(featurelist_phi[2], "Ellipsoid A axis φ azimuth");
		FEATURE_NAMES.put(featurelist_theta[0], "Ellipsoid C axis θ azimuth");
		FEATURE_NAMES.put(featurelist_theta[1], "Ellipsoid B axis θ azimuth");
		FEATURE_NAMES.put(featurelist_theta[2], "Ellipsoid A axis θ azimuth");

		FEATURE_SHORT_NAMES.put(MORPHOLOGY, "Morpho.");
		FEATURE_SHORT_NAMES.put(featurelist_sa[0], "lc");
		FEATURE_SHORT_NAMES.put(featurelist_sa[1], "lb");
		FEATURE_SHORT_NAMES.put(featurelist_sa[2], "la");
		FEATURE_SHORT_NAMES.put(featurelist_phi[0], "φc");
		FEATURE_SHORT_NAMES.put(featurelist_phi[1], "φb");
		FEATURE_SHORT_NAMES.put(featurelist_phi[2], "φa");
		FEATURE_SHORT_NAMES.put(featurelist_theta[0], "θc");
		FEATURE_SHORT_NAMES.put(featurelist_theta[1], "θb");
		FEATURE_SHORT_NAMES.put(featurelist_theta[2], "θa");
		
		FEATURE_DIMENSIONS.put(MORPHOLOGY, Dimension.NONE);
		FEATURE_DIMENSIONS.put(featurelist_sa[0], Dimension.LENGTH);
		FEATURE_DIMENSIONS.put(featurelist_sa[1], Dimension.LENGTH);
		FEATURE_DIMENSIONS.put(featurelist_sa[2], Dimension.LENGTH);
		FEATURE_DIMENSIONS.put(featurelist_phi[0], Dimension.ANGLE);
		FEATURE_DIMENSIONS.put(featurelist_phi[1], Dimension.ANGLE);
		FEATURE_DIMENSIONS.put(featurelist_phi[2], Dimension.ANGLE);
		FEATURE_DIMENSIONS.put(featurelist_sa[0], Dimension.ANGLE);
		FEATURE_DIMENSIONS.put(featurelist_sa[1], Dimension.ANGLE);
		FEATURE_DIMENSIONS.put(featurelist_sa[2], Dimension.ANGLE);

	}
	public static final String KEY = "Spot morphology";
	/** Spherical shape, that is roughly a = b = c. */
	public static final int SPHERE = 0;
	/** Oblate shape, disk shaped, that is roughly a = b > c. */
	public static final int OBLATE = 1;
	/** Prolate shape, rugby ball shape, that is roughly a = b < c. */
	public static final int PROLATE = 2;
	/** Scalene shape, nothing particular, a > b > c. */
	public static final int SCALENE = 3;
	

	private final TrackMateModel<T> model;

	/*
	 * CONSTRUCTOR
	 */
	
	public SpotMorphologyAnalyzerFactory(final TrackMateModel<T> model) {
		this.model = model;
	}
	
	/*
	 * METHODS
	 */

	@Override
	public SpotMorphologyAnalyzer<T> getAnalyzer(int frame, int channel) {
		final ImgPlus<T> img = ImagePlusAdapter.wrapImgPlus(model.getSettings().imp);
		final ImgPlus<T> imgC = HyperSliceImgPlus.fixChannelAxis(img, channel);
		final ImgPlus<T> imgCT = HyperSliceImgPlus.fixTimeAxis(imgC, frame);
		final List<Spot> spots = model.getSpots().get(frame);
		return new SpotMorphologyAnalyzer<T>(imgCT, spots);
	}

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public List<String> getFeatures() {
		return FEATURES;
	}

	@Override
	public Map<String, String> getFeatureShortNames() {
		return FEATURE_SHORT_NAMES;
	}

	@Override
	public Map<String, String> getFeatureNames() {
		return FEATURE_NAMES;
	}

	@Override
	public Map<String, Dimension> getFeatureDimensions() {
		return FEATURE_DIMENSIONS;
	}
}
