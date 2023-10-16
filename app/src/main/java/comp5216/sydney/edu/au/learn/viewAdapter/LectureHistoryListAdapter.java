package comp5216.sydney.edu.au.learn.viewAdapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import comp5216.sydney.edu.au.learn.Common.lectureHistoryDTO;
import comp5216.sydney.edu.au.learn.R;


public class LectureHistoryListAdapter extends RecyclerView.Adapter<LectureHistoryListAdapter.LinearViewHolder>{

    private Context mContext;
    private List<lectureHistoryDTO> lectureHistoryList;
    private OnItemClickListener mlistener;

    public LectureHistoryListAdapter(Context mContext, List<lectureHistoryDTO> lectureHistoryList,  OnItemClickListener listener) {
        this.mContext = mContext;
        this.lectureHistoryList = lectureHistoryList;
        this.mlistener = listener;
    }



    @NonNull
    @Override
    public LectureHistoryListAdapter.LinearViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LectureHistoryListAdapter.LinearViewHolder(LayoutInflater.from(mContext).inflate(R.layout.layout_lecture_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull LinearViewHolder holder, @SuppressLint("RecyclerView") int position) {


        // Use Picasso to load network images and set them to ImageView
        Picasso.get().load(lectureHistoryList.get(position).getCoverImageUrl()).into(holder.coverImage);

        // set the pdf name
        holder.pdfName.setText(lectureHistoryList.get(position).getPdfTitle());

        // 绑定点击事件
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mlistener.onClick(position, lectureHistoryList.get(position).getPdfTitle());
            }
        });
    }

    @Override
    public int getItemCount() {
        return lectureHistoryList.size();
    }

    class LinearViewHolder extends RecyclerView.ViewHolder{

        private ImageView coverImage;

        private TextView pdfName;


        public LinearViewHolder(View itemView){
            super(itemView);
            coverImage = itemView.findViewById(R.id.lectureCoverImage);
            pdfName = itemView.findViewById(R.id.lectureTitle);
        }
    }

    public interface OnItemClickListener{
        void onClick(int pos, String pdfName);
    }
}
