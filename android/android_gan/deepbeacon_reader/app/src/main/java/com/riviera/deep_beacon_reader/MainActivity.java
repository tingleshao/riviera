package com.riviera.deep_beacon_reader;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {


    private TensorFlowInferenceInterface inferenceInterface;

    private Executor executor = Executors.newSingleThreadExecutor();

    private ImageView imageview1;

    private static float[] a  =  {-2.8830645084381104f, 1.180148959159851f, 2.8922781944274902f, -2.7080748081207275f,
            -1.6551251411437988f, 0.7881289720535278f, -1.1626136302947998f, -1.0747623443603516f, -0.422216534614563f,
            4.462748050689697f, 0.866684079170227f, -0.8465906381607056f, 2.0202553272247314f, -0.8157219886779785f,
            1.2617294788360596f, 1.8971368074417114f, -0.5761077404022217f, -1.907153844833374f, 1.4470815658569336f,
            1.5058015584945679f, 1.0783337354660034f, 1.3180818557739258f, 1.3948237895965576f, -1.382979393005371f,
            0.35344764590263367f, -0.08103300631046295f, -0.31112441420555115f, -0.8658198118209839f, -0.41364777088165283f,
            -0.15075089037418365f, -2.5181779861450195f, 0.2849348485469818f, -0.27574336528778076f, 1.4179282188415527f,
            -3.0036840438842773f, -2.10897159576416f, -1.077664852142334f, -1.0090718269348145f, 0.5800838470458984f,
            -0.9934819936752319f, -0.38636288046836853f, 1.548470139503479f, 4.225962162017822f, 0.6225273609161377f,
            -1.9151525497436523f, -0.5872492790222168f, -1.0458825826644897f, 1.60249924659729f, -3.533407688140869f,
            2.429518222808838f, 2.075908899307251f, -0.4011262357234955f, 0.8733375668525696f, -1.1757999658584595f,
            -0.5674439668655396f, -0.6709281802177429f, -1.0302342176437378f, 2.4207754135131836f, -0.5072906613349915f,
            0.2827413082122803f, -3.28432559967041f, -2.286632537841797f, -0.7824265360832214f, 1.8741588592529297f,
            -1.4021811485290527f, -0.015537023544311523f, -0.3766331076622009f, -2.430584669113159f, -1.5975570678710938f,
            0.8278626799583435f, 0.468487024307251f, -1.5764023065567017f, -0.3413003087043762f, 0.8070164918899536f,
            1.5801687240600586f, -0.1829346865415573f, 0.1390010118484497f, -0.32496967911720276f, -2.894637107849121f,
            0.16400229930877686f, -0.6305344700813293f, 0.5352773070335388f, 0.9189140796661377f, 1.0100315809249878f,
            -1.9561344385147095f, -2.67404842376709f, 0.9511394500732422f, -0.20869386196136475f, 0.8539302945137024f,
            -1.7760343551635742f, -1.6367623805999756f, 1.5958318710327148f, 1.4736219644546509f, 1.2995790243148804f,
            -0.7434713244438171f, 0.6129494309425354f, 0.8477692008018494f, 3.235323429107666f, -0.06473882496356964f, -0.14247339963912964f};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

         imageview1 = (ImageView)findViewById(R.id.imageView);
        initTensorflow();
    }

    // **TODO: BLE communications (write, how?)

    // TODO: load an encoding model
    // TODO: take a picture
    // TODO: send picture to model

    // TODO (maybe): need to pre-process the image to make it similar to the training data.

    // TODO(chongshao): find the pipeline to convert the online model to local model.
    private void initTensorflow() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    inferenceInterface = new TensorFlowInferenceInterface(getAssets(), "file:///android_asset/freeze2.pb");
                    int outputSize = (int) inferenceInterface.graph().operation("gen_deconv3/Tanh").output(0).shape().size(3);
                    long[] dims = {1,100};
                    inferenceInterface.feed("input", a, 1, 100);
                    inferenceInterface.run(new String[]{"gen_deconv3/Tanh"});

                    float[] outputs = new float[64*64*3];
                    inferenceInterface.fetch("gen_deconv3/Tanh", outputs);
                    Log.i("DDL", "Output layer size is " + outputSize);
                    Log.i("DDL", "output is " + Arrays.toString(outputs));

                    int[] intoutputs = new int[64*64];
                    for (int i = 0; i < intoutputs.length; ++i) {
                        intoutputs[i] =
                                0xFF000000
                                        | (((int) (outputs[i * 3] * 255)) << 16)
                                        | (((int) (outputs[i * 3 + 1] * 255)) << 8)
                                        | ((int) (outputs[i * 3 + 2] * 255));
                    }

                    // TODO(chongshao): the color is not displayed correctly
                    // TODO(chongshao): change the image size.

                    Bitmap bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.RGB_565);
                    bitmap.setPixels(intoutputs, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
                     imageview1.setImageBitmap(bitmap);

                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }

}
