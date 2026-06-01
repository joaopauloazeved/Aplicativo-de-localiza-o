package com.example.atividade3;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class ConsultarTrilhasActivity extends AppCompatActivity {

    private Banco db;
    private ListView listView;
    private TrilhaAdapter adapter;
    private List<TrilhaItem> trilhas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultar_trilhas);

        db      = new Banco(this);
        trilhas = new ArrayList<>();

        listView = findViewById(R.id.listview_trilhas);
        adapter  = new TrilhaAdapter();
        listView.setAdapter(adapter);

        findViewById(R.id.btn_voltar).setOnClickListener(v -> finish());

        carregarTrilhas();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarTrilhas();
    }

    private void carregarTrilhas() {
        trilhas.clear();
        Cursor c = db.listarTrilhas();
        if (c != null) {
            while (c.moveToNext()) {
                TrilhaItem t = new TrilhaItem();
                t.id         = c.getInt(c.getColumnIndexOrThrow(Banco.COL_ID));
                t.nome       = c.getString(c.getColumnIndexOrThrow(Banco.COL_NOME));
                t.dataInicio = c.getString(c.getColumnIndexOrThrow(Banco.COL_DATA_INICIO));
                t.dataFim    = c.getString(c.getColumnIndexOrThrow(Banco.COL_DATA_FIM));
                t.velMedia   = c.getDouble(c.getColumnIndexOrThrow(Banco.COL_VEL_MEDIA));
                t.velMax     = c.getDouble(c.getColumnIndexOrThrow(Banco.COL_VEL_MAX));
                t.distancia  = c.getDouble(c.getColumnIndexOrThrow(Banco.COL_DISTANCIA));
                t.duracao    = c.getString(c.getColumnIndexOrThrow(Banco.COL_DURACAO));
                trilhas.add(t);
            }
            c.close();
        }
        adapter.notifyDataSetChanged();

        TextView tvVazia = findViewById(R.id.tv_lista_vazia);
        tvVazia.setVisibility(trilhas.isEmpty() ? View.VISIBLE : View.GONE);
        listView.setVisibility(trilhas.isEmpty() ? View.GONE : View.VISIBLE);
    }


    private class TrilhaAdapter extends ArrayAdapter<TrilhaItem> {
        TrilhaAdapter() { super(ConsultarTrilhasActivity.this, 0, trilhas); }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_trilha, parent, false);
            }
            TrilhaItem t = trilhas.get(position);

            ((TextView) convertView.findViewById(R.id.tv_nome_trilha)).setText(t.nome);
            ((TextView) convertView.findViewById(R.id.tv_data_trilha)).setText(t.dataInicio);
            ((TextView) convertView.findViewById(R.id.tv_distancia_trilha))
                    .setText(String.format("%.2f km", t.distancia));

            convertView.findViewById(R.id.btn_editar)
                    .setOnClickListener(v -> dialogEditarNome(t));
            convertView.findViewById(R.id.btn_apagar)
                    .setOnClickListener(v -> confirmarApagarUma(t));
            convertView.findViewById(R.id.btn_ver_mapa)
                    .setOnClickListener(v -> abrirMapaTrilha(t));

            return convertView;
        }
    }


    private void dialogEditarNome(TrilhaItem t) {
        EditText input = new EditText(this);
        input.setText(t.nome);
        input.setSelection(t.nome.length());

        new AlertDialog.Builder(this)
                .setTitle("Editar nome da trilha")
                .setView(input)
                .setPositiveButton("Salvar", (d, w) -> {
                    String novoNome = input.getText().toString().trim();
                    if (!novoNome.isEmpty()) {
                        db.editarNomeTrilha(t.id, novoNome);
                        carregarTrilhas();
                        Toast.makeText(this, "Nome atualizado", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }


    private void confirmarApagarUma(TrilhaItem t) {
        new AlertDialog.Builder(this)
                .setTitle("Apagar trilha")
                .setMessage("Deseja apagar \"" + t.nome + "\"?")
                .setPositiveButton("Apagar", (d, w) -> {
                    db.deletarTrilha(t.id);
                    carregarTrilhas();
                    Toast.makeText(this, "Trilha apagada", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }


    private void abrirMapaTrilha(TrilhaItem t) {
        Intent i = new Intent(this, VisualizarTrilhaActivity.class);
        i.putExtra("trilha_id",  t.id);
        i.putExtra("nome",       t.nome);
        i.putExtra("dataInicio", t.dataInicio);
        i.putExtra("dataFim",    t.dataFim);
        i.putExtra("velMedia",   t.velMedia);
        i.putExtra("velMax",     t.velMax);
        i.putExtra("distancia",  t.distancia);
        i.putExtra("duracao",    t.duracao);
        startActivity(i);
    }


    static class TrilhaItem {
        int id;
        String nome, dataInicio, dataFim, duracao;
        double velMedia, velMax, distancia;
    }
}