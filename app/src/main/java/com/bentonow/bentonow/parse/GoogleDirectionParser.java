package com.bentonow.bentonow.parse;

import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.model.map.StepModel;
import com.bentonow.bentonow.model.map.WaypointModel;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by kokusho on 3/4/16.
 */
public class GoogleDirectionParser extends MainParser {

    public static final String TAG = "GoogleDirectionParser";

    public static WaypointModel parseDirections(String sResponse) {

        JSONObject jsonDirection;

        WaypointModel mWaypoint = null;

        try {
            jsonDirection = new JSONObject(sResponse);
            mWaypoint = new WaypointModel();

            if (parseSection(jsonDirection, "routes")) {
                JSONArray jsonRoutes = jsonDirection.getJSONArray("routes");
                JSONObject jsonRoute = jsonRoutes.getJSONObject(0);

                if (parseSection(jsonRoute, "legs")) {
                    JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
                    JSONObject jsonLeg = jsonLegs.getJSONObject(0);

                    if (parseSection(jsonLeg, "distance")) {
                        JSONObject jsonDistance = jsonLeg.getJSONObject("distance");
                        mWaypoint.setDistance(jsonDistance.getInt("value"));
                    }
                    if (parseSection(jsonLeg, "duration")) {
                        JSONObject jsonDuration = jsonLeg.getJSONObject("duration");
                        mWaypoint.setDuration(jsonDuration.getInt("value"));
                    }
                    if (parseSection(jsonLeg, "steps")) {
                        JSONArray jsonSteps = jsonLeg.getJSONArray("steps");

                        for (int a = 0; a < jsonSteps.length(); a++) {
                            JSONObject jsonStep = jsonSteps.getJSONObject(a);

                            StepModel mStep = new StepModel();

                            if (parseSection(jsonStep, "distance")) {
                                JSONObject jsonStepDistance = jsonStep.getJSONObject("distance");
                                mStep.setDistance(jsonStepDistance.getInt("value"));
                            }

                            if (parseSection(jsonStep, "duration")) {
                                JSONObject jsonStepDistance = jsonStep.getJSONObject("duration");
                                mStep.setDuration(jsonStepDistance.getInt("value"));
                            }

                            if (parseSection(jsonStep, "end_location")) {
                                JSONObject jsonStepDistance = jsonStep.getJSONObject("end_location");
                                mStep.setEnd_location_lat(jsonStepDistance.getDouble("lat"));
                                mStep.setEnd_location_lng(jsonStepDistance.getDouble("lng"));
                            }

                            if (parseSection(jsonStep, "start_location")) {
                                JSONObject jsonStepDistance = jsonStep.getJSONObject("start_location");
                                mStep.setStart_location_lat(jsonStepDistance.getDouble("lat"));
                                mStep.setStart_location_lng(jsonStepDistance.getDouble("lng"));
                            }

                            if (parseSection(jsonStep, "polyline")) {
                                JSONObject jsonStepDistance = jsonStep.getJSONObject("polyline");
                                mStep.setPolyline(jsonStepDistance.getString("points"));
                            }

                            mWaypoint.getaSteps().add(mStep);
                        }
                    }
                }
                if (parseSection(jsonRoute, "overview_polyline")) {
                    JSONObject jsonDuration = jsonRoute.getJSONObject("overview_polyline");
                    mWaypoint.setPoints(jsonDuration.getString("points"));
                }
            }

        } catch (Exception ex) {
            DebugUtils.logDebug(TAG, ex);
            return mWaypoint;
        }


        return mWaypoint;

    }
}
