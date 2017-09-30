/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package qr_reader_logic.google.zxing.client.android_modified.camera;

import android.graphics.Point;
import android.hardware.Camera;
import android.util.Log;
import android.view.View;

import java.util.*;

/**
 * A class which deals with reading, parsing, and setting the camera parameters
 * which are used to configure the camera hardware.
 */
final public class CameraConfigurationManager {

    private static final String TAG = "CameraConfiguration";
    public static boolean isAlreadyInit = false;
    private static Point initPointScreenResolution;
    private static Point initPointCameraResolution;

    // This is bigger than the size of a small screen, which is still supported.
    // The routine
    // below will still select the default (presumably 320x240) size for these.
    // This prevents
    // accidental selection of very low resolution on some devices.
    private static final int MIN_PREVIEW_PIXELS = 470 * 320; // normal screen
    private static final int MAX_PREVIEW_PIXELS = 1280 * 800;

    private Point screenResolution;
    private Point cameraResolution;
    private View view;

    CameraConfigurationManager(View view) {
        this.view = view;
    }

    /**
     * Reads, one time, values from the camera that are needed by the app.
     * Make view size without stretching. Modified by Dmitry Krekota.
     */
    void initFromCameraParameters(Camera camera) {
        if (!isAlreadyInit) {
            Camera.Parameters parameters = camera.getParameters();
            int width = view.getWidth();
            int height = view.getHeight();
            screenResolution = new Point(width, height);
            cameraResolution = findBestPreviewSizeValue(parameters,
                    screenResolution);
            int differenceX = cameraResolution.x - screenResolution.y;
            int differenceY = cameraResolution.y - screenResolution.x;
            //TODO if differenceX | differenceY < 0
            float scale;
            if (differenceX > differenceY) {
                scale = (float) cameraResolution.x / (float) screenResolution.y;
            } else {
                scale = (float) cameraResolution.y / (float) screenResolution.x;
            }
            int newA = Math.round(cameraResolution.y / scale);
            int newB = Math.round(cameraResolution.x / scale);
            initPointCameraResolution
                    = new Point(cameraResolution.x, cameraResolution.y);
            initPointScreenResolution = new Point(newA, newB);
            CameraManager.minFrameHeight = initPointScreenResolution.y;
            CameraManager.minFrameWidth = initPointScreenResolution.x;
            isAlreadyInit = true;
        } else {
            cameraResolution = initPointCameraResolution;
        }
        screenResolution = initPointScreenResolution;
        android.view.ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = initPointScreenResolution.y;
        params.width = initPointScreenResolution.x;
        view.setLayoutParams(params);
    }

