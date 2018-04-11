package com.rivieraa.deep_beacon_reader;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.InputStreamContent;

import org.tensorflow.Shape;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.discovery.Discovery;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.UriTemplate;
import com.google.api.services.discovery.model.JsonSchema;
import com.google.api.services.discovery.model.RestDescription;
import com.google.api.services.discovery.model.RestMethod;
import com.google.api.services.prediction.PredictionScopes;


// TODO: link them together.
public class MainActivity extends AppCompatActivity {

    private TensorFlowInferenceInterface inferenceInterface;

    private TensorFlowInferenceInterface inferenceInterface2;

    private Executor executor = Executors.newSingleThreadExecutor();

    private ImageView compressedPreview;
    private ImageView cameraView;

    private Button decodeImageButton;
    private Button encodeImageButton;

    private Button sendToBeaconButton;

    // BLE Stuff
    // The Eddystone Service UUID, 0xFEAA. See https://github.com/google/eddystone
    private static final ParcelUuid SERVICE_UUID =
            ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB");
    private BluetoothLeAdvertiser adv;
    private AdvertiseCallback advertiseCallback;
    private int txPowerLevel = AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM;
    private int advertiseMode = AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY;
    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private static final byte FRAME_TYPE_UID = 0x00;
    private boolean isBroadcasting = false;

    private boolean isScanning = false;
    private Button scanBeaconButton;
    BluetoothLeScanner btScanner;
    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    private static float[] mnistinput100 = {-2.8830645084381104f, 1.180148959159851f, 2.8922781944274902f, -2.7080748081207275f,
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
            -0.7434713244438171f, 0.6129494309425354f, 0.8477692008018494f, 3.235323429107666f, -0.06473882496356964f, -0.14247339963912964f
    };

    private static float[] birdinput10 = {-4.183666706085205f, -2.4661941528320312f, 1.257184624671936f, -4.537239074707031f, 5.261890888214111f, 7.5772786140441895f, -0.798807680606842f, -7.147511005401611f, 3.960893154144287f, 5.556200981140137f};

    private static final String MNIST_MODEL_ID = "generative_chongshao_20180125_001534";
    private static final String BIRD_MODEL_ID = "generative_chongshao_20180125_001514";
    private static final String TRAFFICSIGN_MODEL_ID  = "generative_chongshao_20180126_222139";
    private static final String TOKEN = "ya29.Gl2XBazVpYhAWGIJ5sydSz86ZyqWAWHCe9vQeGNMhYBOelIpCzUG5f5kS20omAPc0QLphIQn3wRMakumAFfpat41fyO8nVbHRc1oX6MtlArcQRelSBLuXqV4SgH1998";

    // Camera stuff
    private Button cameraButton;
    private static final int CAMERA_REQUEST = 1888; // field

    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private Bitmap pictureTaken = null;
    private String b64ImageString = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        compressedPreview = (ImageView) findViewById(R.id.imageView2);
        cameraView = (ImageView) findViewById(R.id.imageView);

