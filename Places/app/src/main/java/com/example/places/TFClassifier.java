package com.example.places;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

import com.example.places.ml.Model;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class TFClassifier {

    private final static String TAG = "TFClassifier";
    private Context context;
    private TensorImage inputImageBuffer;
    List<String> associatedAxisLabels;

    public TFClassifier(Context context) {
        this.context = context;
        associatedAxisLabels = new ArrayList<>();
        associatedAxisLabels.add("italian");
        associatedAxisLabels.add("japanese");
        associatedAxisLabels.add("drinks");
        associatedAxisLabels.add("coffee");
        associatedAxisLabels.add("burgers");
    }

    public String predict(File image) {

        String prediction;
        try {
            // Process image
            Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
            inputImageBuffer = loadImage(bitmap);

            Model model = Model.newInstance(context);

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            inputFeature0.loadBuffer(inputImageBuffer.getBuffer());

            // Runs model inference and gets result.
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
            float[] out = outputFeature0.getFloatArray();

            float[] values = {out[0],out[1],out[2],out[3],out[4]};

            int position = this.getIndexOfLargest(values);
            if(position < 0) {
                return "";
            }

            prediction = associatedAxisLabels.get(position);

            // Releases model resources if no longer used.
            model.close();

        } catch (IOException e) {
            Log.e(TAG, "Error in predict", e);
            prediction = "";
        }

        return prediction;
    }

    private TensorImage loadImage(Bitmap bitmap) {
        // Loads bitmap into a TensorImage.
        inputImageBuffer = new TensorImage(DataType.FLOAT32);
        inputImageBuffer.load(bitmap);

        // Creates processor for the TensorImage.
        int cropSize = Math.min(bitmap.getWidth(), bitmap.getHeight());
        ImageProcessor imageProcessor = new ImageProcessor.Builder()
                        .add(new ResizeWithCropOrPadOp(cropSize, cropSize))
                        .add(new ResizeOp(224, 224, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                        .add(new NormalizeOp(0, 255))
                        .build();
        return imageProcessor.process(inputImageBuffer);
    }

    private int getIndexOfLargest( float[] array )
    {
        if ( array == null || array.length == 0 ) return -1; // null or empty

        int largest = 0;
        for ( int i = 1; i < array.length; i++ )
        {
            if ( array[i] > array[largest] ) largest = i;
        }

        Log.d(TAG, "Prediction probability: " + String.valueOf(array[largest]));

        // position of the first largest found
        if(array[largest] >= 0.7) {
            return largest;
        } else {
            return -1;
        }
    }

}
