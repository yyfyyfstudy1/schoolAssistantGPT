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


public class DateListAdapter extends RecyclerView.Adapter<DateListAdapter.LinearViewHolder>{

    private Context mContext;
    private List<String> datePlanList;

    public DateListAdapter(Context mContext, List<String> datePlanList) {
        this.mContext = mContext;
        this.datePlanList = datePlanList;
    }

    public void addDatePlan(String datePlan) {
        // 添加数据到list
        datePlanList.add(datePlan);
        // 通知adapter有新数据插入，更新RecyclerView显示
        notifyItemInserted(datePlanList.size() - 1);
    }


    @NonNull
    @Override
    public DateListAdapter.LinearViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DateListAdapter.LinearViewHolder(LayoutInflater.from(mContext).inflate(R.layout.layout_plan_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull LinearViewHolder holder, int position) {
        // set the preference name
        holder.planName.setText(datePlanList.get(position));

    }

    @Override
    public int getItemCount() {
        return datePlanList.size();
    }

    class LinearViewHolder extends RecyclerView.ViewHolder{

        private TextView planName;

        public LinearViewHolder(View itemView){
            super(itemView);
            planName = itemView.findViewById(R.id.planName);
        }
    }
}