        decodeImageButton = (Button) findViewById(R.id.decodeButton);
        decodeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.decodeImage();
            }
        });

        sendToBeaconButton = (Button) findViewById(R.id.broadcastButton);
        sendToBeaconButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.sendToBeacon();
            }
        });

        scanBeaconButton = (Button) findViewById(R.id.scanButton);
        scanBeaconButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.scanBeacon();
            }
        });

        encodeImageButton = (Button) findViewById(R.id.encodeButton);
        encodeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    MainActivity.this.encodeImage();
            }
        });

        Log.d("DDL", "float to byte to float " + Float.toString(-2.8830645084381104f)
                + " " + Float.toString(byteArray2Float(float2ByteArray(-2.8830645084381104f))));

        cameraButton = (Button) findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.takePicture();
            }
        });

        initTensorflow();

        initBLE();

        initCam();
    }

    private void decodeImage() {
        //   inferenceInterface.feed("input", a, 1, 100);
           inferenceInterface.feed("input", birdinput10, 1, 10);

        inferenceInterface.run(new String[]{"gen_deconv3/Tanh"});

        float[] outputs = new float[64 * 64 * 3];
        inferenceInterface.fetch("gen_deconv3/Tanh", outputs);
        Log.i("DDL", "output is " + Arrays.toString(outputs));
        for (int i = 0; i < outputs.length; i++) {
            outputs[i] = (outputs[i] + 1.0f) / 2.0f;
        }
        int[] intoutputs = new int[64 * 64];
        for (int i = 0; i < intoutputs.length; ++i) {
            intoutputs[i] =
                    0xFF000000
                            | (((int) (outputs[i * 3] * 255)) << 16)
                            | (((int) (outputs[i * 3 + 1] * 255)) << 8)
                            | ((int) (outputs[i * 3 + 2] * 255));
        }
        // TODO(chongshao): change the image size.

        Bitmap bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.RGB_565);
        bitmap.setPixels(intoutputs, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        compressedPreview.setImageBitmap(bitmap);
    }

    private void encodeImage() {
        // Code that uses local model
//        float[] input = readImageToFloats();
//        inferenceInterface2.feed("map/TensorArrayStack/TensorArrayGatherV3", input, 1, input.length);
//        float[] outputs = new float[4 * 4 * 512];
//        inferenceInterface2.run(new String[]{"enc_rl3"});
//        inferenceInterface2.fetch("enc_rl3", outputs);
//        Log.i("DDL", "output is " + Arrays.toString(outputs));

        // Code that uses cloud model
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpTransport httpTransport = new com.google.api.client.http.javanet.NetHttpTransport();
                    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
                    Discovery discovery = new Discovery.Builder(httpTransport, jsonFactory, null).build();
                    RestDescription api = discovery.apis().getRest("ml", "v1").execute();
                    RestMethod method = api.getResources().get("projects").getMethods().get("predict");

                    JsonSchema param = new JsonSchema();
                    String projectId = "pelagic-force-171015";
                    // You should have already deployed a model and a version.
                    // For reference, see https://cloud.google.com/ml-engine/docs/how-tos/deploying-models.
                    String modelId = MNIST_MODEL_ID + "_image_to_embed";
                    String versionId = "v1";
                    param.set(
                            "name", String.format("projects/%s/models/%s/versions/%s", projectId, modelId, versionId));

                    GenericUrl url =
                            new GenericUrl(UriTemplate.expand(api.getBaseUrl() + method.getPath(), param, true));
                    Log.d("DDL", "Url: " + url.toString());

                    String contentType = "application/json";
                    InputStream ims = getAssets().open("mnist_img.json");
                    HttpContent content = new InputStreamContent(contentType, ims);

                    Log.d("DDL", "Content: " + content.getLength());

                    InputStream ims2 = getAssets().open("test-162bb5830899.json");

                    List<String> scopes = new ArrayList<>();
                    scopes.add(PredictionScopes.PREDICTION);
                    GoogleCredential credential = GoogleCredential.fromStream(ims2).createScoped(scopes);
                    credential.refreshToken();

                    Log.d("DDL", "Service account scopes: " + credential.getServiceAccountScopesAsString());
                    Log.d("DDL", "Access Token: " + credential.getAccessToken());

                    HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
                    HttpRequest request = requestFactory.buildRequest(method.getHttpMethod(), url, content);
                    // This token needs to be retrieved from $ gcloud auth print-access-token
                    request.setHeaders(new HttpHeaders().setAuthorization("Bearer " + TOKEN));

                    String response = request.execute().parseAsString();
                    Log.d("DDL", "Response: "  + response);

                } catch (IOException e) {
                    Log.d("DDL", "Exception in talking to Google Cloud");
                    Log.d("DDL", e.getMessage());
                    Log.d("DDL", e.getStackTrace().toString());
                }
            }
        });
    }

    private void sendToBeacon() {
        if (!isBroadcasting) {
            isBroadcasting = true;
            AdvertiseSettings advertiseSettings = new AdvertiseSettings.Builder()
                    .setAdvertiseMode(advertiseMode)
                    .setTxPowerLevel(txPowerLevel)
                    .setConnectable(true)
                    .build();
            byte[] serviceData = null;
            byte[] embeddingByte = new byte[10];
            try {
                for (int k = 0; k < birdinput10.length; k++) {
                    float embeddingValue = birdinput10[k];
                    // THis is a 4 byte array, but we only need the first one
                    byte[] byteArray = float2ByteArray(embeddingValue);
                    embeddingByte[k] = byteArray[0];
                    Log.d("DDL", "broadcasted embedding: " + embeddingValue + "  actual: " + byteArray2Float20(byteArray));
                }
                serviceData = buildServiceDataFromByteArray(embeddingByte);
            } catch (IOException e) {
                Log.e("DDL", e.toString());
                Toast.makeText(this, "failed to build service data", Toast.LENGTH_SHORT).show();
                isBroadcasting = false;
            }
            AdvertiseData advertiseData = new AdvertiseData.Builder()
                    .addServiceData(SERVICE_UUID, serviceData)
                    .addServiceUuid(SERVICE_UUID)
                    .setIncludeTxPowerLevel(false)
                    .setIncludeDeviceName(false)
                    .build();
            adv.startAdvertising(advertiseSettings, advertiseData, advertiseCallback);

        } else {
            isBroadcasting = false;
            adv.stopAdvertising(advertiseCallback);
        }
    }

    private byte[] buildServiceData(String data) throws IOException {
        byte txPower = txPowerLevelToByteValue();
   //     byte[] namespaceBytes = toByteArray("11223344556677889910");
        byte[] namespaceBytes = toByteArray(data);
        byte[] instanceBytes = toByteArray("aabbccddeeff");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        os.write(new byte[]{FRAME_TYPE_UID, txPower});
        os.write(namespaceBytes);
        os.write(instanceBytes);
        Log.d("DDL", "Byte array: " + Arrays.toString(os.toByteArray()));
        return os.toByteArray();
    }

    private byte[] buildServiceDataFromByteArray(byte[] data) throws IOException {
        byte txPower = txPowerLevelToByteValue();
        Log.d("DDL", "data: " + Arrays.toString(data));
        //     byte[] namespaceBytes = toByteArray("11223344556677889910");
        byte[] namespaceBytes = data.clone();
        byte[] instanceBytes = toByteArray("aabbccddeeff");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        os.write(new byte[]{FRAME_TYPE_UID, txPower});
        os.write(namespaceBytes);
        os.write(instanceBytes);
        Log.d("DDL", "Byte array: " + Arrays.toString(os.toByteArray()));
        return os.toByteArray();
    }

    private byte txPowerLevelToByteValue() {
        switch (txPowerLevel) {
            case AdvertiseSettings.ADVERTISE_TX_POWER_HIGH:
                return (byte) -16;
            case AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM:
                return (byte) -26;
            case AdvertiseSettings.ADVERTISE_TX_POWER_LOW:
                return (byte) -35;
            default:
                return (byte) -59;
        }
    }

    private void scanBeacon() {
        if (!isScanning) {
            isScanning = true;
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    btScanner.startScan(leScanCallback);
                }
            });
        } else {
            isScanning = false;
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    btScanner.stopScan(leScanCallback);
                }
            });
        }
    }

    // Device scan callback.
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            // TODO(chongshao): verify that get Parcel UUID works.
            if (result.getScanRecord().getServiceData().containsKey( ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB"))) {
                Log.d("DDL", "found eddystone device: " + result.getDevice().getName() + " with data: " + result.getScanRecord());
            }
      //      Set<String> keys = result.getScanRecord().getServiceData().keySet();
      //      Log.d("DDL","Device Name: " + result.getDevice().getName() + " rssi: " + result.getRssi() + "\n" + result.getScanRecord().getServiceData().);
        }
    };

    private byte[] readImageToBytes() {
        byte[] bytes = null;
        try {
            InputStream ims = getAssets().open("sample.jpg");
            bytes = new byte[ims.available()];

            BufferedInputStream buf = new BufferedInputStream(ims);
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bytes;
    }

    private byte[] toByteArray(String hexString) {
        // hexString guaranteed valid.
        int len = hexString.length();
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return bytes;
    }

    private float[] readImageToFloats() {
        byte[] bytes;
        Bitmap bMap = null;
        int[] intValues = new int[64 * 64];
        float[] floatValues = new float[64 * 64 * 3];
        try {
            InputStream ims = getAssets().open("sample.jpg");
            bytes = new byte[ims.available()];

            BufferedInputStream buf = new BufferedInputStream(ims);
            buf.read(bytes, 0, bytes.length);
            bMap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

            buf.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        bMap.getPixels(intValues, 0, bMap.getWidth(), 0, 0, bMap.getWidth(), bMap.getHeight());

        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            floatValues[i * 3] = ((val >> 16) & 0xFF) / 255.0f;
            floatValues[i * 3 + 1] = ((val >> 8) & 0xFF) / 255.0f;
            floatValues[i * 3 + 2] = (val & 0xFF) / 255.0f;
        }
        return floatValues;
    }

    // TODO (maybe): need to pre-process the image to make it similar to the training data.
    private void initTensorflow() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    //inferenceInterface = new TensorFlowInferenceInterface(getAssets(), "file:///android_asset/freeze2.pb");
                    inferenceInterface = new TensorFlowInferenceInterface(getAssets(), "freeze_bird_10_embed_to_image2.pb");
                    int outputSize = (int) inferenceInterface.graph().operation("gen_deconv3/Tanh").output(0).shape().size(3);
                    Log.i("DDL", "Output layer size is " + outputSize);
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
                try {
                    inferenceInterface2 = new TensorFlowInferenceInterface(getAssets(), "file:///android_asset/freeze_mnist_100_image_to_embed3.pb");
                    Shape shape = inferenceInterface2.graph().operation("enc_rl3").output(0).shape();
                    Log.i("DDL", "Encode model output layer size is " + shape.toString());
                } catch (final Exception e) {
                    throw new RuntimeException("Error initialzing Tensorflow encoder model!", e);
                }
            }
        });
    }

    private void initBLE() {
        BluetoothManager manager = (BluetoothManager) getApplicationContext().getSystemService(
                Context.BLUETOOTH_SERVICE);
        BluetoothAdapter btAdapter = manager.getAdapter();

        if (btAdapter == null) {
            Log.d("DDL", "Bluetooth not detected on device");
        } else if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            this.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
        } else if (!btAdapter.isMultipleAdvertisementSupported()) {
            Log.d("DDL", "BLE advertising not supported on this device");
        } else {
            adv = btAdapter.getBluetoothLeAdvertiser();
            advertiseCallback = createAdvertiseCallback();
        }

        btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();

        // Make sure we have access coarse location enabled, if not, prompt the user to enable it
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect peripherals.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                }
            });
            builder.show();
        }
    }

    private void initCam() {
        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    MY_CAMERA_REQUEST_CODE);
        }
    }

    private AdvertiseCallback createAdvertiseCallback() {
        return new AdvertiseCallback() {
            @Override
            public void onStartFailure(int errorCode) {
                switch (errorCode) {
                    case ADVERTISE_FAILED_DATA_TOO_LARGE:
                        showToastAndLogError("ADVERTISE_FAILED_DATA_TOO_LARGE");
                        break;
                    case ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                        showToastAndLogError("ADVERTISE_FAILED_TOO_MANY_ADVERTISERS");
                        break;
                    case ADVERTISE_FAILED_ALREADY_STARTED:
                        showToastAndLogError("ADVERTISE_FAILED_ALREADY_STARTED");
                        break;
                    case ADVERTISE_FAILED_INTERNAL_ERROR:
                        showToastAndLogError("ADVERTISE_FAILED_INTERNAL_ERROR");
                        break;
                    case ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                        showToastAndLogError("ADVERTISE_FAILED_FEATURE_UNSUPPORTED");
                        break;
                    default:
                        showToastAndLogError("startAdvertising failed with unknown error " + errorCode);
                        break;
                }
            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
                return;
            }

            case MY_CAMERA_REQUEST_CODE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void takePicture() {
        Intent cameraIntent = new  Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap picture = (Bitmap) data.getExtras().get("data");//this is your bitmap image and now you can do whatever you want with this
            pictureTaken = Bitmap.createScaledBitmap(picture, 64, 64, true);
            cameraView.setImageBitmap(pictureTaken); //for example I put bmp in an ImageView
            String base64image = getBase64String(pictureTaken);
        }
    }

    private String getBase64String(Bitmap bitmap)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        byte[] imageBytes = baos.toByteArray();

        String base64String = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
        Log.d("DDL", "base64 image: " + base64String);

        return base64String;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showToastAndLogError(String message) {
        showToast(message);
        Log.e("DDL", message);
    }

    public static byte [] float2ByteArray (float value) {
        return ByteBuffer.allocate(4).putFloat(value).array();
    }

    public static float byteArray2Float(byte[] value) {
        byte[] longvalue = new byte[4];
        longvalue[0] = value[0];
        longvalue[1] = 0;
        longvalue[2] = 0;

        longvalue[3] = 0;
       return  ByteBuffer.wrap(longvalue).getFloat();
    }

    public static float byteArray2Float20(byte[] value) {
        byte[] longvalue = new byte[4];
        longvalue[0] = value[0];
        longvalue[1] = value[1];
        longvalue[2] = 0;

        longvalue[3] = 0;
        return  ByteBuffer.wrap(longvalue).getFloat();
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private static InputStream stringToInputStream(String string) {
        InputStream stream = new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
       return stream;
    }
}
