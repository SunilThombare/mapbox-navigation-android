package com.mapbox.services.android.navigation.v5.utils;

import com.mapbox.directions.v5.models.LegStep;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.services.constants.Constants;
import com.mapbox.services.utils.TextUtils;
import com.mapbox.turf.TurfMeasurement;
import com.mapbox.turf.TurfMisc;

import static com.mapbox.turf.TurfConstants.UNIT_METERS;

public final class MeasurementUtils {

  private MeasurementUtils() {
    // Util classes aren't allowed to be initialized.
  }

  /**
   * Calculates the distance between the users current raw {@link android.location.Location} object
   * to the closest {@link Point} in the {@link LegStep}.
   *
   * @param usersRawLocation {@link Point} the raw location where the user is currently located
   * @param step             {@link LegStep} to calculate the closest point on the step to our
   *                         predicted location
   * @return double in distance meters
   * @since 0.2.0
   */
  public static double userTrueDistanceFromStep(Point usersRawLocation, LegStep step) {
    // Check that the leg step contains geometry.
    if (TextUtils.isEmpty(step.geometry())) {

      return 0;
    }

    // Get the lineString from the step geometry.
    LineString lineString = LineString.fromPolyline(step.geometry(), Constants.PRECISION_6);

    // Make sure that the step coordinates isn't empty. If the points equal each other, the distance
    // is obviously zero, so return 0.
    if (lineString.coordinates().isEmpty()
      || usersRawLocation.equals(lineString.coordinates().get(0))) {
      return 0;
    }

    Feature feature = TurfMisc.pointOnLine(usersRawLocation, lineString.coordinates());
    Point snappedPoint = (Point) feature.geometry();

    if (snappedPoint == null) {
      return 0;
    }
    if (Double.isInfinite(snappedPoint.latitude())
      || Double.isInfinite(snappedPoint.longitude())) {
      return TurfMeasurement.distance(usersRawLocation,
        lineString.coordinates().get(0), UNIT_METERS);
    }

    double distance = TurfMeasurement.distance(usersRawLocation, snappedPoint, UNIT_METERS);
    return Double.isNaN(distance) ? 0d : distance;
  }
}
