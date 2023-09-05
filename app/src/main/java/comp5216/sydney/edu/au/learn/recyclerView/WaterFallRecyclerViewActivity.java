package comp5216.sydney.edu.au.learn.recyclerView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import comp5216.sydney.edu.au.learn.R;

public class WaterFallRecyclerViewActivity extends AppCompatActivity {
    private RecyclerView mPvWaterFall;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_fall_recycler_view);
        mPvWaterFall = findViewById(R.id.rv_pu);
        mPvWaterFall.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mPvWaterFall.addItemDecoration(new myDecoration());
        mPvWaterFall.setAdapter(new waterFallAdapter(WaterFallRecyclerViewActivity.this, new waterFallAdapter.OnItemClickListener() {
            @Override
            public void onClick(int pos) {
                Toast.makeText(WaterFallRecyclerViewActivity.this, "click"+ pos, Toast.LENGTH_SHORT).show();
            }
        }));
    }

    class myDecoration extends RecyclerView.ItemDecoration{
        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            int gap = getResources().getDimensionPixelOffset(R.dimen.dividerHeight);
            outRect.set(gap,gap,gap,gap);
        }
    }
}