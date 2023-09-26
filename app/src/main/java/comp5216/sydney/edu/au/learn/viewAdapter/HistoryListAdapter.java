package comp5216.sydney.edu.au.learn.viewAdapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jsoup.Jsoup;

import java.util.List;

import comp5216.sydney.edu.au.learn.R;


public class HistoryListAdapter extends RecyclerView.Adapter<HistoryListAdapter.LinearViewHolder>{

    private Context mContext;
    private List<String> historyList;
    private OnItemClickListener mlistener;

    public HistoryListAdapter(Context mContext, List<String> historyList, OnItemClickListener listener) {
        this.mContext = mContext;
        this.mlistener = listener;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public HistoryListAdapter.LinearViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HistoryListAdapter.LinearViewHolder(LayoutInflater.from(mContext).inflate(R.layout.layout_history_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull LinearViewHolder holder, @SuppressLint("RecyclerView") int position) {

        String htmlContent = historyList.get(position);
        // convert the html content to text
        String plainText = Jsoup.parse(htmlContent).text();
        String result = abbreviateText(plainText, 30);
        // set the preference name
        holder.historyTitle.setText(result);

        // 绑定点击事件
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mlistener.onClick(position, htmlContent);
            }
        });

    }

    public static String abbreviateText(String text, int maxLength) {
        if (text.length() > maxLength) {
            return text.substring(0, maxLength - 3) + "...";
        } else {
            return text;
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    class LinearViewHolder extends RecyclerView.ViewHolder{

        private TextView historyTitle;

        public LinearViewHolder(View itemView){
            super(itemView);
            historyTitle = itemView.findViewById(R.id.historyTitle);
        }
    }

    public interface OnItemClickListener{
        void onClick(int pos, String emailContent);
    }
}
