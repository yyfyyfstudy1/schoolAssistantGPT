package comp5216.sydney.edu.au.learn.fragment;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;


import java.util.ArrayList;

import comp5216.sydney.edu.au.learn.R;
import comp5216.sydney.edu.au.learn.viewAdapter.PreferenceListAdapter;

public class HomeFragment extends Fragment {

    private RecyclerView expandableRecyclerView1;
    private RecyclerView expandableRecyclerView2;
    private  PreferenceListAdapter preferenceListAdapter;
    private MaterialButton composeEmailBtn;
    private EditText thoughtEditText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        expandableRecyclerView1 = view.findViewById(R.id.expandableLayout1);
        expandableRecyclerView2 = view.findViewById(R.id.expandableLayout2);
        composeEmailBtn = view.findViewById(R.id.composeEmailBtn);
        thoughtEditText = view.findViewById(R.id.thoughtEditText);
        // create and set recycler view layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        expandableRecyclerView1.setLayoutManager(layoutManager);

        ArrayList<String> preferenceList = new ArrayList<>();
        preferenceList.add("to ask tutor...");
        preferenceList.add("to mention the team member.....");
        preferenceList.add("to ask tutor...");


        // create and set the adapter
        preferenceListAdapter = new PreferenceListAdapter(getContext(),preferenceList);
        expandableRecyclerView1.setAdapter(preferenceListAdapter);

        // Add divider decoration
        expandableRecyclerView1.addItemDecoration(new myDecoration());


        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.toggleGroup);
        MaterialCardView cardView1 = view.findViewById(R.id.cardView1);
        MaterialCardView cardView2 = view.findViewById(R.id.cardView2);
        final MaterialButton emailButton = view.findViewById(R.id.emailButton);
        final MaterialButton messageButton = view.findViewById(R.id.messageButton);

        toggleGroup.check(R.id.emailButton);

        cardView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (expandableRecyclerView1.getVisibility() == View.GONE) {
                    expandableView(expandableRecyclerView1);
                    collapseView(expandableRecyclerView2);
                } else {
                    collapseView(expandableRecyclerView1);
                }
            }
        });

        cardView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (expandableRecyclerView2.getVisibility() == View.GONE) {
                    expandableView(expandableRecyclerView2);
                    collapseView(expandableRecyclerView1);
                } else {
                    collapseView(expandableRecyclerView2);
                }
            }
        });

        toggleGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (checkedId == R.id.emailButton) {


                } else if (checkedId == R.id.messageButton) {
                    // switch to message model
                }
            }
        });

        // click with call gpt API
        composeEmailBtn.setOnClickListener(this::composeEmailClick);

    }

    private void composeEmailClick(View view){
        gptResponseFragment gptResponseFragment = new gptResponseFragment();
        Bundle args = new Bundle();
        args.putString("userEditContent", thoughtEditText.getText().toString());

        gptResponseFragment.setArguments(args);
        // change fragment
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fl_container, gptResponseFragment);
        // Add FragmentA to the back stack so the user can return to it
        transaction.addToBackStack(null);
        transaction.commitAllowingStateLoss();


    }

    private void expandableView(final RecyclerView layout) {
        layout.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = layout.getMeasuredHeight();

        layout.getLayoutParams().height = 0;
        layout.setVisibility(View.VISIBLE);
        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, targetHeight);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                layout.getLayoutParams().height = (int) animation.getAnimatedValue();
                layout.requestLayout();
            }
        });
        valueAnimator.setDuration(200).start();
    }

    private void collapseView(final RecyclerView layout) {
        final int initialHeight = layout.getMeasuredHeight();

        ValueAnimator valueAnimator = ValueAnimator.ofInt(initialHeight, 0);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                layout.getLayoutParams().height = (int) animation.getAnimatedValue();
                layout.requestLayout();
            }
        });
        valueAnimator.setDuration(200).start();
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                layout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {

            }

        });
    }

    class myDecoration extends RecyclerView.ItemDecoration{
        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.set(0,0,0,getResources().getDimensionPixelOffset(R.dimen.dividerHeight));
        }
    }

}
