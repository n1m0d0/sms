package com.example.serversms;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

import static android.Manifest.permission.READ_SMS;
import static android.Manifest.permission.RECEIVE_SMS;
import static android.Manifest.permission.SEND_SMS;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    Toast msj;
    String phone;
    String message;
    ListView listView;
    ProgressDialog mProgressDialog;
    SmsManager sms = SmsManager.getDefault();
    //String url = "http://10.0.2.2/bdbanco/insertsms.php";
    String url = "http://192.168.1.13/bdbanco/insertsms.php";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        validarPermisos();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            phone = extras.getString("phone");
            message = extras.getString("message");
            enviarformulario();
        } else {
            //finish();
        }
    }

    private boolean validarPermisos() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if((checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && (checkSelfPermission(READ_SMS) == PackageManager.PERMISSION_GRANTED) && (checkSelfPermission(SEND_SMS) == PackageManager.PERMISSION_GRANTED) && (checkSelfPermission(RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED)) {
            return true;
        }

        if ((shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) || (shouldShowRequestPermissionRationale(READ_SMS)) || (shouldShowRequestPermissionRationale(SEND_SMS)) || (shouldShowRequestPermissionRationale(RECEIVE_SMS))) {
            cargardialogo();
        }
        else {
            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, READ_SMS, SEND_SMS, RECEIVE_SMS}, 100);
        }
        return false;
    }

    private void cargardialogo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Permisos Desactivados");
        builder.setMessage("Debe aceptar los permisos para el correcto funcionamiento de la App");
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, READ_SMS, SEND_SMS, RECEIVE_SMS}, 100);
                }
            }
        });
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100) {
            if(grantResults.length == 4 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED && grantResults[3] == PackageManager.PERMISSION_GRANTED) {
            } else {
                cargardialogo2();
            }
        }
    }

    private void cargardialogo2() {
        final CharSequence[] op = {"si", "no"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Desea configurar los permisos manualmente?");
        builder.setItems(op, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(op[which].equals("si")) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent .setData(uri);
                    startActivity(intent);
                }
                else {
                    msj = Toast.makeText(MainActivity.this, "los permisos no fueron aceptados", Toast.LENGTH_LONG);
                    msj.show();
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void enviarformulario() {
        StringRequest stringRequest;
        RequestQueue requestQueue;
        mProgressDialog =  new ProgressDialog(this);
        mProgressDialog.setMessage("Cargando...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        requestQueue = Volley.newRequestQueue(this);
        stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.w("respuesta", "" + response);
                bdSMS conexion = new bdSMS(MainActivity.this);
                try {
                    conexion.abrir();
                    if (response.equals("Error")) {
                        sms.sendTextMessage(phone, null, "Revise los datos. No se puedo procesar su solicitud", null, null);
                        conexion.insertSMS(phone, message, "ERROR");
                    } else {
                        sms.sendTextMessage(phone, null, response, null, null);
                        conexion.insertSMS(phone, message, "ACTIVO");
                        mProgressDialog.dismiss();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                conexion.cerrar();
                finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.w("error", "" + error);
                mProgressDialog.dismiss();
                finish();
            }
        }){
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<>();
                params.put("phone", phone);
                params.put("message", message);
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }
}
