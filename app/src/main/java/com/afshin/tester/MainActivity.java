package com.afshin.tester;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String SERVER_URL = "http://localhost:8080/";

    private static final String[] PERMISSIONS = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    private static final int PICK_FILE_RESULT_CODE = 1;
    private static final int PRINT_ALL_RESULT_CODE = 555;


    TextView txtResult;
    Spinner spnPrinterIds;
    Spinner spnPaperSize;
    EditText edtFontSize;
    CheckBox chkBold;
    CheckBox chkItalic;
    CheckBox chkUnderline;
    CheckBox chkLineFeed;
    CheckBox chkCutPaper;
    CheckBox chkOpenDrawer;
    CheckBox chkStrike;
    CheckBox chkInvert;
    RadioGroup radioGroup;

    Button btnDiscover;
    Button btnPrintImage;
    Button btnPrintText;
    Button btnStatus;
    Button btnCommand;
    Button testPrintingAll;
    private JSONObject allJsonObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        checkPermissions();

        initUi();
    }

    void initUi() {
        btnDiscover = findViewById(R.id.btnDiscover);
        btnPrintImage = findViewById(R.id.btnPrintImage);
        btnPrintText = findViewById(R.id.btnPrintText);
        btnStatus = findViewById(R.id.btnStatus);
        btnCommand = findViewById(R.id.btnCommand);
        testPrintingAll = findViewById(R.id.testPrintingAll);

        btnDiscover.setOnClickListener(this);
        btnPrintImage.setOnClickListener(this);
        btnPrintText.setOnClickListener(this);
        btnStatus.setOnClickListener(this);
        btnCommand.setOnClickListener(this);
        testPrintingAll.setOnClickListener(this);

        txtResult = findViewById(R.id.txtResult);
        spnPrinterIds = findViewById(R.id.spnPrinterIds);
        spnPaperSize = findViewById(R.id.spnPaperSize);
        edtFontSize = findViewById(R.id.edtFontSize);
        chkBold = findViewById(R.id.chkBold);
        chkUnderline = findViewById(R.id.chkUnderline);
        chkItalic = findViewById(R.id.chkItalic);
        chkLineFeed = findViewById(R.id.chkLineFeed);
        chkCutPaper = findViewById(R.id.chkCutPaper);
        chkOpenDrawer = findViewById(R.id.chOpenDrawer);
        chkStrike = findViewById(R.id.chkStrike);
        chkInvert = findViewById(R.id.chkInvert);
        radioGroup = findViewById(R.id.radioGroup);

        String[] paperSizes = {"58", "60", "80"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, paperSizes);
        spnPaperSize.setAdapter(adapter);

        spnPrinterIds.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String id = (String) spnPrinterIds.getSelectedItem();
                boolean bEpson = id.contains("Epson");
                spnPaperSize.setEnabled(bEpson);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        allJsonObject = new JSONObject();

    }

    void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, 100);
            return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();
            try {
                final InputStream imageStream = getContentResolver().openInputStream(uri);
                final Bitmap bitmap = BitmapFactory.decodeStream(imageStream);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();

                String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);
                String printerID = (String) spnPrinterIds.getSelectedItem();

                String paper = (String) spnPaperSize.getSelectedItem();
                int paperSize = -1;
                if (spnPaperSize.isEnabled()) {
                    paperSize = Integer.parseInt(paper);
                }

                if (requestCode == 555){

                    allJsonObject.put("printerID", printerID);
                    JSONArray jsonArray = new JSONArray();


                    JSONObject imageObject = new JSONObject();
                    imageObject.put("typeEnum", 0);
                    imageObject.put("value", base64Image);
                    imageObject.put("paperSize", paperSize);
                    imageObject.put("align", 0);

                    jsonArray.put(imageObject);


                    String fontSize = edtFontSize.getText().toString();
                    boolean bold = chkBold.isChecked();
                    boolean underline = chkUnderline.isChecked();
                    boolean italic = chkItalic.isChecked();
                    boolean strike = chkStrike.isChecked();
                    boolean invert = chkInvert.isChecked();
                    int radioButtonID = radioGroup.getCheckedRadioButtonId();
                    String align = "Center";
                    if (radioButtonID == R.id.radLeft) {
                        align = "Left";
                    } else if (radioButtonID == R.id.radRight) {
                        align = "Right";
                    } else if (radioButtonID == R.id.radJustify) {
                        align = "Justify";
                    }

                    JSONObject textObject = new JSONObject();
                    textObject.put("typeEnum", 1);
                    textObject.put("paperSize", paperSize);
                    textObject.put("value", "After reinstall you can again write files to that directory for which you do not need any permission.");
                    textObject.put("fontSize", fontSize);
                    textObject.put("bold", "" + bold);
                    textObject.put("underline", "" + underline);
                    textObject.put("italic", "" + italic);
                    textObject.put("strike", "" + strike);
                    textObject.put("invert", "" + invert);
                    textObject.put("align", 0);

                    jsonArray.put(textObject);


                    boolean lineFeed = chkLineFeed.isChecked();
                    boolean cutPaper = chkCutPaper.isChecked();
                    boolean openDrawer = chkOpenDrawer.isChecked();

                    JSONObject commandObject = new JSONObject();
                    commandObject.put("typeEnum", 2);
                    commandObject.put("paperSize", paperSize);
                    commandObject.put("value", "");
                    commandObject.put("lineFeed", lineFeed);
                    commandObject.put("cutPaper", cutPaper);
                    commandObject.put("openDrawer", openDrawer);

                    jsonArray.put(commandObject);

                    allJsonObject.put("Data", jsonArray);

                    updateButtonState(false);
                    String url = SERVER_URL + "printAndroid";
                    AndroidNetworking.post(url)
                            .addJSONObjectBody(allJsonObject)
                            .setTag(this)
                            .setPriority(Priority.HIGH)
                            .build()
                            .getAsString(new StringRequestListener() {
                                @Override
                                public void onResponse(String response) {
                                    updateButtonState(true);
                                    Log.d("TAG", "onResponse: "+response);
                                    txtResult.setText(uri + "\n" + response);
                                }

                                @Override
                                public void onError(ANError anError) {
                                    updateButtonState(true);
                                    txtResult.setText(anError.toString());
                                }
                            });

                }else if (requestCode == PICK_FILE_RESULT_CODE){

                    updateButtonState(false);
                    String url = SERVER_URL + "printImage";

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("printerID", printerID);
                    jsonObject.put("image", base64Image);
                    jsonObject.put("alignment", "Center");
                    jsonObject.put("paperSize", "" + paperSize);

                    AndroidNetworking.post(url)
//                        .addBodyParameter("printerID", printerID)
//                        .addBodyParameter("image", base64Image)
//                        .addBodyParameter("alignment", "Center")
//                        .addBodyParameter("paperSize", "" + paperSize)
                            .addJSONObjectBody(jsonObject)
                            .setTag(this)
                            .setPriority(Priority.HIGH)
                            .build()
                            .getAsString(new StringRequestListener() {
                                @Override
                                public void onResponse(String response) {
                                    updateButtonState(true);
                                    txtResult.setText(url + "\nResponse: " + response);
                                }

                                @Override
                                public void onError(ANError anError) {
                                    updateButtonState(true);
                                    txtResult.setText(anError.toString());
                                }
                            });
                }

            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
                Log.d("error", "onActivityResult: " + e.toString());
                updateButtonState(true);
            }
        }else {
            updateButtonState(true);
        }
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        String printerID = (String) spnPrinterIds.getSelectedItem();
        String paper = (String) spnPaperSize.getSelectedItem();
        int paperSize = -1;
        if (spnPaperSize.isEnabled()) {
            paperSize = Integer.parseInt(paper);
        }

        if (id == R.id.btnDiscover) {
            updateButtonState(false);
            String uri = SERVER_URL + "discoverPrinters";
            AndroidNetworking.post(uri)
                    .setTag(this)
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsString(new StringRequestListener() {
                        @Override
                        public void onResponse(String response) {
                            updateButtonState(true);
                            txtResult.setText(uri + "\n" + response);
                            try {
                                JSONObject json = new JSONObject(response);
                                JSONArray printers = json.getJSONArray("connectedPrinters");

                                int count = printers.length();
                                if (count > 0) {
                                    String[] ids = new String[count];
                                    for (int index = 0; index < count; index++) {
                                        JSONObject printer = printers.optJSONObject(index);
                                        String printerID = printer.getString("printerID");
                                        ids[index] = printerID;
                                    }

                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, ids);
                                    spnPrinterIds.setAdapter(adapter);
                                } else {
//                                    String[] ids = {"Epson-Printer-Fake","Star-Printer-Fake"};
                                    String[] ids = {"No Printers Found"};
                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, ids);
                                    spnPrinterIds.setAdapter(adapter);
                                }

                            } catch (JSONException e) {
                                updateButtonState(true);
                                e.printStackTrace();
                                txtResult.setText(e.getMessage());
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            updateButtonState(true);
                            txtResult.setText(anError.toString());
                        }
                    });
        } else {
            if (printerID == null || printerID.isEmpty()) {

                Toast.makeText(this, "Please input valid printer id", Toast.LENGTH_LONG).show();
                return;
            }

            if (id == R.id.btnPrintImage) {
                Intent getFile = new Intent(Intent.ACTION_GET_CONTENT);
                getFile.setType("image/*");
                startActivityForResult(getFile, PICK_FILE_RESULT_CODE);

            } else if (id == R.id.btnPrintText) {
                String fontSize = edtFontSize.getText().toString();
                boolean bold = chkBold.isChecked();
                boolean underline = chkUnderline.isChecked();
                boolean italic = chkItalic.isChecked();
                boolean strike = chkStrike.isChecked();
                boolean invert = chkInvert.isChecked();
                int radioButtonID = radioGroup.getCheckedRadioButtonId();
                String align = "Center";
                if (radioButtonID == R.id.radLeft) {
                    align = "Left";
                } else if (radioButtonID == R.id.radRight) {
                    align = "Right";
                } else if (radioButtonID == R.id.radJustify) {
                    align = "Justify";
                }


//                if (collectAll.isChecked()){
//                    try {
//                        if (!allJsonObject.has("printerID")){
//                            allJsonObject.put("printerID", printerID);
//                        }
//
//                        if (!allJsonObject.has("paperSize")){
//                            allJsonObject.put("paperSize", "" + paperSize);
//                        }
//
//                        allJsonObject.put("text", "After reinstall you can again write files to that directory for which you do not need any permission.");
//                        allJsonObject.put("fontSize", fontSize);
//                        allJsonObject.put("bold", "" + bold);
//                        allJsonObject.put("underline", "" + underline);
//                        allJsonObject.put("italic", "" + italic);
//                        allJsonObject.put("strike", "" + strike);
//                        allJsonObject.put("invert", "" + invert);
//                        allJsonObject.put("align", align);
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//
//                    return;
//                }

                String uri = SERVER_URL + "printAndroid";
                updateButtonState(false);
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("printerID", printerID);
                    jsonObject.put("text", "After reinstall you can again write files to that directory for which you do not need any permission.");
                    jsonObject.put("fontSize", fontSize);
                    jsonObject.put("paperSize", "" + paperSize);
                    jsonObject.put("bold", "" + bold);
                    jsonObject.put("underline", "" + underline);
                    jsonObject.put("italic", "" + italic);
                    jsonObject.put("strike", "" + strike);
                    jsonObject.put("invert", "" + invert);
                    jsonObject.put("align", align);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                AndroidNetworking.post(uri)
//                        .addBodyParameter("printerID", printerID)
//                        .addBodyParameter("text", "After reinstall you can again write files to that directory for which you do not need any permission.")
//                        .addBodyParameter("fontSize", fontSize)
//                        .addBodyParameter("paperSize", "" + paperSize)
//                        .addBodyParameter("bold", "" + bold)
//                        .addBodyParameter("underline", "" + underline)
//                        .addBodyParameter("italic", "" + italic)
//                        .addBodyParameter("strike", "" + strike)
//                        .addBodyParameter("invert", "" + invert)
//                        .addBodyParameter("align", align)
                        .addJSONObjectBody(jsonObject)
                        .setTag(this)
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsString(new StringRequestListener() {
                            @Override
                            public void onResponse(String response) {
                                updateButtonState(true);
                                txtResult.setText(uri + "\n" + response);
                            }

                            @Override
                            public void onError(ANError anError) {
                                updateButtonState(true);
                                txtResult.setText(anError.toString());
                            }
                        });

            } else if (id == R.id.btnStatus) {
                updateButtonState(false);
                String uri = SERVER_URL + "getPrinterStatus";
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("printerID", printerID);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                AndroidNetworking.post(uri)
//                        .addBodyParameter("printerID", printerID)
                        .addJSONObjectBody(jsonObject)
                        .setTag(this)
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsString(new StringRequestListener() {
                            @Override
                            public void onResponse(String response) {
                                updateButtonState(true);
                                txtResult.setText(uri + "\n" + response);
                            }

                            @Override
                            public void onError(ANError anError) {
                                updateButtonState(true);
                                txtResult.setText(anError.toString());
                            }
                        });
            } else if (id == R.id.btnCommand) {
                boolean lineFeed = chkLineFeed.isChecked();
                boolean cutPaper = chkCutPaper.isChecked();
                boolean openDrawer = chkOpenDrawer.isChecked();

                btnCommand.setEnabled(false);
                String uri = SERVER_URL + "sendCommand";
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("printerID", printerID);
                    jsonObject.put("lineFeed", "" + lineFeed);
                    jsonObject.put("cutPaper", "" + cutPaper);
                    jsonObject.put("openDrawer", "" + openDrawer);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                AndroidNetworking.post(uri)
//                        .addBodyParameter("printerID", printerID)
//                        .addBodyParameter("lineFeed", "" + lineFeed)
//                        .addBodyParameter("cutPaper", "" + cutPaper)
                        .addJSONObjectBody(jsonObject)
                        .setTag(this)
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsString(new StringRequestListener() {
                            @Override
                            public void onResponse(String response) {
                                updateButtonState(true);
                                txtResult.setText(uri + "\n" + response);
                            }

                            @Override
                            public void onError(ANError anError) {
                                updateButtonState(true);
                                txtResult.setText(anError.toString());
                            }
                        });
            }else if (id == R.id.testPrintingAll){
                Intent getFile = new Intent(Intent.ACTION_GET_CONTENT);
                getFile.setType("image/*");
                startActivityForResult(getFile, PRINT_ALL_RESULT_CODE);
            }
        }
    }

    private void updateButtonState(boolean enabled){
        btnDiscover.setEnabled(enabled);
        btnPrintImage.setEnabled(enabled);
        btnPrintText.setEnabled(enabled);
        btnStatus.setEnabled(enabled);
        btnCommand.setEnabled(enabled);
    }
}