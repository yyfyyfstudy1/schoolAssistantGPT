package comp5216.sydney.edu.au.learn.viewAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import comp5216.sydney.edu.au.learn.R;


public class PreferenceListAdapter extends RecyclerView.Adapter<PreferenceListAdapter.LinearViewHolder>{

    private Context mContext;
    private List<String> preferenceList;

    public PreferenceListAdapter(Context mContext, List<String> preferenceList) {
        this.mContext = mContext;
        this.preferenceList = preferenceList;
    }

    @NonNull
    @Override
    public PreferenceListAdapter.LinearViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PreferenceListAdapter.LinearViewHolder(LayoutInflater.from(mContext).inflate(R.layout.layout_perference_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull LinearViewHolder holder, int position) {
        // set the preference name
        holder.preferenceName.setText(preferenceList.get(position));

    }

    @Override
    public int getItemCount() {
        return preferenceList.size();
    }

    class LinearViewHolder extends RecyclerView.ViewHolder{

        private TextView preferenceName;

        public LinearViewHolder(View itemView){
            super(itemView);
            preferenceName = itemView.findViewById(R.id.preferenceName);
        }
    }
}
