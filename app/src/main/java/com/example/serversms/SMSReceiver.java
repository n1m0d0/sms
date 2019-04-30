package com.example.serversms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.util.regex.Pattern;

public class SMSReceiver extends BroadcastReceiver {
    final SmsManager sms = SmsManager.getDefault();
    @Override
    public void onReceive(Context context, Intent intent) {

        final Bundle bundle = intent.getExtras();

        try {

            if (bundle != null) {

                final Object[] pdusObj = (Object[]) bundle.get("pdus");

                for (int i = 0; i < pdusObj.length; i++) {

                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();

                    String senderNum = phoneNumber;
                    String message = currentMessage.getDisplayMessageBody();

                    Log.i("SmsReceiver", "senderNum: "+ senderNum + "; message: " + message);


                    // Show Alert
                    /*int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(context,
                            "senderNum: "+ senderNum + ", message: " + message, duration);
                    toast.show();*/

                    /********************************/
                    bdSMS conexion = new bdSMS(context);
                    conexion.abrir();
                    //if (validateRegEx(message, "^[a-zA-Z]+(?:-[a-zA-Z]+)*$")) {
                    if (validateRegEx(message, "[a-zA-Z]+(?:-[a-zA-Z]+)")) {
                        /*sms.sendTextMessage(senderNum, null, message, null, null);
                        Toast.makeText(context, "Sent. " + senderNum + " " + message, Toast.LENGTH_SHORT).show();*/
                        //registro en la BD
                        //conexion.insertSMS(senderNum, message, "ACTIVO");
                        //mandar datos
                        Intent newintent = new Intent(context, MainActivity.class);
                        newintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        newintent.putExtra("phone", senderNum);
                        newintent.putExtra("message", message);
                        context.startActivity(newintent);
                    } else {
                        sms.sendTextMessage(senderNum, null, "Revise los datos. No se puedo procesar su solicitud", null, null);
                        conexion.insertSMS(senderNum, message, "ERROR");
                    }
                    conexion.cerrar();
                    /********************************/

                    /*sms.sendTextMessage(senderNum, null, message, null, null);

                    Toast.makeText(context, "Sent. " + senderNum + " " + message, Toast.LENGTH_SHORT).show();*/

                } // end for loop
            } // bundle is null

        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception smsReceiver" +e);

        }
    }

    private boolean validateRegEx(String datos, String exReg) {
        Pattern pattern = Pattern.compile(exReg);
        return pattern.matcher(datos).matches();
    }


}
