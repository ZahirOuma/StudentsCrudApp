package ma.ensa.projetws;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ma.ensa.projetws.adapters.EtudiantAdapter;
import ma.ensa.projetws.beans.Etudiant;

public class StudentListActivity extends AppCompatActivity implements View.OnClickListener, EtudiantAdapter.OnEtudiantActionListener {

    private RecyclerView recyclerView;
    private EtudiantAdapter adapter;
    private List<Etudiant> etudiantList;
    private RequestQueue requestQueue;
    private Button add;


    // URLs for server requests
    private String loadUrl = "http://10.0.2.2/volley2/volley/sourcefiles/ws/loadEtudiant.php";
    private String updateUrl = "http://10.0.2.2/volley2/volley/sourcefiles/ws/updateEtudiant.php";
    private String deleteUrl = "http://10.0.2.2/volley2/volley/sourcefiles/ws/deleteEtudiant.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        requestQueue = Volley.newRequestQueue(this);

        add = findViewById(R.id.add);
        add.setOnClickListener(this);

        loadStudentsFromServer();
        enableSwipeToDelete();

    }
    private void enableSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Etudiant etudiant = etudiantList.get(position);

                if (direction == ItemTouchHelper.LEFT || direction == ItemTouchHelper.RIGHT) {
                    onDeleteEtudiant(etudiant, position); // Utilise ta méthode existante de suppression
                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                // Tu peux personnaliser l'apparence de la vue pendant le balayage si nécessaire
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        MenuItem menuItem = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return true;
            }
        });
        return true;
    }
    private void performSearch(String query) {
        // Filter your etudiantList based on the query
        List<Etudiant> filteredList = new ArrayList<>();
        for (Etudiant etudiant : etudiantList) {
            if (etudiant.getNom().toLowerCase().contains(query.toLowerCase()) ||
                    etudiant.getPrenom().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(etudiant);
            }
        }
        // Update the RecyclerView with the filtered list
        adapter.updateEtudiants(filteredList);
    }

    @Override
    public void onClick(View v) {
        if (v == add) {
            Intent intent = new Intent(StudentListActivity.this, AddEtudiant.class);
            startActivity(intent);
        }
    }

    private void loadStudentsFromServer() {
        StringRequest request = new StringRequest(Request.Method.POST, loadUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("StudentList", response);
                        try {
                            Type type = new TypeToken<List<Etudiant>>() {}.getType();
                            etudiantList = new Gson().fromJson(response, type);

                            adapter = new EtudiantAdapter(etudiantList, StudentListActivity.this, StudentListActivity.this);
                            recyclerView.setAdapter(adapter);
                        } catch (Exception e) {
                            Log.e("StudentList", "Failed to parse student list", e);
                            Toast.makeText(StudentListActivity.this, "Error parsing student data", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley Error", error.toString());
                        Toast.makeText(StudentListActivity.this, "Error loading students", Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(request);
    }

    @Override
    public void onUpdateEtudiant(Etudiant etudiant, int position) {
        // Handle the update logic
        StringRequest request = new StringRequest(Request.Method.POST, updateUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("UpdateResponse", response);
                        Toast.makeText(StudentListActivity.this, "Student updated successfully", Toast.LENGTH_SHORT).show();
                        loadStudentsFromServer(); // Refresh the list
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley Error", error.toString());
                        Toast.makeText(StudentListActivity.this, "Error updating student", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(etudiant.getId()));
                params.put("nom", etudiant.getNom());
                params.put("prenom", etudiant.getPrenom());
                params.put("ville", etudiant.getVille());
                params.put("sexe", etudiant.getSexe());
                Log.d("Params", params.toString());
                return params;
            }
        };

        requestQueue.add(request);
    }


    @Override
    public void onDeleteEtudiant(Etudiant etudiant, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Student")
                .setMessage("Are you sure you want to delete this student?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        performDelete(etudiant, position);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void performDelete(Etudiant etudiant, int position) {
        StringRequest request = new StringRequest(Request.Method.POST, deleteUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(StudentListActivity.this, "Student deleted successfully", Toast.LENGTH_SHORT).show();
                        loadStudentsFromServer(); // Refresh the list after deletion
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley Error", error.toString());
                        Toast.makeText(StudentListActivity.this, "Error deleting student", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(etudiant.getId()));
                return params;
            }
        };

        requestQueue.add(request);
    }
}