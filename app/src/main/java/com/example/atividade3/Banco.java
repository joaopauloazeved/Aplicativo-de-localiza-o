package com.example.atividade3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class Banco extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "trilhas.db";
    private static final int DATABASE_VERSION  = 1;
    public static final String TABLE_TRILHAS    = "trilhas";
    public static final String COL_ID           = "id";
    public static final String COL_NOME         = "nome";
    public static final String COL_DATA_INICIO  = "data_inicio";
    public static final String COL_DATA_FIM     = "data_fim";
    public static final String COL_VEL_MEDIA    = "vel_media";
    public static final String COL_VEL_MAX      = "vel_max";
    public static final String COL_DISTANCIA    = "distancia";
    public static final String COL_DURACAO      = "duracao";
    public static final String TABLE_PONTOS   = "pontos";
    public static final String COL_TRILHA_ID  = "trilha_id";
    public static final String COL_LATITUDE   = "latitude";
    public static final String COL_LONGITUDE  = "longitude";

    public Banco(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_TRILHAS + " (" +
                COL_ID         + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NOME       + " TEXT, " +
                COL_DATA_INICIO+ " TEXT, " +
                COL_DATA_FIM   + " TEXT, " +
                COL_VEL_MEDIA  + " REAL, " +
                COL_VEL_MAX    + " REAL, " +
                COL_DISTANCIA  + " REAL, " +
                COL_DURACAO    + " TEXT )");

        db.execSQL("CREATE TABLE " + TABLE_PONTOS + " (" +
                COL_ID        + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TRILHA_ID + " INTEGER, " +
                COL_LATITUDE  + " REAL, " +
                COL_LONGITUDE + " REAL )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRILHAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PONTOS);
        onCreate(db);
    }
    public long inserirTrilha(String nome, String dataInicio, String dataFim,
                              double velMedia, double velMax,
                              double distancia, String duracao) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v   = new ContentValues();
        v.put(COL_NOME,        nome);
        v.put(COL_DATA_INICIO, dataInicio);
        v.put(COL_DATA_FIM,    dataFim);
        v.put(COL_VEL_MEDIA,   velMedia);
        v.put(COL_VEL_MAX,     velMax);
        v.put(COL_DISTANCIA,   distancia);
        v.put(COL_DURACAO,     duracao);
        return db.insert(TABLE_TRILHAS, null, v);
    }
    public void finalizarTrilha(long id, String dataFim, double velMedia,
                                double velMax, double distancia, String duracao) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v   = new ContentValues();
        v.put(COL_DATA_FIM,  dataFim);
        v.put(COL_VEL_MEDIA, velMedia);
        v.put(COL_VEL_MAX,   velMax);
        v.put(COL_DISTANCIA, distancia);
        v.put(COL_DURACAO,   duracao);
        db.update(TABLE_TRILHAS, v, "id=?", new String[]{String.valueOf(id)});
    }

    public Cursor listarTrilhas() {
        return getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_TRILHAS +
                " ORDER BY " + COL_ID + " DESC", null);
    }

    public void deletarTrilha(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_TRILHAS, "id=?",       new String[]{String.valueOf(id)});
        db.delete(TABLE_PONTOS,  "trilha_id=?", new String[]{String.valueOf(id)});
    }
    public void deletarTodasTrilhas() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_PONTOS);
        db.execSQL("DELETE FROM " + TABLE_TRILHAS);
    }
    public int deletarTrilhasPorIntervalo(String dataInicio, String dataFim) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id FROM " + TABLE_TRILHAS +
                        " WHERE substr(data_inicio,7,4)||substr(data_inicio,4,2)||substr(data_inicio,1,2)" +
                        " BETWEEN ? AND ?",
                new String[]{
                        dataInicio.substring(6) + dataInicio.substring(3,5) + dataInicio.substring(0,2),
                        dataFim.substring(6)    + dataFim.substring(3,5)    + dataFim.substring(0,2)
                });
        List<Integer> ids = new ArrayList<>();
        while (c.moveToNext()) ids.add(c.getInt(0));
        c.close();

        SQLiteDatabase db = getWritableDatabase();
        for (int id : ids) {
            db.delete(TABLE_TRILHAS, "id=?",       new String[]{String.valueOf(id)});
            db.delete(TABLE_PONTOS,  "trilha_id=?", new String[]{String.valueOf(id)});
        }
        return ids.size();
    }

    public void editarNomeTrilha(int id, String novoNome) {
        ContentValues v = new ContentValues();
        v.put(COL_NOME, novoNome);
        getWritableDatabase().update(TABLE_TRILHAS, v, "id=?",
                new String[]{String.valueOf(id)});
    }
    public void inserirPonto(long trilhaId, double latitude, double longitude) {
        ContentValues v = new ContentValues();
        v.put(COL_TRILHA_ID, trilhaId);
        v.put(COL_LATITUDE,  latitude);
        v.put(COL_LONGITUDE, longitude);
        getWritableDatabase().insert(TABLE_PONTOS, null, v);
    }
    public List<LatLng> listarPontosTrilha(int trilhaId) {
        List<LatLng> lista = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT " + COL_LATITUDE + ", " + COL_LONGITUDE +
                        " FROM " + TABLE_PONTOS +
                        " WHERE " + COL_TRILHA_ID + " = ?" +
                        " ORDER BY " + COL_ID,
                new String[]{String.valueOf(trilhaId)});
        while (c.moveToNext()) {
            lista.add(new LatLng(c.getDouble(0), c.getDouble(1)));
        }
        c.close();
        return lista;
    }
}