    void setDesiredCameraParameters(Camera camera, boolean safeMode) {
        Camera.Parameters parameters = camera.getParameters();

        if (parameters == null) {
            Log.w(TAG,
                    "Device error: no camera parameters are available. Proceeding without configuration.");
            return;
        }

        Log.i(TAG, "Initial camera parameters: " + parameters.flatten());

        if (safeMode) {
            Log.w(TAG,
                    "In camera config safe mode -- most settings will not be honored");
        }


        List<int[]> supportedPreviewFpsRanges = parameters
                .getSupportedPreviewFpsRange();
        int[] minimumPreviewFpsRange = supportedPreviewFpsRanges.get(0);
        parameters.setPreviewFpsRange(minimumPreviewFpsRange[0],
                minimumPreviewFpsRange[1]);
        String focusMode;
        if (safeMode) {
            focusMode = findSettableValue(
                    parameters.getSupportedFocusModes(),
                    Camera.Parameters.FOCUS_MODE_AUTO);
        } else {
            focusMode = findSettableValue(
                    parameters.getSupportedFocusModes(),
                    "continuous-picture",
                    "continuous-video",
                    Camera.Parameters.FOCUS_MODE_AUTO);
        }
        if (!safeMode && focusMode == null) {
            focusMode = findSettableValue(parameters.getSupportedFocusModes(),
                    Camera.Parameters.FOCUS_MODE_MACRO, "edof");
        }
        if (focusMode != null) {
            parameters.setFocusMode(focusMode);
        }

        parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);
        camera.setParameters(parameters);
    }

    Point getCameraResolution() {
        return cameraResolution;
    }

    Point getScreenResolution() {
        return screenResolution;
    }

    boolean getTorchState(Camera camera) {
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            if (parameters != null) {
                String flashMode = camera.getParameters().getFlashMode();
                return flashMode != null
                        && (Camera.Parameters.FLASH_MODE_ON.equals(flashMode) || Camera.Parameters.FLASH_MODE_TORCH
                        .equals(flashMode));
            }
        }
        return false;
    }

    void setTorch(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        camera.setParameters(parameters);
    }

    private Point findBestPreviewSizeValue(Camera.Parameters parameters,
                                           Point screenResolution) {

        List<Camera.Size> rawSupportedSizes = parameters
                .getSupportedPreviewSizes();
        if (rawSupportedSizes == null) {
            Log.w(TAG,
                    "Device returned no supported preview sizes; using default");
            Camera.Size defaultSize = parameters.getPreviewSize();
            return new Point(defaultSize.width, defaultSize.height);
        }

        // Sort by size, descending
        List<Camera.Size> supportedPreviewSizes = new ArrayList<Camera.Size>(
                rawSupportedSizes);
        Collections.sort(supportedPreviewSizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size a, Camera.Size b) {
                int aPixels = a.height * a.width;
                int bPixels = b.height * b.width;
                if (bPixels < aPixels) {
                    return -1;
                }
                if (bPixels > aPixels) {
                    return 1;
                }
                return 0;
            }
        });

        if (Log.isLoggable(TAG, Log.INFO)) {
            StringBuilder previewSizesString = new StringBuilder();
            for (Camera.Size supportedPreviewSize : supportedPreviewSizes) {
                previewSizesString.append(supportedPreviewSize.width)
                        .append('x').append(supportedPreviewSize.height)
                        .append(' ');
            }
            Log.i(TAG, "Supported preview sizes: " + previewSizesString);
        }

        Point bestSize = null;
        float screenAspectRatio = (float) screenResolution.x
                / (float) screenResolution.y;

        float diff = Float.POSITIVE_INFINITY;
        for (Camera.Size supportedPreviewSize : supportedPreviewSizes) {
            int realWidth = supportedPreviewSize.width;
            int realHeight = supportedPreviewSize.height;
            int pixels = realWidth * realHeight;
            if (pixels < MIN_PREVIEW_PIXELS || pixels > MAX_PREVIEW_PIXELS) {
                continue;
            }
            boolean isCandidatePortrait = realWidth < realHeight;
            int maybeFlippedWidth = isCandidatePortrait ? realHeight
                    : realWidth;
            int maybeFlippedHeight = isCandidatePortrait ? realWidth
                    : realHeight;
            if (maybeFlippedWidth == screenResolution.x
                    && maybeFlippedHeight == screenResolution.y) {
                Point exactPoint = new Point(realWidth, realHeight);
                Log.i(TAG, "Found preview size exactly matching screen size: "
                        + exactPoint);
                return exactPoint;
            }
            float aspectRatio = (float) maybeFlippedWidth
                    / (float) maybeFlippedHeight;
            float newDiff = Math.abs(aspectRatio - screenAspectRatio);
            if (newDiff < diff) {
                bestSize = new Point(realWidth, realHeight);
                diff = newDiff;
            }
        }

        if (bestSize == null) {
            Camera.Size defaultSize = parameters.getPreviewSize();
            bestSize = new Point(defaultSize.width, defaultSize.height);
            Log.i(TAG, "No suitable preview sizes, using default: " + bestSize);
        }

        Log.i(TAG, "Found best approximate preview size: " + bestSize);
        return bestSize;
    }

    private static String findSettableValue(Collection<String> supportedValues,
                                            String... desiredValues) {
        Log.i(TAG, "Supported values: " + supportedValues);
        String result = null;
        if (supportedValues != null) {
            for (String desiredValue : desiredValues) {
                if (supportedValues.contains(desiredValue)) {
                    result = desiredValue;
                    break;
                }
            }
        }
        Log.i(TAG, "Settable value: " + result);
        return result;
    }

}
