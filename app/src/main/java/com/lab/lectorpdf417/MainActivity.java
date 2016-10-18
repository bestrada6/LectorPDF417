package com.lab.lectorpdf417;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.microblink.activity.Pdf417ScanActivity;
import com.microblink.recognizers.BaseRecognitionResult;
import com.microblink.recognizers.RecognitionResults;
import com.microblink.recognizers.blinkbarcode.bardecoder.BarDecoderRecognizerSettings;
import com.microblink.recognizers.blinkbarcode.bardecoder.BarDecoderScanResult;
import com.microblink.recognizers.blinkbarcode.pdf417.Pdf417RecognizerSettings;
import com.microblink.recognizers.blinkbarcode.pdf417.Pdf417ScanResult;
import com.microblink.recognizers.blinkbarcode.usdl.USDLRecognizerSettings;
import com.microblink.recognizers.blinkbarcode.usdl.USDLScanResult;
import com.microblink.recognizers.blinkbarcode.zxing.ZXingRecognizerSettings;
import com.microblink.recognizers.blinkbarcode.zxing.ZXingScanResult;
import com.microblink.recognizers.settings.RecognitionSettings;
import com.microblink.recognizers.settings.RecognizerSettings;
import com.microblink.results.barcode.BarcodeDetailedData;

import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String LICENSE_KEY = "LF4HOK6C-2CBLHLKC-2W32Z7CV-Z5Y5Z644-XIDIRD7F-ZFRKASEV-MTUXMWH6-7BSYYAS4";

    private static final int MY_REQUEST_CODE = 1337;

    private static final String TAG = "lectorpdf417";

    private Button btnScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnScan= (Button) findViewById(R.id.btnScan);
        btnScan.setOnClickListener(btnScanOnClickListener);
    }

    View.OnClickListener btnScanOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            btnScanClick();
        }
    };

    public void btnScanClick(){
        Intent intent = new Intent(this, Pdf417ScanActivity.class);

        // If you want sound to be played after the scanning process ends,
        // put here the resource ID of your sound file. (optional)
        intent.putExtra(Pdf417ScanActivity.EXTRAS_BEEP_RESOURCE, R.raw.beep);
        intent.putExtra(Pdf417ScanActivity.EXTRAS_LICENSE_KEY, LICENSE_KEY);

        Pdf417RecognizerSettings pdf417RecognizerSettings = new Pdf417RecognizerSettings();
        pdf417RecognizerSettings.setNullQuietZoneAllowed(true);

        BarDecoderRecognizerSettings oneDimensionalRecognizerSettings = new BarDecoderRecognizerSettings();
        oneDimensionalRecognizerSettings.setScanCode39(true);
        oneDimensionalRecognizerSettings.setScanCode128(true);

        USDLRecognizerSettings usdlRecognizerSettings = new USDLRecognizerSettings();

        ZXingRecognizerSettings zXingRecognizerSettings = new ZXingRecognizerSettings();
        zXingRecognizerSettings.setScanQRCode(true);
        zXingRecognizerSettings.setScanITFCode(true);

        RecognitionSettings recognitionSettings = new RecognitionSettings();
        recognitionSettings.setRecognizerSettingsArray(
                new RecognizerSettings[]{pdf417RecognizerSettings, oneDimensionalRecognizerSettings,
                        usdlRecognizerSettings, zXingRecognizerSettings});

        intent.putExtra(Pdf417ScanActivity.EXTRAS_RECOGNITION_SETTINGS, recognitionSettings);
//        intent.putExtra(Pdf417ScanActivity.EXTRAS_SHOW_DIALOG_AFTER_SCAN, false);
        intent.putExtra(Pdf417ScanActivity.EXTRAS_ALLOW_PINCH_TO_ZOOM, true);
        intent.putExtra(Pdf417ScanActivity.EXTRAS_SHOW_FOCUS_RECTANGLE, true);

        startActivityForResult(intent, MY_REQUEST_CODE);

    };

    private boolean checkIfDataIsUrlAndCreateIntent(String data) {

        boolean barcodeDataIsUrl;
        try {
            @SuppressWarnings("unused")
            URL url = new URL(data);
            barcodeDataIsUrl = true;
        } catch (MalformedURLException exc) {
            barcodeDataIsUrl = false;
        }

        if (barcodeDataIsUrl) {
            // create intent for browser
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(data));
            startActivity(Intent.createChooser(intent, getString(R.string.UseWith)));
        }
        return barcodeDataIsUrl;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( requestCode== MY_REQUEST_CODE && resultCode== Pdf417ScanActivity.RESULT_OK ){
            RecognitionResults results = data.getParcelableExtra(Pdf417ScanActivity.EXTRAS_RECOGNITION_RESULTS);
            BaseRecognitionResult[] resultArray = results.getRecognitionResults();

            StringBuilder sb = new StringBuilder();

            for(BaseRecognitionResult res : resultArray) {
                if(res instanceof Pdf417ScanResult) {
                    Pdf417ScanResult result = (Pdf417ScanResult) res;
                    String barcodeData = result.getStringData();
                    boolean uncertainData = result.isUncertain();
                    BarcodeDetailedData rawData = result.getRawData();
                    byte[] rawDataBuffer = rawData.getAllData();

                    if(checkIfDataIsUrlAndCreateIntent(barcodeData)) {
                        return;
                    } else {
                        // add data to string builder
                        sb.append("PDF417 scan data");
                        if (uncertainData) {
                            sb.append("This scan data is uncertain!\n\n");
                        }
                        sb.append(" string data:\n");
                        sb.append(barcodeData);
                        if (rawData != null) {
                            sb.append("\n\n");
                            sb.append("PDF417 raw data:\n");
                            sb.append(rawData.toString());
                            sb.append("\n");
                            sb.append("PDF417 raw data merged:\n");
                            sb.append("{");
                            for (int i = 0; i < rawDataBuffer.length; ++i) {
                                sb.append((int) rawDataBuffer[i] & 0x0FF);
                                if (i != rawDataBuffer.length - 1) {
                                    sb.append(", ");
                                }
                            }
                            sb.append("}\n\n\n");
                        }
                    }

                }else if(res instanceof BarDecoderScanResult) {

                }else if(res instanceof ZXingScanResult) {

                }else if(res instanceof USDLScanResult) {

                }
            }

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
            startActivity(Intent.createChooser(intent, getString(R.string.UseWith)));

        }
    }
}
