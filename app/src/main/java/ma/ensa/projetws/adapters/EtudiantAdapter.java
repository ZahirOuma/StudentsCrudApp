package ma.ensa.projetws.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;  // Use Glide to load images
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.List;

import ma.ensa.projetws.R;
import ma.ensa.projetws.beans.Etudiant;

public class EtudiantAdapter extends RecyclerView.Adapter<EtudiantAdapter.EtudiantViewHolder> implements Filterable {

    private List<Etudiant> etudiants;
    private List<Etudiant> etudiantsFilter;
    private NewFilter mFilter;

    private Context context;
    private OnEtudiantActionListener actionListener;

    public interface OnEtudiantActionListener {
        void onUpdateEtudiant(Etudiant etudiant, int position);
        void onDeleteEtudiant(Etudiant etudiant, int position);
    }


    public EtudiantAdapter(List<Etudiant> etudiants, Context context, OnEtudiantActionListener actionListener) {
        this.etudiants = etudiants;
        this.context = context;  // Ensure this is the Activity context
        this.actionListener = actionListener;
        this.etudiantsFilter = new ArrayList<>(etudiants); // Initialize filtered list with the original list
        this.mFilter = new NewFilter(this);
    }
    public void updateEtudiants(List<Etudiant> newEtudiants) {
        this.etudiants = newEtudiants;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EtudiantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_etudiant, parent, false);
        return new EtudiantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EtudiantViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Etudiant etudiant = etudiants.get(position);
        holder.nom.setText(etudiant.getNom());
        holder.prenom.setText(etudiant.getPrenom());
        holder.ville.setText(etudiant.getVille());
        holder.sexe.setText(etudiant.getSexe());

        // Check if there's an image URL
        if (etudiant.getImage() != null && !etudiant.getImage().isEmpty()) {
            String imageUrl = "http://10.0.2.2/volley2/volley/sourcefiles/" + etudiant.getImage(); // Adjust this base URL
            Log.d("Image URL", "Loading image from: " + imageUrl);
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.etudiant)
                    .error(R.drawable.etudiant)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.e("Glide", "Image load failed: " + e.getMessage());
                            return false; // Important pour que Glide affiche l'image d'erreur
                        }

                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            Log.d("Glide", "Image loaded successfully");
                            return false;
                        }
                    })
                    .into(holder.img);

        } else {
            // Set default image when URL is missing
            holder.img.setImageResource(R.drawable.etudiant);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v, etudiant, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return etudiants.size();
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    public List<Etudiant> getEtudiants() {
        return etudiants; // Method to access the original list of students
    }

    public List<Etudiant> getEtudiantsFilter() {
        return etudiantsFilter; // Method to access the filtered list of students
    }

    public void clearEtudiantsFilter() {
        etudiantsFilter.clear(); // Method to clear the filtered list
    }

    public void addEtudiantsFilter(Etudiant etudiant) {
        etudiantsFilter.add(etudiant); // Method to add students to the filtered list
    }

    private void showPopupMenu(View view, Etudiant etudiant, int position) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                try {
                    if (item.getItemId() == R.id.action_modify) {
                        showEditDialog(etudiant, position);
                        return true;
                    } else if (item.getItemId() == R.id.action_delete) {
                        if (actionListener != null) {
                            actionListener.onDeleteEtudiant(etudiant, position);
                        }
                        return true;
                    } else {
                        return false;
                    }
                } catch (Exception e) {
                    Log.e("EtudiantAdapter", "Error in popup menu item click: " + e.getMessage());
                    return false;
                }
            }
        });
        popupMenu.show();
    }

    private void showEditDialog(Etudiant etudiant, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Modifier l'étudiant");

        View viewInflated = LayoutInflater.from(context).inflate(R.layout.dialog_edit_etudiant, null);
        EditText inputNom = viewInflated.findViewById(R.id.input_nom);
        EditText inputPrenom = viewInflated.findViewById(R.id.input_prenom);
        EditText inputVille = viewInflated.findViewById(R.id.input_ville);
        EditText inputSexe = viewInflated.findViewById(R.id.input_sexe);

        inputNom.setText(etudiant.getNom());
        inputPrenom.setText(etudiant.getPrenom());
        inputVille.setText(etudiant.getVille());
        inputSexe.setText(etudiant.getSexe());

        builder.setView(viewInflated);

        builder.setPositiveButton("Modifier", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Update student data and notify listener
                etudiant.setNom(inputNom.getText().toString());
                etudiant.setPrenom(inputPrenom.getText().toString());
                etudiant.setVille(inputVille.getText().toString());
                etudiant.setSexe(inputSexe.getText().toString());

                if (actionListener != null) {
                    actionListener.onUpdateEtudiant(etudiant, position);
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    static class EtudiantViewHolder extends RecyclerView.ViewHolder {
        TextView nom;
        TextView prenom;
        TextView ville;
        TextView sexe;
        ImageView img;

        public EtudiantViewHolder(@NonNull View itemView) {
            super(itemView);
            nom = itemView.findViewById(R.id.nom);
            prenom = itemView.findViewById(R.id.prenom);
            ville = itemView.findViewById(R.id.ville);
            sexe = itemView.findViewById(R.id.sexe);
            img = itemView.findViewById(R.id.img); // Make sure this ID matches the one in your layout
        }

    }

    class NewFilter extends Filter {
        private EtudiantAdapter mAdapter;

        public NewFilter(EtudiantAdapter adapter) {
            super();
            this.mAdapter = adapter;
        }

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<Etudiant> filteredList = new ArrayList<>();

            if (charSequence.length() == 0) {
                filteredList.addAll(mAdapter.getEtudiants());
            } else {
                String filterPattern = charSequence.toString().toLowerCase().trim();
                for (Etudiant etudiant : mAdapter.getEtudiants()) {
                    if (etudiant.getNom().toLowerCase().startsWith(filterPattern)) {
                        filteredList.add(etudiant);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            mAdapter.clearEtudiantsFilter(); // Clear the filtered list
            List<Etudiant> etudiants = (List<Etudiant>) filterResults.values; // Get filtered results
            for (Etudiant etudiant : etudiants) {
                mAdapter.addEtudiantsFilter(etudiant); // Add filtered results to the filtered list
            }
            mAdapter.notifyDataSetChanged(); // Notify the change
        }

    }
}
