package comp5216.sydney.edu.au.learn.viewAdapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import comp5216.sydney.edu.au.learn.R;


public class PreferenceListAdapter extends RecyclerView.Adapter<PreferenceListAdapter.GridViewHolder> {

    private Context mContext;
    private List<String> preferenceList;
    private OnItemClickListener mListener;
    private int selectedPosition = -1; // -1 means no selection


    public PreferenceListAdapter(Context mContext, List<String> preferenceList, OnItemClickListener listener) {
        this.mContext = mContext;
        this.preferenceList = preferenceList;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public PreferenceListAdapter.GridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PreferenceListAdapter.GridViewHolder(LayoutInflater.from(mContext).inflate(R.layout.layout_perference_item, parent, false));
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull GridViewHolder holder, int position) {
        // set the preference name
        holder.preferenceName.setText(preferenceList.get(position));

        // Add any other bindings for new/changed view elements here
        if(selectedPosition == position) {
            holder.itemView.setBackground(mContext.getResources().getDrawable(R.drawable.rounded_select_bg)); // set your selected color here
        } else {
            holder.itemView.setBackground(mContext.getResources().getDrawable(R.drawable.rounded_bg));// set your default color here
        }
    }

    @Override
    public int getItemCount() {
        return preferenceList.size();
    }

    class GridViewHolder extends RecyclerView.ViewHolder {

        private TextView preferenceName;

        public GridViewHolder(View itemView) {
            super(itemView);
            preferenceName = itemView.findViewById(R.id.preferenceName);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int prevSelected = selectedPosition;
                    selectedPosition = getAdapterPosition();

                    if (prevSelected != RecyclerView.NO_POSITION) {
                        notifyItemChanged(prevSelected);
                    }
                    notifyItemChanged(selectedPosition);

                    if (mListener != null) {
                        mListener.onItemClick(selectedPosition);
                    }
                }
            });


        }
    }


    public interface OnItemClickListener {
        void onItemClick(int position);
    }

}
