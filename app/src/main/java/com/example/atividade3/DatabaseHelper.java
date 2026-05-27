package com.example.atividade3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // NOME DO BANCO
    private static final String DATABASE_NAME = "trilhas.db";

    // VERSÃO
    private static final int DATABASE_VERSION = 1;

    // TABELA TRILHAS
    public static final String TABLE_TRILHAS = "trilhas";

    public static final String COL_ID = "id";
    public static final String COL_NOME = "nome";
    public static final String COL_DATA_INICIO = "data_inicio";
    public static final String COL_DATA_FIM = "data_fim";
    public static final String COL_VEL_MEDIA = "vel_media";
    public static final String COL_VEL_MAX = "vel_max";
    public static final String COL_DISTANCIA = "distancia";
    public static final String COL_DURACAO = "duracao";

    // TABELA PONTOS
    public static final String TABLE_PONTOS = "pontos";

    public static final String COL_TRILHA_ID = "trilha_id";
    public static final String COL_LATITUDE = "latitude";
    public static final String COL_LONGITUDE = "longitude";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // TABELA TRILHAS
        String createTrilhas =

                "CREATE TABLE " + TABLE_TRILHAS + " (" +

                        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        COL_NOME + " TEXT, " +

                        COL_DATA_INICIO + " TEXT, " +

                        COL_DATA_FIM + " TEXT, " +

                        COL_VEL_MEDIA + " REAL, " +

                        COL_VEL_MAX + " REAL, " +

                        COL_DISTANCIA + " REAL, " +

                        COL_DURACAO + " TEXT )";

        // TABELA PONTOS
        String createPontos =

                "CREATE TABLE " + TABLE_PONTOS + " (" +

                        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        COL_TRILHA_ID + " INTEGER, " +

                        COL_LATITUDE + " REAL, " +

                        COL_LONGITUDE + " REAL )";

        db.execSQL(createTrilhas);

        db.execSQL(createPontos);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion,
                          int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRILHAS);

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PONTOS);

        onCreate(db);
    }

    // SALVAR TRILHA
    public long inserirTrilha(String nome,
                              String dataInicio,
                              String dataFim,
                              double velMedia,
                              double velMax,
                              double distancia,
                              String duracao) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(COL_NOME, nome);

        values.put(COL_DATA_INICIO, dataInicio);

        values.put(COL_DATA_FIM, dataFim);

        values.put(COL_VEL_MEDIA, velMedia);

        values.put(COL_VEL_MAX, velMax);

        values.put(COL_DISTANCIA, distancia);

        values.put(COL_DURACAO, duracao);

        return db.insert(TABLE_TRILHAS,
                null,
                values);
    }

    // SALVAR PONTO DA TRILHA
    public void inserirPonto(long trilhaId,
                             double latitude,
                             double longitude) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(COL_TRILHA_ID, trilhaId);

        values.put(COL_LATITUDE, latitude);

        values.put(COL_LONGITUDE, longitude);

        db.insert(TABLE_PONTOS,
                null,
                values);
    }

    // LISTAR TRILHAS
    public Cursor listarTrilhas() {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM " + TABLE_TRILHAS,
                null
        );
    }

    // APAGAR TRILHA
    public void deletarTrilha(int id) {

        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_TRILHAS,
                "id=?",
                new String[]{String.valueOf(id)});

        db.delete(TABLE_PONTOS,
                "trilha_id=?",
                new String[]{String.valueOf(id)});
    }

    // EDITAR NOME
    public void editarNomeTrilha(int id,
                                 String novoNome) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(COL_NOME, novoNome);

        db.update(TABLE_TRILHAS,
                values,
                "id=?",
                new String[]{String.valueOf(id)});
    }
}