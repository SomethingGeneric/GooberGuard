package cloud.goober.gooberguard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class DomainAdapter extends RecyclerView.Adapter<DomainAdapter.DomainViewHolder> {

    private ArrayList<String> domains;

    public DomainAdapter(ArrayList<String> domains) {
        this.domains = domains;
    }

    @NonNull
    @Override
    public DomainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new DomainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DomainViewHolder holder, int position) {
        holder.domainTextView.setText(domains.get(position));
    }

    @Override
    public int getItemCount() {
        return domains.size();
    }

    public static class DomainViewHolder extends RecyclerView.ViewHolder {
        TextView domainTextView;

        public DomainViewHolder(@NonNull View itemView) {
            super(itemView);
            domainTextView = itemView.findViewById(android.R.id.text1);
        }
    }
}
