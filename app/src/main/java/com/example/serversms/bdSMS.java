package com.example.serversms;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class bdSMS {
    //Tabla SMS
    private static final String idSMS = "id";
    private static final String phone = "phone";
    private static final String message = "message";
    private static final String state = "state";
    // BASE DE DATOS TABLAS
    private static final String BD = "bdSMS";
    private static final String messages = "messages";
    private static final int VERSION_BD = 1;
    private BDHelper nHelper;
    private final Context nContexto;
    private SQLiteDatabase nBD;

    private static class BDHelper extends SQLiteOpenHelper {

        public BDHelper(Context context) {
            super(context, BD, null, VERSION_BD);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // TODO Auto-generated method stub
            db.execSQL("CREATE TABLE " + messages + "(" + idSMS
                    + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + phone
                    + " TEXT NOT NULL, " + message
                    + " TEXT NOT NULL, " + state
                    + " TEXT NOT NULL);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO Auto-generated method stub
            db.execSQL("DROP TABLE IF EXISTS " + messages);
            onCreate(db);
        }
    }

    public bdSMS(Context c) {
        nContexto = c;
    }

    public bdSMS abrir() throws Exception {
        nHelper = new BDHelper(nContexto);
        nBD = nHelper.getWritableDatabase();
        return this;
    }

    public void cerrar() {
        // TODO Auto-generated method stub
        nHelper.close();
    }

    public long insertSMS(String phone, String message, String state) {
        ContentValues cv = new ContentValues();
        cv.put(this.phone, phone);
        cv.put(this.message, message);
        cv.put(this.state, state);
        return nBD.insert(messages, null, cv);
    }

    public Cursor allSMS(){
        String selectAll = "SELECT * FROM " + messages + " LIMIT 10";
        Cursor cursor = nBD.rawQuery(selectAll, null);
        return cursor;
    }
}
